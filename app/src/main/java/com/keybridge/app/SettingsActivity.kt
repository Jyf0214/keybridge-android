package com.keybridge.app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.keybridge.app.ui.theme.KeyBridgeTheme

/**
 * 检测输入法是否已启用
 */
private fun isImeEnabled(context: Context): Boolean {
    val imeManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    return imeManager.enabledInputMethodList.any {
        it.packageName == context.packageName
    }
}

/**
 * 获取当前正在使用的输入法组件名
 * 例如: "com.keybridge.app/com.keybridge.app.ime.KeyBridgeIME"
 */
private fun getCurrentIme(context: Context): String {
    return Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.DEFAULT_INPUT_METHOD
    ) ?: ""
}

/**
 * 检测当前输入法是否为 KeyBridge
 */
private fun isKeyBridgeActive(context: Context): Boolean {
    val current = getCurrentIme(context)
    return current.startsWith("${context.packageName}/")
}

/**
 * 获取当前输入法的简短显示名称
 */
private fun getCurrentImeName(context: Context): String {
    val current = getCurrentIme(context)
    if (current.isEmpty()) return "无"
    val parts = current.split("/")
    if (parts.size < 2) return current
    val packageName = parts[0]
    // 尝试获取应用标签
    return try {
        val pm = context.packageManager
        val appInfo = pm.getApplicationInfo(packageName, 0)
        pm.getApplicationLabel(appInfo).toString()
    } catch (e: Exception) {
        packageName
    }
}

class SettingsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KeyBridgeTheme {
                SettingsScreen()
            }
        }
    }
}

@Composable
private fun SettingsScreen() {
    val context = LocalContext.current

    val isImeEnabled = remember { mutableStateOf(isImeEnabled(context)) }
    val currentImeName = remember { mutableStateOf(getCurrentImeName(context)) }
    val isKeyBridgeActive = remember { mutableStateOf(isKeyBridgeActive(context)) }
    val showSwitchReminder = remember { mutableStateOf(false) }
    val showFirstLaunchDialog = remember { mutableStateOf(!isImeEnabled.value) }

    // 每次回到页面时刷新状态（不使用 LifecycleEventEffect，用 remember + 手动刷新）
    // 因为 remember 只执行一次，所以我们需要在关键时机手动刷新

    // 首次安装引导弹窗
    if (showFirstLaunchDialog.value) {
        FirstLaunchDialog(
            onEnableClick = {
                val intent = Intent(Settings.ACTION_INPUT_METHOD_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            },
            onDismiss = { showFirstLaunchDialog.value = false }
        )
    }

    // 未切换提醒弹窗
    if (showSwitchReminder.value) {
        SwitchReminderDialog(
            currentImeName = currentImeName.value,
            onSwitchClick = {
                showSwitchReminder.value = false
                val imeManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imeManager.showInputMethodPicker()
            },
            onDismiss = { showSwitchReminder.value = false },
            onIgnore = {
                showSwitchReminder.value = false
                context.startActivity(Intent(context, TestActivity::class.java))
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Keyboard,
            contentDescription = "KeyBridge",
            modifier = Modifier.size(72.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "KeyBridge",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "一个完整的电脑键盘输入法\n提供方向键、功能键和修饰键支持",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(40.dp))

        // 第一步：启用
        SetupCard(
            icon = Icons.Default.Settings,
            step = "第一步",
            title = "启用输入法",
            description = "在系统设置中启用 KeyBridge 输入法",
            buttonText = "打开设置",
            completed = isImeEnabled.value,
            onClick = {
                val intent = Intent(Settings.ACTION_INPUT_METHOD_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 第二步：切换（显示当前输入法名称）
        val step2Description = if (isKeyBridgeActive.value) {
            "✅ 当前输入法：${currentImeName.value}"
        } else {
            "当前输入法：${currentImeName.value}（非 KeyBridge）"
        }
        val step2Completed = isKeyBridgeActive.value

        SetupCard(
            icon = Icons.Default.Keyboard,
            step = "第二步",
            title = "切换输入法",
            description = step2Description,
            buttonText = "切换输入法",
            completed = step2Completed,
            enabled = isImeEnabled.value,
            onClick = {
                val imeManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imeManager.showInputMethodPicker()
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 第三步：开始测试（始终显示，任何输入法都能用）
        SetupCard(
            icon = Icons.Default.PlayArrow,
            step = "第三步",
            title = "现在开始吧",
            description = "进入按键检测页面，验证输入法是否正常工作",
            buttonText = "开始测试",
            completed = false,
            enabled = true,
            onClick = {
                // 检查是否切换到了 KeyBridge
                if (isKeyBridgeActive(context)) {
                    context.startActivity(Intent(context, TestActivity::class.java))
                } else {
                    // 弹窗提醒
                    currentImeName.value = getCurrentImeName(context)
                    isKeyBridgeActive.value = isKeyBridgeActive(context)
                    showSwitchReminder.value = true
                }
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 状态提示
        AnimatedVisibility(
            visible = !isImeEnabled.value,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Text(
                text = "⚠️ 请先启用 KeyBridge 输入法",
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Medium
            )
        }

        AnimatedVisibility(
            visible = isImeEnabled.value && !isKeyBridgeActive.value,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Text(
                text = "ℹ️ 已启用，但当前输入法为：${currentImeName.value}",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp
            )
        }

        AnimatedVisibility(
            visible = isKeyBridgeActive.value,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Text(
                text = "✅ KeyBridge 已启用并激活",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * 未切换输入法提醒弹窗
 */
@Composable
private fun SwitchReminderDialog(
    currentImeName: String,
    onSwitchClick: () -> Unit,
    onDismiss: () -> Unit,
    onIgnore: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = {
            Text(
                text = "未切换到 KeyBridge",
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "当前输入法为：$currentImeName",
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "建议先切换到 KeyBridge 输入法，\n以获得最佳测试效果。\n\n你也可以直接进入测试页，\n任何输入法的按键都会被检测。",
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 22.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onSwitchClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Keyboard,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.padding(4.dp))
                Text(text = "切换输入法")
            }
        },
        dismissButton = {
            TextButton(onClick = onIgnore) {
                Text(text = "直接进入测试")
            }
        }
    )
}

/**
 * 首次安装引导弹窗
 */
@Composable
private fun FirstLaunchDialog(
    onEnableClick: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Keyboard,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(
                text = "欢迎使用 KeyBridge",
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "KeyBridge 是一个完整的电脑键盘输入法，\n提供方向键、功能键和修饰键支持。",
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "使用前需要先启用输入法，\n请点击下方按钮前往设置。",
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 22.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onEnableClick()
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.padding(4.dp))
                Text(text = "前往启用输入法")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "稍后设置")
            }
        }
    )
}

@Composable
private fun SetupCard(
    icon: ImageVector,
    step: String,
    title: String,
    description: String,
    buttonText: String,
    completed: Boolean = false,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (completed)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Icon(
                imageVector = if (completed) Icons.Default.CheckCircle else icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (completed) "✅ $step" else step,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = description,
                fontSize = 13.sp,
                color = if (completed) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = enabled,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (completed) MaterialTheme.colorScheme.secondary
                    else MaterialTheme.colorScheme.primary
                )
            ) {
                Text(text = if (completed) "已完成" else buttonText)
            }
        }
    }
}
