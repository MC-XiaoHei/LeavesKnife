package cn.xor7.xiaohei.leavesknife.activities

import cn.xor7.xiaohei.leavesknife.services.leavesknifeConfigService
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import org.gradle.tooling.GradleConnector
import org.gradle.tooling.model.idea.IdeaProject
import java.io.File

class ProjectStartupActivity : ProjectActivity {
    override suspend fun execute(project: Project) {
        val projectDir = project.basePath ?: return
        GradleConnector.newConnector()
            .forProjectDirectory(File(projectDir))
            .connect().use { connection ->
                val ideaProject: IdeaProject = connection.getModel(IdeaProject::class.java)
                project.leavesknifeConfigService.enablePlugin =
                    ideaProject.modules.any { it.name == "paper-api-generator" }
            }
    }
}