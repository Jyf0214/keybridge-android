package com.keybridge.app

import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.keybridge.app.ui.theme.KeyBridgeTheme

/**
 * 按键检测日志条目
 */
data class KeyEventLog(
    val action: String,       // "按下" / "释放" / "组合"
    val keyName: String,      // 按键名称
    val keyCode: Int,         // KeyEvent 码
    val modifiers: List<String>, // 修饰键
    val isHardware: Boolean   // 是否来自外接键盘
)

class TestActivity : ComponentActivity() {

    private val keyLogs = mutableStateListOf<KeyEventLog>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KeyBridgeTheme {
                TestScreen(
                    keyLogs = keyLogs,
                    onBack = { finish() }
                )
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        val log = KeyEventLog(
            action = "按下",
            keyName = getKeyName(keyCode),
            keyCode = keyCode,
            modifiers = getModifiers(event),
            isHardware = event?.deviceId != 0 && event?.device?.isVirtual != true
        )
        keyLogs.add(0, log)
        if (keyLogs.size > 100) keyLogs.removeAt(keyLogs.lastIndex)
        return super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        // 只记录特殊键的释放
        if (isSpecialKey(keyCode)) {
            val log = KeyEventLog(
                action = "释放",
                keyName = getKeyName(keyCode),
                keyCode = keyCode,
                modifiers = getModifiers(event),
                isHardware = event?.deviceId != 0 && event?.device?.isVirtual != true
            )
            keyLogs.add(0, log)
            if (keyLogs.size > 100) keyLogs.removeAt(keyLogs.lastIndex)
        }
        return super.onKeyUp(keyCode, event)
    }

    override fun onKeyMultiple(keyCode: Int, repeatCount: Int, event: KeyEvent?): Boolean {
        val log = KeyEventLog(
            action = "长按 ×$repeatCount",
            keyName = getKeyName(keyCode),
            keyCode = keyCode,
            modifiers = getModifiers(event),
            isHardware = event?.deviceId != 0 && event?.device?.isVirtual != true
        )
        keyLogs.add(0, log)
        if (keyLogs.size > 100) keyLogs.removeAt(keyLogs.lastIndex)
        return super.onKeyMultiple(keyCode, repeatCount, event)
    }

    private fun getModifiers(event: KeyEvent?): List<String> {
        val mods = mutableListOf<String>()
        if (event == null) return mods
        if (event.isCtrlPressed) mods.add("Ctrl")
        if (event.isShiftPressed) mods.add("Shift")
        if (event.isAltPressed) mods.add("Alt")
        if (event.isMetaPressed) mods.add("Meta")
        return mods
    }

    private fun isSpecialKey(keyCode: Int): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_DEL, KeyEvent.KEYCODE_TAB,
            KeyEvent.KEYCODE_ESCAPE, KeyEvent.KEYCODE_SPACE,
            KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_DPAD_DOWN,
            KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.KEYCODE_DPAD_RIGHT,
            KeyEvent.KEYCODE_MOVE_HOME, KeyEvent.KEYCODE_MOVE_END,
            KeyEvent.KEYCODE_PAGE_UP, KeyEvent.KEYCODE_PAGE_DOWN,
            KeyEvent.KEYCODE_INSERT, KeyEvent.KEYCODE_FORWARD_DEL,
            KeyEvent.KEYCODE_CAPS_LOCK, KeyEvent.KEYCODE_NUM_LOCK,
            in KeyEvent.KEYCODE_F1..KeyEvent.KEYCODE_F12,
            in KeyEvent.KEYCODE_CTRL_LEFT..KeyEvent.KEYCODE_META_RIGHT -> true
            else -> false
        }
    }

    private fun getKeyName(keyCode: Int): String {
        return when (keyCode) {
            KeyEvent.KEYCODE_A -> "A"  ; KeyEvent.KEYCODE_B -> "B"
            KeyEvent.KEYCODE_C -> "C"  ; KeyEvent.KEYCODE_D -> "D"
            KeyEvent.KEYCODE_E -> "E"  ; KeyEvent.KEYCODE_F -> "F"
            KeyEvent.KEYCODE_G -> "G"  ; KeyEvent.KEYCODE_H -> "H"
            KeyEvent.KEYCODE_I -> "I"  ; KeyEvent.KEYCODE_J -> "J"
            KeyEvent.KEYCODE_K -> "K"  ; KeyEvent.KEYCODE_L -> "L"
            KeyEvent.KEYCODE_M -> "M"  ; KeyEvent.KEYCODE_N -> "N"
            KeyEvent.KEYCODE_O -> "O"  ; KeyEvent.KEYCODE_P -> "P"
            KeyEvent.KEYCODE_Q -> "Q"  ; KeyEvent.KEYCODE_R -> "R"
            KeyEvent.KEYCODE_S -> "S"  ; KeyEvent.KEYCODE_T -> "T"
            KeyEvent.KEYCODE_U -> "U"  ; KeyEvent.KEYCODE_V -> "V"
            KeyEvent.KEYCODE_W -> "W"  ; KeyEvent.KEYCODE_X -> "X"
            KeyEvent.KEYCODE_Y -> "Y"  ; KeyEvent.KEYCODE_Z -> "Z"
            KeyEvent.KEYCODE_0 -> "0"  ; KeyEvent.KEYCODE_1 -> "1"
            KeyEvent.KEYCODE_2 -> "2"  ; KeyEvent.KEYCODE_3 -> "3"
            KeyEvent.KEYCODE_4 -> "4"  ; KeyEvent.KEYCODE_5 -> "5"
            KeyEvent.KEYCODE_6 -> "6"  ; KeyEvent.KEYCODE_7 -> "7"
            KeyEvent.KEYCODE_8 -> "8"  ; KeyEvent.KEYCODE_9 -> "9"
            KeyEvent.KEYCODE_ENTER -> "Enter ⏎"
            KeyEvent.KEYCODE_DEL -> "Backspace ⌫"
            KeyEvent.KEYCODE_FORWARD_DEL -> "Delete Del"
            KeyEvent.KEYCODE_TAB -> "Tab ⇥"
            KeyEvent.KEYCODE_ESCAPE -> "Esc"
            KeyEvent.KEYCODE_SPACE -> "Space ␣"
            KeyEvent.KEYCODE_DPAD_UP -> "↑"
            KeyEvent.KEYCODE_DPAD_DOWN -> "↓"
            KeyEvent.KEYCODE_DPAD_LEFT -> "←"
            KeyEvent.KEYCODE_DPAD_RIGHT -> "→"
            KeyEvent.KEYCODE_MOVE_HOME -> "Home"
            KeyEvent.KEYCODE_MOVE_END -> "End"
            KeyEvent.KEYCODE_PAGE_UP -> "Page Up"
            KeyEvent.KEYCODE_PAGE_DOWN -> "Page Down"
            KeyEvent.KEYCODE_INSERT -> "Insert"
            KeyEvent.KEYCODE_CAPS_LOCK -> "Caps Lock"
            KeyEvent.KEYCODE_SHIFT_LEFT -> "Shift"
            KeyEvent.KEYCODE_SHIFT_RIGHT -> "Shift"
            KeyEvent.KEYCODE_CTRL_LEFT -> "Ctrl"
            KeyEvent.KEYCODE_CTRL_RIGHT -> "Ctrl"
            KeyEvent.KEYCODE_ALT_LEFT -> "Alt"
            KeyEvent.KEYCODE_ALT_RIGHT -> "Alt"
            KeyEvent.KEYCODE_META_LEFT -> "Meta"
            KeyEvent.KEYCODE_META_RIGHT -> "Meta"
            KeyEvent.KEYCODE_COMMA -> ","
            KeyEvent.KEYCODE_PERIOD -> "."
            KeyEvent.KEYCODE_SLASH -> "/"
            KeyEvent.KEYCODE_BACKSLASH -> "\\"
            KeyEvent.KEYCODE_SEMICOLON -> ";"
            KeyEvent.KEYCODE_APOSTROPHE -> "'"
            KeyEvent.KEYCODE_LEFT_BRACKET -> "["
            KeyEvent.KEYCODE_RIGHT_BRACKET -> "]"
            KeyEvent.KEYCODE_GRAVE -> "`"
            KeyEvent.KEYCODE_MINUS -> "-"
            KeyEvent.KEYCODE_EQUALS -> "="
            in KeyEvent.KEYCODE_F1..KeyEvent.KEYCODE_F12 -> "F${keyCode - KeyEvent.KEYCODE_F1 + 1}"
            else -> "Key($keyCode)"
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TestScreen(
    keyLogs: List<KeyEventLog>,
    onBack: () -> Unit
) {
    val listState = rememberLazyListState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("按键检测") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // 状态提示
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "请在下方输入框中输入，检测所有按键和组合键",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 输入框（点击后弹出键盘，Activity 的 onKeyDown 会捕获所有按键）
            OutlinedTextField(
                value = "",
                onValueChange = {},
                modifier = Modifier.fillMaxWidth(),
                label = { Text("点击此处开始输入测试") },
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = FontFamily.Monospace
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 检测结果标题
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "检测记录",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "共 ${keyLogs.size} 条",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 检测结果列表
            if (keyLogs.isEmpty()) {
                // 空状态
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Keyboard,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "还没有检测到按键",
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            fontSize = 14.sp
                        )
                        Text(
                            text = "请在输入框中按键或连接蓝牙键盘",
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            fontSize = 12.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(keyLogs) { log ->
                        KeyEventLogItem(log)
                    }
                }
            }
        }
    }
}

@Composable
private fun KeyEventLogItem(log: KeyEventLog) {
    val backgroundColor = when {
        log.isHardware -> Color(0xFFE8F5E9) // 绿色 - 外接键盘
        log.modifiers.isNotEmpty() -> Color(0xFFFFF3E0) // 橙色 - 组合键
        else -> Color(0xFFF5F5F5) // 灰色 - 普通按键
    }

    val actionColor = when (log.action) {
        "按下" -> Color(0xFF1565C0)
        "释放" -> Color(0xFF666666)
        else -> Color(0xFFE65100)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(backgroundColor)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 动作标签
        Text(
            text = log.action,
            fontSize = 11.sp,
            color = actionColor,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(48.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        // 修饰键
        if (log.modifiers.isNotEmpty()) {
            Text(
                text = log.modifiers.joinToString("+") + "+",
                fontSize = 12.sp,
                color = Color(0xFFE65100),
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }

        // 按键名称
        Text(
            text = log.keyName,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.weight(1f))

        // 设备来源
        if (log.isHardware) {
            Text(
                text = "⌨ 蓝牙",
                fontSize = 11.sp,
                color = Color(0xFF2E7D32),
                fontWeight = FontWeight.Medium
            )
        }

        // KeyCode
        Text(
            text = "#${log.keyCode}",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontFamily = FontFamily.Monospace
        )
    }
}
