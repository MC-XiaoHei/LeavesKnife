package cn.xor7.xiaohei.leavesknife.services

import com.intellij.openapi.application.runInEdt
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindowManager

const val LEAVESKNIFE_CONFIG_FILE = "leavesknife.properties"

@Service(Service.Level.PROJECT)
class ProjectConfigService(private val project: Project) {
    var enablePlugin = false
        set(value) {
            field = value
            runInEdt {
                ToolWindowManager
                    .getInstance(project)
                    .getToolWindow("Patches")
                    ?.isAvailable = value
            }
        }
}

val Project.leavesknifeConfigService: ProjectConfigService
    get() = this.getService(ProjectConfigService::class.java)
