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
        var testRunnerFilePath: String = "src/test/java/com/karate/runner/TestRunner.java"
    )
    
    private var myState = State()
    
    var testRunnerFilePath: String
        get() = myState.testRunnerFilePath
        set(value) {
            myState.testRunnerFilePath = value
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