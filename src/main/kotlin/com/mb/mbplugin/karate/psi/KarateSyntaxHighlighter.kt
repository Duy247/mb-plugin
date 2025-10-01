package com.mb.mbplugin.karate.psi

import com.intellij.ide.highlighter.XmlFileHighlighter
import com.intellij.json.JsonElementTypes
import com.intellij.json.json5.highlighting.Json5SyntaxHighlightingFactory
import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.tree.IElementType
import com.mb.mbplugin.karate.lexer.KarateLexer
import java.util.*

class KarateSyntaxHighlighter(
    private val project: Project? = null,
    private val virtualFile: VirtualFile? = null
) : SyntaxHighlighterBase() {
    
    companion object {
        private val ATTRIBUTES = mutableMapOf<IElementType, TextAttributesKey>().apply {
            // Keywords
            put(KarateTokenTypes.FEATURE_KEYWORD, GherkinHighlighter.KEYWORD)
            put(KarateTokenTypes.BACKGROUND_KEYWORD, GherkinHighlighter.KEYWORD)
            put(KarateTokenTypes.SCENARIO_KEYWORD, GherkinHighlighter.KEYWORD)
            put(KarateTokenTypes.SCENARIO_OUTLINE_KEYWORD, GherkinHighlighter.KEYWORD)
            put(KarateTokenTypes.EXAMPLES_KEYWORD, GherkinHighlighter.KEYWORD)
            put(KarateTokenTypes.RULE_KEYWORD, GherkinHighlighter.KEYWORD)
            put(KarateTokenTypes.EXAMPLE_KEYWORD, GherkinHighlighter.KEYWORD)
            
            // Step keywords
            put(KarateTokenTypes.STEP_KEYWORD, GherkinHighlighter.STEP_KEYWORD)
            put(KarateTokenTypes.ACTION_KEYWORD, GherkinHighlighter.STEP_KEYWORD)
            
            // Other elements
            put(KarateTokenTypes.COMMENT, GherkinHighlighter.COMMENT)
            put(KarateTokenTypes.TEXT, GherkinHighlighter.TEXT)
            put(KarateTokenTypes.OPERATOR, GherkinHighlighter.TEXT)
            put(KarateTokenTypes.DECLARATION, GherkinHighlighter.KARATE_REFERENCE)
            put(KarateTokenTypes.VARIABLE, GherkinHighlighter.KARATE_VARIABLE)
            put(KarateTokenTypes.TAG, GherkinHighlighter.TAG)
            put(KarateTokenTypes.PYSTRING, GherkinHighlighter.QUOTE)
            put(KarateTokenTypes.PYSTRING_INCOMPLETE, GherkinHighlighter.QUOTE)
            put(KarateTokenTypes.PYSTRING_QUOTES, GherkinHighlighter.QUOTE)
            put(JsonElementTypes.OBJECT, GherkinHighlighter.QUOTE)
            put(KarateTokenTypes.TABLE_CELL, GherkinHighlighter.TABLE_CELL)
            put(KarateTokenTypes.PIPE, GherkinHighlighter.PIPE)
            put(KarateTokenTypes.SINGLE_QUOTED_STRING, GherkinHighlighter.QUOTE)
            put(KarateTokenTypes.DOUBLE_QUOTED_STRING, GherkinHighlighter.QUOTE)
            put(KarateTokenTypes.JSON_INJECTABLE, GherkinHighlighter.KARATE_REFERENCE)
        }
        
        private val JSON_HIGHLIGHTER = Json5SyntaxHighlightingFactory()
        private val XML_HIGHLIGHTER = XmlFileHighlighter()
    }

    override fun getHighlightingLexer(): Lexer = KarateLexer()

    override fun getTokenHighlights(tokenType: IElementType): Array<TextAttributesKey> {
        // First check our custom attributes
        if (ATTRIBUTES.containsKey(tokenType)) {
            return arrayOf(ATTRIBUTES[tokenType]!!)
        }
        
        // Try JSON highlighting
        if (project != null && virtualFile != null) {
            val jsonHighlights = JSON_HIGHLIGHTER.getSyntaxHighlighter(project, virtualFile).getTokenHighlights(tokenType)
            if (jsonHighlights.isNotEmpty()) {
                return jsonHighlights.toList().toTypedArray()
            }
        }
        
        // Try XML highlighting
        val xmlHighlights = XML_HIGHLIGHTER.getTokenHighlights(tokenType)
        if (xmlHighlights.isNotEmpty()) {
            return xmlHighlights.toList().toTypedArray()
        }
        
        return emptyArray()
    }
}