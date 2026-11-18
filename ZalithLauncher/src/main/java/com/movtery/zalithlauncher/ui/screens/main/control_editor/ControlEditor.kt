/*
 * Zalith Launcher 2
 * Copyright (C) 2025-2026 MovTery <movtery228@qq.com> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.movtery.zalithlauncher.ui.screens.main.control_editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.movtery.guide.guideNode
import com.movtery.layer_controller.ControlEditorLayer
import com.movtery.layer_controller.data.ButtonSize
import com.movtery.layer_controller.data.ButtonShape
import com.movtery.layer_controller.data.createNewButtonStyle
import com.movtery.layer_controller.data.ButtonPosition
import com.movtery.layer_controller.data.DefaultDirectionEvents
import com.movtery.layer_controller.data.DefaultLockEvents
import com.movtery.layer_controller.data.JoystickData
import com.movtery.layer_controller.data.NormalData
import com.movtery.layer_controller.data.TextData
import com.movtery.layer_controller.data.VisibilityType
import com.movtery.layer_controller.data.createAdaptiveButtonSize
import com.movtery.layer_controller.data.createWidgetWithUUID
import com.movtery.layer_controller.data.lang.createTranslatable
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.setting.enums.isLauncherInDarkTheme
import com.movtery.zalithlauncher.ui.components.MenuState
import com.movtery.zalithlauncher.ui.components.rememberBoxSize
import com.movtery.zalithlauncher.ui.guide.GuideKeys
import com.movtery.zalithlauncher.ui.screens.main.control_editor.edit_widget.EditWidgetDialog
import com.movtery.zalithlauncher.ui.screens.main.control_editor.style_cabinet.OpenCabinetSource
import com.movtery.zalithlauncher.viewmodel.EditorViewModel
import java.io.File

/**
 * 控制布局编辑器主要UI，用于编辑控制布局
 * @param exit 保存后执行的退出
 * @param menuExit 通过菜单直接调用的“直接退出”
 */
@Composable
fun BoxWithConstraintsScope.ControlEditor(
    viewModel: EditorViewModel,
    targetFile: File,
    exit: () -> Unit,
    menuExit: () -> Unit
) {
    val layers by viewModel.observableLayout.layers.collectAsStateWithLifecycle()
    val styles by viewModel.observableLayout.styles.collectAsStateWithLifecycle()
    val joystickStyles by viewModel.observableLayout.joystickStyles.collectAsStateWithLifecycle()

    /** 默认新建的控件层的名称 */
    val defaultLayerName = stringResource(R.string.control_editor_edit_layer_default)
    /** 默认新建的按键的名称 */
    val defaultButtonName = stringResource(R.string.control_editor_edit_button_default)
    /** 默认新建的文本框的名称 */
    val defaultTextName = stringResource(R.string.control_editor_edit_text_default)

    val density = LocalDensity.current
    val screenSize = rememberBoxSize()

    if (viewModel.isPreviewMode) {
        PreviewControlBox(
            modifier = Modifier.fillMaxSize(),
            observableLayout = viewModel.observableLayout,
            previewScenario = viewModel.previewScenario,
            previewHideLayerWhen = viewModel.previewHideLayerWhen,
        )
    } else {
        ControlEditorLayer(
            observedLayout = viewModel.observableLayout,
            selectedWidget = viewModel.selectedWidget?.data,
            onButtonTap = viewModel::onWidgetTapped,
            onBackgroundClick = {
                //点击背景层时清除选中的控件
                viewModel.selectedWidget = null
            },
            floatingButtons = {
                //设置属性
                ActionButton(
                    painter = painterResource(R.drawable.ic_settings_filled),
                    text = stringResource(R.string.generic_setting),
                    onClick = viewModel::openWidgetEditor
                )
                //复制控件
                ActionButton(
                    painter = painterResource(R.drawable.ic_file_copy_filled),
                    text = stringResource(R.string.control_editor_edit_dialog_clone_widget),
                    onClick = {
                        val widget = viewModel.selectedWidget
                        if (widget != null) {
                            viewModel.requestCloneWidget(widget.data, widget.layer)
                        }
                    }
                )
                //删除
                ActionButton(
                    painter = painterResource(R.drawable.ic_delete_filled),
                    text = stringResource(R.string.generic_delete),
                    onClick = {
                        val widget = viewModel.selectedWidget
                        if (widget != null) {
                            viewModel.requestDeleteWidget(widget.data, widget.layer)
                        }
                    }
                )
            },
            enableSnap = AllSettings.editorEnableWidgetSnap.state,
            snapInAllLayers = AllSettings.editorSnapInAllLayers.state,
            snapMode = AllSettings.editorWidgetSnapMode.state,
            focusedLayer = viewModel.selectedLayer?.takeIf { viewModel.isLayerFocus },
            isDark = isLauncherInDarkTheme()
        )
    }

    EditorMenu(
        state = viewModel.editorMenu,
        closeScreen = { viewModel.editorMenu = MenuState.HIDE },
        layers = layers,
        onReorder = { from, to ->
            viewModel.observableLayout.reorder(from, to)
        },
        selectedLayer = viewModel.selectedLayer,
        onLayerSelected = { layer ->
            viewModel.selectedLayer = layer
        },
        createLayer = {
            viewModel.createLayer(defaultLayerName)
        },
        onAttribute = viewModel::editLayer,
        onHideSwitch = { layer ->
            layer.editorHide = layer.editorHide.not()
            viewModel.onLayerHideChanged(layer.editorHide, layer)
        },
        addNewButton = {
            viewModel.addWidget(layers) { layer ->
                val bedrockStyleId = styles.firstOrNull { it.name == "Bedrock Touch" }?.uuid
                    ?: createNewButtonStyle("Bedrock Touch").let { baseStyle ->
                        val light = baseStyle.lightStyle.copy(
                            alpha = 0.94f,
                            pressedAlpha = 1f,
                            backgroundColor = Color(0xD9343434),
                            pressedBackgroundColor = Color(0xFFB8DDF5),
                            contentColor = Color.White,
                            pressedContentColor = Color(0xFF15202B),
                            borderWidth = 2,
                            pressedBorderWidth = 2,
                            borderColor = Color(0xFFE0E0E0),
                            pressedBorderColor = Color(0xFFFFFFFF),
                            borderRadius = ButtonShape(8f),
                            pressedBorderRadius = ButtonShape(8f)
                        )
                        val dark = baseStyle.darkStyle.copy(
                            alpha = 0.94f,
                            pressedAlpha = 1f,
                            backgroundColor = Color(0xD9262B31),
                            pressedBackgroundColor = Color(0xFFB8DDF5),
                            contentColor = Color.White,
                            pressedContentColor = Color(0xFF15202B),
                            borderWidth = 2,
                            pressedBorderWidth = 2,
                            borderColor = Color(0xFFB8C4CE),
                            pressedBorderColor = Color.White,
                            borderRadius = ButtonShape(8f),
                            pressedBorderRadius = ButtonShape(8f)
                        )
                        baseStyle.copy(lightStyle = light, darkStyle = dark).also {
                            viewModel.observableLayout.addStyle(it)
                        }.uuid
                    }
                layer.addNormalButton(
                    createWidgetWithUUID { uuid ->
                        NormalData(
                            text = createTranslatable(default = defaultButtonName),
                            uuid = uuid,
                            position = nextBedrockButtonPosition(layer),
                            buttonSize = createAdaptiveButtonSize(
                                referenceLength = screenSize.height,
                                density = density.density
                            ),
                            buttonStyle = bedrockStyleId,
                            visibilityType = VisibilityType.ALWAYS,
                            isSwipple = false,
                            isPenetrable = false,
                            isToggleable = false
                        )
                    }
                )
                layer.normalButtons.value.lastOrNull()?.let { viewModel.onWidgetTapped(it, layer) }
            }
        },
        addNewText = {
            viewModel.addWidget(layers) { layer ->
                layer.addTextBox(
                    createWidgetWithUUID { uuid ->
                        TextData(
                            text = createTranslatable(default = defaultTextName),
                            uuid = uuid,
                            position = nextBedrockButtonPosition(layer),
                            buttonSize = createAdaptiveButtonSize(
                                referenceLength = screenSize.height,
                                density = density.density,
                                type = ButtonSize.Type.WrapContent //文本框默认使用包裹内容
                            ),
                            visibilityType = VisibilityType.ALWAYS
                        )
                    }
                )
                layer.textBoxes.value.lastOrNull()?.let { viewModel.onWidgetTapped(it, layer) }
            }
        },
        addNewJoystick = {
            viewModel.addWidget(layers) { layer ->
                layer.addJoystickButton(
                    createWidgetWithUUID { uuid ->
                        JoystickData(
                            uuid = uuid,
                            position = nextBedrockJoystickPosition(layer),
                            sizeType = ButtonSize.Type.Percentage,
                            visibilityType = VisibilityType.ALWAYS,
                            directionEvents = DefaultDirectionEvents,
                            lockEvents = DefaultLockEvents,
                        )
                    }
                )
                layer.joystickButtons.value.lastOrNull()?.let { viewModel.onWidgetTapped(it, layer) }
            }
        },
        openStyleList = viewModel::openStyleList,
        openJoystickStyleList = viewModel::openJoystickStyleList,
        openStyleCabinet = {
            viewModel.openStyleCabinet(OpenCabinetSource.Menu)
        },
        isLayerFocus = viewModel.isLayerFocus,
        onLayerFocusChanged = { viewModel.isLayerFocus = it },
        isPreviewMode = viewModel.isPreviewMode,
        onPreviewChanged = { mode ->
            viewModel.applyEditorHide()
            viewModel.isPreviewMode = mode
        },
        previewScenario = viewModel.previewScenario,
        onPreviewScenarioChanged = { scenario ->
            viewModel.previewScenario = scenario
        },
        previewHideLayerWhen = viewModel.previewHideLayerWhen,
        onPreviewHideLayerChanged = { hideWhen ->
            viewModel.previewHideLayerWhen = hideWhen
        },
        onSave = {
            viewModel.save(targetFile, onSaved = {})
        },
        saveAndExit = {
            viewModel.save(targetFile, onSaved = exit)
        },
        onExit = menuExit,
    )

    MenuBox(
        modifier = Modifier.guideNode(
            key = GuideKeys.Editor.Step.MenuBall,
            holeRadius = 12.dp
        ),
        position = viewModel.editorBallPosition,
        onPositionChanged = { viewModel.editorBallPosition = it },
        opened = viewModel.editorMenu == MenuState.SHOW
    ) {
        viewModel.switchMenu()
    }

    EditWidgetDialog(
        data = viewModel.selectedWidget,
        visible = viewModel.editorOperation == EditorOperation.SelectButton,
        styles = styles,
        joystickStyles = joystickStyles,
        onDismissRequest = viewModel::dismissOperation,
        onDelete = { data, layer ->
            viewModel.requestDeleteWidget(data, layer)
        },
        onClone = { data, layer ->
            viewModel.requestCloneWidget(data, layer)
        },
        onEditWidgetText = viewModel::editWidgetText,
        switchControlLayers = { data, type ->
            viewModel.switchLayersVisibility(data, type)
        },
        sendText = viewModel::sendText,
        openStyleList = viewModel::openStyleList,
        openJoystickStyleList = viewModel::openJoystickStyleList
    )

    //全部对话框
    EditorDialogHost(viewModel)
}

@Composable
private fun ActionButton(
    painter: Painter,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.semantics { role = Role.Button },
        shape = ButtonDefaults.shape,
        color = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(all = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                modifier = Modifier
                    .padding(start = 6.dp)
                    .size(20.dp),
                painter = painter,
                contentDescription = text
            )
            Text(
                modifier = Modifier.padding(end = 6.dp),
                text = text,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}


/**
 * Places newly added touch buttons in a Bedrock-inspired right-side action cluster.
 * Existing widgets are checked first so newly created controls do not all stack at center.
 */
private fun nextBedrockButtonPosition(layer: com.movtery.layer_controller.observable.ObservableControlLayer): ButtonPosition {
    val occupied = buildList {
        addAll(layer.normalButtons.value.map { it.position })
        addAll(layer.textBoxes.value.map { it.position })
        addAll(layer.joystickButtons.value.map { it.position })
    }
    val candidates = listOf(
        ButtonPosition(8800, 7200),
        ButtonPosition(9600, 8000),
        ButtonPosition(8000, 8200),
        ButtonPosition(9200, 9200),
        ButtonPosition(7200, 7000),
        ButtonPosition(7200, 9000),
        ButtonPosition(6000, 7800),
        ButtonPosition(6000, 9200),
        ButtonPosition(5000, 7000),
        ButtonPosition(5000, 9000),
        ButtonPosition(4000, 8000),
        ButtonPosition(3000, 7000),
        ButtonPosition(3000, 9000)
    )
    return candidates.firstOrNull { candidate ->
        occupied.none { current ->
            kotlin.math.abs(current.x - candidate.x) < 750 &&
                kotlin.math.abs(current.y - candidate.y) < 750
        }
    } ?: ButtonPosition(8500, (6500 + occupied.size * 350).coerceAtMost(9700))
}

/**
 * Places the movement joystick on the left side, like a Bedrock touch HUD.
 */
private fun nextBedrockJoystickPosition(layer: com.movtery.layer_controller.observable.ObservableControlLayer): ButtonPosition {
    val occupied = buildList {
        addAll(layer.normalButtons.value.map { it.position })
        addAll(layer.textBoxes.value.map { it.position })
        addAll(layer.joystickButtons.value.map { it.position })
    }
    val candidates = listOf(
        ButtonPosition(1400, 8200),
        ButtonPosition(2200, 8200),
        ButtonPosition(1400, 6800),
        ButtonPosition(2200, 6800),
        ButtonPosition(3200, 8200)
    )
    return candidates.firstOrNull { candidate ->
        occupied.none { current ->
            kotlin.math.abs(current.x - candidate.x) < 1200 &&
                kotlin.math.abs(current.y - candidate.y) < 1200
        }
    } ?: ButtonPosition(1400, 8200)
}
