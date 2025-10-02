package com.mb.mbplugin.karate.formatting

import com.intellij.formatting.*
import com.intellij.lang.ASTNode
import com.intellij.psi.TokenType
import com.intellij.psi.formatter.common.AbstractBlock
import com.mb.mbplugin.karate.psi.KarateTokenTypes
import java.util.*

/**
 * Represents a formatting block for Karate language elements.
 * Each block corresponds to a PSI element and defines how it should be formatted
 * in terms of spacing, indentation, wrapping, and alignment.
 */
class KarateBlock(
    private val node: ASTNode,
    private val wrap: Wrap?,
    private val alignment: Alignment?,
    private val indent: Indent,
    private val spacingBuilder: SpacingBuilder
) : AbstractBlock(node, wrap, alignment) {

    override fun buildChildren(): List<Block> {
        val blocks = mutableListOf<Block>()
        var child = myNode.firstChildNode
        
        while (child != null) {
            // Skip whitespace nodes - they will be handled by spacing rules
            if (child.elementType != TokenType.WHITE_SPACE && child.textLength > 0) {
                val childWrap = getChildWrap(child)
                val childAlignment = getChildAlignment(child)
                val childIndent = getChildIndent(child)
                
                blocks.add(
                    KarateBlock(
                        node = child,
                        wrap = childWrap,
                        alignment = childAlignment,
                        indent = childIndent,
                        spacingBuilder = spacingBuilder
                    )
                )
            }
            child = child.treeNext
        }
        
        return blocks
    }

    override fun getSpacing(child1: Block?, child2: Block): Spacing? {
        return spacingBuilder.getSpacing(this, child1, child2)
    }

    override fun isLeaf(): Boolean {
        return myNode.firstChildNode == null
    }

    override fun getIndent(): Indent? {
        return indent
    }

    override fun getChildAttributes(newChildIndex: Int): ChildAttributes {
        val elementType = myNode.elementType
        
        return when {
            // Feature description should be indented
            elementType == KarateTokenTypes.FEATURE_KEYWORD -> {
                ChildAttributes(Indent.getSpaceIndent(KarateCodeStyleSettings.FEATURE_DESCRIPTION_INDENT), null)
            }
            
            // Scenarios should be indented from Feature
            isScenarioKeyword(elementType) -> {
                ChildAttributes(Indent.getSpaceIndent(KarateCodeStyleSettings.SCENARIO_INDENT), null)
            }
            
            // Background should be indented like scenarios
            elementType == KarateTokenTypes.BACKGROUND_KEYWORD -> {
                ChildAttributes(Indent.getSpaceIndent(KarateCodeStyleSettings.STEP_INDENT), null)
            }
            
            // Steps should be indented from Scenario
            elementType == KarateTokenTypes.STEP_KEYWORD || 
            elementType == KarateTokenTypes.ACTION_KEYWORD ||
            elementType == KarateTokenTypes.ASTERISK -> {
                ChildAttributes(Indent.getSpaceIndent(KarateCodeStyleSettings.STEP_INDENT), null)
            }
            
            // Table cells should be aligned
            elementType == KarateTokenTypes.TABLE_CELL || elementType == KarateTokenTypes.PIPE -> {
                ChildAttributes(Indent.getSpaceIndent(KarateCodeStyleSettings.TABLE_CELL_INDENT), getTableAlignment())
            }
            
            // Examples table should be indented
            elementType == KarateTokenTypes.EXAMPLES_KEYWORD -> {
                ChildAttributes(Indent.getSpaceIndent(KarateCodeStyleSettings.EXAMPLES_INDENT), null)
            }
            
            // Tags should align with their element
            elementType == KarateTokenTypes.TAG -> {
                ChildAttributes(Indent.getNoneIndent(), null)
            }
            
            // Doc strings preserve their content
            elementType == KarateTokenTypes.PYSTRING -> {
                ChildAttributes(Indent.getSpaceIndent(KarateCodeStyleSettings.DOC_STRING_CONTENT_INDENT), null)
            }
            
            else -> ChildAttributes(Indent.getNoneIndent(), null)
        }
    }

    override fun isIncomplete(): Boolean {
        val elementType = myNode.elementType
        
        // Consider blocks incomplete if they're missing expected closing elements
        return when (elementType) {
            KarateTokenTypes.PYSTRING -> {
                // Check if doc string is properly closed
                !hasMatchingPyStringQuotes()
            }
            else -> false
        }
    }

    /**
     * Determines appropriate wrap settings for child nodes
     */
    private fun getChildWrap(child: ASTNode): Wrap? {
        return KarateWrapProcessor.createWrap(child)
    }

    /**
     * Determines appropriate alignment for child nodes
     */
    private fun getChildAlignment(child: ASTNode): Alignment? {
        val elementType = child.elementType
        
        return when {
            // Align table pipes vertically
            elementType == KarateTokenTypes.PIPE && KarateCodeStyleSettings.ALIGN_TABLE_PIPES -> {
                KarateAlignmentProcessor.getTableAlignment(child)
            }
            
            // Align table cells
            elementType == KarateTokenTypes.TABLE_CELL && KarateCodeStyleSettings.ALIGN_TABLE_PIPES -> {
                KarateAlignmentProcessor.getTableAlignment(child)
            }
            
            // Align scenario outline parameters
            elementType == KarateTokenTypes.STEP_PARAMETER_BRACE && KarateCodeStyleSettings.ALIGN_SCENARIO_PARAMETERS -> {
                KarateAlignmentProcessor.getParameterAlignment(child)
            }
            
            else -> null
        }
    }

    /**
     * Determines appropriate indentation for child nodes
     */
    private fun getChildIndent(child: ASTNode): Indent {
        val elementType = child.elementType
        val parentType = myNode.elementType
        
        return when {
            // Feature level - no indentation
            elementType == KarateTokenTypes.FEATURE_KEYWORD -> Indent.getNoneIndent()
            
            // Feature description
            parentType == KarateTokenTypes.FEATURE_KEYWORD && elementType == KarateTokenTypes.TEXT -> {
                Indent.getSpaceIndent(KarateCodeStyleSettings.FEATURE_DESCRIPTION_INDENT)
            }
            
            // Scenarios indented from Feature
            isScenarioKeyword(elementType) -> {
                Indent.getSpaceIndent(KarateCodeStyleSettings.SCENARIO_INDENT)
            }
            
            // Background indented from Feature like scenarios
            elementType == KarateTokenTypes.BACKGROUND_KEYWORD -> {
                Indent.getSpaceIndent(KarateCodeStyleSettings.SCENARIO_INDENT)
            }
            
            // Steps indented from Scenario or Background
            (elementType == KarateTokenTypes.STEP_KEYWORD || 
             elementType == KarateTokenTypes.ACTION_KEYWORD ||
             elementType == KarateTokenTypes.ASTERISK) -> {
                when (parentType) {
                    KarateTokenTypes.BACKGROUND_KEYWORD -> Indent.getSpaceIndent(KarateCodeStyleSettings.STEP_INDENT)
                    else -> Indent.getSpaceIndent(KarateCodeStyleSettings.STEP_INDENT)
                }
            }
            
            // Table cells need to be indented from steps
            elementType == KarateTokenTypes.PIPE -> {
                Indent.getSpaceIndent(KarateCodeStyleSettings.TABLE_CELL_INDENT)
            }
            
            // Table content
            elementType == KarateTokenTypes.TABLE_CELL -> {
                Indent.getSpaceIndent(KarateCodeStyleSettings.TABLE_CELL_INDENT)
            }
            
            elementType == KarateTokenTypes.PYSTRING -> {
                Indent.getSpaceIndent(KarateCodeStyleSettings.DOC_STRING_CONTENT_INDENT)
            }
            
            // Examples block indentation
            elementType == KarateTokenTypes.EXAMPLES_KEYWORD -> {
                Indent.getSpaceIndent(KarateCodeStyleSettings.EXAMPLES_INDENT)
            }
            
            // Tags align with their element
            elementType == KarateTokenTypes.TAG -> {
                when (parentType) {
                    KarateTokenTypes.FEATURE_KEYWORD -> Indent.getNoneIndent()
                    else -> Indent.getSpaceIndent(KarateCodeStyleSettings.SCENARIO_INDENT)
                }
            }
            
            else -> Indent.getNoneIndent()
        }
    }

    /**
     * Helper method to check if element type is a scenario keyword
     */
    private fun isScenarioKeyword(elementType: com.intellij.psi.tree.IElementType): Boolean {
        return KarateTokenTypes.SCENARIOS_KEYWORDS.contains(elementType) ||
               elementType == KarateTokenTypes.BACKGROUND_KEYWORD ||
               elementType == KarateTokenTypes.RULE_KEYWORD
    }

    /**
     * Creates alignment for table elements
     */
    private fun getTableAlignment(): Alignment? {
        return KarateAlignmentProcessor.getTableAlignment(myNode)
    }

    /**
     * Creates alignment for scenario parameters
     */
    private fun getParameterAlignment(): Alignment? {
        return KarateAlignmentProcessor.getParameterAlignment(myNode)
    }

    /**
     * Checks if doc string has properly matching quotes
     */
    private fun hasMatchingPyStringQuotes(): Boolean {
        var child = myNode.firstChildNode
        var openQuotes = 0
        var closeQuotes = 0
        
        while (child != null) {
            if (child.elementType == KarateTokenTypes.PYSTRING_QUOTES) {
                val text = child.text
                if (text.startsWith("\"\"\"")) {
                    if (openQuotes == closeQuotes) {
                        openQuotes++
                    } else {
                        closeQuotes++
                    }
                }
            }
            child = child.treeNext
        }
        
        return openQuotes == closeQuotes && openQuotes > 0
    }
}