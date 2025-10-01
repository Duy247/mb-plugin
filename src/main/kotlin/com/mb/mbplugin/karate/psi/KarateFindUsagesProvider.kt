package com.mb.mbplugin.karate.psi

import com.intellij.lang.cacheBuilder.DefaultWordsScanner
import com.intellij.lang.cacheBuilder.WordsScanner
import com.intellij.lang.findUsages.FindUsagesProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.tree.TokenSet
import com.mb.mbplugin.karate.lexer.KarateLexer

class KarateFindUsagesProvider : FindUsagesProvider {
    override fun getWordsScanner(): WordsScanner {
        return DefaultWordsScanner(
            KarateLexer(),
            KarateTokenTypes.KEYWORDS,
            KarateTokenTypes.COMMENTS,
            TokenSet.EMPTY
        )
    }

    override fun canFindUsagesFor(psiElement: PsiElement): Boolean {
        return psiElement is PsiNamedElement
    }

    override fun getHelpId(psiElement: PsiElement): String {
        return "reference.dialogs.findUsages.other"
    }

    override fun getType(element: PsiElement): String {
        return when {
            element is KaratePsiElement && element.node.elementType == KarateTokenTypes.STEP_KEYWORD -> "Karate Step"
            element is KaratePsiElement && element.node.elementType == KarateTokenTypes.VARIABLE -> "Karate Variable"
            element is KaratePsiElement && element.node.elementType == KarateTokenTypes.DECLARATION -> "Karate Declaration"
            else -> "Karate Element"
        }
    }

    override fun getDescriptiveName(element: PsiElement): String {
        return when (element) {
            is PsiNamedElement -> element.name ?: "Unknown"
            else -> element.text ?: "Unknown"
        }
    }

    override fun getNodeText(element: PsiElement, useFullName: Boolean): String {
        return getDescriptiveName(element)
    }
}