package com.keybridge.app.ime

import android.inputmethodservice.InputMethodService
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.keybridge.app.R
import com.keybridge.app.data.KeyData
import com.keybridge.app.data.KeyType
import com.keybridge.app.data.KeyboardLayout
import com.keybridge.app.data.ModifierState

/**
 * KeyBridge 输入法核心服务
 */
class KeyBridgeIME : InputMethodService() {

    private lateinit var modifierState: ModifierState
    private lateinit var keyEventSender: KeyEventSender
    private var keyboardView: KeyboardView? = null
    private var rootLayout: LinearLayout? = null

    override fun onCreate() {
        super.onCreate()
        modifierState = ModifierState()
        keyEventSender = KeyEventSender()
    }

    override fun onCreateInputView(): View {
        val rootView = layoutInflater.inflate(R.layout.keyboard_layout, null)
        rootLayout = rootView.findViewById(R.id.keyboard_root)

        keyboardView = rootView.findViewById(R.id.keyboard_view)
        val tabMain = rootView.findViewById<TextView>(R.id.tab_main)
        val tabSymbols = rootView.findViewById<TextView>(R.id.tab_symbols)
        val tabFunction = rootView.findViewById<TextView>(R.id.tab_function)

        // 现在不需要标签页了，隐藏标签栏
        rootView.findViewById<LinearLayout>(R.id.tab_bar)?.visibility = View.GONE

        // 设置按键点击回调
        keyboardView?.onKeyClick = ::handleKeyAction

        // 显示键盘
        keyboardView?.setKeyboardLayout(KeyboardLayout.keyboardRows)

        // 应用系统安全区域 insets（圆角、导航栏）
        applySystemInsets(rootView)

        return rootView
    }

    /**
     * 读取系统圆角和导航栏高度，应用为底部 padding
     */
    private fun applySystemInsets(view: View) {
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val displayCutout = insets.getInsets(WindowInsetsCompat.Type.displayCutout())

            val bottomInset = maxOf(systemBars.bottom, displayCutout.bottom)
            val leftInset = maxOf(systemBars.left, displayCutout.left)
            val rightInset = maxOf(systemBars.right, displayCutout.right)
            val topInset = maxOf(systemBars.top, displayCutout.top)

            v.setPadding(leftInset, topInset, rightInset, bottomInset)

            insets
        }
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        if (!restarting) {
            modifierState.reset()
            keyboardView?.setActiveModifiers(emptySet())
        }
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        super.onFinishInputView(finishingInput)
        modifierState.reset()
    }

    /**
     * 处理所有按键动作
     */
    private fun handleKeyAction(keyData: KeyData) {
        val inputConnection = currentInputConnection

        when (keyData.type) {
            KeyType.MODIFIER -> {
                when (keyData.keyCode) {
                    android.view.KeyEvent.KEYCODE_SHIFT_LEFT,
                    android.view.KeyEvent.KEYCODE_SHIFT_RIGHT ->
                        modifierState.toggleLock(ModifierState.Modifier.SHIFT)
                    android.view.KeyEvent.KEYCODE_CTRL_LEFT,
                    android.view.KeyEvent.KEYCODE_CTRL_RIGHT ->
                        modifierState.toggleLock(ModifierState.Modifier.CTRL)
                    android.view.KeyEvent.KEYCODE_ALT_LEFT,
                    android.view.KeyEvent.KEYCODE_ALT_RIGHT ->
                        modifierState.toggleLock(ModifierState.Modifier.ALT)
                    android.view.KeyEvent.KEYCODE_META_LEFT,
                    android.view.KeyEvent.KEYCODE_META_RIGHT ->
                        modifierState.toggleLock(ModifierState.Modifier.META)
                }
                updateModifierVisual()
            }

            KeyType.CHAR -> {
                val ctrlActive = modifierState.isActive(ModifierState.Modifier.CTRL)
                val altActive = modifierState.isActive(ModifierState.Modifier.ALT)
                val shiftActive = modifierState.isActive(ModifierState.Modifier.SHIFT)

                // 如果有任何修饰键激活，发送组合键事件（如 Ctrl+C、Alt+X）
                if (ctrlActive || altActive) {
                    keyEventSender.handleModifierCombo(
                        inputConnection, keyData.keyCode,
                        ctrlActive, altActive, shiftActive
                    )
                } else {
                    // 正常字符输入
                    if (keyData.commitText != null) {
                        val char = if (shiftActive) keyData.commitText.uppercase()
                                   else keyData.commitText
                        keyEventSender.sendChar(inputConnection, char.first())
                    } else {
                        val metaState = modifierState.getMetaState()
                        if (shiftActive && keyData.shiftLabel != null) {
                            keyEventSender.sendChar(inputConnection, keyData.shiftLabel.first(), metaState)
                        } else if (shiftActive) {
                            val char = getKeyLabel(keyData.keyCode).firstOrNull()
                            if (char != null) keyEventSender.sendChar(inputConnection, char.uppercaseChar(), metaState)
                        } else {
                            val char = getKeyLabel(keyData.keyCode).firstOrNull()
                            if (char != null) keyEventSender.sendChar(inputConnection, char.lowercaseChar(), metaState)
                        }
                    }
                }
                modifierState.onCharacterInput()
                updateModifierVisual()
            }

            KeyType.NAVIGATION -> {
                val metaState = modifierState.getMetaState()
                keyEventSender.sendKeyEvent(inputConnection, keyData.keyCode, metaState)
            }

            KeyType.SPECIAL -> {
                when (keyData.keyCode) {
                    android.view.KeyEvent.KEYCODE_ENTER -> {
                        // Enter 优先用 commitText("\n")，兼容性最好
                        val metaState = modifierState.getMetaState()
                        val handled = keyEventSender.handleModifierCombo(
                            inputConnection, keyData.keyCode,
                            modifierState.isActive(ModifierState.Modifier.CTRL),
                            modifierState.isActive(ModifierState.Modifier.ALT),
                            modifierState.isActive(ModifierState.Modifier.SHIFT)
                        )
                        if (!handled) {
                            keyEventSender.sendChar(inputConnection, '\n')
                        }
                    }
                    android.view.KeyEvent.KEYCODE_DEL -> {
                        // Backspace 用 deleteSurroundingText，最可靠
                        val metaState = modifierState.getMetaState()
                        val handled = keyEventSender.handleModifierCombo(
                            inputConnection, keyData.keyCode,
                            modifierState.isActive(ModifierState.Modifier.CTRL),
                            modifierState.isActive(ModifierState.Modifier.ALT),
                            modifierState.isActive(ModifierState.Modifier.SHIFT)
                        )
                        if (!handled) {
                            inputConnection?.deleteSurroundingText(1, 0)
                        }
                    }
                    android.view.KeyEvent.KEYCODE_FORWARD_DEL -> {
                        // Delete（向前删除）
                        val handled = keyEventSender.handleModifierCombo(
                            inputConnection, keyData.keyCode,
                            modifierState.isActive(ModifierState.Modifier.CTRL),
                            modifierState.isActive(ModifierState.Modifier.ALT),
                            modifierState.isActive(ModifierState.Modifier.SHIFT)
                        )
                        if (!handled) {
                            inputConnection?.deleteSurroundingText(0, 1)
                        }
                    }
                    else -> {
                        // 其他特殊键（Tab、Esc、CapsLock 等）
                        val metaState = modifierState.getMetaState()
                        keyEventSender.sendKeyEvent(inputConnection, keyData.keyCode, metaState)
                    }
                }
                // Enter 和 Backspace 输入后也清理临时修饰键
                if (keyData.keyCode == android.view.KeyEvent.KEYCODE_ENTER ||
                    keyData.keyCode == android.view.KeyEvent.KEYCODE_DEL) {
                    modifierState.onCharacterInput()
                    updateModifierVisual()
                }
            }

            KeyType.FUNCTION -> {
                val metaState = modifierState.getMetaState()
                keyEventSender.sendKeyEvent(inputConnection, keyData.keyCode, metaState)
            }
        }
    }

    private fun updateModifierVisual() {
        val active = mutableSetOf<Int>()
        if (modifierState.isLocked(ModifierState.Modifier.SHIFT)) {
            active.add(android.view.KeyEvent.KEYCODE_SHIFT_LEFT)
        }
        if (modifierState.isLocked(ModifierState.Modifier.CTRL)) {
            active.add(android.view.KeyEvent.KEYCODE_CTRL_LEFT)
        }
        if (modifierState.isLocked(ModifierState.Modifier.ALT)) {
            active.add(android.view.KeyEvent.KEYCODE_ALT_LEFT)
        }
        if (modifierState.isLocked(ModifierState.Modifier.META)) {
            active.add(android.view.KeyEvent.KEYCODE_META_LEFT)
        }
        keyboardView?.setActiveModifiers(active)
    }

    private fun getKeyLabel(keyCode: Int): String {
        return when (keyCode) {
            android.view.KeyEvent.KEYCODE_A -> "A"
            android.view.KeyEvent.KEYCODE_B -> "B"
            android.view.KeyEvent.KEYCODE_C -> "C"
            android.view.KeyEvent.KEYCODE_D -> "D"
            android.view.KeyEvent.KEYCODE_E -> "E"
            android.view.KeyEvent.KEYCODE_F -> "F"
            android.view.KeyEvent.KEYCODE_G -> "G"
            android.view.KeyEvent.KEYCODE_H -> "H"
            android.view.KeyEvent.KEYCODE_I -> "I"
            android.view.KeyEvent.KEYCODE_J -> "J"
            android.view.KeyEvent.KEYCODE_K -> "K"
            android.view.KeyEvent.KEYCODE_L -> "L"
            android.view.KeyEvent.KEYCODE_M -> "M"
            android.view.KeyEvent.KEYCODE_N -> "N"
            android.view.KeyEvent.KEYCODE_O -> "O"
            android.view.KeyEvent.KEYCODE_P -> "P"
            android.view.KeyEvent.KEYCODE_Q -> "Q"
            android.view.KeyEvent.KEYCODE_R -> "R"
            android.view.KeyEvent.KEYCODE_S -> "S"
            android.view.KeyEvent.KEYCODE_T -> "T"
            android.view.KeyEvent.KEYCODE_U -> "U"
            android.view.KeyEvent.KEYCODE_V -> "V"
            android.view.KeyEvent.KEYCODE_W -> "W"
            android.view.KeyEvent.KEYCODE_X -> "X"
            android.view.KeyEvent.KEYCODE_Y -> "Y"
            android.view.KeyEvent.KEYCODE_Z -> "Z"
            android.view.KeyEvent.KEYCODE_0 -> "0"
            android.view.KeyEvent.KEYCODE_1 -> "1"
            android.view.KeyEvent.KEYCODE_2 -> "2"
            android.view.KeyEvent.KEYCODE_3 -> "3"
            android.view.KeyEvent.KEYCODE_4 -> "4"
            android.view.KeyEvent.KEYCODE_5 -> "5"
            android.view.KeyEvent.KEYCODE_6 -> "6"
            android.view.KeyEvent.KEYCODE_7 -> "7"
            android.view.KeyEvent.KEYCODE_8 -> "8"
            android.view.KeyEvent.KEYCODE_9 -> "9"
            else -> ""
        }
    }
}
