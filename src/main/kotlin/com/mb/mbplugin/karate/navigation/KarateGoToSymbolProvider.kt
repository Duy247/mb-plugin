package com.mb.mbplugin.karate.navigation

import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.mb.mbplugin.karate.psi.KarateFile
import com.mb.mbplugin.karate.psi.KaratePsiElement
import com.mb.mbplugin.karate.psi.KarateTokenTypes
import java.util.regex.Pattern

class KarateGoToSymbolProvider : GotoDeclarationHandler {
    companion object {
        private val FILE_PATTERN = Pattern.compile("[^ :]+\\.[^ :]+")
    }

    override fun getGotoDeclarationTargets(
        sourceElement: PsiElement?,
        offset: Int,
        editor: Editor?
    ): Array<PsiElement>? {
        if (sourceElement == null) return null

        val containingFile = sourceElement.containingFile
        if (containingFile !is KarateFile) return null

        val elementText = sourceElement.text
        val results = mutableListOf<PsiElement>()

        // Handle file references (classpath: references)
        if (elementText.contains("classpath:")) {
            val filePaths = extractClasspathReferences(elementText)
            results.addAll(resolveClasspathFiles(sourceElement, filePaths))
        }

        // Handle variable references
        if (sourceElement is KaratePsiElement) {
            when (sourceElement.node.elementType) {
                KarateTokenTypes.VARIABLE,
                KarateTokenTypes.DECLARATION -> {
                    results.addAll(findVariableDefinitions(sourceElement, elementText))
                }
                KarateTokenTypes.STEP_KEYWORD,
                KarateTokenTypes.ACTION_KEYWORD -> {
                    results.addAll(findStepDefinitions(sourceElement, elementText))
                }
            }
        }

        // Handle quoted strings that might be file references
        val matcher = FILE_PATTERN.matcher(elementText)
        if (matcher.find()) {
            val fileName = matcher.group()
            val relativeFile = sourceElement.containingFile.virtualFile?.parent?.findFileByRelativePath(fileName)
            if (relativeFile != null) {
                val psiFile = sourceElement.manager.findFile(relativeFile)
                if (psiFile != null) {
                    results.add(psiFile)
                }
            }
        }

        return if (results.isNotEmpty()) results.toTypedArray() else null
    }

    override fun getActionText(context: DataContext): String? = null

    private fun extractClasspathReferences(text: String): List<String> {
        return text.split("\"", "'")
            .filter { it.startsWith("classpath:") }
            .flatMap { it.split("classpath:") }
            .flatMap { it.split("@") }
            .filter { it.isNotEmpty() }
    }

    private fun resolveClasspathFiles(sourceElement: PsiElement, filePaths: List<String>): List<PsiElement> {
        val results = mutableListOf<PsiElement>()
        val project = sourceElement.project
        val psiManager = sourceElement.manager

        // Try to find files in source roots
        try {
            val module = com.intellij.openapi.module.ModuleUtilCore.findModuleForPsiElement(sourceElement)
            if (module != null) {
                val sourceRoots = com.intellij.openapi.roots.ModuleRootManager.getInstance(module).sourceRoots
                
                for (filePath in filePaths) {
                    for (sourceRoot in sourceRoots) {
                        val file = sourceRoot.findFileByRelativePath(filePath)
                        if (file != null) {
                            val psiFile = psiManager.findFile(file)
                            if (psiFile != null) {
                                results.add(psiFile)
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // Ignore errors in file resolution
        }

        return results
    }

    private fun findVariableDefinitions(sourceElement: PsiElement, variableName: String): List<PsiElement> {
        val results = mutableListOf<PsiElement>()
        val containingFile = sourceElement.containingFile

        // Search for variable definitions in the same file
        val allElements = PsiTreeUtil.findChildrenOfType(containingFile, KaratePsiElement::class.java)
        
        for (element in allElements) {
            if (element.node.elementType == KarateTokenTypes.DECLARATION && 
                element.text.contains(variableName)) {
                results.add(element)
            }
        }

        return results
    }

    private fun findStepDefinitions(sourceElement: PsiElement, stepText: String): List<PsiElement> {
        val results = mutableListOf<PsiElement>()
        val containingFile = sourceElement.containingFile

        // Search for step definitions in the same file
        val allElements = PsiTreeUtil.findChildrenOfType(containingFile, KaratePsiElement::class.java)
        
        for (element in allElements) {
            if ((element.node.elementType == KarateTokenTypes.STEP_KEYWORD ||
                 element.node.elementType == KarateTokenTypes.ACTION_KEYWORD) && 
                element.text.contains(stepText.trim())) {
                results.add(element)
            }
        }

        return results
    }
}