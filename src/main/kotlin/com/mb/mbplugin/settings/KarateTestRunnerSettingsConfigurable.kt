package com.mb.mbplugin.settings

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import javax.swing.JComponent
import javax.swing.JPanel

class KarateTestRunnerSettingsConfigurable(private val project: Project) : Configurable {
    
    private var testRunnerFilePathField: JBTextField? = null
    private var mainPanel: JPanel? = null
    
    override fun getDisplayName(): String = "Karate Test Runner"
    
    override fun createComponent(): JComponent? {
        testRunnerFilePathField = JBTextField()
        
        mainPanel = FormBuilder.createFormBuilder()
            .addLabeledComponent(
                JBLabel("TestRunner.java file path:"), 
                testRunnerFilePathField!!, 
                1, 
                false
            )
            .addComponentFillVertically(JPanel(), 0)
            .panel
        
        return mainPanel
    }
    
    override fun isModified(): Boolean {
        val settings = KarateTestRunnerSettings.getInstance(project)
        return testRunnerFilePathField?.text != settings.testRunnerFilePath
    }
    
    override fun apply() {
        val settings = KarateTestRunnerSettings.getInstance(project)
        settings.testRunnerFilePath = testRunnerFilePathField?.text ?: ""
    }
    
    override fun reset() {
        val settings = KarateTestRunnerSettings.getInstance(project)
        testRunnerFilePathField?.text = settings.testRunnerFilePath
    }
    
    override fun disposeUIResources() {
        testRunnerFilePathField = null
        mainPanel = null
    }
}