package com.keybridge.app.ime

import android.inputmethodservice.InputMethodService
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
import android.widget.TextView
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
    private var tabMain: TextView? = null
    private var tabSymbols: TextView? = null
    private var tabBar: LinearLayout? = null

    override fun onCreate() {
        super.onCreate()
        modifierState = ModifierState()
        keyEventSender = KeyEventSender()
    }

    override fun onCreateInputView(): View {
        val rootView = layoutInflater.inflate(R.layout.keyboard_layout, null)

        keyboardView = rootView.findViewById(R.id.keyboard_view)
        tabMain = rootView.findViewById(R.id.tab_main)
        tabSymbols = rootView.findViewById(R.id.tab_symbols)
        val tabFunction = rootView.findViewById<TextView>(R.id.tab_function)
        tabBar = rootView.findViewById(R.id.tab_bar)

        // 设置标签页点击
        tabMain?.setOnClickListener { switchPage(0) }
        tabSymbols?.setOnClickListener { switchPage(1) }
        tabFunction?.setOnClickListener { switchPage(2) }

        // 设置按键点击回调
        keyboardView?.onKeyClick = ::handleKeyAction

        // 默认显示主键盘
        keyboardView?.setKeyboardLayout(KeyboardLayout.mainPage)

        return rootView
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

    private fun switchPage(pageIndex: Int) {
        val pages = listOf(KeyboardLayout.mainPage, KeyboardLayout.symbolsPage, KeyboardLayout.functionPage)
        keyboardView?.setPage(pageIndex)
        keyboardView?.setKeyboardLayout(pages[pageIndex])

        // 更新标签样式
        val tabs = listOf(tabMain, tabSymbols, tabBar?.findViewById(R.id.tab_function))
        tabs.forEachIndexed { index, tab ->
            tab?.setTextColor(
                if (index == pageIndex) android.graphics.Color.parseColor("#1565C0")
                else android.graphics.Color.parseColor("#666666")
            )
            tab?.setTypeface(null, if (index == pageIndex) android.graphics.Typeface.BOLD else android.graphics.Typeface.NORMAL)
        }
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
                if (keyData.commitText != null) {
                    keyEventSender.sendChar(inputConnection, keyData.commitText.first())
                } else {
                    val isShiftActive = modifierState.isActive(ModifierState.Modifier.SHIFT)
                    val metaState = modifierState.getMetaState()

                    if (isShiftActive && keyData.shiftLabel != null) {
                        keyEventSender.sendChar(inputConnection, keyData.shiftLabel.first(), metaState)
                    } else if (isShiftActive) {
                        val char = getKeyLabel(keyData.keyCode).firstOrNull()
                        if (char != null) keyEventSender.sendChar(inputConnection, char.uppercaseChar(), metaState)
                    } else {
                        val char = getKeyLabel(keyData.keyCode).firstOrNull()
                        if (char != null) keyEventSender.sendChar(inputConnection, char.lowercaseChar(), metaState)
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
                val metaState = modifierState.getMetaState()
                val handled = keyEventSender.handleModifierCombo(
                    inputConnection,
                    keyData.keyCode,
                    modifierState.isActive(ModifierState.Modifier.CTRL),
                    modifierState.isActive(ModifierState.Modifier.ALT)
                )
                if (!handled) {
                    keyEventSender.sendKeyEvent(inputConnection, keyData.keyCode, metaState)
                }
                if (keyData.keyCode == android.view.KeyEvent.KEYCODE_ENTER) {
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
