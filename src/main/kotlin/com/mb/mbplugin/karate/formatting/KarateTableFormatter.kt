package com.mb.mbplugin.karate.formatting

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile

/**
 * Specialized formatter for Karate tables.
 * Handles table alignment, spacing, and formatting.
 */
object KarateTableFormatter {
    
    /**
     * Formats tables in the given text range
     */
    fun formatTables(file: PsiFile, range: TextRange): String {
        val text = file.text.substring(range.startOffset, range.endOffset)
        return formatTableText(text)
    }
    
    /**
     * Formats table text with proper alignment
     */
    fun formatTableText(text: String): String {
        val lines = text.split('\n')
        val tableLines = mutableListOf<String>()
        var inTable = false
        
        for (line in lines) {
            val trimmedLine = line.trim()
            
            if (isTableLine(trimmedLine)) {
                if (!inTable) {
                    // Starting a new table, format any previous accumulated table
                    if (tableLines.isNotEmpty()) {
                        val formattedTable = formatTable(tableLines)
                        tableLines.clear()
                        tableLines.addAll(formattedTable)
                    }
                    inTable = true
                }
                tableLines.add(trimmedLine)
            } else {
                if (inTable) {
                    // End of table, format it
                    val formattedTable = formatTable(tableLines)
                    tableLines.clear()
                    tableLines.addAll(formattedTable)
                    inTable = false
                }
                tableLines.add(line) // Keep original line with indentation
            }
        }
        
        // Format any remaining table
        if (inTable && tableLines.isNotEmpty()) {
            val formattedTable = formatTable(tableLines)
            tableLines.clear()
            tableLines.addAll(formattedTable)
        }
        
        return tableLines.joinToString("\n")
    }
    
    /**
     * Checks if a line is a table line (starts and ends with |)
     */
    private fun isTableLine(line: String): Boolean {
        return line.startsWith("|") && line.endsWith("|") && line.length > 2
    }
    
    /**
     * Formats a table with proper alignment and spacing
     */
    private fun formatTable(tableLines: List<String>): List<String> {
        if (tableLines.isEmpty()) return tableLines
        
        // Parse table cells
        val table = tableLines.map { line ->
            parseCells(line)
        }
        
        if (table.isEmpty()) return tableLines
        
        // Calculate column widths
        val columnCount = table.maxOfOrNull { it.size } ?: 0
        val columnWidths = IntArray(columnCount)
        
        for (row in table) {
            for (i in row.indices) {
                columnWidths[i] = maxOf(columnWidths[i], row[i].length)
            }
        }
        
        // Format each row
        return table.map { row ->
            formatTableRow(row, columnWidths)
        }
    }
    
    /**
     * Parses cells from a table line
     */
    private fun parseCells(line: String): List<String> {
        val trimmed = line.trim()
        if (!trimmed.startsWith("|") || !trimmed.endsWith("|")) {
            return listOf(trimmed)
        }
        
        // Remove leading and trailing pipes and split by |
        val content = trimmed.substring(1, trimmed.length - 1)
        return content.split("|").map { it.trim() }
    }
    
    /**
     * Formats a table row with proper spacing and alignment
     */
    private fun formatTableRow(cells: List<String>, columnWidths: IntArray): String {
        val formattedCells = mutableListOf<String>()
        
        for (i in cells.indices) {
            val cell = cells[i]
            val width = if (i < columnWidths.size) columnWidths[i] else cell.length
            
            // Left-align content with 1 space padding
            val paddedCell = " ${cell.padEnd(width)} "
            formattedCells.add(paddedCell)
        }
        
        // Handle case where this row has fewer cells than the maximum
        for (i in cells.size until columnWidths.size) {
            val paddedCell = " ${"".padEnd(columnWidths[i])} "
            formattedCells.add(paddedCell)
        }
        
        return "|${formattedCells.joinToString("|")}|"
    }
    
    /**
     * Formats tables in a PSI element
     */
    fun formatTablesInElement(element: PsiElement): PsiElement {
        // This would involve traversing the PSI tree and formatting table elements
        // For now, we'll handle this through text-based formatting in the post-processor
        return element
    }
}
