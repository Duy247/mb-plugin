package com.mb.mbplugin.karate.psi

import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.psi.tree.IElementType
import com.mb.mbplugin.karate.lexer.KarateLexer

class KarateSyntaxHighlighter : SyntaxHighlighterBase() {
    companion object {
        private val ATTRIBUTES = mutableMapOf<IElementType, TextAttributesKey>().apply {
            put(KarateTokenTypes.COMMENT, GherkinHighlighter.COMMENT)
            put(KarateTokenTypes.FEATURE_KEYWORD, GherkinHighlighter.KEYWORD)
            put(KarateTokenTypes.BACKGROUND_KEYWORD, GherkinHighlighter.KEYWORD)
            put(KarateTokenTypes.SCENARIO_KEYWORD, GherkinHighlighter.KEYWORD)
            put(KarateTokenTypes.SCENARIO_OUTLINE_KEYWORD, GherkinHighlighter.KEYWORD)
            put(KarateTokenTypes.EXAMPLES_KEYWORD, GherkinHighlighter.KEYWORD)
            put(KarateTokenTypes.STEP_KEYWORD, GherkinHighlighter.STEP_KEYWORD)
            put(KarateTokenTypes.ACTION_KEYWORD, GherkinHighlighter.STEP_KEYWORD)
            put(KarateTokenTypes.TAG, GherkinHighlighter.TAG)
            put(KarateTokenTypes.TEXT, GherkinHighlighter.TEXT)
            put(KarateTokenTypes.PIPE, GherkinHighlighter.PIPE)
            put(KarateTokenTypes.SINGLE_QUOTED_STRING, GherkinHighlighter.QUOTE)
            put(KarateTokenTypes.DOUBLE_QUOTED_STRING, GherkinHighlighter.QUOTE)
            put(KarateTokenTypes.TABLE_CELL, GherkinHighlighter.TABLE_CELL)
            put(KarateTokenTypes.VARIABLE, GherkinHighlighter.KARATE_VARIABLE)
            put(KarateTokenTypes.DECLARATION, GherkinHighlighter.KARATE_REFERENCE)
        }
    }

    override fun getHighlightingLexer(): Lexer = KarateLexer()

    override fun getTokenHighlights(tokenType: IElementType): Array<TextAttributesKey> {
        val attribute = ATTRIBUTES[tokenType]
        return if (attribute != null) {
            arrayOf(attribute)
        } else {
            emptyArray()
        }
    }
}