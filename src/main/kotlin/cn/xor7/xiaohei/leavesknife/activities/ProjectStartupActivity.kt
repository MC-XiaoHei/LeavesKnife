package cn.xor7.xiaohei.leavesknife.activities

import cn.xor7.xiaohei.leavesknife.services.PatchType
import cn.xor7.xiaohei.leavesknife.services.PatchesInfo
import cn.xor7.xiaohei.leavesknife.services.PluginStatus
import cn.xor7.xiaohei.leavesknife.services.leavesknifeStoreService
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
            if (status != PluginStatus.TOOLWINDOW_ENABLED) PluginStatus.MISSING_CONFIG
            else PluginStatus.ENABLED
        } else {
            PluginStatus.DISABLED
        }
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