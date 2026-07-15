package com.keybridge.app.ui.keyboard

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.keybridge.app.data.KeyData
import com.keybridge.app.data.KeyType
import com.keybridge.app.data.KeyboardLayout
import com.keybridge.app.data.KeyboardPage
import com.keybridge.app.data.ModifierState

/**
 * KeyBridge 完整键盘视图
 */
@Composable
fun KeyBridgeKeyboard(
    onKeyAction: (KeyData) -> Unit,
    modifierState: ModifierState = ModifierState(),
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val pages = KeyboardLayout.pages
    val pageTitles = KeyboardPage.entries.map { it.title }

    Surface(
        modifier = modifier.fillMaxWidth(),
        tonalElevation = 3.dp,
        shadowElevation = 8.dp,
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
        ) {
            // 页面切换标签栏
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                indicator = { tabPositions ->
                    if (selectedTab < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            height = 3.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                divider = {}
            ) {
                pageTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontSize = 13.sp,
                                fontWeight = if (selectedTab == index)
                                    FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            // 键盘内容区域（带滑动动画）
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    slideInHorizontally { if (targetState > initialState) it else -it }
                        .togetherWith(slideOutHorizontally { if (targetState > initialState) -it else it })
                },
                label = "pageTransition"
            ) { tabIndex ->
                KeyboardPageContent(
                    keys = pages[tabIndex],
                    onKeyAction = onKeyAction,
                    modifierState = modifierState
                )
            }
        }
    }
}

/**
 * 单个键盘页面内容
 */
@Composable
private fun KeyboardPageContent(
    keys: List<List<KeyData>>,
    onKeyAction: (KeyData) -> Unit,
    modifierState: ModifierState
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        keys.forEach { row ->
            KeyRow(
                keys = row,
                onKeyAction = onKeyAction,
                modifierState = modifierState
            )
        }
    }
}

/**
 * 单行按键
 */
@Composable
private fun KeyRow(
    keys: List<KeyData>,
    onKeyAction: (KeyData) -> Unit,
    modifierState: ModifierState
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        keys.forEach { keyData ->
            val isActive = when (keyData.keyCode) {
                android.view.KeyEvent.KEYCODE_SHIFT_LEFT,
                android.view.KeyEvent.KEYCODE_SHIFT_RIGHT ->
                    modifierState.isLocked(ModifierState.Modifier.SHIFT)
                android.view.KeyEvent.KEYCODE_CTRL_LEFT,
                android.view.KeyEvent.KEYCODE_CTRL_RIGHT ->
                    modifierState.isLocked(ModifierState.Modifier.CTRL)
                android.view.KeyEvent.KEYCODE_ALT_LEFT,
                android.view.KeyEvent.KEYCODE_ALT_RIGHT ->
                    modifierState.isLocked(ModifierState.Modifier.ALT)
                android.view.KeyEvent.KEYCODE_META_LEFT,
                android.view.KeyEvent.KEYCODE_META_RIGHT ->
                    modifierState.isLocked(ModifierState.Modifier.META)
                else -> false
            }

            KeyButton(
                label = keyData.label,
                type = keyData.type,
                isActive = isActive,
                widthWeight = keyData.widthWeight,
                onClick = { onKeyAction(keyData) },
                modifier = Modifier.weight(keyData.widthWeight)
            )
        }
    }
}
