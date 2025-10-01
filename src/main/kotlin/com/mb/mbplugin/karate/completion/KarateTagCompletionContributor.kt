package com.mb.mbplugin.karate.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiElement
import com.intellij.util.ProcessingContext
import com.mb.mbplugin.karate.KarateLanguage
import com.mb.mbplugin.karate.index.KarateFeatureIndex

/**
 * Provides autocomplete for Karate tags with call read('classpath:...') suggestions
 */
class KarateTagCompletionContributor : CompletionContributor() {
    
    init {
        // Trigger completion when typing @ followed by characters in Karate files
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement().withLanguage(KarateLanguage.INSTANCE),
            KarateTagCompletionProvider()
        )
    }
}

class KarateTagCompletionProvider : CompletionProvider<CompletionParameters>() {
    
    override fun addCompletions(
        parameters: CompletionParameters,
        context: ProcessingContext,
        result: CompletionResultSet
    ) {
        val element = parameters.position
        val offset = parameters.offset
        val file = parameters.originalFile
        val project = file.project
        
        // Get the text before cursor
        val textBeforeCursor = file.text.substring(0, offset)
        val lastAtIndex = textBeforeCursor.lastIndexOf('@')
        
        // Only proceed if we found @ and it's relatively recent
        if (lastAtIndex == -1 || offset - lastAtIndex > 50) return
        
        // Extract the partial tag being typed (after @)
        val partialTag = textBeforeCursor.substring(lastAtIndex + 1)
            .takeWhile { it.isLetterOrDigit() || it == '_' }
        
        // Get all tags that start with the partial input
        val matchingTags = getAllTagsStartingWith(project, partialTag)
        
        // Create completion suggestions
        matchingTags.forEach { tagData ->
            val suggestion = "* call read('classpath:${tagData.relativePath}@${tagData.tag}')"
            
            val lookupElement = LookupElementBuilder.create("@${tagData.tag}")
                .withPresentableText(suggestion)
                .withTypeText("Karate scenario call")
                .withTailText(" (${tagData.relativePath})", true)
                .withInsertHandler { insertContext, _ ->
                    val document = insertContext.document
                    val startOffset = insertContext.startOffset
                    val tailOffset = insertContext.tailOffset
                    
                    // Remove what was just inserted by the completion system
                    document.deleteString(startOffset, tailOffset)
                    
                    // Find and replace the @tag pattern
                    val currentText = document.text
                    var replaceStart = startOffset - 1
                    
                    // Go backwards to find the @ symbol
                    while (replaceStart >= 0 && currentText[replaceStart] != '@') {
                        if (!currentText[replaceStart].isLetterOrDigit() && currentText[replaceStart] != '_') {
                            break
                        }
                        replaceStart--
                    }
                    
                    // If we found @, replace from there
                    if (replaceStart >= 0 && currentText[replaceStart] == '@') {
                        var replaceEnd = replaceStart + 1
                        // Find end of tag
                        while (replaceEnd < currentText.length && 
                               (currentText[replaceEnd].isLetterOrDigit() || currentText[replaceEnd] == '_')) {
                            replaceEnd++
                        }
                        
                        // Replace @tag with our suggestion
                        document.replaceString(replaceStart, replaceEnd, suggestion)
                    } else {
                        // Fallback: just insert at current position
                        document.insertString(startOffset, suggestion)
                    }
                }
            
            result.addElement(lookupElement)
        }
    }
    
    private fun getAllTagsStartingWith(
        project: com.intellij.openapi.project.Project, 
        prefix: String
    ): List<com.mb.mbplugin.karate.index.KarateFeatureData> {
        val result = mutableListOf<com.mb.mbplugin.karate.index.KarateFeatureData>()
        
        try {
            // Get all feature files
            val allFeatureFiles = KarateFeatureIndex.getAllFeatureFiles(project)
            
            allFeatureFiles.forEach { virtualFile ->
                try {
                    val psiFile = com.intellij.psi.PsiManager.getInstance(project).findFile(virtualFile)
                    if (psiFile != null) {
                        val tags = extractTagsFromFile(psiFile.text)
                        val relativePath = getRelativePathFromSourceRoot(project, virtualFile)
                        
                        // Filter tags that start with prefix (case insensitive)
                        tags.filter { tag -> 
                            prefix.isEmpty() || tag.startsWith(prefix, ignoreCase = true) 
                        }.forEach { tag ->
                            result.add(
                                com.mb.mbplugin.karate.index.KarateFeatureData(
                                    relativePath, tag, virtualFile.path
                                )
                            )
                        }
                    }
                } catch (e: Exception) {
                    // Ignore individual file errors
                }
            }
        } catch (e: Exception) {
            // Ignore errors
        }
        
        // Return unique entries (same tag might appear multiple times in same file)
        return result.distinctBy { "${it.relativePath}@${it.tag}" }
    }
    
    private fun extractTagsFromFile(text: String): List<String> {
        val tags = mutableListOf<String>()
        
        // Regex to find tags like @login, @smoke, etc. at the beginning of lines or after whitespace
        val tagPattern = Regex("(?:^|\\s)@([a-zA-Z_][a-zA-Z0-9_]*)", RegexOption.MULTILINE)
        tagPattern.findAll(text).forEach { match ->
            tags.add(match.groupValues[1]) // Add without @ symbol
        }
        
        return tags.distinct()
    }
    
    private fun getRelativePathFromSourceRoot(
        project: com.intellij.openapi.project.Project, 
        file: com.intellij.openapi.vfs.VirtualFile
    ): String {
        val projectPath = project.basePath ?: return file.path
        val filePath = file.path
        
        // Common source root patterns for Karate tests
        val sourceRoots = listOf(
            "src/test/java", 
            "src/main/java", 
            "src/test/resources", 
            "src/main/resources",
            "src/test/kotlin",
            "src/main/kotlin"
        )
        
        for (sourceRoot in sourceRoots) {
            val sourceRootPath = "$projectPath/$sourceRoot".replace("/", java.io.File.separator)
            if (filePath.startsWith(sourceRootPath)) {
                // Return path after the source root (this is the classpath)
                return filePath.substring(sourceRootPath.length + 1)
                    .replace("\\", "/")
                    .replace(java.io.File.separator, "/")
            }
        }
        
        // Fallback: try to find using module source roots from IntelliJ
        try {
            val moduleManager = com.intellij.openapi.module.ModuleManager.getInstance(project)
            val modules = moduleManager.modules
            for (module in modules) {
                val moduleRootManager = com.intellij.openapi.roots.ModuleRootManager.getInstance(module)
                
                // Check all source roots
                val sourceRoots = moduleRootManager.sourceRoots
                for (sourceRoot in sourceRoots) {
                    if (filePath.startsWith(sourceRoot.path)) {
                        return filePath.substring(sourceRoot.path.length + 1)
                            .replace("\\", "/")
                            .replace(java.io.File.separator, "/")
                    }
                }
            }
        } catch (e: Exception) {
            // Ignore module resolution errors
        }
        
        // Final fallback: relative to project root
        return if (filePath.startsWith(projectPath)) {
            filePath.substring(projectPath.length + 1)
                .replace("\\", "/")
                .replace(java.io.File.separator, "/")
        } else {
            file.name
        }
    }
}