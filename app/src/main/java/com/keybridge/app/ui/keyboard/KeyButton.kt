package com.keybridge.app.ui.keyboard

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.keybridge.app.data.KeyType

/**
 * 单个键盘按键组件
 * 带按压缩放动画和触觉反馈
 */
@Composable
fun KeyButton(
    label: String,
    type: KeyType,
    isActive: Boolean = false,
    widthWeight: Float = 1f,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var pressed by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    // 按压缩放动画
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.9f else 1f,
        label = "keyScale"
    )

    // 根据按键类型选择颜色
    val backgroundColor = when {
        isActive -> MaterialTheme.colorScheme.primary
        type == KeyType.MODIFIER -> MaterialTheme.colorScheme.secondaryContainer
        type == KeyType.SPECIAL -> MaterialTheme.colorScheme.primaryContainer
        type == KeyType.NAVIGATION -> MaterialTheme.colorScheme.tertiaryContainer
        type == KeyType.FUNCTION -> MaterialTheme.colorScheme.surfaceVariant
        else -> MaterialTheme.colorScheme.surface
    }

    val textColor = when {
        isActive -> MaterialTheme.colorScheme.onPrimary
        type == KeyType.MODIFIER -> MaterialTheme.colorScheme.onSecondaryContainer
        type == KeyType.SPECIAL -> MaterialTheme.colorScheme.onPrimaryContainer
        type == KeyType.NAVIGATION -> MaterialTheme.colorScheme.onTertiaryContainer
        type == KeyType.FUNCTION -> MaterialTheme.colorScheme.onSurfaceVariant
        else -> MaterialTheme.colorScheme.onSurface
    }

    val fontSize = when {
        type == KeyType.NAVIGATION -> 16.sp
        label.length > 3 -> 11.sp
        label.length > 2 -> 12.sp
        else -> 14.sp
    }

    Box(
        modifier = modifier
            .padding(horizontal = 2.dp, vertical = 2.dp)
            .scale(scale)
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        pressed = true
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        tryAwaitRelease()
                        pressed = false
                    },
                    onTap = { onClick() }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = fontSize,
            fontWeight = if (type == KeyType.MODIFIER || type == KeyType.SPECIAL)
                FontWeight.Bold else FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 10.dp)
        )
    }
}
