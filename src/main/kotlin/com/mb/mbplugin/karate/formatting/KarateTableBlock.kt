package com.mb.mbplugin.karate.formatting

import com.intellij.formatting.*
import com.intellij.lang.ASTNode
import com.intellij.psi.formatter.common.AbstractBlock
import com.mb.mbplugin.karate.psi.KarateTokenTypes

/**
 * Specialized formatting block for Karate tables.
 * Handles proper table alignment and indentation.
 */
class KarateTableBlock(
    node: ASTNode,
    wrap: Wrap?,
    alignment: Alignment?,
    private val spacingBuilder: SpacingBuilder,
    private val tableIndent: Int
) : AbstractBlock(node, wrap, alignment) {

    override fun buildChildren(): List<Block> {
        val blocks = mutableListOf<Block>()
        var child = myNode.firstChildNode
        
        while (child != null) {
            if (child.elementType != com.intellij.psi.TokenType.WHITE_SPACE && child.textLength > 0) {
                val childAlignment = if (child.elementType == KarateTokenTypes.PIPE) {
                    getTablePipeAlignment()
                } else null
                
                blocks.add(
                    KarateBlock(
                        node = child,
                        wrap = null,
                        alignment = childAlignment,
                        indent = getTableCellIndent(child),
                        spacingBuilder = spacingBuilder
                    )
                )
            }
            child = child.treeNext
        }
        
        return blocks
    }

    override fun getIndent(): Indent = Indent.getSpaceIndent(tableIndent)

    override fun getSpacing(child1: Block?, child2: Block): Spacing? {
        return spacingBuilder.getSpacing(this, child1, child2)
    }

    override fun isLeaf(): Boolean = myNode.firstChildNode == null

    override fun getChildAttributes(newChildIndex: Int): ChildAttributes {
        return ChildAttributes(Indent.getSpaceIndent(tableIndent), getTablePipeAlignment())
    }
    
    private fun getTablePipeAlignment(): Alignment? {
        return if (KarateCodeStyleSettings.ALIGN_TABLE_PIPES) {
            KarateAlignmentProcessor.getTableAlignment(myNode)
        } else null
    }
    
    private fun getTableCellIndent(child: ASTNode): Indent {
        return when (child.elementType) {
            KarateTokenTypes.PIPE -> Indent.getNoneIndent()
            KarateTokenTypes.TABLE_CELL -> Indent.getNoneIndent() // Cells don't need extra indent within table
            else -> Indent.getNoneIndent()
        }
    }
}