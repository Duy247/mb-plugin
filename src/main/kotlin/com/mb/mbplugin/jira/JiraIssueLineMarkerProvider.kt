package com.mb.mbplugin.jira

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.IconLoader
import com.intellij.psi.PsiElement
import com.mb.mbplugin.karate.psi.KaratePsiElement
import com.mb.mbplugin.karate.psi.KarateTokenTypes
import com.mb.mbplugin.settings.JiraSettings
import javax.swing.Icon

class JiraIssueLineMarkerProvider : LineMarkerProvider {
    
    companion object {
        private val JIRA_ISSUE_PATTERN = Regex("@(MBA-\\d+)")
        private val JIRA_ICON: Icon = IconLoader.getIcon("/icons/jira-icon.svg", JiraIssueLineMarkerProvider::class.java)
    }
    
    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        val text = element.text
        val matches = JIRA_ISSUE_PATTERN.findAll(text).toList()
        
        if (matches.isEmpty()) return null
        
        // Only show gutter icon for tag elements, not for Feature or Background keywords
        if (!isTagElement(element)) {
            return null
        }
        
        // For multiple matches, create marker for the first one but show all in tooltip
        val firstMatch = matches.first()
        val issueKey = firstMatch.groupValues[1]
        
        val project = element.project
        val settings = JiraSettings.getInstance(project)
        val baseUrl = settings.jiraBaseUrl.trimEnd('/')
        
        val tooltipText = if (baseUrl.isEmpty()) {
            if (matches.size == 1) {
                "Jira Issue $issueKey - Configure Jira base URL in Settings"
            } else {
                val allIssues = matches.map { it.groupValues[1] }.joinToString(", ")
                "Jira Issues: $allIssues - Configure Jira base URL in Settings"
            }
        } else {
            if (matches.size == 1) {
                "Jira Issue $issueKey - Click to open in browser"
            } else {
                val allIssues = matches.map { it.groupValues[1] }.joinToString(", ")
                "Jira Issues: $allIssues - Click to open menu"
            }
        }
        
        return LineMarkerInfo(
            element,
            element.textRange,
            JIRA_ICON,
            { tooltipText },
            { _, _ ->
                if (baseUrl.isNotEmpty()) {
                    if (matches.size == 1) {
                        // Single issue - open directly
                        val jiraUrl = "$baseUrl/browse/$issueKey"
                        val viewer = JiraIssueViewer.getInstance(project)
                        viewer.showJiraIssue(issueKey, jiraUrl)
                    } else {
                        // Multiple issues - show menu
                        showIssueSelectionMenu(project, matches, baseUrl, element)
                    }
                } else {
                    com.intellij.openapi.ui.Messages.showInfoMessage(
                        project,
                        "Please configure your Jira base URL in Settings > Tools > Jira Integration",
                        "Jira Base URL Not Configured"
                    )
                }
            },
            GutterIconRenderer.Alignment.CENTER,
            { 
                if (matches.size == 1) {
                    "Open Jira Issue $issueKey"
                } else {
                    val allIssues = matches.map { it.groupValues[1] }.joinToString(", ")
                    "Open Jira Issues: $allIssues"
                }
            }
        )
    }
    
    private fun isTagElement(element: PsiElement): Boolean {
        // Check if this element is a tag element
        if (element is KaratePsiElement && element.node.elementType == KarateTokenTypes.TAG) {
            return true
        }
        
        // Check if this element is part of a tag (child of tag element)
        var parent = element.parent
        while (parent != null) {
            if (parent is KaratePsiElement && parent.node.elementType == KarateTokenTypes.TAG) {
                return true
            }
            parent = parent.parent
        }
        
        // Explicitly exclude Feature and Background keywords even if they contain MBA tags
        if (element is KaratePsiElement) {
            when (element.node.elementType) {
                KarateTokenTypes.FEATURE_KEYWORD,
                KarateTokenTypes.BACKGROUND_KEYWORD -> return false
            }
        }
        
        // For other elements, check if it's a tag by looking at the text pattern and context
        // Allow if element text starts with @ or contains tag-like patterns
        val text = element.text.trim()
        return text.startsWith("@") || 
               (text.contains("@MBA-") && !text.lowercase().startsWith("feature") && !text.lowercase().startsWith("background"))
    }
    
    private fun showIssueSelectionMenu(
        project: Project, 
        matches: List<MatchResult>, 
        baseUrl: String, 
        element: PsiElement
    ) {
        val popup = com.intellij.openapi.ui.popup.JBPopupFactory.getInstance()
            .createPopupChooserBuilder(matches.map { it.groupValues[1] })
            .setTitle("Select Jira Issue to Open")
            .setItemChosenCallback { selectedIssue ->
                val jiraUrl = "$baseUrl/browse/$selectedIssue"
                val viewer = JiraIssueViewer.getInstance(project)
                viewer.showJiraIssue(selectedIssue, jiraUrl)
            }
            .createPopup()
        
        popup.showInBestPositionFor(com.intellij.openapi.editor.EditorFactory.getInstance().allEditors.first())
    }
}