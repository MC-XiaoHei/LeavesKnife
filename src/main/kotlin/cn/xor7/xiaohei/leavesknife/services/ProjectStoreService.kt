package cn.xor7.xiaohei.leavesknife.services

import cn.xor7.xiaohei.leavesknife.CommonBundle
import cn.xor7.xiaohei.leavesknife.dialogs.PluginConfigurationDialog
import com.intellij.notification.Notification
import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnActionEvent
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
    var needConfigure = false
        set(value) {
            if (value) enablePlugin = false
            if (field == value) return
            field = value
            if (value) {
                @Suppress("DialogTitleCapitalization")
                NotificationGroupManager.getInstance()
                    .getNotificationGroup("LeavesKnife")
                    .createNotification(
                        CommonBundle.message("notification.configure.title"),
                        NotificationType.INFORMATION
                    )
                    .addAction(object : NotificationAction(CommonBundle.message("notification.configure.action")) {
                        override fun actionPerformed(e: AnActionEvent, notification: Notification) {
                            notification.hideBalloon()
                            PluginConfigurationDialog(project).show()
                        }
                    })
                    .notify(project)
            }
        }
    var modulePaths: MutableMap<String, String> = mutableMapOf()
    val patchesInfo: MutableMap<PatchType, PatchesInfo> = mutableMapOf()
    val properties = Properties()
    val configPath: Path = Paths.get(project.guessProjectDir()?.path ?: ".", LEAVESKNIFE_CONFIG_FILE)
}

val Project.leavesknifeStoreService: ProjectStoreService
    get() = this.getService(ProjectStoreService::class.java)

data class PatchesInfo(var module: String, var path: String, var base: String)

enum class PatchType {
    SERVER, API, GENERATED_API
}