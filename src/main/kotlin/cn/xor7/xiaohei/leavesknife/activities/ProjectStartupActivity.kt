package cn.xor7.xiaohei.leavesknife.activities

import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity

class ProjectStartupActivity : ProjectActivity {
    override suspend fun execute(project: Project) {
        println("Project opened.")
    }
}