package com.mb.mbplugin.karate.psi

import org.jetbrains.plugins.cucumber.psi.GherkinFile
import org.jetbrains.plugins.cucumber.psi.GherkinFileType
import org.jetbrains.plugins.cucumber.psi.impl.GherkinFileImpl
import com.intellij.psi.PsiFile

/**
 * This class provides aliases for the Gherkin plugin's classes for backward compatibility
 * with our existing code that previously used our own KarateLanguage.
 */
typealias KarateFile = GherkinFile