package cn.xor7.xiaohei.leavesknife.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import java.nio.file.Files
import java.nio.file.Paths

const val LEAVESKNIFE_CONFIG_FILE = "leavesknife.properties"

@Service(Service.Level.PROJECT)
class ProjectConfigService(project: Project) {
    var enablePlugin = false

    init {
        project.guessProjectDir()?.let {
            enablePlugin = Files.exists(
                Paths.get(
                    it.path,
                    LEAVESKNIFE_CONFIG_FILE
                )
            )
        }
    }
}
