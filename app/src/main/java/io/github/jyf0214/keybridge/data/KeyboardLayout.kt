package io.github.jyf0214.keybridge.data

import android.view.KeyEvent

/**
 * 键盘按键类型
 */
enum class KeyType {
    CHAR,       // 普通字符键
    FUNCTION,   // 功能键（Shift、Ctrl、Alt 等）
    MODIFIER,   // 修饰键（可切换锁定状态）
    NAVIGATION, // 导航键（方向键）
    SPECIAL     // 特殊键（Enter、Backspace 等）
}

/**
 * 单个按键定义
 */
data class KeyData(
    val label: String,
    val keyCode: Int,
    val type: KeyType,
    val widthWeight: Float = 1f,
    val shiftLabel: String? = null,
    val commitText: String? = null
)

/**
 * 仿机械键盘布局（单页展示，所有按键可见）
 *
 * ┌ Esc  1  2  3  4  5  6  7  8  9  0  -  =  Del  ─┐
 * │ Tab   Q  W  E  R  T  Y  U  I  O  P  [  ]   \    │
 * │ Caps   A  S  D  F  G  H  J  K  L  ;  '   Enter  │
 * │ Shift   Z  X  C  V  B  N  M  ,  .  /   Shift    │
 * │ Ctrl Alt Fn        Space        ←  ↑  ↓  →      │
 * └──────────────────────────────────────────────────┘
 */
object KeyboardLayout {

    val keyboardRows: List<List<KeyData>> = listOf(
        // Row 1: Esc + 数字行
        listOf(
            KeyData("Esc", KeyEvent.KEYCODE_ESCAPE, KeyType.SPECIAL, 1.2f),
            KeyData("1", KeyEvent.KEYCODE_1, KeyType.CHAR, 1f, shiftLabel = "!"),
            KeyData("2", KeyEvent.KEYCODE_2, KeyType.CHAR, 1f, shiftLabel = "@"),
            KeyData("3", KeyEvent.KEYCODE_3, KeyType.CHAR, 1f, shiftLabel = "#"),
            KeyData("4", KeyEvent.KEYCODE_4, KeyType.CHAR, 1f, shiftLabel = "$"),
            KeyData("5", KeyEvent.KEYCODE_5, KeyType.CHAR, 1f, shiftLabel = "%"),
            KeyData("6", KeyEvent.KEYCODE_6, KeyType.CHAR, 1f, shiftLabel = "^"),
            KeyData("7", KeyEvent.KEYCODE_7, KeyType.CHAR, 1f, shiftLabel = "&"),
            KeyData("8", KeyEvent.KEYCODE_8, KeyType.CHAR, 1f, shiftLabel = "*"),
            KeyData("9", KeyEvent.KEYCODE_9, KeyType.CHAR, 1f, shiftLabel = "("),
            KeyData("0", KeyEvent.KEYCODE_0, KeyType.CHAR, 1f, shiftLabel = ")"),
            KeyData("-", KeyEvent.KEYCODE_MINUS, KeyType.CHAR, 1f, commitText = "-"),
            KeyData("=", KeyEvent.KEYCODE_EQUALS, KeyType.CHAR, 1f, commitText = "="),
            KeyData("Del", KeyEvent.KEYCODE_DEL, KeyType.SPECIAL, 1.8f)
        ),
        // Row 2: QWERTY
        listOf(
            KeyData("Tab", KeyEvent.KEYCODE_TAB, KeyType.SPECIAL, 1.5f),
            KeyData("Q", KeyEvent.KEYCODE_Q, KeyType.CHAR),
            KeyData("W", KeyEvent.KEYCODE_W, KeyType.CHAR),
            KeyData("E", KeyEvent.KEYCODE_E, KeyType.CHAR),
            KeyData("R", KeyEvent.KEYCODE_R, KeyType.CHAR),
            KeyData("T", KeyEvent.KEYCODE_T, KeyType.CHAR),
            KeyData("Y", KeyEvent.KEYCODE_Y, KeyType.CHAR),
            KeyData("U", KeyEvent.KEYCODE_U, KeyType.CHAR),
            KeyData("I", KeyEvent.KEYCODE_I, KeyType.CHAR),
            KeyData("O", KeyEvent.KEYCODE_O, KeyType.CHAR),
            KeyData("P", KeyEvent.KEYCODE_P, KeyType.CHAR),
            KeyData("[", KeyEvent.KEYCODE_LEFT_BRACKET, KeyType.CHAR, 1f, commitText = "["),
            KeyData("]", KeyEvent.KEYCODE_RIGHT_BRACKET, KeyType.CHAR, 1f, commitText = "]"),
            KeyData("\\", KeyEvent.KEYCODE_BACKSLASH, KeyType.CHAR, 1.3f, commitText = "\\")
        ),
        // Row 3: Home 行
        listOf(
            KeyData("Caps", KeyEvent.KEYCODE_CAPS_LOCK, KeyType.SPECIAL, 1.8f),
            KeyData("A", KeyEvent.KEYCODE_A, KeyType.CHAR),
            KeyData("S", KeyEvent.KEYCODE_S, KeyType.CHAR),
            KeyData("D", KeyEvent.KEYCODE_D, KeyType.CHAR),
            KeyData("F", KeyEvent.KEYCODE_F, KeyType.CHAR),
            KeyData("G", KeyEvent.KEYCODE_G, KeyType.CHAR),
            KeyData("H", KeyEvent.KEYCODE_H, KeyType.CHAR),
            KeyData("J", KeyEvent.KEYCODE_J, KeyType.CHAR),
            KeyData("K", KeyEvent.KEYCODE_K, KeyType.CHAR),
            KeyData("L", KeyEvent.KEYCODE_L, KeyType.CHAR),
            KeyData(";", KeyEvent.KEYCODE_SEMICOLON, KeyType.CHAR, commitText = ";"),
            KeyData("'", KeyEvent.KEYCODE_APOSTROPHE, KeyType.CHAR, commitText = "'"),
            KeyData("Enter", KeyEvent.KEYCODE_ENTER, KeyType.SPECIAL, 2.2f)
        ),
        // Row 4: Shift 行
        listOf(
            KeyData("Shift", KeyEvent.KEYCODE_SHIFT_LEFT, KeyType.MODIFIER, 2.2f),
            KeyData("Z", KeyEvent.KEYCODE_Z, KeyType.CHAR),
            KeyData("X", KeyEvent.KEYCODE_X, KeyType.CHAR),
            KeyData("C", KeyEvent.KEYCODE_C, KeyType.CHAR),
            KeyData("V", KeyEvent.KEYCODE_V, KeyType.CHAR),
            KeyData("B", KeyEvent.KEYCODE_B, KeyType.CHAR),
            KeyData("N", KeyEvent.KEYCODE_N, KeyType.CHAR),
            KeyData("M", KeyEvent.KEYCODE_M, KeyType.CHAR),
            KeyData(",", KeyEvent.KEYCODE_COMMA, KeyType.CHAR, commitText = ","),
            KeyData(".", KeyEvent.KEYCODE_PERIOD, KeyType.CHAR, commitText = "."),
            KeyData("/", KeyEvent.KEYCODE_SLASH, KeyType.CHAR, commitText = "/"),
            KeyData("Shift", KeyEvent.KEYCODE_SHIFT_RIGHT, KeyType.MODIFIER, 2.2f)
        ),
        // Row 5: 底部控制行
        listOf(
            KeyData("Ctrl", KeyEvent.KEYCODE_CTRL_LEFT, KeyType.MODIFIER, 1.3f),
            KeyData("Alt", KeyEvent.KEYCODE_ALT_LEFT, KeyType.MODIFIER, 1.3f),
            KeyData("Fn", KeyEvent.KEYCODE_MENU, KeyType.FUNCTION, 1.3f),
            KeyData("Space", KeyEvent.KEYCODE_SPACE, KeyType.CHAR, 3.5f, commitText = " "),
            KeyData("←", KeyEvent.KEYCODE_DPAD_LEFT, KeyType.NAVIGATION, 0.8f),
            KeyData("↑", KeyEvent.KEYCODE_DPAD_UP, KeyType.NAVIGATION, 0.8f),
            KeyData("↓", KeyEvent.KEYCODE_DPAD_DOWN, KeyType.NAVIGATION, 0.8f),
            KeyData("→", KeyEvent.KEYCODE_DPAD_RIGHT, KeyType.NAVIGATION, 0.8f)
        )
    )
}
