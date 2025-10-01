package com.mb.mbplugin.jira

import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.codeInsight.intention.PriorityAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.util.IncorrectOperationException
import com.mb.mbplugin.settings.JiraSettings

class OpenJiraIssueIntention : IntentionAction, PriorityAction {
    
    companion object {
        private val JIRA_ISSUE_PATTERN = Regex("@(MBA-\\d+)")
    }
    
    private var issueKey: String = ""
    
    override fun getText(): String = "Open Jira Issue $issueKey"
    
    override fun getFamilyName(): String = "Jira Integration"
    
    override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?): Boolean {
        if (editor == null || file == null) return false
        
        val offset = editor.caretModel.offset
        val document = editor.document
        val lineNumber = document.getLineNumber(offset)
        val lineStartOffset = document.getLineStartOffset(lineNumber)
        val lineEndOffset = document.getLineEndOffset(lineNumber)
        val lineText = document.getText(com.intellij.openapi.util.TextRange(lineStartOffset, lineEndOffset))
        
        // Find all Jira issues on the line
        val matches = JIRA_ISSUE_PATTERN.findAll(lineText).toList()
        
        // Check if cursor is within any of the tag ranges
        for (match in matches) {
            val tagStartInLine = match.range.first
            val tagEndInLine = match.range.last + 1
            val tagStartOffset = lineStartOffset + tagStartInLine
            val tagEndOffset = lineStartOffset + tagEndInLine
            
            if (offset >= tagStartOffset && offset <= tagEndOffset) {
                issueKey = match.groupValues[1]
                return true
            }
        }
        
        return false
    }
    
    @Throws(IncorrectOperationException::class)
    override fun invoke(project: Project, editor: Editor?, file: PsiFile?) {
        if (issueKey.isEmpty()) return
        
        val settings = JiraSettings.getInstance(project)
        val baseUrl = settings.jiraBaseUrl.trimEnd('/')
        
        if (baseUrl.isEmpty()) {
            com.intellij.openapi.ui.Messages.showInfoMessage(
                project,
                "Please configure your Jira base URL in Settings > Tools > Jira Integration",
                "Jira Base URL Not Configured"
            )
            return
        }
        
        val jiraUrl = "$baseUrl/browse/$issueKey"
        val viewer = JiraIssueViewer.getInstance(project)
        viewer.showJiraIssue(issueKey, jiraUrl)
    }
    
    override fun startInWriteAction(): Boolean = false
    
    override fun getPriority(): PriorityAction.Priority = PriorityAction.Priority.HIGH
}