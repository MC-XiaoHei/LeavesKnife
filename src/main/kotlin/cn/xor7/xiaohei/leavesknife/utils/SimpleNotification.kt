package cn.xor7.xiaohei.leavesknife.utils

import cn.xor7.xiaohei.leavesknife.Icons
import com.intellij.notification.Notification
import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnActionEvent

fun createSimpleNotification(
    title: String,
    type: NotificationType,
): Notification = NotificationGroupManager.getInstance()
    .getNotificationGroup("LeavesKnife")
    .createNotification(title, type)

fun createSimpleNotification(
    title: String,
    type: NotificationType,
    actionName: String,
    action: (AnActionEvent) -> Unit,
): Notification = createSimpleNotification(title, type)
    .addAction(object : NotificationAction(actionName) {
        override fun actionPerformed(e: AnActionEvent, notification: Notification) {
            notification.hideBalloon()
            action(e)
        }
    })

fun createPluginInfoNotification(
    title: String,
    actionName: String,
    action: (AnActionEvent) -> Unit,
): Notification = createSimpleNotification(title, NotificationType.INFORMATION, actionName, action)
    .setIcon(Icons.pluginIcon)