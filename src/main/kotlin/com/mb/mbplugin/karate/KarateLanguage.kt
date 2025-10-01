package com.mb.mbplugin.karate

import com.intellij.lang.Language

class KarateLanguage : Language("Karate") {
    companion object {
        @JvmStatic
        val INSTANCE = KarateLanguage()
    }

    override fun getDisplayName(): String = "Karate"
}