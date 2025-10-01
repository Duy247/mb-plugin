package com.mb.mbplugin.karate.psi

import com.intellij.psi.tree.IElementType
import com.mb.mbplugin.karate.KarateLanguage

class KarateElementType(debugName: String) : IElementType(debugName, KarateLanguage.INSTANCE)