package cn.xor7.xiaohei.leavesknife.services

import cn.xor7.xiaohei.leavesknife.toolWindow.PatchesToolWindowFactory
import cn.xor7.xiaohei.leavesknife.utils.createWatchService
import com.intellij.icons.ExpUiIcons
import com.intellij.openapi.application.runInEdt
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.wm.ToolWindowManager
import com.jetbrains.rd.util.CopyOnWriteArrayList
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardWatchEventKinds
import java.util.*
import javax.swing.Icon
import kotlin.concurrent.thread

const val LEAVESKNIFE_CONFIG_FILE = "leavesknife.properties"

@Suppress("MemberVisibilityCanBePrivate")
@Service(Service.Level.PROJECT)
class ProjectStoreService(private val project: Project) {
    var status = PluginStatus.DISABLED
        set(value) {
            if (field == value) return
            runInEdt {
                ToolWindowManager
                    .getInstance(project)
                    .getToolWindow("Patches")
                    ?.isAvailable = value == PluginStatus.ENABLED || value == PluginStatus.TOOLWINDOW_ENABLED
            }
            if (value == PluginStatus.ENABLED) onEnable()
            if (value == PluginStatus.TOOLWINDOW_ENABLED) onToolWindowEnable()
            field = value
        }
    var modulePaths: MutableMap<String, String> = mutableMapOf()
    val patchesInfo: MutableMap<PatchType, PatchesInfo> = mutableMapOf()
    val properties = Properties()
    val configPath: Path = Paths.get(project.guessProjectDir()?.path ?: ".", LEAVESKNIFE_CONFIG_FILE)
    val patchesList = mutableMapOf<PatchType, CopyOnWriteArrayList<Patch>>(
        PatchType.SERVER to CopyOnWriteArrayList(),
        PatchType.API to CopyOnWriteArrayList(),
        PatchType.GENERATED_API to CopyOnWriteArrayList()
    )

    private fun onEnable() {
        println("Enabling plugin")
        patchesInfo.forEach { patchInfoEntry ->
            val path = patchInfoEntry.value.path
            val patchType = patchInfoEntry.key
            println("Creating watcher for $path")
            thread {
                createWatchService(
                    Paths.get(path),
                    StandardWatchEventKinds.ENTRY_MODIFY
                ) {
                    val newPatchesList = CopyOnWriteArrayList<Patch>()
                    File(path).list()?.forEach {
                        newPatchesList.add(Patch(it))
                    }
                    patchesList[patchType] = newPatchesList
                    runInEdt {
                        PatchesToolWindowFactory.ServerPatchesToolWindow.INSTANCE?.updateTree()
                    }
                    return@createWatchService true
                }
            }
        }
    }

    private fun onToolWindowEnable() {
        println("Enabling tool window")
        patchesInfo.forEach { patchInfoEntry ->
            val path = patchInfoEntry.value.path
            val patchType = patchInfoEntry.key
            val patchesDir = File(path)
            if (!patchesDir.exists()) {
                println("Patches directory not found: $path")
                status = PluginStatus.BROKEN_CONFIG
                return
            }
            patchesDir.list()?.forEach {
                patchesList[patchType]?.add(Patch(it))
            }
        }
        runInEdt {
            PatchesToolWindowFactory.ServerPatchesToolWindow.INSTANCE?.updateTree()
        }
    }
}

enum class PluginStatus {
    DISABLED, MISSING_CONFIG, BROKEN_CONFIG, TOOLWINDOW_ENABLED, ENABLED
}

val Project.leavesknifeStoreService: ProjectStoreService
    get() = this.getService(ProjectStoreService::class.java)

data class PatchesInfo(
    var module: String,
    var path: String,
    var base: String,
)

data class Patch(
    var name: String,
    var status: PatchStatus,
) {
    constructor(name: String) : this(name, PatchStatus.NORMAL)

    override fun toString(): String = name
}

enum class PatchStatus(val icon: Icon) {
    NORMAL(ExpUiIcons.Vcs.Patch),
    APPLIED(ExpUiIcons.Status.Success),
    APPLYING(ExpUiIcons.Status.Error)
}

enum class PatchType {
    SERVER, API, GENERATED_API
}