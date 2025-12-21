package com.way.samurai.koinstudy.presentation.scoped

class TaskWizardTracker {
    private val steps = mutableListOf<String>()

    fun markStep(step: String) {
        steps += step
    }

    fun history(): List<String> = steps.toList()

    companion object {
        const val SCOPE_NAME = "taskWizardScope"
    }
}
