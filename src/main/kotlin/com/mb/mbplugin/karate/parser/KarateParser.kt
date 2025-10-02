package com.mb.mbplugin.karate.parser

import com.intellij.lang.ASTNode
import com.intellij.lang.PsiBuilder
import com.intellij.lang.PsiParser
import com.intellij.psi.tree.IElementType
import com.mb.mbplugin.karate.psi.KarateTokenTypes

class KarateParser : PsiParser {
    override fun parse(root: IElementType, builder: PsiBuilder): ASTNode {
        val marker = builder.mark()
        
        while (!builder.eof()) {
            when (builder.tokenType) {
                KarateTokenTypes.COMMENT -> builder.advanceLexer()
                KarateTokenTypes.TAG -> builder.advanceLexer()
                KarateTokenTypes.FEATURE_KEYWORD -> parseFeature(builder)
                KarateTokenTypes.SCENARIO_KEYWORD,
                KarateTokenTypes.SCENARIO_OUTLINE_KEYWORD -> parseScenario(builder)
                KarateTokenTypes.BACKGROUND_KEYWORD -> parseBackground(builder)
                KarateTokenTypes.STEP_KEYWORD,
                KarateTokenTypes.ACTION_KEYWORD,
                KarateTokenTypes.ASTERISK -> parseStep(builder)
                else -> builder.advanceLexer()
            }
        }
        
        marker.done(root)
        return builder.treeBuilt
    }

    private fun parseFeature(builder: PsiBuilder) {
        val marker = builder.mark()
        builder.advanceLexer() // consume FEATURE_KEYWORD
        
        // Parse feature title and description
        while (!builder.eof() && 
               builder.tokenType != KarateTokenTypes.SCENARIO_KEYWORD &&
               builder.tokenType != KarateTokenTypes.BACKGROUND_KEYWORD) {
            builder.advanceLexer()
        }
        
        marker.done(KarateTokenTypes.FEATURE_KEYWORD)
    }

    private fun parseScenario(builder: PsiBuilder) {
        val marker = builder.mark()
        builder.advanceLexer() // consume SCENARIO_KEYWORD
        
        // Parse scenario content
        while (!builder.eof() && 
               builder.tokenType != KarateTokenTypes.SCENARIO_KEYWORD &&
               builder.tokenType != KarateTokenTypes.FEATURE_KEYWORD) {
            if (builder.tokenType == KarateTokenTypes.STEP_KEYWORD ||
                builder.tokenType == KarateTokenTypes.ACTION_KEYWORD ||
                builder.tokenType == KarateTokenTypes.ASTERISK) {
                parseStep(builder)
            } else {
                builder.advanceLexer()
            }
        }
        
        marker.done(KarateTokenTypes.SCENARIO_KEYWORD)
    }

    private fun parseBackground(builder: PsiBuilder) {
        val marker = builder.mark()
        builder.advanceLexer() // consume BACKGROUND_KEYWORD
        
        // Parse background content
        while (!builder.eof() && 
               builder.tokenType != KarateTokenTypes.SCENARIO_KEYWORD &&
               builder.tokenType != KarateTokenTypes.FEATURE_KEYWORD) {
            if (builder.tokenType == KarateTokenTypes.STEP_KEYWORD ||
                builder.tokenType == KarateTokenTypes.ACTION_KEYWORD ||
                builder.tokenType == KarateTokenTypes.ASTERISK) {
                parseStep(builder)
            } else {
                builder.advanceLexer()
            }
        }
        
        marker.done(KarateTokenTypes.BACKGROUND_KEYWORD)
    }

    private fun parseStep(builder: PsiBuilder) {
        val marker = builder.mark()
        builder.advanceLexer() // consume STEP_KEYWORD, ACTION_KEYWORD, or ASTERISK
        
        // Parse step content until end of line
        while (!builder.eof() && builder.tokenText != "\n") {
            builder.advanceLexer()
        }
        
        marker.done(KarateTokenTypes.STEP_KEYWORD)
    }
}