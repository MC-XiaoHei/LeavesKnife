package cn.xor7.xiaohei.leavesknife.activities

import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import org.gradle.tooling.GradleConnector
import org.gradle.tooling.ProjectConnection
import org.gradle.tooling.model.idea.IdeaProject
import java.io.File

class ProjectStartupActivity : ProjectActivity {
    override suspend fun execute(project: Project) {
        println("Project opened.")
        val projectDir = project.basePath ?: return
        GradleConnector.newConnector()
            .forProjectDirectory(File(projectDir))
            .connect().use { connection ->
                val ideaProject: IdeaProject = connection.getModel(IdeaProject::class.java)
                for (module in ideaProject.modules) {
                    println("Module: ${module.name}")
                    for (dependency in module.dependencies) {
                        println("  Dependency: ${dependency}")
                    }
                }
            }
    }
}