package com.mb.mbplugin.jira

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.IconLoader
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiComment
import com.intellij.psi.util.elementType
import org.jetbrains.plugins.cucumber.psi.GherkinElementTypes
import org.jetbrains.plugins.cucumber.psi.GherkinTag
import org.jetbrains.plugins.cucumber.psi.GherkinTokenTypes
import com.mb.mbplugin.settings.JiraSettings
import javax.swing.Icon

class JiraIssueLineMarkerProvider : LineMarkerProvider {
    
    companion object {
        // Icon will still be static
        private val JIRA_ICON: Icon = IconLoader.getIcon("/icons/jira-icon.svg", JiraIssueLineMarkerProvider::class.java)
    }
    
    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        // Process only leaf elements (TAG token) that are children of GherkinTag
        if (element.elementType != GherkinTokenTypes.TAG || element.parent !is GherkinTag) {
            return null
        }
        
        // Get the parent GherkinTag element for easier access
        val tagElement = element.parent as GherkinTag
        
        val project = tagElement.project
        val settings = JiraSettings.getInstance(project)
        
        // Get the project prefixes from settings
        val prefixes = settings.getProjectPrefixes()
        if (prefixes.isEmpty()) {
            return null
        }
        
        // Create a regex pattern that matches any of the configured project patterns
        val patternStr = prefixes.joinToString("|", prefix = "@((", postfix = ")\\d+)") { 
            Regex.escape(it)
        }
        
        val pattern = try {
            Regex(patternStr)
        } catch (e: Exception) {
            // If there's an error in the pattern, fall back to a basic pattern
            Regex("@([A-Z]+-\\d+)")
        }
        
        val text = tagElement.text
        val matches = pattern.findAll(text).toList()
        
        if (matches.isEmpty()) return null
        
        // For multiple matches, create marker for the first one but show all in tooltip
        val firstMatch = matches.first()
        val issueKey = firstMatch.groupValues[1]
        
        // We already have project and settings variables above, so use them
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
        
        // Use the leaf element (the TAG token) for the line marker, not its parent GherkinTag
        return LineMarkerInfo(
            element, // Use the leaf element directly
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
    
    // We've removed the isTagElement method and moved the check directly to getLineMarkerInfo
    
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