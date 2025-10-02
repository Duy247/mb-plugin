package com.mb.mbplugin.karate.psi

import com.intellij.json.JsonTokenType
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet

interface KarateTokenTypes {
    companion object {
        @JvmStatic
        val COMMENT = KarateElementType("COMMENT")
        @JvmStatic
        val TEXT = KarateElementType("TEXT")
        @JvmStatic
        val OPERATOR = KarateElementType("OPERATOR")
        @JvmStatic
        val EXAMPLES_KEYWORD = KarateElementType("EXAMPLES_KEYWORD")
        @JvmStatic
        val FEATURE_KEYWORD = KarateElementType("FEATURE_KEYWORD")
        @JvmStatic
        val RULE_KEYWORD = KarateElementType("RULE_KEYWORD")
        @JvmStatic
        val BACKGROUND_KEYWORD = KarateElementType("BACKGROUND_KEYWORD")
        @JvmStatic
        val SCENARIO_KEYWORD = KarateElementType("SCENARIO_KEYWORD")
        @JvmStatic
        val EXAMPLE_KEYWORD = KarateElementType("EXAMPLE_KEYWORD")
        @JvmStatic
        val SCENARIO_OUTLINE_KEYWORD = KarateElementType("SCENARIO_OUTLINE_KEYWORD")
        @JvmStatic
        val STEP_KEYWORD = KarateElementType("STEP_KEYWORD")
        @JvmStatic
        val ACTION_KEYWORD = KarateElementType("ACTION_KEYWORD")
        @JvmStatic
        val ASTERISK = KarateElementType("ASTERISK")
        @JvmStatic
        val STEP_PARAMETER_BRACE = KarateElementType("STEP_PARAMETER_BRACE")
        @JvmStatic
        val STEP_PARAMETER_TEXT = KarateElementType("STEP_PARAMETER_TEXT")
        @JvmStatic
        val COLON = KarateElementType("COLON")
        @JvmStatic
        val TAG = KarateElementType("TAG")
        @JvmStatic
        val DECLARATION = KarateElementType("DECLARATION")
        @JvmStatic
        val VARIABLE = KarateElementType("VARIABLE")
        @JvmStatic
        val PYSTRING_QUOTES = KarateElementType("PYSTRING_QUOTES")
        @JvmStatic
        val PYSTRING = KarateElementType("PYSTRING_ELEMENT")
        @JvmStatic
        val PYSTRING_INCOMPLETE = KarateElementType("PYSTRING_INCOMPLETE")
        @JvmStatic
        val SINGLE_QUOTED_STRING = KarateElementType("SINGLE_QUOTED_STRING")
        @JvmStatic
        val DOUBLE_QUOTED_STRING = KarateElementType("DOUBLE_QUOTED_STRING")
        @JvmStatic
        val JSON_INJECTABLE: IElementType = JsonTokenType("JSON_INJECTABLE")
        @JvmStatic
        val PIPE = KarateElementType("PIPE")
        @JvmStatic
        val OPEN_PAREN = KarateElementType("OPEN_PAREN")
    @JvmStatic
    val CLOSE_PAREN = KarateElementType("CLOSE_PAREN")
    @JvmStatic
    val TABLE_CELL = KarateElementType("TABLE_CELL")
    @JvmStatic
    val TABLE_HEADER_CELL = KarateElementType("TABLE_HEADER_CELL")
    @JvmStatic
    val PLACEHOLDER_START = KarateElementType("PLACEHOLDER_START")
    @JvmStatic
    val PLACEHOLDER_END = KarateElementType("PLACEHOLDER_END")
    @JvmStatic
    val PLACEHOLDER_CONTENT = KarateElementType("PLACEHOLDER_CONTENT")
    @JvmStatic
    val KARATE_DSL_KEYWORD = KarateElementType("KARATE_DSL_KEYWORD")
    @JvmStatic
    val HTTP_KEYWORD = KarateElementType("HTTP_KEYWORD")
    @JvmStatic
    val JSON_START = KarateElementType("JSON_START")
    @JvmStatic
    val JSON_END = KarateElementType("JSON_END")
    @JvmStatic
    val XML_START = KarateElementType("XML_START")
    @JvmStatic
    val XML_END = KarateElementType("XML_END")
    @JvmStatic
    val EXAMPLE_VARIABLE = KarateElementType("EXAMPLE_VARIABLE")

    @JvmStatic
    val KEYWORDS = TokenSet.create(
        FEATURE_KEYWORD, RULE_KEYWORD, EXAMPLE_KEYWORD,
        BACKGROUND_KEYWORD, SCENARIO_KEYWORD, SCENARIO_OUTLINE_KEYWORD,
        EXAMPLES_KEYWORD, ACTION_KEYWORD, STEP_KEYWORD, ASTERISK,
        KARATE_DSL_KEYWORD, HTTP_KEYWORD
    )        
    
        @JvmStatic
        val QUOTED_STRING = TokenSet.create(SINGLE_QUOTED_STRING, DOUBLE_QUOTED_STRING)

        @JvmStatic
        val TEXT_LIKE = TokenSet.create(TEXT, OPERATOR)

        @JvmStatic
        val SCENARIOS_KEYWORDS = TokenSet.create(SCENARIO_KEYWORD, SCENARIO_OUTLINE_KEYWORD, EXAMPLE_KEYWORD)

        @JvmStatic
        val COMMENTS = TokenSet.create(COMMENT)

        @JvmStatic
        val PLACEHOLDERS = TokenSet.create(PLACEHOLDER_START, PLACEHOLDER_END, PLACEHOLDER_CONTENT)

        @JvmStatic
        val DSL_KEYWORDS = TokenSet.create(KARATE_DSL_KEYWORD, HTTP_KEYWORD, ACTION_KEYWORD)

        @JvmStatic
        val TABLE_ELEMENTS = TokenSet.create(TABLE_CELL, TABLE_HEADER_CELL, PIPE)

        @JvmStatic
        val JSON_ELEMENTS = TokenSet.create(JSON_START, JSON_END)

        @JvmStatic
        val XML_ELEMENTS = TokenSet.create(XML_START, XML_END)
    }
}