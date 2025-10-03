package com.mb.mbplugin.karate.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiElement
import com.intellij.util.ProcessingContext
import com.intellij.openapi.diagnostic.Logger
import org.jetbrains.plugins.cucumber.psi.GherkinLanguage
import com.mb.mbplugin.karate.index.KarateFeatureIndex

/**
 * Provides autocomplete for Karate tags with call read('classpath:...') suggestions
 */
class KarateTagCompletionContributor : CompletionContributor() {
    
    init {
        // Trigger completion when typing @ followed by characters in Gherkin files
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement().withLanguage(GherkinLanguage.INSTANCE),
            KarateTagCompletionProvider()
        )
    }
}

class KarateTagCompletionProvider : CompletionProvider<CompletionParameters>() {
    
    companion object {
        private val LOG = Logger.getInstance(KarateTagCompletionProvider::class.java)
    }
    
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
        val lastHashIndex = textBeforeCursor.lastIndexOf('#')
        
        // Check if we're in a context where completion should be triggered
        // 1. After @ symbol for tag-based completion
        // 2. After # symbol for feature file name completion
        
        var partialTag = ""
        var isTagCompletion = false
        var isFileCompletion = false
        
        // Check which trigger is more recent and within range
        if (lastAtIndex != -1 && offset - lastAtIndex <= 50 && 
            (lastHashIndex == -1 || lastAtIndex > lastHashIndex)) {
            // Tag-based completion (@ trigger)
            partialTag = textBeforeCursor.substring(lastAtIndex + 1)
                .takeWhile { it.isLetterOrDigit() || it == '_' || Character.isLetter(it) }
            isTagCompletion = true
        } else if (lastHashIndex != -1 && offset - lastHashIndex <= 50 && 
                   (lastAtIndex == -1 || lastHashIndex > lastAtIndex)) {
            // Feature file name completion (# trigger)
            partialTag = textBeforeCursor.substring(lastHashIndex + 1)
                .takeWhile { it.isLetterOrDigit() || it == '_' || Character.isLetter(it) }
            isFileCompletion = true
        } else {
            return
        }
        
        // Get all matching tags and feature files
        val matchingItems = getAllMatchingItems(project, partialTag, isTagCompletion, isFileCompletion)
        
        // Create completion suggestions
        matchingItems.forEach { tagData ->
            // Properly normalize the path for classpath usage - handles UTF-8 and spaces
            val normalizedPath = normalizeClasspathPath(tagData.relativePath)
            
            if (tagData.tag.isEmpty()) {
                // This is a feature file suggestion (tag is empty)
                val suggestion = "* call read('classpath:${normalizedPath}')"
                val fileName = java.io.File(tagData.absolutePath).nameWithoutExtension
                
                // Create lookup string that shows the file name
                val uniqueLookupString = fileName
                val presentableText = "* call read('classpath:${normalizedPath}')"
                
                val lookupElement = LookupElementBuilder.create(uniqueLookupString)
                    .withPresentableText(presentableText)
                    .withTypeText("${fileName}.feature")
                    .withTailText(" (${normalizedPath})", true)
                    .withInsertHandler { insertContext, _ ->
                        handleFeatureFileInsertion(insertContext, suggestion, isTagCompletion, isFileCompletion)
                    }
                
                result.addElement(lookupElement)
            } else {
                // This is a tag-based suggestion
                val suggestion = "* call read('classpath:${normalizedPath}@${tagData.tag}')"
                
                // Create a completely unique lookup string by including file path
                val fileName = java.io.File(tagData.absolutePath).nameWithoutExtension
                val uniqueLookupString = "@${tagData.tag}__${fileName}__${System.nanoTime()}"
                
                // Make the presentable text show the actual suggestion
                val presentableText = "* call read('classpath:${normalizedPath}@${tagData.tag}')"
                
                val lookupElement = LookupElementBuilder.create(uniqueLookupString)
                    .withPresentableText(presentableText)
                    .withLookupString("@${tagData.tag}")  // This is what the user types to match
                    .withTypeText("${fileName}.feature")
                    .withTailText(" (${normalizedPath})", true)
                    .withInsertHandler { insertContext, _ ->
                        // Use the appropriate handler based on which trigger was used
                        if (isFileCompletion) {
                            // If triggered by #, use feature file insertion handler
                            handleFeatureFileInsertion(insertContext, suggestion, isTagCompletion, isFileCompletion)
                        } else {
                            // If triggered by @, use tag insertion handler
                            handleTagInsertion(insertContext, suggestion)
                        }
                    }
                
                result.addElement(lookupElement)
            }
        }
    }
    
    private fun getAllMatchingItems(
        project: com.intellij.openapi.project.Project, 
        prefix: String,
        isTagCompletion: Boolean,
        isFileCompletion: Boolean
    ): List<com.mb.mbplugin.karate.index.KarateFeatureData> {
        val result = mutableListOf<com.mb.mbplugin.karate.index.KarateFeatureData>()
        
        try {
            // Get all feature files
            val allFeatureFiles = KarateFeatureIndex.getAllFeatureFiles(project)
            
            allFeatureFiles.forEach { virtualFile ->
                try {
                    val psiFile = com.intellij.psi.PsiManager.getInstance(project).findFile(virtualFile)
                    if (psiFile != null) {
                        val relativePath = getRelativePathFromSourceRoot(project, virtualFile)
                        
                        // Debug logging for path issues
                        LOG.debug("Processing file: ${virtualFile.path}")
                        LOG.debug("Relative path: $relativePath")
                        
                        if (isTagCompletion) {
                            // Tag-based completion - find tags that contain the prefix
                            val tags = extractTagsFromFile(psiFile.text)
                            LOG.debug("Found tags: $tags")
                            
                            // Filter tags that contain the prefix anywhere in the tag name (case insensitive)
                            tags.filter { tag -> 
                                prefix.isEmpty() || tag.contains(prefix, ignoreCase = true)
                            }.forEach { tag ->
                                result.add(
                                    com.mb.mbplugin.karate.index.KarateFeatureData(
                                        relativePath, tag, virtualFile.path
                                    )
                                )
                            }
                        }
                        
                        if (isFileCompletion) {
                            // Feature file name-based suggestions (only when # is used)
                            val fileName = virtualFile.nameWithoutExtension
                            if (prefix.isEmpty() || fileName.contains(prefix, ignoreCase = true)) {
                                // 1. Create entry for the feature file itself
                                result.add(
                                    com.mb.mbplugin.karate.index.KarateFeatureData(
                                        relativePath, "", virtualFile.path
                                    )
                                )
                                
                                // 2. Create entries for ALL tags from this matching file
                                val tags = extractTagsFromFile(psiFile.text)
                                tags.forEach { tag ->
                                    result.add(
                                        com.mb.mbplugin.karate.index.KarateFeatureData(
                                            relativePath, tag, virtualFile.path
                                        )
                                    )
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    // Ignore individual file errors
                }
            }
        } catch (e: Exception) {
            // Ignore errors
        }
        
        // Return unique entries - only remove duplicates where the same tag appears multiple times in the same file
        // But allow the same tag to appear from different files
        return result.distinctBy { "${it.absolutePath}@${it.tag}" }
    }

    private fun handleTagInsertion(insertContext: com.intellij.codeInsight.completion.InsertionContext, suggestion: String) {
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
            val char = currentText[replaceStart]
            if (!char.isLetterOrDigit() && char != '_' && !Character.isLetter(char)) {
                break
            }
            replaceStart--
        }
        
        // If we found @, replace from there
        if (replaceStart >= 0 && currentText[replaceStart] == '@') {
            var replaceEnd = replaceStart + 1
            // Find end of tag
            while (replaceEnd < currentText.length && 
                   (currentText[replaceEnd].isLetterOrDigit() || 
                    currentText[replaceEnd] == '_' || 
                    Character.isLetter(currentText[replaceEnd]))) {
                replaceEnd++
            }
            
            // Replace @tag with our suggestion
            document.replaceString(replaceStart, replaceEnd, suggestion)
        } else {
            // Fallback: just insert at current position
            document.insertString(startOffset, suggestion)
        }
    }

    private fun handleFeatureFileInsertion(
        insertContext: com.intellij.codeInsight.completion.InsertionContext, 
        suggestion: String,
        isTagCompletion: Boolean,
        isFileCompletion: Boolean
    ) {
        val document = insertContext.document
        val startOffset = insertContext.startOffset
        val tailOffset = insertContext.tailOffset
        
        // Remove what was just inserted by the completion system
        document.deleteString(startOffset, tailOffset)
        
        if (isTagCompletion) {
            // Handle insertion like tag completion (after @)
            handleTagInsertion(insertContext, suggestion)
        } else if (isFileCompletion) {
            // Handle insertion for feature file name completion (after #) - copy @ logic
            val currentText = document.text
            var replaceStart = startOffset - 1
            
            // Go backwards to find the # symbol
            while (replaceStart >= 0 && currentText[replaceStart] != '#') {
                val char = currentText[replaceStart]
                if (!char.isLetterOrDigit() && char != '_' && !Character.isLetter(char)) {
                    break
                }
                replaceStart--
            }
            
            // If we found #, replace from there
            if (replaceStart >= 0 && currentText[replaceStart] == '#') {
                var replaceEnd = replaceStart + 1
                // Find end of tag
                while (replaceEnd < currentText.length && 
                       (currentText[replaceEnd].isLetterOrDigit() || 
                        currentText[replaceEnd] == '_' || 
                        Character.isLetter(currentText[replaceEnd]))) {
                    replaceEnd++
                }
                
                // Replace #word with our suggestion
                document.replaceString(replaceStart, replaceEnd, suggestion)
            } else {
                // Fallback: just insert at current position
                document.insertString(startOffset, suggestion)
            }
        }
    }

    private fun getAllTagsStartingWith(
        project: com.intellij.openapi.project.Project, 
        prefix: String
    ): List<com.mb.mbplugin.karate.index.KarateFeatureData> {
        // This method is kept for backward compatibility but now delegates to getAllMatchingItems
        return getAllMatchingItems(project, prefix, true, false)
    }
    
    private fun extractTagsFromFile(text: String): List<String> {
        val tags = mutableListOf<String>()
        
        // Enhanced regex to find tags like @login, @smoke, etc. 
        // Supports UTF-8 characters in tag names using Unicode property classes:
        // \p{L} = any Unicode letter (supports accented characters, Asian characters, etc.)
        // \p{N} = any Unicode number
        val tagPattern = Regex("(?:^|\\s)@([\\p{L}_][\\p{L}\\p{N}_]*)", RegexOption.MULTILINE)
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
                val relativePath = filePath.substring(sourceRootPath.length + 1)
                    .replace("\\", "/")
                    .replace(java.io.File.separator, "/")
                // Ensure proper UTF-8 encoding and handle spaces
                return relativePath
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
                        val relativePath = filePath.substring(sourceRoot.path.length + 1)
                            .replace("\\", "/")
                            .replace(java.io.File.separator, "/")
                        // Ensure proper UTF-8 encoding and handle spaces
                        return relativePath
                    }
                }
            }
        } catch (e: Exception) {
            // Ignore module resolution errors
        }
        
        // Final fallback: relative to project root
        return if (filePath.startsWith(projectPath)) {
            val relativePath = filePath.substring(projectPath.length + 1)
                .replace("\\", "/")
                .replace(java.io.File.separator, "/")
            // Ensure proper UTF-8 encoding and handle spaces
            relativePath
        } else {
            file.name
        }
    }
    
    /**
     * Normalize path for classpath usage - handles UTF-8 and spaces properly
     * Karate's classpath supports spaces and UTF-8 characters natively,
     * so no URL encoding is needed, just consistent forward slashes.
     */
    private fun normalizeClasspathPath(path: String): String {
        // Classpath in Karate supports spaces and UTF-8 characters natively
        // Just ensure forward slashes for consistency
        return path.replace("\\", "/")
    }
}