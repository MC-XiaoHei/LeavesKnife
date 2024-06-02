package cn.xor7.xiaohei.leavesknife.services

import cn.xor7.xiaohei.leavesknife.CommonBundle
import com.intellij.notification.Notification
import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnActionEvent
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
    var needConfigure = false
        set(value) {
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
                    .addAction(object : NotificationAction(
                        CommonBundle.message("notification.configure.action")
                    ) {
                        override fun actionPerformed(e: AnActionEvent, notification: Notification) {
                            project.leavesknifeConfigService.enablePlugin = true
                            notification.hideBalloon()
                        }
                    })
                    .notify(project)
            }
        }
}

val Project.leavesknifeConfigService: ProjectConfigService
    get() = this.getService(ProjectConfigService::class.java)