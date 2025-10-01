package com.mb.mbplugin.karate.psi

import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.options.colors.AttributesDescriptor
import com.intellij.openapi.options.colors.ColorDescriptor
import com.intellij.openapi.options.colors.ColorSettingsPage
import com.intellij.openapi.util.IconLoader
import javax.swing.Icon

class GherkinColorsPage : ColorSettingsPage {
    companion object {
        private val DESCRIPTORS = arrayOf(
            AttributesDescriptor("Comment", GherkinHighlighter.COMMENT),
            AttributesDescriptor("Keyword", GherkinHighlighter.KEYWORD),
            AttributesDescriptor("Step Keyword", GherkinHighlighter.STEP_KEYWORD),
            AttributesDescriptor("String", GherkinHighlighter.QUOTE),
            AttributesDescriptor("Tag", GherkinHighlighter.TAG),
            AttributesDescriptor("Text", GherkinHighlighter.TEXT),
            AttributesDescriptor("Table Pipe", GherkinHighlighter.PIPE),
            AttributesDescriptor("Variable", GherkinHighlighter.KARATE_VARIABLE),
            AttributesDescriptor("Reference", GherkinHighlighter.KARATE_REFERENCE)
        )
    }

    override fun getIcon(): Icon? = IconLoader.getIcon("/META-INF/pluginIcon.svg", GherkinColorsPage::class.java)

    override fun getHighlighter(): SyntaxHighlighter = KarateSyntaxHighlighter()

    override fun getDemoText(): String = """
        # This is a comment
        @tag1 @tag2
        Feature: Sample Karate Feature
          This is a feature description
          
          Background:
            * def baseUrl = 'https://api.example.com'
            * def token = 'Bearer xyz123'
            
          Scenario: Get user information
            Given url baseUrl + '/users/1'
            And header Authorization = token
            When method get
            Then status 200
            And match response.name == 'John Doe'
            
          Scenario Outline: Validate multiple users
            Given url baseUrl + '/users/<id>'
            When method get
            Then status <expectedStatus>
            
            Examples:
              | id | expectedStatus |
              | 1  | 200           |
              | 2  | 200           |
              | 99 | 404           |
    """.trimIndent()

    override fun getDisplayName(): String = "Karate"

    override fun getAttributeDescriptors(): Array<AttributesDescriptor> = DESCRIPTORS

    override fun getColorDescriptors(): Array<ColorDescriptor> = ColorDescriptor.EMPTY_ARRAY

    override fun getAdditionalHighlightingTagToDescriptorMap(): Map<String, TextAttributesKey>? = null
}