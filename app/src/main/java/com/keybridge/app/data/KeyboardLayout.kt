package com.keybridge.app.data

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
    val shiftKeyCode: Int = 0,
    val iconDescription: String? = null,
    val commitText: String? = null    // 直接提交文本（用于无 KeyEvent 的符号）
)

/**
 * 键盘页面类型
 */
enum class KeyboardPage(val title: String) {
    MAIN("主键盘"),
    SYMBOLS("符号"),
    FUNCTION("功能键")
}

/**
 * 完整键盘布局定义
 */
object KeyboardLayout {

    // ===== 主键盘页面 =====
    val mainPage = listOf(
        // 数字行
        listOf(
            KeyData("1", KeyEvent.KEYCODE_1, KeyType.CHAR, shiftLabel = "!"),
            KeyData("2", KeyEvent.KEYCODE_2, KeyType.CHAR, shiftLabel = "@"),
            KeyData("3", KeyEvent.KEYCODE_3, KeyType.CHAR, shiftLabel = "#"),
            KeyData("4", KeyEvent.KEYCODE_4, KeyType.CHAR, shiftLabel = "$"),
            KeyData("5", KeyEvent.KEYCODE_5, KeyType.CHAR, shiftLabel = "%"),
            KeyData("6", KeyEvent.KEYCODE_6, KeyType.CHAR, shiftLabel = "^"),
            KeyData("7", KeyEvent.KEYCODE_7, KeyType.CHAR, shiftLabel = "&"),
            KeyData("8", KeyEvent.KEYCODE_8, KeyType.CHAR, shiftLabel = "*"),
            KeyData("9", KeyEvent.KEYCODE_9, KeyType.CHAR, shiftLabel = "("),
            KeyData("0", KeyEvent.KEYCODE_0, KeyType.CHAR, shiftLabel = ")")
        ),
        // QWERTY 行
        listOf(
            KeyData("Q", KeyEvent.KEYCODE_Q, KeyType.CHAR),
            KeyData("W", KeyEvent.KEYCODE_W, KeyType.CHAR),
            KeyData("E", KeyEvent.KEYCODE_E, KeyType.CHAR),
            KeyData("R", KeyEvent.KEYCODE_R, KeyType.CHAR),
            KeyData("T", KeyEvent.KEYCODE_T, KeyType.CHAR),
            KeyData("Y", KeyEvent.KEYCODE_Y, KeyType.CHAR),
            KeyData("U", KeyEvent.KEYCODE_U, KeyType.CHAR),
            KeyData("I", KeyEvent.KEYCODE_I, KeyType.CHAR),
            KeyData("O", KeyEvent.KEYCODE_O, KeyType.CHAR),
            KeyData("P", KeyEvent.KEYCODE_P, KeyType.CHAR)
        ),
        // ASDF 行
        listOf(
            KeyData("A", KeyEvent.KEYCODE_A, KeyType.CHAR),
            KeyData("S", KeyEvent.KEYCODE_S, KeyType.CHAR),
            KeyData("D", KeyEvent.KEYCODE_D, KeyType.CHAR),
            KeyData("F", KeyEvent.KEYCODE_F, KeyType.CHAR),
            KeyData("G", KeyEvent.KEYCODE_G, KeyType.CHAR),
            KeyData("H", KeyEvent.KEYCODE_H, KeyType.CHAR),
            KeyData("J", KeyEvent.KEYCODE_J, KeyType.CHAR),
            KeyData("K", KeyEvent.KEYCODE_K, KeyType.CHAR),
            KeyData("L", KeyEvent.KEYCODE_L, KeyType.CHAR)
        ),
        // ZXCV 行
        listOf(
            KeyData("⇧", KeyEvent.KEYCODE_SHIFT_LEFT, KeyType.MODIFIER, widthWeight = 1.5f, iconDescription = "Shift"),
            KeyData("Z", KeyEvent.KEYCODE_Z, KeyType.CHAR),
            KeyData("X", KeyEvent.KEYCODE_X, KeyType.CHAR),
            KeyData("C", KeyEvent.KEYCODE_C, KeyType.CHAR),
            KeyData("V", KeyEvent.KEYCODE_V, KeyType.CHAR),
            KeyData("B", KeyEvent.KEYCODE_B, KeyType.CHAR),
            KeyData("N", KeyEvent.KEYCODE_N, KeyType.CHAR),
            KeyData("M", KeyEvent.KEYCODE_M, KeyType.CHAR),
            KeyData("⌫", KeyEvent.KEYCODE_DEL, KeyType.SPECIAL, widthWeight = 1.5f, iconDescription = "Backspace")
        ),
        // 底部控制行
        listOf(
            KeyData("ABC", KeyEvent.KEYCODE_LANGUAGE_SWITCH, KeyType.FUNCTION, widthWeight = 1f, iconDescription = "切换键盘"),
            KeyData(",", KeyEvent.KEYCODE_COMMA, KeyType.CHAR),
            KeyData("⏎", KeyEvent.KEYCODE_ENTER, KeyType.SPECIAL, widthWeight = 2f, iconDescription = "Enter"),
            KeyData(".", KeyEvent.KEYCODE_PERIOD, KeyType.CHAR),
            KeyData("⇧↑", KeyEvent.KEYCODE_DPAD_UP, KeyType.NAVIGATION, widthWeight = 1f, iconDescription = "上")
        )
    )

    // ===== 符号页面 =====
    val symbolsPage = listOf(
        listOf(
            KeyData("~", KeyEvent.KEYCODE_GRAVE, KeyType.CHAR, shiftLabel = "`"),
            KeyData("@", KeyEvent.KEYCODE_AT, KeyType.CHAR, commitText = "@"),
            KeyData("#", KeyEvent.KEYCODE_POUND, KeyType.CHAR, commitText = "#"),
            KeyData("$", KeyEvent.KEYCODE_UNKNOWN, KeyType.CHAR, commitText = "$"),
            KeyData("%", KeyEvent.KEYCODE_UNKNOWN, KeyType.CHAR, commitText = "%"),
            KeyData("^", KeyEvent.KEYCODE_POWER, KeyType.CHAR, commitText = "^"),
            KeyData("&", KeyEvent.KEYCODE_UNKNOWN, KeyType.CHAR, commitText = "&"),
            KeyData("*", KeyEvent.KEYCODE_STAR, KeyType.CHAR, commitText = "*"),
            KeyData("(", KeyEvent.KEYCODE_UNKNOWN, KeyType.CHAR, commitText = "("),
            KeyData(")", KeyEvent.KEYCODE_UNKNOWN, KeyType.CHAR, commitText = ")")
        ),
        listOf(
            KeyData("-", KeyEvent.KEYCODE_MINUS, KeyType.CHAR),
            KeyData("_", KeyEvent.KEYCODE_MINUS, KeyType.CHAR, shiftLabel = "_", commitText = "_"),
            KeyData("=", KeyEvent.KEYCODE_EQUALS, KeyType.CHAR),
            KeyData("+", KeyEvent.KEYCODE_EQUALS, KeyType.CHAR, shiftLabel = "+", commitText = "+"),
            KeyData("[", KeyEvent.KEYCODE_LEFT_BRACKET, KeyType.CHAR),
            KeyData("]", KeyEvent.KEYCODE_RIGHT_BRACKET, KeyType.CHAR),
            KeyData("{", KeyEvent.KEYCODE_LEFT_BRACKET, KeyType.CHAR, shiftLabel = "{", commitText = "{"),
            KeyData("}", KeyEvent.KEYCODE_RIGHT_BRACKET, KeyType.CHAR, shiftLabel = "}", commitText = "}"),
            KeyData("\\", KeyEvent.KEYCODE_BACKSLASH, KeyType.CHAR),
            KeyData("|", KeyEvent.KEYCODE_BACKSLASH, KeyType.CHAR, shiftLabel = "|", commitText = "|")
        ),
        listOf(
            KeyData(";", KeyEvent.KEYCODE_SEMICOLON, KeyType.CHAR),
            KeyData(":", KeyEvent.KEYCODE_SEMICOLON, KeyType.CHAR, shiftLabel = ":", commitText = ":"),
            KeyData("'", KeyEvent.KEYCODE_APOSTROPHE, KeyType.CHAR),
            KeyData("\"", KeyEvent.KEYCODE_APOSTROPHE, KeyType.CHAR, shiftLabel = "\"", commitText = "\""),
            KeyData("/", KeyEvent.KEYCODE_SLASH, KeyType.CHAR),
            KeyData("?", KeyEvent.KEYCODE_SLASH, KeyType.CHAR, shiftLabel = "?", commitText = "?"),
            KeyData("<", KeyEvent.KEYCODE_COMMA, KeyType.CHAR, shiftLabel = "<", commitText = "<"),
            KeyData(">", KeyEvent.KEYCODE_PERIOD, KeyType.CHAR, shiftLabel = ">", commitText = ">"),
            KeyData("!", KeyEvent.KEYCODE_UNKNOWN, KeyType.CHAR, commitText = "!"),
            KeyData("?", KeyEvent.KEYCODE_UNKNOWN, KeyType.CHAR, commitText = "?")
        ),
        listOf(
            KeyData("Tab", KeyEvent.KEYCODE_TAB, KeyType.SPECIAL, widthWeight = 2f, iconDescription = "Tab"),
            KeyData("Space", KeyEvent.KEYCODE_SPACE, KeyType.SPECIAL, widthWeight = 4f, iconDescription = "空格"),
            KeyData("Enter", KeyEvent.KEYCODE_ENTER, KeyType.SPECIAL, widthWeight = 2f, iconDescription = "Enter")
        )
    )

    // ===== 功能键页面 =====
    val functionPage = listOf(
        // F1-F4
        listOf(
            KeyData("Esc", KeyEvent.KEYCODE_ESCAPE, KeyType.SPECIAL, widthWeight = 1.5f, iconDescription = "Escape"),
            KeyData("F1", KeyEvent.KEYCODE_F1, KeyType.FUNCTION),
            KeyData("F2", KeyEvent.KEYCODE_F2, KeyType.FUNCTION),
            KeyData("F3", KeyEvent.KEYCODE_F3, KeyType.FUNCTION),
            KeyData("F4", KeyEvent.KEYCODE_F4, KeyType.FUNCTION)
        ),
        // F5-F8
        listOf(
            KeyData("Tab", KeyEvent.KEYCODE_TAB, KeyType.SPECIAL, widthWeight = 1.5f, iconDescription = "Tab"),
            KeyData("F5", KeyEvent.KEYCODE_F5, KeyType.FUNCTION),
            KeyData("F6", KeyEvent.KEYCODE_F6, KeyType.FUNCTION),
            KeyData("F7", KeyEvent.KEYCODE_F7, KeyType.FUNCTION),
            KeyData("F8", KeyEvent.KEYCODE_F8, KeyType.FUNCTION)
        ),
        // F9-F12
        listOf(
            KeyData("Del", KeyEvent.KEYCODE_FORWARD_DEL, KeyType.SPECIAL, widthWeight = 1.5f, iconDescription = "Delete"),
            KeyData("F9", KeyEvent.KEYCODE_F9, KeyType.FUNCTION),
            KeyData("F10", KeyEvent.KEYCODE_F10, KeyType.FUNCTION),
            KeyData("F11", KeyEvent.KEYCODE_F11, KeyType.FUNCTION),
            KeyData("F12", KeyEvent.KEYCODE_F12, KeyType.FUNCTION)
        ),
        // 修饰键行
        listOf(
            KeyData("Ctrl", KeyEvent.KEYCODE_CTRL_LEFT, KeyType.MODIFIER, widthWeight = 1.5f, iconDescription = "Ctrl"),
            KeyData("Alt", KeyEvent.KEYCODE_ALT_LEFT, KeyType.MODIFIER, widthWeight = 1.5f, iconDescription = "Alt"),
            KeyData("Shift", KeyEvent.KEYCODE_SHIFT_LEFT, KeyType.MODIFIER, widthWeight = 1.5f, iconDescription = "Shift"),
            KeyData("Meta", KeyEvent.KEYCODE_META_LEFT, KeyType.MODIFIER, widthWeight = 1.5f, iconDescription = "Meta")
        ),
        // 导航键行
        listOf(
            KeyData("Home", KeyEvent.KEYCODE_MOVE_HOME, KeyType.SPECIAL, iconDescription = "Home"),
            KeyData("↑", KeyEvent.KEYCODE_DPAD_UP, KeyType.NAVIGATION, iconDescription = "上"),
            KeyData("End", KeyEvent.KEYCODE_MOVE_END, KeyType.SPECIAL, iconDescription = "End"),
            KeyData("Enter", KeyEvent.KEYCODE_ENTER, KeyType.SPECIAL, iconDescription = "Enter")
        ),
        listOf(
            KeyData("PgUp", KeyEvent.KEYCODE_PAGE_UP, KeyType.SPECIAL, iconDescription = "Page Up"),
            KeyData("←", KeyEvent.KEYCODE_DPAD_LEFT, KeyType.NAVIGATION, iconDescription = "左"),
            KeyData("↓", KeyEvent.KEYCODE_DPAD_DOWN, KeyType.NAVIGATION, iconDescription = "下"),
            KeyData("→", KeyEvent.KEYCODE_DPAD_RIGHT, KeyType.NAVIGATION, iconDescription = "右"),
            KeyData("PgDn", KeyEvent.KEYCODE_PAGE_DOWN, KeyType.SPECIAL, iconDescription = "Page Down")
        ),
        // 空格行
        listOf(
            KeyData("Insert", KeyEvent.KEYCODE_INSERT, KeyType.SPECIAL, iconDescription = "Insert"),
            KeyData("Space", KeyEvent.KEYCODE_SPACE, KeyType.SPECIAL, widthWeight = 5f, iconDescription = "空格"),
            KeyData("Bksp", KeyEvent.KEYCODE_DEL, KeyType.SPECIAL, iconDescription = "Backspace")
        )
    )

    val pages = listOf(mainPage, symbolsPage, functionPage)
}
