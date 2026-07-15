package com.keybridge.app.event

import android.view.KeyEvent

/**
 * IME 事件数据
 */
data class KeyEventLogEntry(
    val action: String,
    val keyName: String,
    val keyCode: Int,
    val modifiers: List<String>,
    val isHardware: Boolean
)

/**
 * IME 事件日志单例
 * IME 写入，TestActivity 读取，同进程共享内存
 */
object EventLog {

    private val listeners = mutableListOf<() -> Unit>()

    fun addListener(listener: () -> Unit) {
        synchronized(listeners) { listeners.add(listener) }
    }

    fun removeListener(listener: () -> Unit) {
        synchronized(listeners) { listeners.remove(listener) }
    }

    private fun notifyListeners() {
        synchronized(listeners) { listeners.toList() }.forEach { it() }
    }

    @Synchronized
    fun logKey(action: String, keyCode: Int, metaState: Int) {
        val entry = KeyEventLogEntry(
            action = action,
            keyName = resolveKeyName(keyCode),
            keyCode = keyCode,
            modifiers = resolveModifiers(metaState),
            isHardware = false
        )
        synchronized(imeEvents) {
            imeEvents.add(0, entry)
            if (imeEvents.size > 200) imeEvents.removeAt(imeEvents.lastIndex)
        }
        notifyListeners()
    }

    @Synchronized
    fun logChar(char: Char) {
        val entry = KeyEventLogEntry(
            action = "输入",
            keyName = "'$char'",
            keyCode = char.code,
            modifiers = emptyList(),
            isHardware = false
        )
        synchronized(imeEvents) {
            imeEvents.add(0, entry)
            if (imeEvents.size > 200) imeEvents.removeAt(imeEvents.lastIndex)
        }
        notifyListeners()
    }

    @Synchronized
    fun snapshot(): List<KeyEventLogEntry> {
        synchronized(imeEvents) { return ArrayList(imeEvents) }
    }

    private val imeEvents = mutableListOf<KeyEventLogEntry>()

    private fun resolveKeyName(keyCode: Int): String {
        return when (keyCode) {
            KeyEvent.KEYCODE_A -> "A"; KeyEvent.KEYCODE_B -> "B"
            KeyEvent.KEYCODE_C -> "C"; KeyEvent.KEYCODE_D -> "D"
            KeyEvent.KEYCODE_E -> "E"; KeyEvent.KEYCODE_F -> "F"
            KeyEvent.KEYCODE_G -> "G"; KeyEvent.KEYCODE_H -> "H"
            KeyEvent.KEYCODE_I -> "I"; KeyEvent.KEYCODE_J -> "J"
            KeyEvent.KEYCODE_K -> "K"; KeyEvent.KEYCODE_L -> "L"
            KeyEvent.KEYCODE_M -> "M"; KeyEvent.KEYCODE_N -> "N"
            KeyEvent.KEYCODE_O -> "O"; KeyEvent.KEYCODE_P -> "P"
            KeyEvent.KEYCODE_Q -> "Q"; KeyEvent.KEYCODE_R -> "R"
            KeyEvent.KEYCODE_S -> "S"; KeyEvent.KEYCODE_T -> "T"
            KeyEvent.KEYCODE_U -> "U"; KeyEvent.KEYCODE_V -> "V"
            KeyEvent.KEYCODE_W -> "W"; KeyEvent.KEYCODE_X -> "X"
            KeyEvent.KEYCODE_Y -> "Y"; KeyEvent.KEYCODE_Z -> "Z"
            KeyEvent.KEYCODE_0 -> "0"; KeyEvent.KEYCODE_1 -> "1"
            KeyEvent.KEYCODE_2 -> "2"; KeyEvent.KEYCODE_3 -> "3"
            KeyEvent.KEYCODE_4 -> "4"; KeyEvent.KEYCODE_5 -> "5"
            KeyEvent.KEYCODE_6 -> "6"; KeyEvent.KEYCODE_7 -> "7"
            KeyEvent.KEYCODE_8 -> "8"; KeyEvent.KEYCODE_9 -> "9"
            KeyEvent.KEYCODE_ENTER -> "Enter ⏎"
            KeyEvent.KEYCODE_DEL -> "Backspace ⌫"
            KeyEvent.KEYCODE_FORWARD_DEL -> "Delete"
            KeyEvent.KEYCODE_TAB -> "Tab ⇥"
            KeyEvent.KEYCODE_ESCAPE -> "Esc"
            KeyEvent.KEYCODE_SPACE -> "Space"
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
            KeyEvent.KEYCODE_SHIFT_LEFT, KeyEvent.KEYCODE_SHIFT_RIGHT -> "Shift"
            KeyEvent.KEYCODE_CTRL_LEFT, KeyEvent.KEYCODE_CTRL_RIGHT -> "Ctrl"
            KeyEvent.KEYCODE_ALT_LEFT, KeyEvent.KEYCODE_ALT_RIGHT -> "Alt"
            KeyEvent.KEYCODE_META_LEFT, KeyEvent.KEYCODE_META_RIGHT -> "Meta"
            KeyEvent.KEYCODE_COMMA -> ","; KeyEvent.KEYCODE_PERIOD -> "."
            KeyEvent.KEYCODE_SLASH -> "/"; KeyEvent.KEYCODE_BACKSLASH -> "\\"
            KeyEvent.KEYCODE_SEMICOLON -> ";"; KeyEvent.KEYCODE_APOSTROPHE -> "'"
            KeyEvent.KEYCODE_LEFT_BRACKET -> "["; KeyEvent.KEYCODE_RIGHT_BRACKET -> "]"
            KeyEvent.KEYCODE_GRAVE -> "`"; KeyEvent.KEYCODE_MINUS -> "-"
            KeyEvent.KEYCODE_EQUALS -> "="
            KeyEvent.KEYCODE_F1 -> "F1"; KeyEvent.KEYCODE_F2 -> "F2"
            KeyEvent.KEYCODE_F3 -> "F3"; KeyEvent.KEYCODE_F4 -> "F4"
            KeyEvent.KEYCODE_F5 -> "F5"; KeyEvent.KEYCODE_F6 -> "F6"
            KeyEvent.KEYCODE_F7 -> "F7"; KeyEvent.KEYCODE_F8 -> "F8"
            KeyEvent.KEYCODE_F9 -> "F9"; KeyEvent.KEYCODE_F10 -> "F10"
            KeyEvent.KEYCODE_F11 -> "F11"; KeyEvent.KEYCODE_F12 -> "F12"
            else -> "Key($keyCode)"
        }
    }

    private fun resolveModifiers(metaState: Int): List<String> {
        val mods = mutableListOf<String>()
        if (metaState and KeyEvent.META_CTRL_ON != 0) mods.add("Ctrl")
        if (metaState and KeyEvent.META_SHIFT_ON != 0) mods.add("Shift")
        if (metaState and KeyEvent.META_ALT_ON != 0) mods.add("Alt")
        if (metaState and KeyEvent.META_META_ON != 0) mods.add("Meta")
        return mods
    }
}
