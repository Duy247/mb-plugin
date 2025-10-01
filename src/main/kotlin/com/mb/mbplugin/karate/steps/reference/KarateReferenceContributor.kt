package com.mb.mbplugin.karate.steps.reference

import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiReferenceContributor
import com.intellij.psi.PsiReferenceRegistrar
import com.mb.mbplugin.karate.psi.KaratePsiElement

class KarateReferenceContributor : PsiReferenceContributor() {
    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement(KaratePsiElement::class.java),
            KarateStepReferenceProvider()
        )
    }
}