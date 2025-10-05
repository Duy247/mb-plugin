package com.mb.mbplugin.karate.psi

import com.intellij.openapi.fileTypes.LanguageFileType
import com.intellij.openapi.util.IconLoader
import org.jetbrains.plugins.cucumber.psi.GherkinLanguage
import javax.swing.Icon

class GherkinFileType : LanguageFileType(GherkinLanguage.INSTANCE) {
    companion object {
        @JvmStatic
        val INSTANCE = GherkinFileType()
    }

    override fun getName(): String = "Karate"

    override fun getDescription(): String = "Karate feature files"

    override fun getDefaultExtension(): String = "feature"

    override fun getIcon(): Icon? = IconLoader.getIcon("/icons/karate-file.svg", GherkinFileType::class.java)
}