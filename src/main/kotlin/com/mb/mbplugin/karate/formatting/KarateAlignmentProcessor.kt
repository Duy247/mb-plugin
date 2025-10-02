package com.mb.mbplugin.karate.formatting

import com.intellij.formatting.Alignment
import com.intellij.lang.ASTNode
import com.intellij.psi.tree.IElementType
import com.mb.mbplugin.karate.psi.KarateTokenTypes
import java.util.*

/**
 * Handles alignment decisions for Karate language elements
 */
object KarateAlignmentProcessor {
    
    private val tableAlignments = mutableMapOf<ASTNode, Alignment>()
    private val parameterAlignments = mutableMapOf<ASTNode, Alignment>()
    
    /**
     * Gets or creates alignment for table elements
     */
    fun getTableAlignment(node: ASTNode): Alignment? {
        if (!KarateCodeStyleSettings.ALIGN_TABLE_PIPES) return null
        
        val tableRoot = findTableRoot(node) ?: return null
        
        return tableAlignments.getOrPut(tableRoot) {
            Alignment.createAlignment(true)
        }
    }
    
    /**
     * Gets or creates alignment for scenario parameters
     */
    fun getParameterAlignment(node: ASTNode): Alignment? {
        if (!KarateCodeStyleSettings.ALIGN_SCENARIO_PARAMETERS) return null
        
        val scenarioRoot = findScenarioRoot(node) ?: return null
        
        return parameterAlignments.getOrPut(scenarioRoot) {
            Alignment.createAlignment(true)
        }
    }
    
    /**
     * Creates alignment for table columns based on alignment style
     */
    fun createTableColumnAlignment(columnIndex: Int): Alignment? {
        if (!KarateCodeStyleSettings.ALIGN_TABLE_PIPES) return null
        
        return when (KarateCodeStyleSettings.TABLE_COLUMN_ALIGNMENT) {
            KarateCodeStyleSettings.TableColumnAlignment.LEFT -> {
                Alignment.createAlignment(true, Alignment.Anchor.LEFT)
            }
            KarateCodeStyleSettings.TableColumnAlignment.RIGHT -> {
                Alignment.createAlignment(true, Alignment.Anchor.RIGHT)
            }
            KarateCodeStyleSettings.TableColumnAlignment.CENTER -> {
                // Center alignment is achieved by creating a child alignment
                val baseAlignment = Alignment.createAlignment(true)
                Alignment.createChildAlignment(baseAlignment)
            }
        }
    }
    
    /**
     * Determines if node should be aligned
     */
    fun shouldAlign(node: ASTNode): Boolean {
        val elementType = node.elementType
        
        return when {
            elementType == KarateTokenTypes.PIPE && KarateCodeStyleSettings.ALIGN_TABLE_PIPES -> true
            elementType == KarateTokenTypes.TABLE_CELL && KarateCodeStyleSettings.ALIGN_TABLE_PIPES -> true
            elementType == KarateTokenTypes.STEP_PARAMETER_BRACE && KarateCodeStyleSettings.ALIGN_SCENARIO_PARAMETERS -> true
            else -> false
        }
    }
    
    /**
     * Finds the root table node for alignment purposes
     */
    private fun findTableRoot(node: ASTNode): ASTNode? {
        var current = node
        
        while (current.treeParent != null) {
            val parent = current.treeParent
            
            // Look for step node that contains this table
            if (isStepElement(parent.elementType)) {
                return parent
            }
            
            // Look for examples block
            if (parent.elementType == KarateTokenTypes.EXAMPLES_KEYWORD) {
                return parent
            }
            
            current = parent
        }
        
        return null
    }
    
    /**
     * Finds the root scenario node for parameter alignment
     */
    private fun findScenarioRoot(node: ASTNode): ASTNode? {
        var current = node
        
        while (current.treeParent != null) {
            val parent = current.treeParent
            
            if (KarateTokenTypes.SCENARIOS_KEYWORDS.contains(parent.elementType)) {
                return parent
            }
            
            current = parent
        }
        
        return null
    }
    
    private fun isStepElement(elementType: IElementType): Boolean {
        return elementType == KarateTokenTypes.STEP_KEYWORD ||
                elementType == KarateTokenTypes.ACTION_KEYWORD ||
                elementType == KarateTokenTypes.ASTERISK
    }
    
    /**
     * Clears cached alignments
     */
    fun clearCache() {
        tableAlignments.clear()
        parameterAlignments.clear()
    }
}