package cn.xor7.xiaohei.leavesknife.activities

import cn.xor7.xiaohei.leavesknife.services.leavesknifeStoreService
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.startup.ProjectActivity
import org.gradle.tooling.GradleConnector
import org.gradle.tooling.model.idea.IdeaProject
import java.io.File
import java.nio.file.Files


class ProjectStartupActivity : ProjectActivity {
    override suspend fun execute(project: Project) {
        val store = project.leavesknifeStoreService
        project.guessProjectDir()?.let { projectDir ->
            if (Files.exists(store.configPath)) {
                // TODO 检查配置文件合法性 合法则设置 enablePlugin 为 true
            }
            GradleConnector.newConnector()
                .forProjectDirectory(File(projectDir.path))
                .connect().use { connection ->
                    val ideaProject: IdeaProject = connection.getModel(IdeaProject::class.java)
                    store.modulePaths = ideaProject.modules.filter {
                        it.gradleProject.path != ":"
                    }.associateTo(mutableMapOf()) {
                        it.name to it.contentRoots.first().rootDirectory.absolutePath
                    }
                    if (!store.modulePaths.containsKey("paper-api-generator") && !store.enablePlugin) return@let
                    if (!store.enablePlugin) store.needConfigure = true
                }
        }
    }
}