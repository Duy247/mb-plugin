package com.mb.mbplugin.karate.formatting

import com.intellij.formatting.Wrap
import com.intellij.formatting.WrapType
import com.intellij.lang.ASTNode
import com.intellij.psi.tree.IElementType
import com.mb.mbplugin.karate.psi.KarateTokenTypes

/**
 * Handles wrapping decisions for Karate language elements
 */
object KarateWrapProcessor {
    
    /**
     * Determines if a node should be wrapped
     */
    fun shouldWrap(node: ASTNode, maxLineLength: Int): Boolean {
        val elementType = node.elementType
        val text = node.text
        
        return when {
            // Always wrap scenarios for readability
            isScenarioElement(elementType) -> true
            
            // Don't wrap long steps by default (simplified)
            isStepElement(elementType) -> false
            
            // Don't wrap table cells by default (simplified)
            elementType == KarateTokenTypes.TABLE_CELL -> false
            
            // Don't wrap doc strings content
            elementType == KarateTokenTypes.PYSTRING -> false
            
            else -> false
        }
    }
    
    /**
     * Creates appropriate wrap for the given node
     */
    fun createWrap(node: ASTNode): Wrap? {
        val elementType = node.elementType
        
        return when {
            isScenarioElement(elementType) -> Wrap.createWrap(WrapType.ALWAYS, false)
            
            // Simplified - no wrapping for steps or table cells by default
            isStepElement(elementType) -> null
            
            elementType == KarateTokenTypes.TABLE_CELL -> null
            
            else -> null
        }
    }
    
    private fun isScenarioElement(elementType: IElementType): Boolean {
        return KarateTokenTypes.SCENARIOS_KEYWORDS.contains(elementType) ||
                elementType == KarateTokenTypes.BACKGROUND_KEYWORD ||
                elementType == KarateTokenTypes.RULE_KEYWORD ||
                elementType == KarateTokenTypes.EXAMPLES_KEYWORD
    }
    
    private fun isStepElement(elementType: IElementType): Boolean {
        return elementType == KarateTokenTypes.STEP_KEYWORD ||
                elementType == KarateTokenTypes.ACTION_KEYWORD ||
                elementType == KarateTokenTypes.ASTERISK
    }
    
    private fun calculateTableRowLength(node: ASTNode): Int {
        var length = 0
        var current = node.treeParent?.firstChildNode
        
        while (current != null) {
            if (current.elementType == KarateTokenTypes.TABLE_CELL || 
                current.elementType == KarateTokenTypes.PIPE) {
                length += current.textLength
            }
            current = current.treeNext
        }
        
        return length
    }
}