package com.mb.mbplugin.settings

import com.intellij.openapi.components.*
import com.intellij.openapi.project.Project

@Service(Service.Level.PROJECT)
@State(
    name = "JiraSettings",
    storages = [Storage("jiraSettings.xml")]
)
class JiraSettings : PersistentStateComponent<JiraSettings.State> {
    
    data class State(
        var jiraBaseUrl: String = ""
    )
    
    private var myState = State()
    
    var jiraBaseUrl: String
        get() = myState.jiraBaseUrl
        set(value) {
            myState.jiraBaseUrl = value
        }
    
    override fun getState(): State = myState
    
    override fun loadState(state: State) {
        myState = state
    }
    
    companion object {
        fun getInstance(project: Project): JiraSettings = 
            project.service<JiraSettings>()
    }
}