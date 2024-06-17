package cn.xor7.xiaohei.leavesknife.utils

import java.io.File
import java.nio.file.*

fun createWatchService(
    path: Path,
    vararg events: WatchEvent.Kind<*>?,
    onEvent: (WatchEvent<*>) -> Boolean,
) {
    val watchService: WatchService = FileSystems.getDefault().newWatchService()
    path.register(watchService, events)
    var key: WatchKey
    while ((watchService.take().also { key = it }) != null && key.isValid) {
        for (event: WatchEvent<*> in key.pollEvents()) {
            if (!onEvent(event)) return
        }
        key.reset()
    }
}

// 此函数使用了 Kotlin 2.0.0 中的新功能 Local variables and further scopes 但 IDEA 插件尚未更新导致报错，故添加此注解
@Suppress("SMARTCAST_IMPOSSIBLE")
fun createGitRebaseWatchService(
    dotGitPath: String,
    onEvent: (Int) -> Unit,
) {
    createWatchService(
        Path.of(dotGitPath),
        StandardWatchEventKinds.ENTRY_MODIFY
    ) parentEvent@{
        val rebaseApplyPath = "$dotGitPath/rebase-apply"
        val rebaseApplyFile = File(rebaseApplyPath)
        var file: File? = null
        var next = 0
        if (rebaseApplyFile.exists()) {
            createWatchService(
                Path.of(rebaseApplyPath),
                StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_DELETE
            ) childEvent@{
                if (file == null) file = File("$rebaseApplyPath/next")
                if (!file.exists()) return@childEvent false
                val content = file.readLines().first().toInt()
                if (content != next) {
                    next = content
                    onEvent(file.readLines().first().toInt())
                }
                return@childEvent true
            }
            return@parentEvent false
        } else return@parentEvent true
    }
}

fun createModuleApplyPatchesWatcher(
    projectDir: String,
    moduleBase: String,
    onEvent: (Int) -> Unit,
) {
    if (File(moduleBase).exists()) createModuleDotGitPathWatcher(moduleBase, onEvent)
    else createWatchService(
        Path.of(projectDir),
        StandardWatchEventKinds.ENTRY_CREATE
    ) parentEvent@{
        if (!File(moduleBase).exists()) return@parentEvent true
        createModuleDotGitPathWatcher(moduleBase, onEvent)
        return@parentEvent false
    }
}

private fun createModuleDotGitPathWatcher(
    moduleBase: String,
    onEvent: (Int) -> Unit,
) {
    val dotGitPath = "$moduleBase/.git"
    if (File(dotGitPath).exists()) createGitRebaseWatchService(dotGitPath) { onEvent(it) }
    else createWatchService(Path.of(moduleBase), StandardWatchEventKinds.ENTRY_CREATE) childEvent@{
        if (!File(dotGitPath).exists()) return@childEvent true
        createGitRebaseWatchService(dotGitPath) { onEvent(it) }
        return@childEvent false
    }
}