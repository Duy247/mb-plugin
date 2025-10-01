package com.mb.mbplugin.jira

import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManagerListener

class JiraProjectListener : ProjectManagerListener {
    
    override fun projectClosing(project: Project) {
        JiraIssueViewer.disposeInstance(project)
    }
}