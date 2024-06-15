package cn.xor7.xiaohei.leavesknife.toolWindow

import cn.xor7.xiaohei.leavesknife.services.ProjectStoreService
import com.intellij.openapi.application.runInEdt
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.ui.components.JBPanel
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.dsl.builder.panel

class PatchesToolWindowFactory : ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val serverPatchesToolWindow = ServerPatchesToolWindow(toolWindow)
        val content = ContentFactory.getInstance().createContent(
            serverPatchesToolWindow.getContent(),
            null,
            false
        )
        toolWindow.contentManager.addContent(content)
    }

    override suspend fun isApplicableAsync(project: Project): Boolean = true

    override fun shouldBeAvailable(project: Project) = false

    class ServerPatchesToolWindow(toolWindow: ToolWindow) {
        private val service = toolWindow.project.service<ProjectStoreService>()

        fun getContent() = panel {
            row {
                label("Hello, World!")
            }
        }
    }
}