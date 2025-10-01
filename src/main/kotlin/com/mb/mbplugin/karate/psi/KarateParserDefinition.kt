package com.mb.mbplugin.karate.psi

import com.intellij.lang.ASTNode
import com.intellij.lang.ParserDefinition
import com.intellij.lang.PsiParser
import com.intellij.lexer.Lexer
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.tree.TokenSet
import com.mb.mbplugin.karate.KarateLanguage
import com.mb.mbplugin.karate.lexer.KarateLexer
import com.mb.mbplugin.karate.parser.KarateParser

class KarateParserDefinition : ParserDefinition {
    companion object {
        val FILE = IFileElementType(KarateLanguage.INSTANCE)
    }

    override fun createLexer(project: Project?): Lexer = KarateLexer()

    override fun createParser(project: Project?): PsiParser = KarateParser()

    override fun getFileNodeType(): IFileElementType = FILE

    override fun getCommentTokens(): TokenSet = KarateTokenTypes.COMMENTS

    override fun getStringLiteralElements(): TokenSet = KarateTokenTypes.QUOTED_STRING

    override fun createElement(node: ASTNode): PsiElement = KaratePsiElement(node)

    override fun createFile(viewProvider: FileViewProvider): PsiFile = KarateFile(viewProvider)
}