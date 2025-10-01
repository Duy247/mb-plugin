package com.mb.mbplugin.karate.steps.reference

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceProvider
import com.intellij.util.ProcessingContext
import com.mb.mbplugin.karate.psi.KarateTokenTypes

class KarateStepReferenceProvider : PsiReferenceProvider() {
    override fun getReferencesByElement(
        element: PsiElement,
        context: ProcessingContext
    ): Array<PsiReference> {
        val elementType = element.node?.elementType
        
        // Create references for step keywords and variables
        return when (elementType) {
            KarateTokenTypes.STEP_KEYWORD,
            KarateTokenTypes.ACTION_KEYWORD,
            KarateTokenTypes.VARIABLE,
            KarateTokenTypes.DECLARATION -> {
                arrayOf(KarateStepReference(element, TextRange(0, element.textLength)))
            }
            else -> emptyArray()
        }
    }
}