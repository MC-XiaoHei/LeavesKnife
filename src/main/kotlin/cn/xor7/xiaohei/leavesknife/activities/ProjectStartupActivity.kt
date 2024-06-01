package cn.xor7.xiaohei.leavesknife.activities

import cn.xor7.xiaohei.leavesknife.services.LEAVESKNIFE_CONFIG_FILE
import cn.xor7.xiaohei.leavesknife.services.leavesknifeConfigService
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.startup.ProjectActivity
import org.gradle.tooling.GradleConnector
import org.gradle.tooling.model.idea.IdeaProject
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

class ProjectStartupActivity : ProjectActivity {
    override suspend fun execute(project: Project) {
        project.guessProjectDir()?.let {
            if (Files.exists(
                    Paths.get(
                        it.path,
                        LEAVESKNIFE_CONFIG_FILE
                    )
                )
            ) {
                project.leavesknifeConfigService.enablePlugin = true
                return@let
            }
            GradleConnector.newConnector()
                .forProjectDirectory(File(it.path))
                .connect().use { connection ->
                    val ideaProject: IdeaProject = connection.getModel(IdeaProject::class.java)
                    if (ideaProject.modules.any { it.name == "paper-api-generator" }) {
                        // project.leavesknifeConfigService.enablePlugin = true
                        // TODO: 提示用户是否启用插件
                        return@let
                    }
                }
        }
    }
}