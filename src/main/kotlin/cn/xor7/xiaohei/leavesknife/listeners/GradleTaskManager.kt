package cn.xor7.xiaohei.leavesknife.listeners

import cn.xor7.xiaohei.leavesknife.services.PatchType
import cn.xor7.xiaohei.leavesknife.services.leavesknifeStoreService
import cn.xor7.xiaohei.leavesknife.utils.createModuleApplyPatchesWatcher
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskId
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskNotificationListener
import org.jetbrains.plugins.gradle.service.task.GradleTaskManagerExtension
import org.jetbrains.plugins.gradle.settings.GradleExecutionSettings
import kotlin.concurrent.thread

class GradleTaskManager : GradleTaskManagerExtension {
    private val applyPatchesWatcherThreadSet = mutableSetOf<Thread>()

    override fun executeTasks(
        id: ExternalSystemTaskId,
        taskNames: MutableList<String>,
        projectPath: String,
        settings: GradleExecutionSettings?,
        jvmParametersSetup: String?,
        listener: ExternalSystemTaskNotificationListener,
    ): Boolean {
        taskNames
            .filter { name -> name.startsWith("apply") && name.endsWith("Patches") }
            .forEach { name ->
                val store = id.findProject()?.leavesknifeStoreService ?: return@forEach
                applyPatchesWatcherThreadSet.forEach { it.interrupt() }
                with(store) {
                    try {
                        when (name) {
                            "applyServerPatches" -> setOf(patchesInfo[PatchType.SERVER]!!)
                            "applyApiPatches" -> setOf(patchesInfo[PatchType.API]!!)
                            "applyGeneratedApiPatches" -> setOf(patchesInfo[PatchType.GENERATED_API]!!)
                            "applyPatches" -> setOf(
                                patchesInfo[PatchType.SERVER]!!,
                                patchesInfo[PatchType.API]!!,
                                patchesInfo[PatchType.GENERATED_API]!!
                            )

                            else -> return@forEach
                        }.forEach { info ->
                            println("Creating watcher for ${info.base}")
                            applyPatchesWatcherThreadSet.add(thread {
                                createModuleApplyPatchesWatcher(projectPath, info.base) {
                                    println(it)
                                }
                            })
                        }
                    } catch (_: Exception) {
                        return@forEach
                    }
                }
            }
        return super.executeTasks(id, taskNames, projectPath, settings, jvmParametersSetup, listener)
    }

    override fun cancelTask(id: ExternalSystemTaskId, listener: ExternalSystemTaskNotificationListener): Boolean = false
}