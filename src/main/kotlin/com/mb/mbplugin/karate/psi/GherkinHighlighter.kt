package com.mb.mbplugin.karate.psi

import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey

object GherkinHighlighter {
    @JvmStatic
    val COMMENT = TextAttributesKey.createTextAttributesKey(
        "GHERKIN_COMMENT",
        DefaultLanguageHighlighterColors.DOC_COMMENT
    )

    @JvmStatic
    val STEP_KEYWORD = TextAttributesKey.createTextAttributesKey(
        "KARATE_STEP_KEYWORD",
        DefaultLanguageHighlighterColors.DOC_COMMENT
    )

    @JvmStatic
    val QUOTE = TextAttributesKey.createTextAttributesKey(
        "QUOTE",
        DefaultLanguageHighlighterColors.STRING
    )

    @JvmStatic
    val KEYWORD = TextAttributesKey.createTextAttributesKey(
        "GHERKIN_KEYWORD",
        DefaultLanguageHighlighterColors.KEYWORD
    )

    @JvmStatic
    val TEXT = TextAttributesKey.createTextAttributesKey(
        "GHERKIN_TEXT",
        DefaultLanguageHighlighterColors.STRING
    )

    @JvmStatic
    val TAG = TextAttributesKey.createTextAttributesKey(
        "GHERKIN_TAG",
        DefaultLanguageHighlighterColors.METADATA
    )

    @JvmStatic
    val REGEXP_PARAMETER = TextAttributesKey.createTextAttributesKey(
        "GHERKIN_REGEXP_PARAMETER",
        DefaultLanguageHighlighterColors.PARAMETER
    )

    @JvmStatic
    val TABLE_CELL = TextAttributesKey.createTextAttributesKey(
        "GHERKIN_TABLE_CELL",
        REGEXP_PARAMETER
    )

    @JvmStatic
    val KARATE_REFERENCE = TextAttributesKey.createTextAttributesKey(
        "KARATE_REFERENCE",
        DefaultLanguageHighlighterColors.INSTANCE_FIELD
    )

    @JvmStatic
    val KARATE_VARIABLE = TextAttributesKey.createTextAttributesKey(
        "KARATE_VARIABLE",
        DefaultLanguageHighlighterColors.INSTANCE_FIELD
    )

    @JvmStatic
    val PIPE = TextAttributesKey.createTextAttributesKey("GHERKIN_TABLE_PIPE", KEYWORD)

    @JvmStatic
    val KARATE_DSL_KEYWORD = TextAttributesKey.createTextAttributesKey(
        "KARATE_DSL_KEYWORD",
        DefaultLanguageHighlighterColors.KEYWORD
    )

    @JvmStatic
    val HTTP_KEYWORD = TextAttributesKey.createTextAttributesKey(
        "KARATE_HTTP_KEYWORD",
        DefaultLanguageHighlighterColors.INSTANCE_METHOD
    )

    @JvmStatic
    val TABLE_HEADER_CELL = TextAttributesKey.createTextAttributesKey(
        "KARATE_TABLE_HEADER",
        DefaultLanguageHighlighterColors.CLASS_NAME
    )

    @JvmStatic
    val PLACEHOLDER_VARIABLE = TextAttributesKey.createTextAttributesKey(
        "KARATE_PLACEHOLDER",
        DefaultLanguageHighlighterColors.TEMPLATE_LANGUAGE_COLOR
    )

    @JvmStatic
    val JSON_CONTENT = TextAttributesKey.createTextAttributesKey(
        "KARATE_JSON",
        DefaultLanguageHighlighterColors.BRACES
    )

    @JvmStatic
    val XML_CONTENT = TextAttributesKey.createTextAttributesKey(
        "KARATE_XML",
        DefaultLanguageHighlighterColors.MARKUP_TAG
    )
    
    @JvmStatic
    val TABLE_FIRST_ROW = TextAttributesKey.createTextAttributesKey(
        "KARATE_TABLE_FIRST_ROW",
        DefaultLanguageHighlighterColors.INTERFACE_NAME
    )
    
    @JvmStatic
    val KARATE_FUNCTION = TextAttributesKey.createTextAttributesKey(
        "KARATE_FUNCTION",
        DefaultLanguageHighlighterColors.FUNCTION_DECLARATION
    )
    
    @JvmStatic
    val KARATE_OPERATOR = TextAttributesKey.createTextAttributesKey(
        "KARATE_OPERATOR",
        DefaultLanguageHighlighterColors.OPERATION_SIGN
    )
    
    @JvmStatic
    val EXAMPLE_VARIABLE = TextAttributesKey.createTextAttributesKey(
        "KARATE_EXAMPLE_VARIABLE",
        DefaultLanguageHighlighterColors.HIGHLIGHTED_REFERENCE
    )
}