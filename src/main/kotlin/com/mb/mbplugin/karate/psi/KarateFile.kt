package com.mb.mbplugin.karate.psi

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.FileViewProvider
import com.mb.mbplugin.karate.KarateLanguage

class KarateFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, KarateLanguage.INSTANCE) {
    override fun getFileType(): FileType = GherkinFileType.INSTANCE

    override fun toString(): String = "KarateFile:${name}"
}