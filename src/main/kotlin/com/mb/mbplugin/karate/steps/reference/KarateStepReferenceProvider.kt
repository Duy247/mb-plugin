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
        private val CALL_READ_PATTERN = Pattern.compile(
            "call\\s+read\\s*\\(\\s*['\"]classpath:([^'\"]+\\.feature)(@\\w+)?['\"]",
            Pattern.CASE_INSENSITIVE
        )
    }

    override fun getReferencesByElement(
        element: PsiElement,
        context: ProcessingContext
    ): Array<PsiReference> {
        val references = mutableListOf<PsiReference>()
        val elementText = element.text
        
        // Check for call read patterns first (higher priority)
        val callReadMatcher = CALL_READ_PATTERN.matcher(elementText)
        if (callReadMatcher.find()) {
            val start = callReadMatcher.start()
            val end = callReadMatcher.end()
            val callReadRange = TextRange(start, end)
            references.add(KarateFeatureCallReference(element, callReadRange))
        } else {
            // Create a general reference for the entire element
            val textRange = TextRange(0, element.textLength)
            references.add(KarateStepReference(element, textRange))
        }
        
        // Look for variable patterns in the text (only if not a call read)
        if (!callReadMatcher.find()) {
            callReadMatcher.reset()
            val matcher = VARIABLE_PATTERN.matcher(elementText)
            while (matcher.find()) {
                val start = matcher.start()
                val end = matcher.end()
                val variableRange = TextRange(start, end)
                references.add(KarateStepReference(element, variableRange))
            }
        }
        
        return references.toTypedArray()
    }
}