package com.mb.mbplugin.karate.psi

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode

open class KaratePsiElement(node: ASTNode) : ASTWrapperPsiElement(node)