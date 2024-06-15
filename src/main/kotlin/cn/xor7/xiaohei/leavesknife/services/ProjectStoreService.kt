package cn.xor7.xiaohei.leavesknife.services

import com.intellij.openapi.application.runInEdt
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.wm.ToolWindowManager
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*

const val LEAVESKNIFE_CONFIG_FILE = "leavesknife.properties"

@Service(Service.Level.PROJECT)
class ProjectStoreService(private val project: Project) {
    var status = PluginStatus.DISABLED
        set(value) {
            if(field == value) return
            runInEdt {
                ToolWindowManager
                    .getInstance(project)
                    .getToolWindow("Patches")
                    ?.isAvailable = value == PluginStatus.ENABLED || value == PluginStatus.TOOLWINDOW_ENABLED
            }
            field = value
        }
    var modulePaths: MutableMap<String, String> = mutableMapOf()
    val patchesInfo: MutableMap<PatchType, PatchesInfo> = mutableMapOf()
    val properties = Properties()
    val configPath: Path = Paths.get(project.guessProjectDir()?.path ?: ".", LEAVESKNIFE_CONFIG_FILE)
}

enum class PluginStatus {
    DISABLED, MISSING_CONFIG, BROKEN_CONFIG, TOOLWINDOW_ENABLED, ENABLED
}

val Project.leavesknifeStoreService: ProjectStoreService
    get() = this.getService(ProjectStoreService::class.java)

data class PatchesInfo(var module: String, var path: String, var base: String)

enum class PatchType {
    SERVER, API, GENERATED_API
}