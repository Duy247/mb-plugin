package com.mb.mbplugin.karate.test

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.execution.ProgramRunnerUtil
import com.intellij.execution.RunManager
import com.intellij.execution.RunnerAndConfigurationSettings
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationType
import com.intellij.execution.configurations.ConfigurationTypeUtil
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.execution.junit.JUnitConfiguration
import com.intellij.execution.junit.JUnitConfigurationType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.module.ModuleUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.util.IconLoader
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.PsiManager
import com.intellij.psi.util.elementType
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.plugins.cucumber.psi.GherkinTag
import org.jetbrains.plugins.cucumber.psi.GherkinTokenTypes
import org.jetbrains.plugins.cucumber.psi.GherkinScenario
import com.mb.mbplugin.settings.KarateTestRunnerSettings
import java.io.File
import java.util.concurrent.CompletableFuture
import javax.swing.Icon

/**
 * Provides run test gutter icons for scenario tags in Gherkin feature files.
 * This replaces the cucumber test run configuration but doesn't execute anything yet.
 */
class KarateTestRunLineMarkerProvider : LineMarkerProvider {
    
    companion object {
        private val TEST_RUN_ICON: Icon = IconLoader.getIcon("/icons/test-run.svg", KarateTestRunLineMarkerProvider::class.java)
    }
    
    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        // Only process TAG tokens that are children of GherkinTag
        if (element.elementType != GherkinTokenTypes.TAG || element.parent !is GherkinTag) {
            return null
        }
        
        val tagElement = element.parent as GherkinTag
        
        // For now, let's show the icon for ALL tags to test if it works
        val tagText = element.text
        if (tagText.isBlank() || !tagText.startsWith("@")) {
            return null
        }
        
        val tooltipText = "Create and prepare TestRunner.java with tag '$tagText'"
        
        return LineMarkerInfo(
            element,
            element.textRange,
            TEST_RUN_ICON,
            { tooltipText },
            { _, _ ->
                // Update TestRunner.java with tag and file path
                writeTestRunInfo(tagElement, tagText)
            },
            GutterIconRenderer.Alignment.CENTER,
            { "Click to create TestRunner.java for $tagText" }
        )
    }
    
    /**
     * Check if the given tag is followed by a scenario
     */
    private fun isTagFollowedByScenario(tagElement: GherkinTag): Boolean {
        // Find the next scenario element after this tag
        val scenario = findNextScenario(tagElement)
        return scenario != null
    }
    
    /**
     * Find the next scenario after the given tag element
     */
    private fun findNextScenario(tagElement: GherkinTag): GherkinScenario? {
        var current: PsiElement? = tagElement.nextSibling
        
        while (current != null) {
            // Skip whitespace and other tags
            if (current is GherkinScenario) {
                return current
            }
            // If we encounter another tag block or other major element, stop looking
            if (current is GherkinTag && current != tagElement) {
                break
            }
            current = current.nextSibling
        }
        
        // Try a different approach using tree traversal
        return PsiTreeUtil.getNextSiblingOfType(tagElement, GherkinScenario::class.java)
    }
    
    /**
     * Find the name of the scenario associated with this tag
     */
    private fun findAssociatedScenarioName(tagElement: GherkinTag): String? {
        val scenario = findNextScenario(tagElement)
        return scenario?.scenarioName
    }
    
    /**
     * Write test run information to TestRunner.java file and execute the test
     */
    private fun writeTestRunInfo(tagElement: GherkinTag, tagText: String) {
        // Collect all PSI-related data immediately while we're in the correct thread context
        val project = tagElement.project
        val psiFile = tagElement.containingFile
        val virtualFile = psiFile?.virtualFile
        
        if (virtualFile == null) {
            println("Error: Could not get virtual file")
            return
        }
        
        // Get the path from source root and project base path immediately
        val filePath = getPathFromSourceRoot(project, virtualFile)
        val projectBasePath = project.basePath
        
        // Get the configurable test runner file path from settings
        val settings = KarateTestRunnerSettings.getInstance(project)
        val configuredPath = settings.testRunnerFilePath
        
        // Now run the file writing in a background thread with all data collected
        ApplicationManager.getApplication().executeOnPooledThread {
            try {
                if (projectBasePath != null) {
                    val testRunnerFile = File(projectBasePath, configuredPath)
                    
                    // Create the directory structure if it doesn't exist
                    testRunnerFile.parentFile?.mkdirs()
                    
                    // Extract package name from the configured path
                    val packageName = extractPackageFromPath(configuredPath)
                    val className = testRunnerFile.nameWithoutExtension
                    
                    // TestRunner.java template content
                    val template = if (settings.testRunnerTemplate.isNotEmpty()) {
                        settings.testRunnerTemplate
                    } else {
                        // Default template if settings is empty
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
                    
                    // Replace template variables with actual values
                    val testRunnerContent = template
                        .replace("{{PACKAGE_NAME}}", packageName)
                        .replace("{{CLASS_NAME}}", className)
                        .replace("{{FILE_PATH}}", filePath)
                        .replace("{{TAG_TEXT}}", tagText)
                    
                    // Write the content to the file
                    testRunnerFile.writeText(testRunnerContent)
                    
                    println("${testRunnerFile.name} created/updated with tag: $tagText and path: $filePath")
                    println("File location: ${testRunnerFile.absolutePath}")
                    
                    // Wait 1 second then run the test
                    CompletableFuture.runAsync {
                        Thread.sleep(1000) // Wait 1 second
                        
                        // Execute the test on EDT
                        ApplicationManager.getApplication().invokeLater {
                            ApplicationManager.getApplication().runReadAction {
                                runJUnitTest(project, testRunnerFile)
                            }
                        }
                    }
                } else {
                    println("Error: Could not get project base path")
                }
                
            } catch (e: Exception) {
                println("Error creating TestRunner.java: ${e.message}")
            }
        }
    }
    
    /**
     * Extract package name from file path
     * e.g., "src/test/java/com/karate/runner/TestRunner.java" -> "com.karate.runner"
     */
    private fun extractPackageFromPath(filePath: String): String {
        val normalizedPath = filePath.replace('\\', '/')
        
        // Find the java directory
        val javaIndex = normalizedPath.lastIndexOf("/java/")
        if (javaIndex != -1) {
            val packagePath = normalizedPath.substring(javaIndex + 6) // Skip "/java/"
            val lastSlashIndex = packagePath.lastIndexOf('/')
            if (lastSlashIndex != -1) {
                return packagePath.substring(0, lastSlashIndex).replace('/', '.')
            }
        }
        
        // Fallback to default package
        return "com.karate.runner"
    }
    
    /**
     * Run JUnit test for the created TestRunner file
     */
    private fun runJUnitTest(project: Project, testRunnerFile: File) {
        try {
            // Refresh the virtual file system to ensure the new file is visible
            VirtualFileManager.getInstance().syncRefresh()
            
            // Find the virtual file for the created Java file
            val virtualFile = VirtualFileManager.getInstance().findFileByUrl("file://${testRunnerFile.absolutePath.replace('\\', '/')}")
            if (virtualFile == null) {
                println("Error: Could not find virtual file for ${testRunnerFile.absolutePath}")
                return
            }
            
            // Execute the test in the UI thread
            ApplicationManager.getApplication().invokeLater {
                try {
                    val runManager = RunManager.getInstance(project)
                    
                    // Get JUnit configuration type
                    val junitConfigType = JUnitConfigurationType.getInstance()
                    val junitFactory = junitConfigType.factory
                    
                    // Get the PSI file and find the test class
                    val psiFile = PsiManager.getInstance(project).findFile(virtualFile)
                    if (psiFile !is PsiJavaFile) {
                        println("Error: File is not a Java file")
                        openFileInEditor(project, virtualFile)
                        return@invokeLater
                    }
                    
                    val testClass = psiFile.classes.firstOrNull()
                    if (testClass == null) {
                        println("Error: No class found in the Java file")
                        openFileInEditor(project, virtualFile)
                        return@invokeLater
                    }
                    
                    // Create JUnit configuration
                    val junitConfig = JUnitConfiguration("Karate Test - ${testClass.name}", project, junitFactory)
                    
                    // Configure the JUnit test to run the specific class
                    val junitData = junitConfig.persistentData
                    junitData.TEST_OBJECT = JUnitConfiguration.TEST_CLASS
                    junitData.MAIN_CLASS_NAME = testClass.qualifiedName
                    
                    // Add JVM parameters to fix Java 17+ compatibility issues with Truffle/GraalVM
                    val currentVmParams = junitData.VM_PARAMETERS ?: ""
                    val compatibilityParams = listOf(
                        "--add-opens", "java.base/sun.nio.ch=ALL-UNNAMED",
                        "--add-opens", "java.base/sun.misc=ALL-UNNAMED", 
                        "--add-opens", "java.base/java.lang=ALL-UNNAMED",
                        "--add-opens", "java.base/java.lang.invoke=ALL-UNNAMED",
                        "--add-opens", "java.base/java.util=ALL-UNNAMED",
                        "--add-opens", "java.base/java.nio=ALL-UNNAMED",
                        "--add-opens", "java.base/sun.security.util=ALL-UNNAMED",
                        "--add-exports", "java.base/sun.misc=ALL-UNNAMED",
                        "--add-exports", "java.base/sun.nio.ch=ALL-UNNAMED",
                        "-Dpolyglot.js.nashorn-compat=false",
                        "-Dpolyglot.engine.AllowExperimentalOptions=true",
                        "-Dgraalvm.locatorDisabled=true"
                    ).joinToString(" ")
                    
                    junitData.VM_PARAMETERS = if (currentVmParams.isBlank()) {
                        "-ea $compatibilityParams"
                    } else {
                        "$currentVmParams $compatibilityParams"
                    }
                    
                    println("JVM Parameters set: ${junitData.VM_PARAMETERS}")
                    println("Note: If using JDK 17+, consider switching to JDK 11 or 16 for better Karate compatibility")
                    
                    // Set ShortenCommandLine to avoid "Command line is too long" errors
                    junitConfig.setShortenCommandLine(com.intellij.execution.ShortenCommandLine.ARGS_FILE)
                    
                    // Set the module (using read action to avoid threading violations)
                    val module = ReadAction.compute<com.intellij.openapi.module.Module?, Throwable> {
                        ModuleUtil.findModuleForFile(virtualFile, project)
                    }
                    if (module != null) {
                        junitConfig.setModule(module)
                    }
                    
                    // Create run configuration settings
                    val settings = runManager.createConfiguration(junitConfig, junitFactory)
                    settings.name = "Karate Test - ${testClass.name}"
                    
                    // Add and execute the configuration
                    runManager.addConfiguration(settings)
                    runManager.selectedConfiguration = settings
                    
                    // Execute the configuration
                    ProgramRunnerUtil.executeConfiguration(settings, DefaultRunExecutor.getRunExecutorInstance())
                    
                    println("Successfully launched JUnit test execution for: ${testClass.qualifiedName}")
                    
                } catch (e: Exception) {
                    println("Error creating or executing JUnit test configuration: ${e.message}")
                    e.printStackTrace()
                    
                    // Fallback: Open the file in the editor
                    openFileInEditor(project, virtualFile)
                }
            }
            
        } catch (e: Exception) {
            println("Error processing test file: ${e.message}")
            e.printStackTrace()
        }
    }
    
    /**
     * Open the test file in the editor as a fallback
     */
    private fun openFileInEditor(project: Project, virtualFile: VirtualFile) {
        try {
            val fileEditorManager = com.intellij.openapi.fileEditor.FileEditorManager.getInstance(project)
            fileEditorManager.openFile(virtualFile, true)
            println("Opened test file in editor: ${virtualFile.path}")
        } catch (e: Exception) {
            println("Error opening file in editor: ${e.message}")
        }
    }
    
    /**
     * Get the path of the file relative to source root
     */
    private fun getPathFromSourceRoot(project: Project, virtualFile: VirtualFile): String {
        val projectRootManager = ProjectRootManager.getInstance(project)
        val sourceRoots = projectRootManager.contentSourceRoots
        
        for (sourceRoot in sourceRoots) {
            if (virtualFile.path.startsWith(sourceRoot.path)) {
                return virtualFile.path.substring(sourceRoot.path.length + 1).replace('\\', '/')
            }
        }
        
        // If not under a source root, use relative path from project base
        val projectBasePath = project.basePath
        return if (projectBasePath != null && virtualFile.path.startsWith(projectBasePath)) {
            virtualFile.path.substring(projectBasePath.length + 1).replace('\\', '/')
        } else {
            virtualFile.name
        }
    }
}