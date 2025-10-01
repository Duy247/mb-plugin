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
    private val settings = JiraSettings.getInstance(project)
    
    override fun getDisplayName(): String = "Jira Integration"
    
    override fun createComponent(): JComponent {
        val panel: DialogPanel = panel {
            row("Jira Base URL:") {
                jiraBaseUrlField = textField()
                    .comment("Enter your Jira base URL (e.g., https://mycompanyjira.atlassian.net)")
                    .component
            }
        }
        
        reset()
        return panel
    }
    
    override fun isModified(): Boolean {
        return jiraBaseUrlField.text != settings.jiraBaseUrl
    }
    
    @Throws(ConfigurationException::class)
    override fun apply() {
        val newUrl = jiraBaseUrlField.text.trim()
        
        // Basic validation
        if (newUrl.isNotEmpty() && !newUrl.startsWith("http")) {
            throw ConfigurationException("Jira base URL must start with http:// or https://")
        }
        
        settings.jiraBaseUrl = newUrl
    }
    
    override fun reset() {
        jiraBaseUrlField.text = settings.jiraBaseUrl
    }
}