package com.mb.mbplugin.karate.formatting

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.source.codeStyle.PostFormatProcessor
import com.intellij.openapi.util.TextRange
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.mb.mbplugin.karate.psi.GherkinFileType

/**
 * Post-processor for Karate formatting that handles special cases
 * that can't be handled by the main formatting engine
 */
class KaratePostFormatProcessor : PostFormatProcessor {
    
    override fun processElement(source: PsiElement, settings: CodeStyleSettings): PsiElement {
        if (!isKarateFile(source.containingFile)) {
            return source
        }
        
        return processKarateElement(source)
    }
    
    override fun processText(
        source: PsiFile,
        rangeToReformat: TextRange,
        settings: CodeStyleSettings
    ): TextRange {
        if (!isKarateFile(source)) {
            return rangeToReformat
        }

        val document = source.viewProvider.document ?: return rangeToReformat
        val originalText = document.getText(rangeToReformat)
        val formattedText = formatTablesInText(originalText)
        
        if (formattedText != originalText) {
            document.replaceString(rangeToReformat.startOffset, rangeToReformat.endOffset, formattedText)
            return TextRange(rangeToReformat.startOffset, rangeToReformat.startOffset + formattedText.length)
        }
        
        return rangeToReformat
    }
    
    private fun isKarateFile(file: PsiFile?): Boolean {
        return file?.fileType == GherkinFileType.INSTANCE
    }
    
    private fun processKarateElement(element: PsiElement): PsiElement {
        // Handle special Karate-specific formatting that can't be done in the main formatter
        
        // For now, just basic processing
        ensureNewlineAtEOF(element)
        removeTrailingWhitespace(element)
        
        return element
    }
    
    private fun processKarateText(file: PsiFile, range: TextRange): TextRange {
        val text = file.text
        val originalText = text.substring(range.startOffset, range.endOffset)
        
        // Format tables in the text
        val formattedText = formatTablesInText(originalText)
        
        // Apply other post-processing changes
        var newText = formattedText
        if (!newText.endsWith("\n")) {
            newText += "\n"
        }
        
        newText = removeTrailingWhitespaceFromText(newText)
        
        // If text changed, we need to replace it
        if (newText != originalText) {
            // For now, just return the original range - 
            // actual text replacement would require more complex PSI manipulation
            return TextRange(range.startOffset, range.endOffset + (newText.length - originalText.length))
        }
        
        return range
    }
    
    /**
     * Formats tables in the given text
     */
    private fun formatTablesInText(text: String): String {
        val lines = text.split('\n')
        val result = mutableListOf<String>()
        var tableLines = mutableListOf<String>()
        var inTable = false
        
        for (line in lines) {
            val trimmedLine = line.trim()
            
            if (isTableLine(trimmedLine)) {
                if (!inTable) {
                    inTable = true
                    tableLines.clear()
                }
                tableLines.add(line) // Keep original line with indentation context
            } else {
                if (inTable) {
                    // End of table - format and add it
                    result.addAll(formatTableLines(tableLines))
                    tableLines.clear()
                    inTable = false
                }
                result.add(line)
            }
        }
        
        // Handle table at end of text
        if (inTable && tableLines.isNotEmpty()) {
            result.addAll(formatTableLines(tableLines))
        }
        
        return result.joinToString("\n")
    }
    
    /**
     * Checks if a line is a table line
     */
    private fun isTableLine(line: String): Boolean {
        val trimmed = line.trim()
        return trimmed.startsWith("|") && trimmed.endsWith("|") && trimmed.count { it == '|' } > 2
    }
    
    /**
     * Formats a list of table lines
     */
    private fun formatTableLines(lines: List<String>): List<String> {
        if (lines.isEmpty()) return lines
        
        // Use the TABLE_CELL_INDENT for consistent indentation
        val indentation = " ".repeat(KarateCodeStyleSettings.TABLE_CELL_INDENT)
        
        // Parse all table rows
        val rows = lines.mapNotNull { line ->
            val trimmed = line.trim()
            if (trimmed.startsWith("|") && trimmed.endsWith("|") && trimmed.length > 2) {
                // Remove leading and trailing pipes and split
                val content = trimmed.substring(1, trimmed.length - 1)
                content.split("|").map { it.trim() }
            } else {
                null
            }
        }
        
        if (rows.isEmpty()) return lines
        
        // Calculate maximum column widths
        val maxColumns = rows.maxOfOrNull { it.size } ?: 0
        val columnWidths = IntArray(maxColumns)
        
        for (row in rows) {
            for (i in row.indices) {
                if (i < maxColumns) {
                    columnWidths[i] = maxOf(columnWidths[i], row[i].length)
                }
            }
        }
        
        // Format each row with proper alignment
        return rows.map { row ->
            formatTableRow(row, columnWidths, indentation)
        }
    }
    
    /**
     * Formats a single table row
     */
    private fun formatTableRow(cells: List<String>, columnWidths: IntArray, indentation: String): String {
        val formattedCells = mutableListOf<String>()
        
        for (i in 0 until columnWidths.size) {
            val cell = if (i < cells.size) cells[i] else ""
            val width = columnWidths[i]
            // Left align with 1 space padding: " content "
            val paddedCell = " ${cell.padEnd(width)} "
            formattedCells.add(paddedCell)
        }
        
        return "$indentation|${formattedCells.joinToString("|")}|"
    }
    
    private fun ensureNewlineAtEOF(element: PsiElement) {
        val file = element.containingFile
        val text = file.text
        
        if (!text.endsWith("\n")) {
            // Add newline at end of file
            // This would typically be done through PSI manipulation
            // For now, this is a placeholder for the actual implementation
        }
    }
    
    private fun removeTrailingWhitespace(element: PsiElement) {
        // Remove trailing whitespace from lines
        // This would involve traversing the PSI tree and removing trailing spaces
    }
    
    private fun removeTrailingWhitespaceFromText(text: String): String {
        return text.lines().joinToString("\n") { line ->
            line.trimEnd()
        }
    }
}