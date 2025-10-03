package com.mb.mbplugin.karate

import com.intellij.lang.Language
import org.jetbrains.plugins.cucumber.psi.GherkinLanguage

/**
 * This class provides backward compatibility with existing code
 * that relied on KarateLanguage.INSTANCE
 */
object KarateLanguage {
    @JvmStatic
    val INSTANCE = GherkinLanguage.INSTANCE
}