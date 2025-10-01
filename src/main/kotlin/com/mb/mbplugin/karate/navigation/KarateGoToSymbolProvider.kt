package com.mb.mbplugin.karate.navigation

import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
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
        if (sourceElement == null) {
            return emptyArray()
        }

        val quotedSplit = sourceElement.text.split("[\"']".toRegex())
        val filePaths = quotedSplit
            .filter { it.startsWith("classpath:") }
            .flatMap { it.split("classpath:") }
            .flatMap { it.split("@") }
            .filter { it.isNotEmpty() }

        if (filePaths.isNotEmpty()) {
            return goToClasspath(sourceElement, filePaths)
        } else {
            val potentialFilePath = quotedSplit
                .map { FILE_PATTERN.matcher(it) }
                .filter { it.matches() }
                .map { it.group(0) }
                .firstOrNull()
                ?.let { 
                    sourceElement.containingFile.virtualFile?.parent?.findFileByRelativePath(it)
                }

            if (potentialFilePath != null) {
                val psiFile = PsiManager.getInstance(sourceElement.project).findFile(potentialFilePath)
                return if (psiFile != null) arrayOf(psiFile) else emptyArray()
            }
        }
        return emptyArray()
    }

    override fun getActionText(context: DataContext): String? = null

    private fun goToClasspath(sourceElement: PsiElement, filePaths: List<String>): Array<PsiElement> {
        val containingFile = sourceElement.containingFile ?: return emptyArray()
        val module = ModuleUtilCore.findModuleForFile(containingFile) ?: return emptyArray()
        val sourceRoots = ModuleRootManager.getInstance(module).sourceRoots
        val psiManager = PsiManager.getInstance(sourceElement.project)
        
        val list = sourceRoots
            .mapNotNull { it.findFileByRelativePath(filePaths[0]) }
            .mapNotNull { psiManager.findFile(it) }

        return when (filePaths.size) {
            1 -> list.toTypedArray()
            2 -> {
                list.mapNotNull { f ->
                    val textOffsetNewline = f.text.indexOf("@${filePaths[1]}\n")
                    val textOffset = f.text.indexOf("@${filePaths[1]}")
                    val textOffsetRet = if (textOffsetNewline == -1) textOffset else textOffsetNewline
                    f.findElementAt(textOffsetRet)
                }.toTypedArray()
            }
            else -> emptyArray()
        }
    }
}