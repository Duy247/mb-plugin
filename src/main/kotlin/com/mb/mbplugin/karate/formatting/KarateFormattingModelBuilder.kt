package com.mb.mbplugin.karate.formatting

import com.intellij.formatting.*
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.mb.mbplugin.karate.KarateLanguage
import com.mb.mbplugin.karate.psi.KarateTokenTypes

/**
 * Main entry point for Karate code formatting.
 * This class builds the formatting model that defines how Karate feature files should be formatted.
 */
class KarateFormattingModelBuilder : FormattingModelBuilder {
    
    override fun createModel(formattingContext: FormattingContext): FormattingModel {
        val settings = formattingContext.codeStyleSettings
        val node = formattingContext.node
        val file = formattingContext.containingFile
        
        // Create spacing builder for Karate-specific spacing rules
        val spacingBuilder = createSpacingBuilder(settings)
        
        // Create the root formatting block
        val rootBlock = KarateBlock(
            node = node,
            wrap = null,
            alignment = null,
            indent = Indent.getNoneIndent(),
            spacingBuilder = spacingBuilder
        )
        
        return FormattingModelProvider.createFormattingModelForPsiFile(file, rootBlock, settings)
    }
    
    /**
     * Creates spacing rules for Karate language elements
     */
    private fun createSpacingBuilder(settings: CodeStyleSettings): SpacingBuilder {
        return SpacingBuilder(settings, KarateLanguage.INSTANCE)
            // Feature header spacing
            .after(KarateTokenTypes.FEATURE_KEYWORD).spaces(1)
            .after(KarateTokenTypes.COLON).spaces(KarateCodeStyleSettings.SPACES_AFTER_COLON)
            
            // Scenario spacing  
            .after(KarateTokenTypes.SCENARIO_KEYWORD).spaces(1)
            .after(KarateTokenTypes.SCENARIO_OUTLINE_KEYWORD).spaces(1)
            .after(KarateTokenTypes.EXAMPLE_KEYWORD).spaces(1)
            .after(KarateTokenTypes.BACKGROUND_KEYWORD).spaces(1)
            .after(KarateTokenTypes.RULE_KEYWORD).spaces(1)
            
            // Step keywords spacing
            .after(KarateTokenTypes.STEP_KEYWORD).spaces(1)
            .after(KarateTokenTypes.ACTION_KEYWORD).spaces(1)
            .after(KarateTokenTypes.ASTERISK).spaces(1)
            
            // Table formatting - spacing around pipes
            .before(KarateTokenTypes.PIPE).spaces(KarateCodeStyleSettings.SPACES_AROUND_PIPE)
            .after(KarateTokenTypes.PIPE).spaces(KarateCodeStyleSettings.SPACES_AROUND_PIPE)
            
            // Table cell formatting
            .around(KarateTokenTypes.TABLE_CELL).spaces(0) // No extra spaces around table cells
            
            // Keep table rows together
            .between(KarateTokenTypes.PIPE, KarateTokenTypes.PIPE).spaces(0)
            
            // Examples keyword spacing
            .after(KarateTokenTypes.EXAMPLES_KEYWORD).spaces(1)
            
            // Tag spacing - space after @ symbol if not at start of line
            .after(KarateTokenTypes.TAG).spaces(KarateCodeStyleSettings.SPACES_BETWEEN_TAGS)
            
            // String and variable spacing
            .around(KarateTokenTypes.OPERATOR).spaces(1)
            
            // Blank lines configuration
            .between(KarateTokenTypes.FEATURE_KEYWORD, KarateTokenTypes.SCENARIOS_KEYWORDS)
                .blankLines(KarateCodeStyleSettings.BLANK_LINES_AFTER_FEATURE)
            .between(KarateTokenTypes.SCENARIOS_KEYWORDS, KarateTokenTypes.SCENARIOS_KEYWORDS)
                .blankLines(KarateCodeStyleSettings.BLANK_LINES_BETWEEN_SCENARIOS)
            .between(KarateTokenTypes.BACKGROUND_KEYWORD, KarateTokenTypes.SCENARIOS_KEYWORDS)
                .blankLines(KarateCodeStyleSettings.BLANK_LINES_AFTER_BACKGROUND)
            .between(KarateTokenTypes.EXAMPLES_KEYWORD, KarateTokenTypes.SCENARIOS_KEYWORDS)
                .blankLines(KarateCodeStyleSettings.BLANK_LINES_AFTER_EXAMPLES)
                
            // Preserve content within doc strings and comments
            .withinPair(KarateTokenTypes.PYSTRING_QUOTES, KarateTokenTypes.PYSTRING_QUOTES).lineBreakInCode()
            .around(KarateTokenTypes.COMMENT).lineBreakInCode()
    }
}