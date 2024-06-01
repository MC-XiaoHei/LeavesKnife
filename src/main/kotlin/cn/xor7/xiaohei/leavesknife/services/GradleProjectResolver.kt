package cn.xor7.xiaohei.leavesknife.services

import com.intellij.openapi.externalSystem.model.DataNode
import com.intellij.openapi.externalSystem.model.project.ModuleData
import com.intellij.openapi.externalSystem.model.project.ProjectData
import org.gradle.tooling.model.idea.IdeaModule
import org.jetbrains.plugins.gradle.service.project.AbstractProjectResolverExtension

class GradleProjectResolver : AbstractProjectResolverExtension() {
    override fun populateModuleDependencies(
        gradleModule: IdeaModule,
        ideModule: DataNode<ModuleData>,
        ideProject: DataNode<ProjectData>,
    ) {
        super.populateModuleDependencies(gradleModule, ideModule, ideProject)
        println("populateModuleDependencies")
    }
}