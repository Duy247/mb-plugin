package com.mb.mbplugin.karate.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiFile
import com.mb.mbplugin.karate.psi.GherkinFileType

/**
 * Action to format Karate tables in the current file
 */
class FormatKarateTablesAction : AnAction("Format Karate Tables") {
    
    override fun actionPerformed(e: AnActionEvent) {
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val psiFile = e.getData(CommonDataKeys.PSI_FILE) ?: return
        
        if (!isKarateFile(psiFile)) return
        
        WriteCommandAction.runWriteCommandAction(psiFile.project) {
            formatTablesInEditor(editor)
        }
    }
    
    override fun update(e: AnActionEvent) {
        val psiFile = e.getData(CommonDataKeys.PSI_FILE)
        e.presentation.isEnabledAndVisible = isKarateFile(psiFile)
    }
    
    private fun isKarateFile(file: PsiFile?): Boolean {
        return file?.fileType == GherkinFileType.INSTANCE
    }
    
    private fun formatTablesInEditor(editor: Editor) {
        val document = editor.document
        val text = document.text
        val formattedText = formatTablesInText(text)
        
        if (formattedText != text) {
            document.setText(formattedText)
        }
    }
    
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
                tableLines.add(line) // Keep original indentation
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
    
    private fun isTableLine(line: String): Boolean {
        val trimmed = line.trim()
        return trimmed.startsWith("|") && trimmed.endsWith("|") && trimmed.count { it == '|' } > 2
    }
    
    private fun formatTableLines(lines: List<String>): List<String> {
        if (lines.isEmpty()) return lines
        
        // Use TABLE_CELL_INDENT for consistent indentation (6 spaces)
        val indentation = " ".repeat(6) // TABLE_CELL_INDENT constant
        
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
}