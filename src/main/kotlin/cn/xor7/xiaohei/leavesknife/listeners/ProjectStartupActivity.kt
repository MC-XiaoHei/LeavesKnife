package cn.xor7.xiaohei.leavesknife.listeners

import cn.xor7.xiaohei.leavesknife.CommonBundle
import cn.xor7.xiaohei.leavesknife.dialogs.PluginConfigurationDialog
import cn.xor7.xiaohei.leavesknife.services.PatchType
import cn.xor7.xiaohei.leavesknife.services.PatchesInfo
import cn.xor7.xiaohei.leavesknife.services.PluginStatus
import cn.xor7.xiaohei.leavesknife.services.leavesknifeStoreService
import cn.xor7.xiaohei.leavesknife.utils.createPluginInfoNotification
import cn.xor7.xiaohei.leavesknife.utils.createSimpleNotification
import com.intellij.notification.NotificationType
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.startup.ProjectActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.gradle.tooling.GradleConnector
import org.gradle.tooling.model.idea.IdeaProject
import java.io.File
import java.nio.file.Files
import kotlin.io.path.inputStream

class ProjectStartupActivity : ProjectActivity {
    override suspend fun execute(project: Project) = with(project.leavesknifeStoreService) {
        if (Files.exists(configPath)) {
            configPath.inputStream().use {
                properties.load(it)
                try {
                    patchesInfo[PatchType.SERVER] = PatchesInfo(
                        properties.getProperty("patches.server.module")!!,
                        properties.getProperty("patches.server.path")!!,
                        properties.getProperty("patches.server.base")!!
                    )
                    patchesInfo[PatchType.API] = PatchesInfo(
                        properties.getProperty("patches.api.module")!!,
                        properties.getProperty("patches.api.path")!!,
                        properties.getProperty("patches.api.base")!!
                    )
                    patchesInfo[PatchType.GENERATED_API] = PatchesInfo(
                        properties.getProperty("patches.generated-api.module")!!,
                        properties.getProperty("patches.generated-api.path")!!,
                        properties.getProperty("patches.generated-api.base")!!
                    )
                    status = PluginStatus.TOOLWINDOW_ENABLED
                } catch (_: Exception) {
                    status = PluginStatus.BROKEN_CONFIG
                    thisLogger().warn("Failed to read plugin config")
                }
            }
        }
        scanModules(project)
        status = if (modulePaths.containsKey("paper-api-generator")) {
            when (status) {
                PluginStatus.TOOLWINDOW_ENABLED -> PluginStatus.ENABLED
                PluginStatus.BROKEN_CONFIG -> {
                    createSimpleNotification(
                        CommonBundle.message("notification.broken_config.title"),
                        NotificationType.ERROR,
                        CommonBundle.message("notification.broken_config.action")
                    ) {
                        PluginConfigurationDialog(project).show()
                    }.notify(project)
                    PluginStatus.BROKEN_CONFIG
                }

                PluginStatus.DISABLED -> {
                    createPluginInfoNotification(
                        CommonBundle.message("notification.missing_config.title"),
                        CommonBundle.message("notification.missing_config.action")
                    ) {
                        PluginConfigurationDialog(project).show()
                    }.notify(project)
                    PluginStatus.MISSING_CONFIG
                }

                else -> PluginStatus.DISABLED
            }
        } else {
            createSimpleNotification(
                CommonBundle.message("notification.unexpected_config.title"),
                NotificationType.WARNING
            ).notify(project)
            PluginStatus.DISABLED
        }
        if(status == PluginStatus.ENABLED) onEnable()
    }

    private fun onEnable() {

    }

    private suspend fun scanModules(project: Project) {
        withContext(Dispatchers.IO) {
            project.guessProjectDir()?.let { projectDir ->
                GradleConnector.newConnector()
                    .forProjectDirectory(File(projectDir.path))
                    .connect().use { connection ->
                        val ideaProject: IdeaProject = connection.getModel(IdeaProject::class.java)
                        project.leavesknifeStoreService.modulePaths = ideaProject.modules.filter {
                            it.gradleProject.path != ":"
                        }.associateTo(mutableMapOf()) {
                            it.name to it.contentRoots.first().rootDirectory.absolutePath
                        }
                    }
            }
        }
    }
}