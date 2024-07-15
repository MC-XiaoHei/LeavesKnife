package cn.xor7.xiaohei.leavesknife.toolWindow

import cn.xor7.xiaohei.leavesknife.services.Patch
import cn.xor7.xiaohei.leavesknife.services.PatchType
import cn.xor7.xiaohei.leavesknife.services.leavesknifeStoreService
import com.intellij.ide.util.treeView.NodeRenderer
import com.intellij.openapi.application.runInEdt
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.treeStructure.Tree
import javax.swing.JTree
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel

class PatchesToolWindowFactory : ToolWindowFactory, DumbAware {
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
        private val rootNode = DefaultMutableTreeNode()
        private val patchesGroupNodes = mutableMapOf(
            PatchType.SERVER to DefaultMutableTreeNode(PatchType.SERVER.name),
            PatchType.API to DefaultMutableTreeNode(PatchType.API.name),
            PatchType.GENERATED_API to DefaultMutableTreeNode(PatchType.GENERATED_API.name)
        )
        private val treeModel = DefaultTreeModel(rootNode)

        init {
            INSTANCE = this
        }

        fun getContent() = SimpleToolWindowPanel(true, true).apply {
            setContent(panel {
                row {
                    rootNode.add(patchesGroupNodes[PatchType.SERVER])
                    rootNode.add(patchesGroupNodes[PatchType.API])
                    rootNode.add(patchesGroupNodes[PatchType.GENERATED_API])
                    val tree = Tree(treeModel)
                    tree.isRootVisible = false
                    tree.cellRenderer = MyNodeRenderer
                    val scrollPane = JBScrollPane(tree)
                    cell(scrollPane).align(Align.FILL).resizableColumn()
                }.resizableRow()
                row {
                    button("Refresh") {
                        println("Refreshing patches")
                        println(store.patchesList)
                        updateTree()
                    }
                }
            })
        }

        fun updateTree() = runInEdt {
            store.patchesList.forEach { (patchType, patches) ->
                val node = patchesGroupNodes[patchType] ?: return@forEach
                node.removeAllChildren()
                patches.forEach { node.add(DefaultMutableTreeNode(it).apply { userObject = it }) }
            }
        }

        companion object {
            var INSTANCE: ServerPatchesToolWindow? = null
        }
    }

    object MyNodeRenderer : NodeRenderer() {
        private fun readResolve(): Any = MyNodeRenderer
        override fun customizeCellRenderer(
            tree: JTree,
            value: Any,
            selected: Boolean,
            expanded: Boolean,
            leaf: Boolean,
            row: Int,
            hasFocus: Boolean,
        ) {
            super.customizeCellRenderer(tree, value, selected, expanded, leaf, row, hasFocus)

            if (value is DefaultMutableTreeNode && value.userObject is Patch ) {
                val patch = value.userObject as Patch
                icon = patch.status.icon
            }
        }
    }
}