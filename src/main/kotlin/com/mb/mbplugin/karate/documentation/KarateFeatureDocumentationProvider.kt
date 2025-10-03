package com.mb.mbplugin.karate.documentation

import com.intellij.lang.documentation.AbstractDocumentationProvider
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTreeUtil
import com.mb.mbplugin.karate.index.KarateFeatureIndex
import com.mb.mbplugin.karate.psi.KarateFile
import com.mb.mbplugin.karate.psi.KaratePsiElement
import com.mb.mbplugin.karate.psi.KarateTokenTypes
import java.util.regex.Pattern

class KarateFeatureDocumentationProvider : AbstractDocumentationProvider() {
    
    companion object {
        // Pattern to match: call read('classpath:path/to/file.feature@scenario') or call read("classpath:path/to/file.feature@scenario")
        private val CALL_READ_PATTERN = Pattern.compile(
            "call\\s+read\\s*\\(\\s*['\"]classpath:([^'\"]+\\.feature)(@\\w+)?['\"]",
            Pattern.CASE_INSENSITIVE
        )
        
        // Pattern to match documentation blocks like: * text scenarioName_DOC = """..."""
        private val DOC_PATTERN = Pattern.compile(
            "\\*\\s+text\\s+(\\w+_DOC)\\s*=\\s*\"\"\"([\\s\\S]*?)\"\"\"",
            Pattern.MULTILINE or Pattern.CASE_INSENSITIVE
        )
        
        // Alternative pattern for single line documentation
        private val SINGLE_LINE_DOC_PATTERN = Pattern.compile(
            "\\*\\s+text\\s+(\\w+_DOC)\\s*=\\s*['\"]([^'\"]*)['\"]",
            Pattern.CASE_INSENSITIVE
        )
    }

    override fun generateDoc(element: PsiElement?, originalElement: PsiElement?): String? {
        if (element == null || originalElement == null) return null
        
        // Find the exact call read pattern that contains the cursor position
        val callReadInfo = findCallReadAtPosition(element, originalElement)
        if (callReadInfo == null) return null
        
        val (featureFilePath, scenarioTag) = callReadInfo
        val documentation = findDocumentationForFeature(element.project, featureFilePath, scenarioTag)
        
        // Return formatted documentation or fallback message
        return if (documentation != null) {
            // Documentation found, format it properly
            formatDocumentation(documentation, featureFilePath.substringAfterLast("/"), scenarioTag)
        } else {
            // No documentation found, show appropriate message
            val fileName = featureFilePath.substringAfterLast("/")
            buildString {
                append("<html><body>")
                append("<b>Feature File: ${escapeHtml(fileName)}</b><br>")
                if (scenarioTag != null) {
                    append("<b>Scenario: ${escapeHtml(scenarioTag)}</b><br>")
                    append("<br><i>No documentation found for scenario '@$scenarioTag'.</i><br>")
                    append("<i>Expected documentation variable: <code>${scenarioTag}_DOC</code></i>")
                } else {
                    append("<br><i>No documentation found for this feature.</i>")
                }
                append("</body></html>")
            }
        }
    }

    override fun getQuickNavigateInfo(element: PsiElement?, originalElement: PsiElement?): String? {
        if (element == null || originalElement == null) return null
        
        // Find the exact call read pattern that contains the cursor position
        val callReadInfo = findCallReadAtPosition(element, originalElement)
        if (callReadInfo == null) return null
        
        val (featureFilePath, scenarioTag) = callReadInfo
        val fileName = featureFilePath.substringAfterLast("/")
        
        return if (scenarioTag != null) {
            "Feature: $fileName, Scenario: $scenarioTag"
        } else {
            "Feature: $fileName"
        }
    }

    override fun getCustomDocumentationElement(
        editor: com.intellij.openapi.editor.Editor,
        file: PsiFile,
        contextElement: PsiElement?,
        targetOffset: Int
    ): PsiElement? {
        // This helps identify the correct element for documentation
        if (contextElement?.text?.contains("call read") == true) {
            return contextElement
        }
        
        // Check parent elements
        var current = contextElement
        while (current != null) {
            if (current.text.contains("call read")) {
                return current
            }
            current = current.parent
        }
        
        return contextElement
    }

    private fun findCallReadAtPosition(element: PsiElement, originalElement: PsiElement): Pair<String, String?>? {
        // Get the cursor offset within the file
        val offset = originalElement.textOffset
        
        // Start with the current element and traverse up to find a containing element with call read
        var current: PsiElement? = element
        while (current != null) {
            val text = current.text
            val matcher = CALL_READ_PATTERN.matcher(text)
            
            // Find all call read patterns in this element
            while (matcher.find()) {
                val matchStart = current.textOffset + matcher.start()
                val matchEnd = current.textOffset + matcher.end()
                
                // Check if the cursor is within this specific call read statement
                if (offset >= matchStart && offset <= matchEnd) {
                    val featureFilePath = matcher.group(1)
                    val scenarioTag = matcher.group(2)?.substring(1) // Remove @ prefix
                    return Pair(featureFilePath, scenarioTag)
                }
            }
            current = current.parent
        }
        
        return null
    }

    private fun findDocumentationForFeature(
        project: Project, 
        featureFilePath: String, 
        scenarioTag: String?
    ): String? {
        // Extract filename from path
        val fileName = featureFilePath.substringAfterLast("/")
        
        // Get all feature files and filter by name
        val allFeatureFiles = KarateFeatureIndex.getAllFeatureFiles(project)
        val matchingFiles = allFeatureFiles.filter { virtualFile ->
            virtualFile.name == fileName
        }
        
        for (virtualFile in matchingFiles) {
            val psiFile = PsiManager.getInstance(project).findFile(virtualFile)
            if (psiFile is KarateFile) {
                val documentation = extractDocumentationFromFile(psiFile, scenarioTag)
                if (documentation != null) {
                    // Return raw documentation text, not formatted HTML
                    return documentation
                }
            }
        }
        
        return null
    }

    private fun extractDocumentationFromFile(karateFile: KarateFile, scenarioTag: String?): String? {
        val fileText = karateFile.text
        
        if (scenarioTag != null) {
            // Strict matching: only look for {scenarioTag}_DOC pattern
            val expectedDocVarName = "${scenarioTag}_DOC"
            
            // Try multi-line documentation with exact name matching
            val docMatcher = DOC_PATTERN.matcher(fileText)
            while (docMatcher.find()) {
                val docVarName = docMatcher.group(1)
                val docContent = docMatcher.group(2)
                
                // Exact match only
                if (docVarName == expectedDocVarName) {
                    return docContent.trim()
                }
            }
            
            // Try single-line documentation with exact name matching
            val singleLineMatcher = SINGLE_LINE_DOC_PATTERN.matcher(fileText)
            while (singleLineMatcher.find()) {
                val docVarName = singleLineMatcher.group(1)
                val docContent = singleLineMatcher.group(2)
                
                // Exact match only
                if (docVarName == expectedDocVarName) {
                    return docContent.trim()
                }
            }
            
            // If we have a specific scenario tag but found no exact documentation for it, return null
            return null
        }
        
        // Only if no specific scenario tag is provided, return the first documentation block
        val docMatcher = DOC_PATTERN.matcher(fileText)
        if (docMatcher.find()) {
            return docMatcher.group(2).trim()
        }
        
        // Try single-line as fallback
        val singleLineMatcher = SINGLE_LINE_DOC_PATTERN.matcher(fileText)
        if (singleLineMatcher.find()) {
            return singleLineMatcher.group(2).trim()
        }
        
        return null
    }

    private fun formatDocumentation(documentation: String, fileName: String, scenarioTag: String?): String {
        val safeDocumentation = documentation.takeIf { it.isNotBlank() } ?: "No documentation content available"
        val safeFileName = fileName.takeIf { it.isNotBlank() } ?: "Unknown file"
        
        // Process documentation to remove leading indentation
        val processedDocumentation = removeLeadingIndentation(safeDocumentation)
        
        // Use HTML with proper line breaks
        return buildString {
            append("<html><body style=\"font-family: 'JetBrains Mono', 'Fira Code', 'Consolas', monospace;\">")
            append("<b>Feature Documentation</b><br><br>")
            append("<b>File:</b> ${escapeHtml(safeFileName)}<br>")
            if (scenarioTag != null && scenarioTag.isNotBlank()) {
                append("<b>Scenario:</b> ${escapeHtml(scenarioTag)}<br>")
            }
            append("<pre style=\"font-family: 'JetBrains Mono', 'Fira Code', 'Consolas', monospace; margin: 0; padding: 0;\">")
            append(escapeHtml(processedDocumentation))
            append("</pre>")
            append("</body></html>")
        }
    }
    
    /**
     * Removes leading indentation from documentation text.
     * This ensures all lines are aligned to the left regardless of original indentation.
     */
    private fun removeLeadingIndentation(text: String): String {
        val lines = text.lines()
        if (lines.isEmpty()) return text
        
        return lines.joinToString("\n") { line ->
            line.trimStart()
        }
    }

    private fun escapeHtml(text: String): String {
        return text.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;")
    }
}