package cn.xor7.xiaohei.leavesknife.utils

import com.intellij.notification.Notification
import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project

fun notifyPluginInfo(
    project: Project,
    title: String,
    actionName: String,
    action: (AnActionEvent) -> Unit,
) {
    NotificationGroupManager.getInstance()
        .getNotificationGroup("LeavesKnife")
        .createNotification(
            title,
            NotificationType.INFORMATION
        )
        .addAction(object : NotificationAction(actionName) {
            override fun actionPerformed(e: AnActionEvent, notification: Notification) {
                notification.hideBalloon()
                action(e)
            }
        })
        .notify(project)
}