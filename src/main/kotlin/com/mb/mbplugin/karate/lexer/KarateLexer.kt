package com.mb.mbplugin.karate.lexer

import com.intellij.lexer.LexerBase
import com.intellij.psi.tree.IElementType
import com.mb.mbplugin.karate.psi.KarateTokenTypes

class KarateLexer : LexerBase() {
    private var buffer: CharSequence? = null
    private var startOffset = 0
    private var endOffset = 0
    private var position = 0
    private var tokenType: IElementType? = null
    private var tokenStart = 0
    private var tokenEnd = 0

    override fun start(buffer: CharSequence, startOffset: Int, endOffset: Int, initialState: Int) {
        this.buffer = buffer
        this.startOffset = startOffset
        this.endOffset = endOffset
        this.position = startOffset
        advance()
    }

    override fun getState(): Int = 0

    override fun getTokenType(): IElementType? = tokenType

    override fun getTokenStart(): Int = tokenStart

    override fun getTokenEnd(): Int = tokenEnd

    override fun advance() {
        if (position >= endOffset) {
            tokenType = null
            return
        }

        tokenStart = position
        
        when {
            isAtEnd() -> {
                tokenType = null
                return
            }
            isWhitespace(currentChar()) -> skipWhitespace()
            currentChar() == '#' -> readComment()
            currentChar() == '@' -> readTag()
            currentChar() == '|' -> readPipe()
            currentChar() == '"' -> readQuotedString('"')
            currentChar() == '\'' -> readQuotedString('\'')
            currentChar() == ':' -> readColon()
            isKeywordStart() -> readKeywordOrText()
            else -> readText()
        }
        
        tokenEnd = position
    }

    override fun getBufferSequence(): CharSequence = buffer ?: ""

    override fun getBufferEnd(): Int = endOffset

    private fun currentChar(): Char = if (position < endOffset) buffer!![position] else '\u0000'

    private fun isAtEnd(): Boolean = position >= endOffset

    private fun skipWhitespace() {
        while (position < endOffset && isWhitespace(currentChar())) {
            position++
        }
        tokenType = com.intellij.psi.TokenType.WHITE_SPACE
    }

    private fun readComment() {
        position++ // skip '#'
        while (position < endOffset && currentChar() != '\n') {
            position++
        }
        tokenType = KarateTokenTypes.COMMENT
    }

    private fun readTag() {
        position++ // skip '@'
        while (position < endOffset && !isWhitespace(currentChar()) && currentChar() != '\n') {
            position++
        }
        tokenType = KarateTokenTypes.TAG
    }

    private fun readPipe() {
        position++ // skip '|'
        tokenType = KarateTokenTypes.PIPE
    }

    private fun readQuotedString(quote: Char) {
        position++ // skip opening quote
        while (position < endOffset && currentChar() != quote) {
            position++
        }
        if (position < endOffset) position++ // skip closing quote
        
        tokenType = if (quote == '"') KarateTokenTypes.DOUBLE_QUOTED_STRING else KarateTokenTypes.SINGLE_QUOTED_STRING
    }

    private fun readColon() {
        position++
        tokenType = KarateTokenTypes.COLON
    }

    private fun readKeywordOrText() {
        val start = position
        while (position < endOffset && (currentChar().isLetterOrDigit() || currentChar() == '_')) {
            position++
        }
        
        val text = buffer!!.subSequence(start, position).toString().lowercase()
        tokenType = when (text) {
            "feature" -> KarateTokenTypes.FEATURE_KEYWORD
            "background" -> KarateTokenTypes.BACKGROUND_KEYWORD
            "scenario" -> KarateTokenTypes.SCENARIO_KEYWORD
            "scenario outline" -> KarateTokenTypes.SCENARIO_OUTLINE_KEYWORD
            "examples" -> KarateTokenTypes.EXAMPLES_KEYWORD
            "given", "when", "then", "and", "but" -> KarateTokenTypes.STEP_KEYWORD
            "match", "assert", "print", "call", "read", "set", "remove", "replace", "eval" -> KarateTokenTypes.ACTION_KEYWORD
            else -> KarateTokenTypes.TEXT
        }
    }

    private fun readText() {
        while (position < endOffset && !isSpecialChar(currentChar()) && !isWhitespace(currentChar())) {
            position++
        }
        tokenType = KarateTokenTypes.TEXT
    }

    private fun isWhitespace(c: Char): Boolean = c.isWhitespace()

    private fun isKeywordStart(): Boolean = currentChar().isLetter()

    private fun isSpecialChar(c: Char): Boolean = c in "@#|\"':"
}