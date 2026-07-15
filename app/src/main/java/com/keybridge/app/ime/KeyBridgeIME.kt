package com.keybridge.app.ime

import android.animation.ObjectAnimator
import android.inputmethodservice.InputMethodService
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.keybridge.app.R
import com.keybridge.app.data.KeyData
import com.keybridge.app.data.KeyType
import com.keybridge.app.data.KeyboardLayout
import com.keybridge.app.data.ModifierState
import com.keybridge.app.event.EventLog

/**
 * KeyBridge 输入法核心服务
 */
class KeyBridgeIME : InputMethodService() {

    private lateinit var modifierState: ModifierState
    private lateinit var keyEventSender: KeyEventSender
    private var keyboardView: KeyboardView? = null
    private var rootLayout: LinearLayout? = null

    // 工具栏
    private var toolbar: View? = null
    private var modShift: TextView? = null
    private var modCtrl: TextView? = null
    private var modAlt: TextView? = null
    private var modMeta: TextView? = null
    private var btnNavToggle: ImageView? = null

    // 页面切换
    private var isNavPage = false
    private var navPage: View? = null
    private var keyboardContainer: View? = null

    override fun onCreate() {
        super.onCreate()
        modifierState = ModifierState()
        keyEventSender = KeyEventSender()
    }

    override fun onCreateInputView(): View {
        val rootView = layoutInflater.inflate(R.layout.keyboard_layout, null)
        rootLayout = rootView.findViewById(R.id.keyboard_root)

        // 工具栏
        toolbar = rootView.findViewById(R.id.toolbar)
        modShift = rootView.findViewById(R.id.mod_shift)
        modCtrl = rootView.findViewById(R.id.mod_ctrl)
        modAlt = rootView.findViewById(R.id.mod_alt)
        modMeta = rootView.findViewById(R.id.mod_meta)
        btnNavToggle = rootView.findViewById(R.id.btn_nav_toggle)

        // 普通键盘
        keyboardView = rootView.findViewById(R.id.keyboard_view)
        keyboardView?.onKeyClick = ::handleKeyAction
        keyboardView?.setKeyboardLayout(KeyboardLayout.keyboardRows)

        // 导航页面
        navPage = rootView.findViewById(R.id.nav_page)
        setupNavPage(rootView)

        // 页面切换按钮
        btnNavToggle?.setOnClickListener { togglePage() }

        // 应用系统安全区域 insets
        applySystemInsets(rootView)

        return rootView
    }

    /**
     * 设置导航页面按钮点击事件
     */
    private fun setupNavPage(rootView: View) {
        val nav = navPage ?: return

        // 方向键
        nav.findViewById<View>(R.id.nav_up)?.setOnClickListener {
            keyEventSender.sendKeyEvent(currentInputConnection, KeyEvent.KEYCODE_DPAD_UP)
        }
        nav.findViewById<View>(R.id.nav_down)?.setOnClickListener {
            keyEventSender.sendKeyEvent(currentInputConnection, KeyEvent.KEYCODE_DPAD_DOWN)
        }
        nav.findViewById<View>(R.id.nav_left)?.setOnClickListener {
            keyEventSender.sendKeyEvent(currentInputConnection, KeyEvent.KEYCODE_DPAD_LEFT)
        }
        nav.findViewById<View>(R.id.nav_right)?.setOnClickListener {
            keyEventSender.sendKeyEvent(currentInputConnection, KeyEvent.KEYCODE_DPAD_RIGHT)
        }

        // 编辑操作
        nav.findViewById<View>(R.id.nav_undo)?.setOnClickListener {
            sendEditorAction(KeyEvent.KEYCODE_Z, ctrl = true)
        }
        nav.findViewById<View>(R.id.nav_redo)?.setOnClickListener {
            sendEditorAction(KeyEvent.KEYCODE_Z, ctrl = true, shift = true)
        }
        nav.findViewById<View>(R.id.nav_backspace)?.setOnClickListener {
            currentInputConnection?.deleteSurroundingText(1, 0)
        }
        nav.findViewById<View>(R.id.nav_cut)?.setOnClickListener {
            sendEditorAction(KeyEvent.KEYCODE_X, ctrl = true)
        }
        nav.findViewById<View>(R.id.nav_copy)?.setOnClickListener {
            sendEditorAction(KeyEvent.KEYCODE_C, ctrl = true)
        }
        nav.findViewById<View>(R.id.nav_paste)?.setOnClickListener {
            sendEditorAction(KeyEvent.KEYCODE_V, ctrl = true)
        }
        nav.findViewById<View>(R.id.nav_select_all)?.setOnClickListener {
            sendEditorAction(KeyEvent.KEYCODE_A, ctrl = true)
        }
    }

    /**
     * 发送 Ctrl/Shift 组合键
     */
    private fun sendEditorAction(keyCode: Int, ctrl: Boolean = false, shift: Boolean = false) {
        var meta = 0
        if (ctrl) meta = meta or KeyEvent.META_CTRL_ON
        if (shift) meta = meta or KeyEvent.META_SHIFT_ON
        keyEventSender.sendKeyEvent(currentInputConnection, keyCode, meta)
    }

    /**
     * 切换普通键盘 / 导航页面，带淡入淡出动效
     */
    private fun togglePage() {
        val kb = keyboardView ?: return
        val nav = navPage ?: return
        val targetAlpha: Float
        val duration = 180L

        if (isNavPage) {
            // 导航 → 普通：导航淡出，键盘淡入
            targetAlpha = 0f
            ObjectAnimator.ofFloat(nav, "alpha", 1f, 0f).apply {
                this.duration = duration
                start()
            }
            kb.postDelayed({
                nav.visibility = View.GONE
                kb.visibility = View.VISIBLE
                kb.alpha = 0f
                ObjectAnimator.ofFloat(kb, "alpha", 0f, 1f).apply {
                    this.duration = duration
                    start()
                }
            }, duration)
            btnNavToggle?.setImageResource(R.drawable.ic_nav_toggle)
        } else {
            // 普通 → 导航：键盘淡出，导航淡入
            targetAlpha = 0f
            ObjectAnimator.ofFloat(kb, "alpha", 1f, 0f).apply {
                this.duration = duration
                start()
            }
            kb.postDelayed({
                kb.visibility = View.GONE
                nav.visibility = View.VISIBLE
                nav.alpha = 0f
                ObjectAnimator.ofFloat(nav, "alpha", 0f, 1f).apply {
                    this.duration = duration
                    start()
                }
            }, duration)
            btnNavToggle?.setImageResource(R.drawable.ic_keyboard)
        }
        isNavPage = !isNavPage
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
            updateToolbarStatus()
        }
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        super.onFinishInputView(finishingInput)
        modifierState.reset()
        updateToolbarStatus()
    }

    /**
     * 处理所有按键动作
     */
    private fun handleKeyAction(keyData: KeyData) {
        val inputConnection = currentInputConnection

        when (keyData.type) {
            KeyType.MODIFIER -> {
                when (keyData.keyCode) {
                    KeyEvent.KEYCODE_SHIFT_LEFT,
                    KeyEvent.KEYCODE_SHIFT_RIGHT ->
                        modifierState.toggleLock(ModifierState.Modifier.SHIFT)
                    KeyEvent.KEYCODE_CTRL_LEFT,
                    KeyEvent.KEYCODE_CTRL_RIGHT ->
                        modifierState.toggleLock(ModifierState.Modifier.CTRL)
                    KeyEvent.KEYCODE_ALT_LEFT,
                    KeyEvent.KEYCODE_ALT_RIGHT ->
                        modifierState.toggleLock(ModifierState.Modifier.ALT)
                    KeyEvent.KEYCODE_META_LEFT,
                    KeyEvent.KEYCODE_META_RIGHT ->
                        modifierState.toggleLock(ModifierState.Modifier.META)
                }
                updateModifierVisual()
                updateToolbarStatus()
            }

            KeyType.CHAR -> {
                val ctrlActive = modifierState.isActive(ModifierState.Modifier.CTRL)
                val altActive = modifierState.isActive(ModifierState.Modifier.ALT)
                val shiftActive = modifierState.isActive(ModifierState.Modifier.SHIFT)

                if (ctrlActive || altActive) {
                    // 组合键（Ctrl+C、Alt+X 等）——记录到事件日志
                    val metaState = modifierState.getMetaState()
                    EventLog.logKey("组合键", keyData.keyCode, metaState)
                    keyEventSender.handleModifierCombo(
                        inputConnection, keyData.keyCode,
                        ctrlActive, altActive, shiftActive
                    )
                } else {
                    if (keyData.commitText != null) {
                        val char = if (shiftActive) keyData.commitText.uppercase()
                                   else keyData.commitText
                        EventLog.logChar(char.first())
                        keyEventSender.sendChar(inputConnection, char.first())
                    } else {
                        val metaState = modifierState.getMetaState()
                        if (shiftActive && keyData.shiftLabel != null) {
                            EventLog.logChar(keyData.shiftLabel.first())
                            keyEventSender.sendChar(inputConnection, keyData.shiftLabel.first(), metaState)
                        } else if (shiftActive) {
                            val char = getKeyLabel(keyData.keyCode).firstOrNull()
                            if (char != null) {
                                EventLog.logChar(char.uppercaseChar())
                                keyEventSender.sendChar(inputConnection, char.uppercaseChar(), metaState)
                            }
                        } else {
                            val char = getKeyLabel(keyData.keyCode).firstOrNull()
                            if (char != null) {
                                EventLog.logChar(char.lowercaseChar())
                                keyEventSender.sendChar(inputConnection, char.lowercaseChar(), metaState)
                            }
                        }
                    }
                }
                modifierState.onCharacterInput()
                updateModifierVisual()
                updateToolbarStatus()
            }

            KeyType.NAVIGATION -> {
                val metaState = modifierState.getMetaState()
                EventLog.logKey("按下", keyData.keyCode, metaState)
                keyEventSender.sendKeyEvent(inputConnection, keyData.keyCode, metaState)
            }

            KeyType.SPECIAL -> {
                val metaState = modifierState.getMetaState()
                EventLog.logKey("按下", keyData.keyCode, metaState)
                when (keyData.keyCode) {
                    KeyEvent.KEYCODE_ENTER -> {
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
                    KeyEvent.KEYCODE_DEL -> {
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
                    KeyEvent.KEYCODE_FORWARD_DEL -> {
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
                        val metaState = modifierState.getMetaState()
                        keyEventSender.sendKeyEvent(inputConnection, keyData.keyCode, metaState)
                    }
                }
                if (keyData.keyCode == KeyEvent.KEYCODE_ENTER ||
                    keyData.keyCode == KeyEvent.KEYCODE_DEL) {
                    modifierState.onCharacterInput()
                    updateModifierVisual()
                    updateToolbarStatus()
                }
            }

            KeyType.FUNCTION -> {
                val metaState = modifierState.getMetaState()
                EventLog.logKey("按下", keyData.keyCode, metaState)
                keyEventSender.sendKeyEvent(inputConnection, keyData.keyCode, metaState)
            }
        }
    }

    /**
     * 更新键盘视图上的修饰键高亮
     */
    private fun updateModifierVisual() {
        val active = mutableSetOf<Int>()
        if (modifierState.isLocked(ModifierState.Modifier.SHIFT)) {
            active.add(KeyEvent.KEYCODE_SHIFT_LEFT)
        }
        if (modifierState.isLocked(ModifierState.Modifier.CTRL)) {
            active.add(KeyEvent.KEYCODE_CTRL_LEFT)
        }
        if (modifierState.isLocked(ModifierState.Modifier.ALT)) {
            active.add(KeyEvent.KEYCODE_ALT_LEFT)
        }
        if (modifierState.isLocked(ModifierState.Modifier.META)) {
            active.add(KeyEvent.KEYCODE_META_LEFT)
        }
        keyboardView?.setActiveModifiers(active)
    }

    /**
     * 更新工具栏修饰键状态显示
     */
    private fun updateToolbarStatus() {
        val activeColor = 0xFF1565C0.toInt()
        val inactiveColor = 0xFF999999.toInt()

        val shiftActive = modifierState.isLocked(ModifierState.Modifier.SHIFT)
        val ctrlActive = modifierState.isLocked(ModifierState.Modifier.CTRL)
        val altActive = modifierState.isLocked(ModifierState.Modifier.ALT)
        val metaActive = modifierState.isLocked(ModifierState.Modifier.META)

        modShift?.visibility = if (shiftActive) View.VISIBLE else View.GONE
        modShift?.setTextColor(activeColor)

        modCtrl?.visibility = if (ctrlActive) View.VISIBLE else View.GONE
        modCtrl?.setTextColor(activeColor)

        modAlt?.visibility = if (altActive) View.VISIBLE else View.GONE
        modAlt?.setTextColor(activeColor)

        modMeta?.visibility = if (metaActive) View.VISIBLE else View.GONE
        modMeta?.setTextColor(activeColor)
    }

    private fun getKeyLabel(keyCode: Int): String {
        return when (keyCode) {
            KeyEvent.KEYCODE_A -> "A"
            KeyEvent.KEYCODE_B -> "B"
            KeyEvent.KEYCODE_C -> "C"
            KeyEvent.KEYCODE_D -> "D"
            KeyEvent.KEYCODE_E -> "E"
            KeyEvent.KEYCODE_F -> "F"
            KeyEvent.KEYCODE_G -> "G"
            KeyEvent.KEYCODE_H -> "H"
            KeyEvent.KEYCODE_I -> "I"
            KeyEvent.KEYCODE_J -> "J"
            KeyEvent.KEYCODE_K -> "K"
            KeyEvent.KEYCODE_L -> "L"
            KeyEvent.KEYCODE_M -> "M"
            KeyEvent.KEYCODE_N -> "N"
            KeyEvent.KEYCODE_O -> "O"
            KeyEvent.KEYCODE_P -> "P"
            KeyEvent.KEYCODE_Q -> "Q"
            KeyEvent.KEYCODE_R -> "R"
            KeyEvent.KEYCODE_S -> "S"
            KeyEvent.KEYCODE_T -> "T"
            KeyEvent.KEYCODE_U -> "U"
            KeyEvent.KEYCODE_V -> "V"
            KeyEvent.KEYCODE_W -> "W"
            KeyEvent.KEYCODE_X -> "X"
            KeyEvent.KEYCODE_Y -> "Y"
            KeyEvent.KEYCODE_Z -> "Z"
            KeyEvent.KEYCODE_0 -> "0"
            KeyEvent.KEYCODE_1 -> "1"
            KeyEvent.KEYCODE_2 -> "2"
            KeyEvent.KEYCODE_3 -> "3"
            KeyEvent.KEYCODE_4 -> "4"
            KeyEvent.KEYCODE_5 -> "5"
            KeyEvent.KEYCODE_6 -> "6"
            KeyEvent.KEYCODE_7 -> "7"
            KeyEvent.KEYCODE_8 -> "8"
            KeyEvent.KEYCODE_9 -> "9"
            else -> ""
        }
    }
}
