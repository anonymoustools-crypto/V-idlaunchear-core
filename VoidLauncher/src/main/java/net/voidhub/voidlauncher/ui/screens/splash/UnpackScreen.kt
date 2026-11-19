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
 */

package net.voidhub.voidlauncher.ui.screens.splash

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.nonInteractiveScrollbar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import net.voidhub.voidlauncher.R
import net.voidhub.voidlauncher.components.InstallableItem
import net.voidhub.voidlauncher.ui.base.BaseScreen
import net.voidhub.voidlauncher.ui.components.AnimatedDirection
import net.voidhub.voidlauncher.ui.components.BackgroundCard
import net.voidhub.voidlauncher.ui.components.MarqueeText
import net.voidhub.voidlauncher.ui.components.ScalingActionButton
import net.voidhub.voidlauncher.ui.components.animated
import net.voidhub.voidlauncher.ui.components.rememberAnimatedContext
import net.voidhub.voidlauncher.ui.theme.itemColor
import net.voidhub.voidlauncher.ui.theme.onItemColor
import net.voidhub.voidlauncher.utils.animation.getAnimateTween
import net.voidhub.voidlauncher.viewmodel.SplashBackStackViewModel

@Composable
fun UnpackScreen(
    items: List<InstallableItem>,
    screenViewModel: SplashBackStackViewModel,
    onAgreeClick: () -> Unit = {}
) {
    BaseScreen(
        screenKey = SplashOperation.Unpacking,
        currentKey = screenViewModel.splashScreen.currentKey
    ) { isVisible ->
        val states = items.map { item ->
            item.state.collectAsStateWithLifecycle().value
        }
        val finished = states.count { it == InstallableItem.State.FINISHED }
        val installing = states.any { it == InstallableItem.State.RUNNING }
        val hasFailed = states.any { it == InstallableItem.State.FAILED }
        val canInstall = !installing && states.any {
            it == InstallableItem.State.NOT_STARTED ||
                it == InstallableItem.State.PENDING ||
                it == InstallableItem.State.FAILED
        }
        val progress = if (items.isEmpty()) 0f else finished.toFloat() / items.size

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(
                modifier = Modifier.animated(
                    rememberAnimatedContext(isVisible = isVisible),
                    from = AnimatedDirection.Right
                )
            ) {
                Text(
                    text = "VØID  /  CORE SETUP",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = if (installing) "Preparing your launcher…" else "Ready to install",
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    text = "Download and prepare the components required by VøidLaunchear.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "SETUP PROGRESS",
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "$finished / ${items.size} complete",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = progress,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            BackgroundCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                influencedByBackground = false,
                shape = MaterialTheme.shapes.extraLarge
            ) {
                val scrollState = rememberLazyListState()
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .nonInteractiveScrollbar(
                            state = scrollState.scrollIndicatorState!!,
                            orientation = Orientation.Vertical
                        ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    state = scrollState
                ) {
                    items(items) { item ->
                        TaskItem(item = item)
                    }
                }
            }

            ScalingActionButton(
                enabled = canInstall,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                onClick = onAgreeClick
            ) {
                MarqueeText(
                    text = stringResource(
                        if (hasFailed) R.string.generic_retry else R.string.splash_screen_agree
                    )
                )
            }
        }
    }
}

@Composable
private fun TaskItem(
    item: InstallableItem,
    modifier: Modifier = Modifier
) {
    val state by item.state.collectAsStateWithLifecycle()
    val message by item.task.taskMessage.collectAsStateWithLifecycle()

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = itemColor(),
        contentColor = onItemColor()
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 14.dp, top = 10.dp, bottom = 10.dp)
                    .animateContentSize(animationSpec = getAnimateTween())
            ) {
                Text(text = item.name, style = MaterialTheme.typography.labelLarge)
                item.summary?.let {
                    Text(text = it, style = MaterialTheme.typography.labelSmall)
                }
                if (state == InstallableItem.State.RUNNING || state == InstallableItem.State.FAILED) {
                    message?.let { taskMessage ->
                        Text(
                            text = taskMessage,
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 1
                        )
                    }
                }
            }

            val iconModifier = Modifier
                .padding(horizontal = 14.dp)
                .size(20.dp)
            when (state) {
                InstallableItem.State.NOT_STARTED -> Icon(
                    painter = painterResource(R.drawable.ic_folder_zip_outlined),
                    contentDescription = null,
                    modifier = iconModifier
                )
                InstallableItem.State.PENDING -> Icon(
                    painter = painterResource(R.drawable.ic_update),
                    contentDescription = null,
                    modifier = iconModifier
                )
                InstallableItem.State.RUNNING -> CircularProgressIndicator(
                    modifier = iconModifier,
                    strokeWidth = 2.dp
                )
                InstallableItem.State.FINISHED -> Icon(
                    painter = painterResource(R.drawable.ic_check),
                    contentDescription = null,
                    modifier = iconModifier
                )
                InstallableItem.State.FAILED -> Icon(
                    painter = painterResource(R.drawable.ic_warning_filled),
                    contentDescription = null,
                    modifier = iconModifier
                )
                else -> Unit
            }
        }
    }
}
