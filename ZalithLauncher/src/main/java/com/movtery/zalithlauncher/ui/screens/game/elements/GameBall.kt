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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.movtery.zalithlauncher.ui.screens.game.elements

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.setting.enums.FpsDisplayMode
import com.movtery.zalithlauncher.utils.device.VoidFpsMonitor
import com.movtery.zalithlauncher.ui.components.FloatingBall
import com.movtery.zalithlauncher.ui.screens.content.elements.MemoryPreview


@Composable
fun DraggableGameBall(
    position: Offset,
    onPositionChanged: (Offset) -> Unit,
    onSavePos: () -> Unit,
    gameFps: () -> Int?,
    fpsDisplayMode: FpsDisplayMode,
    fpsHistory: () -> List<Int>,
    fpsMax: () -> Int,
    fpsMin: () -> Int,
    showMemory: Boolean,
    opened: Boolean,
    alpha: Float = 1f,
    onClick: () -> Unit = {}
) {
    FloatingBall(
        modifier = Modifier.focusProperties { canFocus = false },
        position = position,
        onPositionChanged = onPositionChanged,
        onSavePos = onSavePos,
        onClick = onClick,
        alpha = alpha,
        color = Color(0xCC202020),
        contentColor = Color.White,
        shape = RoundedCornerShape(12.dp)
    ) {
        GameBallContent(
            gameFps = gameFps,
            fpsDisplayMode = fpsDisplayMode,
            fpsHistory = fpsHistory,
            fpsMax = fpsMax,
            fpsMin = fpsMin,
            showMemory = showMemory,
            opened = opened,
        )
    }
}

@Composable
private fun GameBallContent(
    gameFps: () -> Int?,
    fpsDisplayMode: FpsDisplayMode,
    fpsHistory: () -> List<Int>,
    fpsMax: () -> Int,
    fpsMin: () -> Int,
    showMemory: Boolean,
    opened: Boolean,
) {
    val fps = gameFps()
    val history = fpsHistory()
    val fpsMaxValue = fpsMax()
    val fpsMinValue = fpsMin()
    val stutterDropDetected = remember(history) {
        VoidFpsMonitor.hasSevereRecentDrop(history)
    }
    val showFps = fps != null

    Row(
        modifier = Modifier
            .padding(horizontal = 4.dp, vertical = 4.dp)
            .animateContentSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(RoundedCornerShape(7.dp))
                .background(Color.White.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Crossfade(opened) { state ->
                Icon(
                    modifier = Modifier.size(14.dp),
                    painter = painterResource(
                        if (state) R.drawable.ic_menu_open else R.drawable.ic_menu
                    ),
                    contentDescription = null,
                    tint = Color.White
                )
            }
        }

        if (showFps) {
            Text(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.White.copy(alpha = 0.12f))
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                text = "${fps}",
                color = Color.White,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
private fun ColumnScope.CustomAnimatedVisibility(
    visible: Boolean,
    content: @Composable (AnimatedVisibilityScope.() -> Unit)
) {
    AnimatedVisibility(
        visible = visible,
        enter = expandIn(expandFrom = Alignment.CenterStart) + fadeIn(),
        exit = shrinkOut(shrinkTowards = Alignment.CenterStart) + fadeOut(),
        content = content
    )
}
