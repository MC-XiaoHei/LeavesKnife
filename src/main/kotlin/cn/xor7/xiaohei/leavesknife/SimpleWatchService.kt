package cn.xor7.xiaohei.leavesknife

import java.nio.file.*

fun createWatchService(
    path: Path,
    vararg events: WatchEvent.Kind<*>?,
    onEvent: (WatchEvent<*>) -> Boolean,
) {
    val watchService: WatchService = FileSystems.getDefault().newWatchService()
    path.register(watchService, events)
    var key: WatchKey
    while ((watchService.take().also { key = it }) != null) {
        for (event: WatchEvent<*> in key.pollEvents()) {
            if (onEvent(event)) return
        }
        key.reset()
    }
}