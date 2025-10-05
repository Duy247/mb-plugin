package com.mb.mbplugin.jira

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.keymap.KeymapUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.IconLoader
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiComment
import com.intellij.psi.util.elementType
import org.jetbrains.plugins.cucumber.psi.GherkinElementTypes
import org.jetbrains.plugins.cucumber.psi.GherkinTag
import org.jetbrains.plugins.cucumber.psi.GherkinTokenTypes
import java.awt.event.InputEvent
import java.awt.event.KeyEvent
import com.mb.mbplugin.settings.JiraSettings
import javax.swing.Icon

class JiraIssueLineMarkerProvider : LineMarkerProvider {
    
    companion object {
        // Icon will still be static
        private val JIRA_ICON: Icon = IconLoader.getIcon("/icons/jira-icon.svg", JiraIssueLineMarkerProvider::class.java)
    }
    
    // Track which lines already have markers to avoid duplicates
    private val processedLines = mutableSetOf<Int>()
    
    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        // Process only leaf elements (TAG token) that are children of GherkinTag
        if (element.elementType != GherkinTokenTypes.TAG || element.parent !is GherkinTag) {
            return null
        }
        
        // Get the parent GherkinTag element for easier access
        val tagElement = element.parent as GherkinTag
        
        // Get line number from element to track processed lines
        val document = element.containingFile?.viewProvider?.document ?: return null
        val lineNumber = document.getLineNumber(element.textOffset)
        
        // Skip if we already processed this line
        if (lineNumber in processedLines) {
            return null
        }
        
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
        
        // Find all Jira issues in the line
        val lineText = document.getText(com.intellij.openapi.util.TextRange(
            document.getLineStartOffset(lineNumber),
            document.getLineEndOffset(lineNumber)
        ))
        
        val matches = pattern.findAll(lineText).toList()
        if (matches.isEmpty()) return null
        
        // Add this line to processed lines set
        processedLines.add(lineNumber)
        
        // For multiple matches, create marker for the first one but show all in tooltip
        val firstMatch = matches.first()
        val issueKey = firstMatch.groupValues[1]
        
        // We already have project and settings variables above, so use them
        val baseUrl = settings.jiraBaseUrl.trimEnd('/')
        
        // Create Click text
        val clickText = "Click"
        
        val tooltipText = if (baseUrl.isEmpty()) {
            if (matches.size == 1) {
                "Jira Issue $issueKey - Configure Jira base URL in Settings"
            } else {
                val allIssues = matches.map { it.groupValues[1] }.joinToString(", ")
                "Jira Issues: $allIssues - Configure Jira base URL in Settings"
            }
        } else {
            if (matches.size == 1) {
                "Jira Issue $issueKey - $clickText to open in browser"
            } else {
                val allIssues = matches.map { it.groupValues[1] }.joinToString(", ")
                "Jira Issues: $allIssues - $clickText to open menu"
            }
        }
        
        // Use the leaf element (the TAG token) for the line marker, not its parent GherkinTag
        return LineMarkerInfo(
            element, // Use the leaf element directly
            element.textRange,
            JIRA_ICON,
            { tooltipText },
            { mouseEvent, _ ->
                if (mouseEvent != null) {
                    val component = mouseEvent.component
                    val point = mouseEvent.point
                    val popup = com.intellij.openapi.ui.popup.JBPopupFactory.getInstance()
                        .createPopupChooserBuilder(matches.map { it.groupValues[1] })
                        .setTitle("Select Jira Issue to Open")
                        .setItemChosenCallback { selectedIssue ->
                            val jiraUrl = "$baseUrl/browse/$selectedIssue"
                            val viewer = JiraIssueViewer.getInstance(project)
                            viewer.showJiraIssue(selectedIssue, jiraUrl)
                        }
                        .createPopup()

                    // Convert local mouse point to screen coordinates
                    val screenPoint = java.awt.Point(point)
                    javax.swing.SwingUtilities.convertPointToScreen(screenPoint, component)

                    // Slightly adjust Y to place it just below the icon
                    screenPoint.y += 10

                    popup.showInScreenCoordinates(component, screenPoint)
                }
            },
            GutterIconRenderer.Alignment.CENTER,
            { 
                if (matches.size == 1) {
                    "$clickText to open Jira issue $issueKey"
                } else {
                    val allIssues = matches.map { it.groupValues[1] }.joinToString(", ")
                    "$clickText to open Jira issues: $allIssues"
                }
            }
        )
    }
    
    // Override the collectSlowLineMarkers to clear the processedLines set before each run
    override fun collectSlowLineMarkers(
        elements: List<PsiElement>, 
        result: MutableCollection<in LineMarkerInfo<*>>
    ) {
        // Clear the processed lines set before each run
        processedLines.clear()
        super.collectSlowLineMarkers(elements, result)
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
        
    }
}