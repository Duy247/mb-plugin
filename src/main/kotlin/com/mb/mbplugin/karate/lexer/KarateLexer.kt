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
    private var isInTableRow = false
    private var isFirstTableRow = false

    // Enhanced keyword sets for better recognition
    private val KARATE_DSL_KEYWORDS = setOf(
        "def", "text", "eval", "match", "assert", "call", "read", "set", "remove", "replace",
        "configure", "callonce", "print", "get", "copy", "table", "yaml", "csv", "string",
        "json", "xml", "xmlstring", "bytes", "listen", "doc", "param", "header", "cookie",
        "form", "multipart", "params", "headers", "cookies", "retry", "soap",
        "contains", "only", "deep", "schema", "type", "each", "js", "graphql", "if", "else", "compare",
        "delay", "repeat", "karateEnv", "karateTags", "driver", "robot", "report"
    )
    
    private val HTTP_KEYWORDS = setOf(
        "url", "path", "method", "status", "request", "response", "responseBytes", 
        "responseStatus", "responseHeaders", "responseCookies", "responseTime", 
        "responseType", "requestTimeStamp", "get", "post", "put", "delete", "patch", "head", "options",
        "connect", "trace", "http", "https", "ws", "wss", "graphql", "soap", "multipart", "form"
    )

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
        val startPosition = position // Save starting position to check for progress
        
        when {
            isAtEnd() -> {
                tokenType = null
                return
            }
            isWhitespace(currentChar()) -> skipWhitespace()
            currentChar() == '#' -> readComment()
            currentChar() == '@' -> readTag()
            currentChar() == '|' -> readPipe()
            currentChar() == '*' -> readAsterisk()
            currentChar() == '"' -> readQuotedString('"')
            currentChar() == '\'' -> readQuotedString('\'')
            currentChar() == ':' -> readColon()
            currentChar() == '<' && isPlaceholderStart() -> readPlaceholder()
            isJsonContentStart() -> readJsonContent()  // Use the new method for full JSON content
            currentChar() == '{' -> readJsonStart()
            currentChar() == '}' -> readJsonEnd()
            isXmlContentStart() -> readXmlContent()    // Use the new method for full XML content
            currentChar() == '<' && !isPlaceholderStart() -> readXmlStart()
            currentChar() == '>' -> readXmlEnd()
            isInTableRow && !isWhitespace(currentChar()) && currentChar() != '\n' -> readTableCell()
            isKeywordStart() -> readKeywordOrText()
            else -> readText()
        }
        
        // Check if we're at the end of a table row
        if (isInTableRow && (currentChar() == '\n' || position >= endOffset)) {
            isInTableRow = false
        }
        
        // Ensure position always advances
        if (position == startPosition) {
            // If no progress was made, forcibly advance position
            position++
            tokenType = KarateTokenTypes.TEXT
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
        // Check if this is the start of a table row
        if (!isInTableRow) {
            isInTableRow = true
            isFirstTableRow = !hasSeenTableBefore()
        }
        tokenType = KarateTokenTypes.PIPE
        
        // After a pipe, let's check if we have a table cell to read
        if (position < endOffset && currentChar() != '|' && !isWhitespace(currentChar()) && currentChar() != '\n') {
            // Remember the current position so we can return to it
            val savedPosition = position
            position = savedPosition // Move back to start of cell content
            
            // We'll handle the table cell in the next advance() call
        }
    }

    private fun readAsterisk() {
        position++ // skip '*'
        tokenType = KarateTokenTypes.ASTERISK
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
        
        // Handle multi-word keywords like "scenario outline"
        if (peekMultiWordKeyword()) {
            readMultiWordKeyword()
            return
        }
        
        while (position < endOffset && (currentChar().isLetterOrDigit() || currentChar() == '_' || currentChar() == '-')) {
            position++
        }
        
        val text = buffer!!.subSequence(start, position).toString().lowercase()
        tokenType = when (text) {
            "feature" -> KarateTokenTypes.FEATURE_KEYWORD
            "background" -> KarateTokenTypes.BACKGROUND_KEYWORD
            "scenario" -> KarateTokenTypes.SCENARIO_KEYWORD
            "examples" -> KarateTokenTypes.EXAMPLES_KEYWORD
            "rule" -> KarateTokenTypes.RULE_KEYWORD
            "example" -> KarateTokenTypes.EXAMPLE_KEYWORD
            "given", "when", "then", "and", "but" -> KarateTokenTypes.STEP_KEYWORD
            in KARATE_DSL_KEYWORDS -> KarateTokenTypes.KARATE_DSL_KEYWORD
            in HTTP_KEYWORDS -> KarateTokenTypes.HTTP_KEYWORD
            else -> {
                // Check if this might be a Karate action keyword
                when {
                    text in setOf("match", "assert", "print", "call", "read", "set", "remove", "replace", "eval") -> 
                        KarateTokenTypes.ACTION_KEYWORD
                    else -> KarateTokenTypes.TEXT
                }
            }
        }
    }

    private fun readText() {
        val start = position
        
        while (position < endOffset && !isSpecialChar(currentChar()) && !isWhitespace(currentChar())) {
            position++
        }
        
        // Check if this is a Karate variable reference
        val text = buffer!!.subSequence(start, position).toString()
        if (text.startsWith("#") && text.length > 1) {
            // This is a Karate variable reference
            tokenType = KarateTokenTypes.VARIABLE
        } else {
            // Regular text
            tokenType = KarateTokenTypes.TEXT
        }
    }

    private fun isWhitespace(c: Char): Boolean = c.isWhitespace()

    private fun isKeywordStart(): Boolean = currentChar().isLetter()

    private fun isSpecialChar(c: Char): Boolean = c in "@#|*\"':<>{}"

    // New helper methods for enhanced functionality
    private fun isPlaceholderStart(): Boolean {
        return position + 1 < endOffset && (buffer!![position + 1].isLetterOrDigit() || buffer!![position + 1] == '_')
    }
    
    private fun readPlaceholder() {
        position++ // skip '<'
        
        // Read placeholder content if present
        val contentStart = position
        val variableName = StringBuilder()
        
        while (position < endOffset && currentChar() != '>') {
            variableName.append(currentChar())
            position++
        }
        
        if (position < endOffset && currentChar() == '>') {
            // Don't forget to consume the closing bracket
            position++
        }
        
        if (position - contentStart > 0) {
            // There was content inside the placeholder
            val content = variableName.toString()
            
            // Check if this variable matches a table header cell name
            // For simplicity, we're assuming any alphanumeric placeholder is an example variable
            // In a more sophisticated implementation, we would check if it matches a table header
            if (content.all { it.isLetterOrDigit() || it == '_' }) {
                tokenType = KarateTokenTypes.EXAMPLE_VARIABLE
            } else {
                tokenType = KarateTokenTypes.PLACEHOLDER_CONTENT
            }
        } else {
            // Just an empty placeholder
            tokenType = KarateTokenTypes.PLACEHOLDER_START
        }
    }
    
    private fun readJsonStart() {
        position++ // skip '{'
        tokenType = KarateTokenTypes.JSON_START
    }
    
    private fun readJsonEnd() {
        position++ // skip '}'
        tokenType = KarateTokenTypes.JSON_END
    }
    
    // Helper methods for JSON parsing
    private fun isJsonContentStart(): Boolean {
        return currentChar() == '{'
    }
    
    private fun readJsonContent() {
        // This is a simplified JSON parser
        // In a real implementation, we'd use a more sophisticated approach
        var depth = 1  // Already saw one opening brace
        position++     // Skip the opening brace
        
        while (position < endOffset && depth > 0) {
            when (currentChar()) {
                '{' -> depth++
                '}' -> depth--
                '"' -> skipJsonString()
                '\'' -> skipJsonString('\'')
            }
            if (position < endOffset && depth > 0) {
                position++
            }
        }
        
        tokenType = KarateTokenTypes.JSON_INJECTABLE
    }
    
    private fun skipJsonString(quote: Char = '"') {
        position++  // Skip the opening quote
        while (position < endOffset && currentChar() != quote) {
            // Handle escaped quotes
            if (currentChar() == '\\' && position + 1 < endOffset) {
                position += 2  // Skip the escape and the character
            } else {
                position++
            }
        }
        // Skip the closing quote
        if (position < endOffset) {
            position++
        }
    }
    
    private fun readXmlStart() {
        position++ // skip '<'
        tokenType = KarateTokenTypes.XML_START
    }
    
    private fun readXmlEnd() {
        position++ // skip '>'
        tokenType = KarateTokenTypes.XML_END
    }
    
    // Helper methods for XML parsing
    private fun isXmlContentStart(): Boolean {
        return currentChar() == '<' && !isPlaceholderStart()
    }
    
    private fun readXmlContent() {
        // This is a simplified XML parser
        // In a real implementation, we'd use a more sophisticated approach
        position++ // Skip the opening '<'
        
        // Find matching closing tag
        var depth = 1
        while (position < endOffset && depth > 0) {
            when {
                currentChar() == '<' && lookAhead(1) != '/' -> {
                    depth++
                    position++
                }
                currentChar() == '<' && lookAhead(1) == '/' -> {
                    depth--
                    position += 2 // Skip '</' together
                }
                currentChar() == '>' -> {
                    position++
                    if (depth == 0) break
                }
                else -> position++
            }
        }
        
        tokenType = KarateTokenTypes.XML_START // Using XML_START as a catch-all for XML content
    }
    
    private fun lookAhead(offset: Int): Char {
        return if (position + offset < endOffset) buffer!![position + offset] else '\u0000'
    }
    
    private fun peekMultiWordKeyword(): Boolean {
        val remaining = buffer!!.subSequence(position, endOffset).toString().lowercase()
        return remaining.startsWith("scenario outline")
    }
    
    private fun readMultiWordKeyword() {
        val remaining = buffer!!.subSequence(position, endOffset).toString().lowercase()
        when {
            remaining.startsWith("scenario outline") -> {
                position += "scenario outline".length
                tokenType = KarateTokenTypes.SCENARIO_OUTLINE_KEYWORD
            }
        }
    }
    
    private fun hasSeenTableBefore(): Boolean {
        // Simple heuristic: look backwards for previous pipe characters
        for (i in (position - 1) downTo startOffset) {
            if (buffer!![i] == '|') return true
            if (buffer!![i] == '\n') break
        }
        return false
    }
    
    // Track table headers for example variables
    private val tableHeaderNames = mutableListOf<String>()
    
    private fun readTableCell() {
        val start = position
        
        // Read until next pipe or end of line
        while (position < endOffset && currentChar() != '|' && currentChar() != '\n') {
            position++
        }
        
        // Trim trailing whitespace
        var end = position
        while (end > start && isWhitespace(buffer!![end - 1])) {
            end--
        }
        
        // Extract cell content
        val cellContent = buffer!!.subSequence(start, end).toString().trim()
        
        // Determine if this is a header cell (in first row) or regular cell
        if (isFirstTableRow) {
            // This is a header cell, store its name for variable highlighting
            tableHeaderNames.add(cellContent)
            tokenType = KarateTokenTypes.TABLE_HEADER_CELL
        } else {
            tokenType = KarateTokenTypes.TABLE_CELL
        }
    }
}