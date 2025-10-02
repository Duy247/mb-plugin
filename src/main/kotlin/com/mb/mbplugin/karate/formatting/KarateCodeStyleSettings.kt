package com.mb.mbplugin.karate.formatting

/**
 * Simple constants for Karate code formatting.
 * No complex settings UI - just sensible defaults.
 */
object KarateCodeStyleSettings {
    
    // ============= INDENTATION SETTINGS =============
    const val FEATURE_DESCRIPTION_INDENT = 2
    const val SCENARIO_INDENT = 2
    const val STEP_INDENT = 4
    const val TABLE_CELL_INDENT = 6
    const val EXAMPLES_INDENT = 4
    const val DOC_STRING_CONTENT_INDENT = 6
    
    // ============= SPACING SETTINGS =============
    const val SPACES_AROUND_PIPE = 1
    const val SPACES_BEFORE_COLON = 0
    const val SPACES_AFTER_COLON = 1
    const val SPACES_BETWEEN_TAGS = 1
    
    // ============= BLANK LINE SETTINGS =============
    const val BLANK_LINES_AFTER_FEATURE = 1
    const val BLANK_LINES_BETWEEN_SCENARIOS = 1
    const val BLANK_LINES_AFTER_BACKGROUND = 1
    const val BLANK_LINES_AFTER_EXAMPLES = 1
    
    // ============= ALIGNMENT SETTINGS =============
    const val ALIGN_TABLE_PIPES = true
    const val ALIGN_SCENARIO_PARAMETERS = true
    
    // ============= TABLE SETTINGS =============
    val TABLE_COLUMN_ALIGNMENT = TableColumnAlignment.LEFT
    const val WRAP_TABLE_CELLS = false
    
    enum class TableColumnAlignment {
        LEFT, RIGHT, CENTER
    }
}