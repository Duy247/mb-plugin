package com.mb.mbplugin.jira

import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.codeInsight.intention.PriorityAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.util.IncorrectOperationException
import com.mb.mbplugin.settings.JiraSettings

class OpenMultipleJiraIssuesIntention : IntentionAction, PriorityAction {
    
    companion object {
        private val JIRA_ISSUE_PATTERN = Regex("@(MBA-\\d+)")
    }
    
    private var issueKeys: List<String> = emptyList()
    
    override fun getText(): String {
        return if (issueKeys.size <= 3) {
            "🔗 Open Jira Issues: ${issueKeys.joinToString(", ")}"
        } else {
            "🔗 Open Jira Issues (${issueKeys.size} issues)"
        }
    }
    
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
        
        if (matches.size > 1) {
            issueKeys = matches.map { it.groupValues[1] }
            return true
        }
        
        return false
    }
    
    @Throws(IncorrectOperationException::class)
    override fun invoke(project: Project, editor: Editor?, file: PsiFile?) {
        if (issueKeys.isEmpty()) return
        
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
        
        // Show popup menu to select which issue to open
        val popup = com.intellij.openapi.ui.popup.JBPopupFactory.getInstance()
            .createPopupChooserBuilder(issueKeys)
            .setTitle("Select Jira Issue to Open")
            .setItemChosenCallback { selectedIssue ->
                val jiraUrl = "$baseUrl/browse/$selectedIssue"
                val viewer = JiraIssueViewer.getInstance(project)
                viewer.showJiraIssue(selectedIssue, jiraUrl)
            }
            .createPopup()
        
        // Show popup at cursor location
        if (editor != null) {
            popup.showInBestPositionFor(editor)
        }
    }
    
    override fun startInWriteAction(): Boolean = false
    
    override fun getPriority(): PriorityAction.Priority = PriorityAction.Priority.HIGH
}