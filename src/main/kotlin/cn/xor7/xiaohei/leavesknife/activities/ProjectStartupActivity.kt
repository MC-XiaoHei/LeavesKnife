package cn.xor7.xiaohei.leavesknife.activities

import cn.xor7.xiaohei.leavesknife.CommonBundle
import cn.xor7.xiaohei.leavesknife.services.LEAVESKNIFE_CONFIG_FILE
import cn.xor7.xiaohei.leavesknife.services.leavesknifeConfigService
import com.intellij.notification.Notification
import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.startup.ProjectActivity
import org.gradle.tooling.GradleConnector
import org.gradle.tooling.model.idea.IdeaProject
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

class ProjectStartupActivity : ProjectActivity {
    override suspend fun execute(project: Project) {
        project.guessProjectDir()?.let {
            if (Files.exists(
                    Paths.get(
                        it.path,
                        LEAVESKNIFE_CONFIG_FILE
                    )
                )
            ) {
                project.leavesknifeConfigService.enablePlugin = true
                return@let
            }
            GradleConnector.newConnector()
                .forProjectDirectory(File(it.path))
                .connect().use { connection ->
                    val ideaProject: IdeaProject = connection.getModel(IdeaProject::class.java)
                    if (ideaProject.modules.any { it.name == "paper-api-generator" }) {
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
                        return@let
                    }
                }
        }
    }
}