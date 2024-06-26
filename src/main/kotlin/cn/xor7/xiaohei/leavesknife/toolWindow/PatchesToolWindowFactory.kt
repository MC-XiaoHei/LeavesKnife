package cn.xor7.xiaohei.leavesknife.toolWindow

import cn.xor7.xiaohei.leavesknife.services.leavesknifeStoreService
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.AlignY
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.builder.plus
import com.intellij.ui.treeStructure.Tree
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel

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
        private val store = toolWindow.project.leavesknifeStoreService
        private val rootNode = DefaultMutableTreeNode("Root Node")
        private val treeModel = DefaultTreeModel(rootNode)

        fun getContent() = panel {
            row {
                rootNode.add(DefaultMutableTreeNode("Child Node"))
                val tree = Tree(treeModel)
                tree.isRootVisible = false
                scrollCell(tree).align(AlignX.FILL + AlignY.TOP)
            }.resizableRow()
        }
    }
}