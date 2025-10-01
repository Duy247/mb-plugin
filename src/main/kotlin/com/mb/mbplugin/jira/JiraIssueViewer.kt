package com.mb.mbplugin.jira

import com.intellij.ide.BrowserUtil
import com.intellij.openapi.project.Project

class JiraIssueViewer(private val project: Project) {
    
    fun showJiraIssue(issueKey: String, jiraUrl: String) {
        try {
            // Open URL in default browser
            BrowserUtil.browse(jiraUrl)
        } catch (e: Exception) {
            showErrorMessage("Failed to open Jira issue in browser: ${e.message}")
        }
    }
    
    private fun showErrorMessage(message: String) {
        com.intellij.openapi.ui.Messages.showErrorDialog(
            project,
            message,
            "Jira Issue Viewer Error"
        )
    }
    
    fun dispose() {
        // Nothing to dispose when using external browser
    }
    
    companion object {
        private val instances = mutableMapOf<Project, JiraIssueViewer>()
        
        fun getInstance(project: Project): JiraIssueViewer {
            return instances.getOrPut(project) { JiraIssueViewer(project) }
        }
        
        fun disposeInstance(project: Project) {
            instances.remove(project)?.dispose()
        }
    }
}