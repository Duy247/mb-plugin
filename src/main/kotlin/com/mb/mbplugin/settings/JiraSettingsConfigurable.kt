package com.mb.mbplugin.settings

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.panel
import javax.swing.JComponent

class JiraSettingsConfigurable(private val project: Project) : Configurable {
    
    private lateinit var jiraBaseUrlField: JBTextField
    private lateinit var jiraPatternsField: JBTextField
    private val settings = JiraSettings.getInstance(project)
    
    override fun getDisplayName(): String = "Jira Integration"
    
    override fun createComponent(): JComponent {
        val panel: DialogPanel = panel {
            row("Jira Base URL:") {
                jiraBaseUrlField = textField()
                    .comment("Enter your Jira base URL (e.g., https://mycompanyjira.atlassian.net)")
                    .component
            }
            
            row("Jira Project Patterns:") {
                jiraPatternsField = textField()
                    .comment("Enter comma-separated patterns (e.g., MBA-*, MATS-*)")
                    .component
            }
        }
        
        reset()
        return panel
    }
    
    override fun isModified(): Boolean {
        return jiraBaseUrlField.text != settings.jiraBaseUrl ||
               jiraPatternsField.text != settings.jiraProjectPatterns
    }
    
    @Throws(ConfigurationException::class)
    override fun apply() {
        val newUrl = jiraBaseUrlField.text.trim()
        val newPatterns = jiraPatternsField.text.trim()
        
        // Basic validation
        if (newUrl.isNotEmpty() && !newUrl.startsWith("http")) {
            throw ConfigurationException("Jira base URL must start with http:// or https://")
        }
        
        // Validate patterns format
        if (newPatterns.isNotEmpty()) {
            val patterns = newPatterns.split(",")
            for (pattern in patterns) {
                val trimmed = pattern.trim()
                if (trimmed.isEmpty()) continue
                if (!trimmed.matches(Regex("[A-Z]+-\\*"))) {
                    throw ConfigurationException("Invalid pattern format: $trimmed. Expected format: PROJECT-* (e.g., MBA-*)")
                }
            }
        }
        
        settings.jiraBaseUrl = newUrl
        settings.jiraProjectPatterns = newPatterns
    }
    
    override fun reset() {
        jiraBaseUrlField.text = settings.jiraBaseUrl
        jiraPatternsField.text = settings.jiraProjectPatterns
    }
}