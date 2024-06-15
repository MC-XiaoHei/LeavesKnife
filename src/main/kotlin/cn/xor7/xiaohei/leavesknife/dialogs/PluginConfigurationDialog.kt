package cn.xor7.xiaohei.leavesknife.dialogs

import cn.xor7.xiaohei.leavesknife.CommonBundle
import cn.xor7.xiaohei.leavesknife.services.PatchType
import cn.xor7.xiaohei.leavesknife.services.PatchesInfo
import cn.xor7.xiaohei.leavesknife.services.PluginStatus
import cn.xor7.xiaohei.leavesknife.services.leavesknifeStoreService
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.dsl.builder.*
import org.jetbrains.annotations.Nullable
import java.util.*
import javax.swing.DefaultComboBoxModel
import javax.swing.JComponent

class PluginConfigurationDialog(private val project: Project) : DialogWrapper(true) {
    private val store = project.leavesknifeStoreService
    private val pathTextFields = mutableMapOf<PatchType, Cell<TextFieldWithBrowseButton>>()
    private val moduleComboBoxes = mutableMapOf<PatchType, Cell<ComboBox<String>>>()

    init {
        title = CommonBundle.message("dialog.configure.title")
        init()
        startTrackingValidation()
    }

    @Nullable
    override fun createCenterPanel(): JComponent = panel {
        createPatchGroup(PatchType.SERVER, "Server")
        createPatchGroup(PatchType.API, "API")
        createPatchGroup(PatchType.GENERATED_API, "Generated API")
    }

    @Suppress("DialogTitleCapitalization")
    private fun Panel.createPatchGroup(patchType: PatchType, name: String) {
        if (!store.patchesInfo.containsKey(patchType)) {
            store.patchesInfo[patchType] = PatchesInfo(getFallbackOption(patchType), "", "")
        }
        group("$name Patches") {
            row(CommonBundle.message("dialog.configure.common.module")) {
                moduleComboBoxes[patchType] =
                    comboBox(DefaultComboBoxModel(store.modulePaths.keys.toTypedArray())).bindItem(
                        getter = {
                            store.patchesInfo[patchType]?.module ?: getFallbackOption(patchType)
                        },
                        setter = {
                            store.patchesInfo[patchType]?.module = it ?: getFallbackOption(patchType)
                        }
                    ).validationOnInput { component ->
                        validateModule(patchType, component)
                    }.validationOnApply { component ->
                        validateModule(patchType, component)
                    }
            }
            row(CommonBundle.message("dialog.configure.common.path")) {
                pathTextFields[patchType] = textFieldWithBrowseButton(
                    browseDialogTitle = CommonBundle.message(
                        "dialog.configure.${patchType.name.lowercase(Locale.getDefault())}.browse.title"
                    ),
                    project = project,
                    fileChooserDescriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor(),
                    fileChosen = { chosenFile -> chosenFile.path }
                ).bindText(
                    getter = {
                        store.patchesInfo[patchType]?.path ?: ""
                    },
                    setter = {
                        store.patchesInfo[patchType]?.path = it
                    }
                ).validationOnInput { component ->
                    if (component.text.isEmpty()) error(CommonBundle.message("dialog.configure.common.path.error.empty"))
                    else when (patchType) {
                        PatchType.SERVER -> null
                        PatchType.API ->
                            if (component.text == pathTextFields[PatchType.SERVER]?.component?.text) {
                                error(CommonBundle.message("dialog.configure.common.path.error.same"))
                            } else null

                        PatchType.GENERATED_API ->
                            if (component.text == pathTextFields[PatchType.SERVER]?.component?.text ||
                                component.text == pathTextFields[PatchType.API]?.component?.text
                            ) {
                                error(CommonBundle.message("dialog.configure.common.path.error.same"))
                            } else null
                    }
                }.validationOnApply {
                    if (it.text.isEmpty()) error(CommonBundle.message("dialog.configure.common.path.error.empty"))
                    else null
                }
            }
        }
    }

    override fun doOKAction() {
        super.doOKAction()
        with(store) {
            status = PluginStatus.ENABLED
            val configFile = configPath.toFile()
            if (!configFile.exists()) configFile.createNewFile()
            with(properties) {
                val serverPatchesInfo = patchesInfo[PatchType.SERVER]!!
                serverPatchesInfo.base = modulePaths[serverPatchesInfo.module]!!
                setProperty("patches.server.module", serverPatchesInfo.module)
                setProperty("patches.server.path", serverPatchesInfo.path)
                setProperty("patches.server.base", serverPatchesInfo.base)
                val apiPatchesInfo = patchesInfo[PatchType.API]!!
                apiPatchesInfo.base = serverPatchesInfo.base
                setProperty("patches.api.module", apiPatchesInfo.module)
                setProperty("patches.api.path", apiPatchesInfo.path)
                setProperty("patches.api.base", serverPatchesInfo.base)
                val generatedApiPatchesInfo = patchesInfo[PatchType.GENERATED_API]!!
                generatedApiPatchesInfo.base = modulePaths[generatedApiPatchesInfo.module]!!
                setProperty("patches.generated-api.module", generatedApiPatchesInfo.module)
                setProperty("patches.generated-api.path", generatedApiPatchesInfo.path)
                setProperty("patches.generated-api.base", serverPatchesInfo.base)
            }
            configFile.outputStream().use { fileOutputStream ->
                properties.store(fileOutputStream, null)
            }
            project.guessProjectDir()?.refresh(true, false)
        }
    }

    @Suppress("DialogTitleCapitalization")
    private fun validateModule(patchType: PatchType, component: ComboBox<String>): ValidationInfo? {
        return when (patchType) {
            PatchType.SERVER -> null
            PatchType.API ->
                if (component.selectedItem as String ==
                    moduleComboBoxes[PatchType.SERVER]?.component?.selectedItem as String
                ) {
                    ValidationInfo(CommonBundle.message("dialog.configure.common.module.error.same"))
                } else null

            PatchType.GENERATED_API ->
                if (component.selectedItem as String ==
                    moduleComboBoxes[PatchType.SERVER]?.component?.selectedItem as String ||
                    component.selectedItem as String ==
                    moduleComboBoxes[PatchType.API]?.component?.selectedItem as String
                ) {
                    ValidationInfo(CommonBundle.message("dialog.configure.common.module.error.same"))
                } else null
        }
    }

    private fun getFallbackOption(patchType: PatchType): String {
        return if (patchType == PatchType.GENERATED_API)
            store.modulePaths.keys.find { it.contains("generator", ignoreCase = true) }
                ?: store.modulePaths.keys.first()
        else
            store.modulePaths.keys.find { it.contains(patchType.name, ignoreCase = true) }
                ?: store.modulePaths.keys.first()
    }
}