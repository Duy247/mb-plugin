package com.mb.mbplugin.karate.steps.reference

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceProvider
import com.intellij.util.ProcessingContext
import java.util.regex.Pattern

class KarateStepReferenceProvider : PsiReferenceProvider() {
    companion object {
        private val VARIABLE_PATTERN = Pattern.compile("([\\w.]+)")
    }

    override fun getReferencesByElement(
        element: PsiElement,
        context: ProcessingContext
    ): Array<PsiReference> {
        val references = mutableListOf<PsiReference>()
        
        // Create a general reference for the entire element
        val textRange = TextRange(0, element.textLength)
        references.add(KarateStepReference(element, textRange))
        
        // Look for variable patterns in the text
        val matcher = VARIABLE_PATTERN.matcher(element.text)
        while (matcher.find()) {
            val start = matcher.start()
            val end = matcher.end()
            val variableRange = TextRange(start, end)
            references.add(KarateStepReference(element, variableRange))
        }
        
        return references.toTypedArray()
    }
}