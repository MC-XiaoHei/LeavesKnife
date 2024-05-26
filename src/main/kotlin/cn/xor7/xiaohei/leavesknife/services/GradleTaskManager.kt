package cn.xor7.xiaohei.leavesknife.services

import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskId
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskNotificationListener
import org.jetbrains.plugins.gradle.service.task.GradleTaskManagerExtension
import org.jetbrains.plugins.gradle.settings.GradleExecutionSettings

class GradleTaskManager : GradleTaskManagerExtension {
    override fun executeTasks(
        id: ExternalSystemTaskId,
        taskNames: MutableList<String>,
        projectPath: String,
        settings: GradleExecutionSettings?,
        jvmParametersSetup: String?,
        listener: ExternalSystemTaskNotificationListener,
    ): Boolean {
        println("taskname: $taskNames, projectPath: $projectPath, settings: $settings, jvmParametersSetup: $jvmParametersSetup, listener: $listener")
        return super.executeTasks(id, taskNames, projectPath, settings, jvmParametersSetup, listener)
    }

    override fun cancelTask(id: ExternalSystemTaskId, listener: ExternalSystemTaskNotificationListener): Boolean {
        return false
    }
}