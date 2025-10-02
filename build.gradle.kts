plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.1.0"
    id("org.jetbrains.intellij.platform") version "2.7.1"
}

group = "com.mb"
version = "1.0.3"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

// Configure IntelliJ Platform Gradle Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin.html
dependencies {
    intellijPlatform {
        create("IC", "2025.1.4.1")
        testFramework(org.jetbrains.intellij.platform.gradle.TestFrameworkType.Platform)

        // Add necessary plugin dependencies for compilation
        bundledPlugin("com.intellij.java")
        bundledPlugin("com.intellij.modules.json")
        bundledPlugin("org.intellij.intelliLang")
    }
}

intellijPlatform {
    pluginConfiguration {
        ideaVersion {
            sinceBuild = "251"
        }

        changeNotes = """
            Version 1.0.3
            - Added Jira Integration: Hover on @MBA-XXXXX tags and use Alt+Enter to open Jira issues in default browser
            - Configurable Jira base URL in Settings > Tools > Jira Integration
            - Gutter icons for quick access to Jira issues
            - Previous features: Karate language support, syntax highlighting, code completion
        """.trimIndent()
    }
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "21"
        targetCompatibility = "21"
    }
    
    // Skip the buildSearchableOptions task that's causing issues
    buildSearchableOptions {
        enabled = false
    }
    prepareJarSearchableOptions {
        enabled = false
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
    }
}
