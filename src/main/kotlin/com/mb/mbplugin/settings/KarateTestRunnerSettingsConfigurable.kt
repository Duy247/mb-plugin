package com.mb.mbplugin.settings

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.FormBuilder
import java.awt.Dimension
import javax.swing.*

class KarateTestRunnerSettingsConfigurable(private val project: Project) : Configurable {
    
    private var testRunnerFilePathField: JBTextField? = null
    private var testRunnerTemplateArea: JTextArea? = null
    private var mainPanel: JPanel? = null
    
    override fun getDisplayName(): String = "Karate Test Runner"
    
    override fun createComponent(): JComponent? {
        testRunnerFilePathField = JBTextField()
        
        // Create template text area with syntax highlighting-like appearance
        testRunnerTemplateArea = JTextArea(25, 80).apply {
            font = java.awt.Font(java.awt.Font.MONOSPACED, java.awt.Font.PLAIN, 12)
            tabSize = 4
            lineWrap = false
            wrapStyleWord = false
        }
        
        val scrollPane = JBScrollPane(testRunnerTemplateArea).apply {
            preferredSize = Dimension(600, 400)
            verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
            horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
        }
        
        // Create help text for template variables
        val helpText = JBLabel("<html><b>Available template variables:</b><br>" +
            "{{PACKAGE_NAME}} - Java package name derived from file path<br>" +
            "{{CLASS_NAME}} - Java class name derived from file name<br>" +
            "{{FILE_PATH}} - Relative path to the feature file<br>" +
            "{{TAG_TEXT}} - The selected tag text<br><br>" +
            "<i>Tip: Use standard Java syntax with these placeholders</i></html>")
        
        mainPanel = FormBuilder.createFormBuilder()
            .addLabeledComponent(
                JBLabel("TestRunner.java file path:"), 
                testRunnerFilePathField!!, 
                1, 
                false
            )
            .addVerticalGap(10)
            .addLabeledComponent(
                JBLabel("TestRunner.java template:"),
                scrollPane,
                1,
                true
            )
            .addVerticalGap(5)
            .addComponent(helpText)
            .addComponentFillVertically(JPanel(), 0)
            .panel
        
        return mainPanel
    }
    
    override fun isModified(): Boolean {
        val settings = KarateTestRunnerSettings.getInstance(project)
        return testRunnerFilePathField?.text != settings.testRunnerFilePath ||
               testRunnerTemplateArea?.text != settings.testRunnerTemplate
    }
    
    override fun apply() {
        val settings = KarateTestRunnerSettings.getInstance(project)
        settings.testRunnerFilePath = testRunnerFilePathField?.text ?: ""
        settings.testRunnerTemplate = testRunnerTemplateArea?.text ?: ""
    }
    
    override fun reset() {
        val settings = KarateTestRunnerSettings.getInstance(project)
        testRunnerFilePathField?.text = settings.testRunnerFilePath
        testRunnerTemplateArea?.text = settings.testRunnerTemplate.ifEmpty { 
            // Default template if empty
            """
package {{PACKAGE_NAME}};

import com.intuit.karate.junit5.Karate;
import org.junit.jupiter.api.BeforeAll;
import com.karate.mock.MockServerRunner;

class {{CLASS_NAME}} {
    @Karate.Test
    Karate testMockApi() {
        return Karate.run("classpath:{{FILE_PATH}}")
                .tags("{{TAG_TEXT}}")
                .karateEnv("local")
                .relativeTo(getClass());
    }
}
""".trimIndent()
        }
    }
    
    override fun disposeUIResources() {
        testRunnerFilePathField = null
        testRunnerTemplateArea = null
        mainPanel = null
    }
}