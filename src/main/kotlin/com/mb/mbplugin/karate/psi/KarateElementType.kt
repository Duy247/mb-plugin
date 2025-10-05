package com.mb.mbplugin.karate.psi

import com.intellij.psi.tree.IElementType
import org.jetbrains.plugins.cucumber.psi.GherkinLanguage

class KarateElementType(debugName: String) : IElementType(debugName, GherkinLanguage.INSTANCE)