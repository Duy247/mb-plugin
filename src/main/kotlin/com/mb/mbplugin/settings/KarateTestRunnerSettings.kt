package com.mb.mbplugin.settings

import com.intellij.openapi.components.*
import com.intellij.openapi.project.Project

@Service(Service.Level.PROJECT)
@State(
    name = "KarateTestRunnerSettings",
    storages = [Storage("karateTestRunnerSettings.xml")]
)
class KarateTestRunnerSettings : PersistentStateComponent<KarateTestRunnerSettings.State> {
    
    data class State(
        var testRunnerFilePath: String = "src/test/java/com/karate/runner/TestRunner.java",
        var testRunnerTemplate: String = """
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
    )
    
    private var myState = State()
    
    var testRunnerFilePath: String
        get() = myState.testRunnerFilePath
        set(value) {
            myState.testRunnerFilePath = value
        }
    
    var testRunnerTemplate: String
        get() = myState.testRunnerTemplate
        set(value) {
            myState.testRunnerTemplate = value
        }
    
    override fun getState(): State = myState
    
    override fun loadState(state: State) {
        myState = state
    }
    
    companion object {
        fun getInstance(project: Project): KarateTestRunnerSettings {
            return project.service<KarateTestRunnerSettings>()
        }
    }
}