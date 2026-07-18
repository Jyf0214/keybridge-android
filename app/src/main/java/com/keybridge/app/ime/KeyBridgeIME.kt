package com.keybridge.app.ime

import android.animation.ObjectAnimator
import android.content.res.Configuration
import android.graphics.Rect
import android.inputmethodservice.InputMethodService
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.keybridge.app.R
import com.keybridge.app.data.KeyData
import com.keybridge.app.data.KeyType
import com.keybridge.app.data.KeyboardLayout
import com.keybridge.app.data.ModifierState
import com.keybridge.app.event.EventLog

/**
 * KeyBridge 输入法核心服务
 * 支持竖屏全宽模式和横屏浮动卡片模式
 */
class KeyBridgeIME : InputMethodService() {

    private lateinit var modifierState: ModifierState
    private lateinit var keyEventSender: KeyEventSender
    private var keyboardView: KeyboardView? = null
    private var rootLayout: View? = null
    private var keyboardCard: CardView? = null

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

    // 浮动模式
    private var isFloatingMode = false
    private var isInLandscape = false

    override fun onCreate() {
        super.onCreate()
        modifierState = ModifierState()
        keyEventSender = KeyEventSender()
    }

    override fun onCreateInputView(): View {
        val rootView = layoutInflater.inflate(R.layout.keyboard_layout, null)
        rootLayout = rootView.findViewById(R.id.keyboard_root)
        keyboardCard = rootView.findViewById(R.id.keyboard_card)

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

        // 设置拖拽
        setupDrag()

        // 检测方向并应用模式
        checkOrientation()

        return rootView
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        checkOrientation()
    }

    /**
     * 检测屏幕方向，切换浮动/全宽模式
     */
    private fun checkOrientation() {
        val config = resources.configuration
        val landscape = config.orientation == Configuration.ORIENTATION_LANDSCAPE
        if (landscape != isInLandscape) {
            isInLandscape = landscape
            applyMode()
        }
    }

    private fun applyMode() {
        val card = keyboardCard ?: return
        val root = rootLayout ?: return

        isFloatingMode = isInLandscape

        if (isFloatingMode) {
            // 横屏：浮动卡片模式
            root.setBackgroundColor(android.graphics.Color.TRANSPARENT)

            val params = card.layoutParams as FrameLayout.LayoutParams
            params.width = (resources.displayMetrics.widthPixels * 0.55f).toInt()
            params.gravity = android.view.Gravity.BOTTOM or android.view.Gravity.END
            params.setMargins(0, 0, dp(12), dp(12))
            card.layoutParams = params
            card.alpha = 0.95f
        } else {
            // 竖屏：全宽底部模式
            root.setBackgroundColor(android.graphics.Color.TRANSPARENT)

            val params = card.layoutParams as FrameLayout.LayoutParams
            params.width = FrameLayout.LayoutParams.MATCH_PARENT
            params.gravity = android.view.Gravity.BOTTOM
            params.setMargins(0, 0, 0, 0)
            card.layoutParams = params
            card.alpha = 1.0f
        }

        // 触摸区域随布局变化自动更新
        card.post { requestInputViewUpdate() }
    }

    /**
     * 触发系统重新计算输入法布局（更新触摸区域）
     */
    private fun requestInputViewUpdate() {
        window?.window?.decorView?.requestLayout()
    }

    /**
     * 设置拖拽功能（仅浮动模式）
     */
    private fun setupDrag() {
        val card = keyboardCard ?: return
        val toolbar = toolbar ?: return

        var dX = 0f
        var dY = 0f
        var isDragging = false

        toolbar.setOnTouchListener { _, event ->
            if (!isFloatingMode) return@setOnTouchListener false

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    dX = card.x - event.rawX
                    dY = card.y - event.rawY
                    isDragging = false
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val newX = event.rawX + dX
                    val newY = event.rawY + dY

                    // 限制在屏幕范围内
                    val maxX = resources.displayMetrics.widthPixels - card.width
                    val maxY = resources.displayMetrics.heightPixels - card.height
                    card.x = newX.coerceIn(0f, maxX.toFloat())
                    card.y = newY.coerceIn(0f, maxY.toFloat())

                    isDragging = true
                    // 坐标变化后系统会自动重新调用 onComputeInsets
                    requestInputViewUpdate()
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    if (isDragging) {
                        isDragging = false
                        true
                    } else {
                        false
                    }
                }
                else -> false
            }
        }
    }

    /**
     * 核心：告诉系统哪些区域是可触摸的
     * 横屏浮动模式下，只有卡片区域响应触摸，其余区域穿透到背后的应用
     */
    override fun onComputeInsets(outInsets: Insets) {
        super.onComputeInsets(outInsets)

        val card = keyboardCard ?: return

        if (isFloatingMode) {
            // 浮动模式：透明区域触摸穿透
            val screenHeight = window?.window?.decorView?.height ?: return

            // 告诉系统"我没有遮挡内容"（不挤压后面的 App）
            outInsets.contentTopInsets = screenHeight
            outInsets.visibleTopInsets = screenHeight

            // 设置触摸区域为卡片所在的矩形
            outInsets.touchableInsets = Insets.TOUCHABLE_INSETS_REGION

            val rect = Rect()
            card.getGlobalVisibleRect(rect)
            outInsets.touchableRegion.set(rect)
        }
        // 非浮动模式：使用默认行为（全宽底部键盘）
    }

    /**
     * 设置导航页面按钮点击事件
     */
    private fun setupNavPage(rootView: View) {
        val nav = navPage ?: return

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

        nav.findViewById<View>(R.id.nav_enter)?.setOnClickListener {
            keyEventSender.sendKeyEvent(currentInputConnection, KeyEvent.KEYCODE_ENTER)
        }

        nav.findViewById<View>(R.id.nav_copy)?.setOnClickListener {
            sendEditorAction(KeyEvent.KEYCODE_C, ctrl = true)
        }
        nav.findViewById<View>(R.id.nav_paste)?.setOnClickListener {
            sendEditorAction(KeyEvent.KEYCODE_V, ctrl = true)
        }
        nav.findViewById<View>(R.id.nav_switch)?.setOnClickListener {
            togglePage()
        }
        nav.findViewById<View>(R.id.nav_ime_picker)?.setOnClickListener {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
            imm.showInputMethodPicker()
        }
    }

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
        val duration = 180L

        if (isNavPage) {
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
        } else {
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
        }
        isNavPage = !isNavPage
        // 布局变化后更新触摸区域
        keyboardCard?.post { requestInputViewUpdate() }
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

    private fun handleKeyAction(keyData: KeyData) {
        val inputConnection = currentInputConnection

        when (keyData.type) {
            KeyType.MODIFIER -> {
                when (keyData.keyCode) {
                    KeyEvent.KEYCODE_SHIFT_LEFT, KeyEvent.KEYCODE_SHIFT_RIGHT ->
                        modifierState.toggleLock(ModifierState.Modifier.SHIFT)
                    KeyEvent.KEYCODE_CTRL_LEFT, KeyEvent.KEYCODE_CTRL_RIGHT ->
                        modifierState.toggleLock(ModifierState.Modifier.CTRL)
                    KeyEvent.KEYCODE_ALT_LEFT, KeyEvent.KEYCODE_ALT_RIGHT ->
                        modifierState.toggleLock(ModifierState.Modifier.ALT)
                    KeyEvent.KEYCODE_META_LEFT, KeyEvent.KEYCODE_META_RIGHT ->
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

    private fun updateModifierVisual() {
        val active = mutableSetOf<Int>()
        if (modifierState.isLocked(ModifierState.Modifier.SHIFT))
            active.add(KeyEvent.KEYCODE_SHIFT_LEFT)
        if (modifierState.isLocked(ModifierState.Modifier.CTRL))
            active.add(KeyEvent.KEYCODE_CTRL_LEFT)
        if (modifierState.isLocked(ModifierState.Modifier.ALT))
            active.add(KeyEvent.KEYCODE_ALT_LEFT)
        if (modifierState.isLocked(ModifierState.Modifier.META))
            active.add(KeyEvent.KEYCODE_META_LEFT)
        keyboardView?.setActiveModifiers(active)
    }

    private fun updateToolbarStatus() {
        modShift?.visibility = if (modifierState.isLocked(ModifierState.Modifier.SHIFT)) View.VISIBLE else View.GONE
        modShift?.setTextColor(0xFF1A73E8.toInt())
        modCtrl?.visibility = if (modifierState.isLocked(ModifierState.Modifier.CTRL)) View.VISIBLE else View.GONE
        modCtrl?.setTextColor(0xFF1A73E8.toInt())
        modAlt?.visibility = if (modifierState.isLocked(ModifierState.Modifier.ALT)) View.VISIBLE else View.GONE
        modAlt?.setTextColor(0xFF1A73E8.toInt())
        modMeta?.visibility = if (modifierState.isLocked(ModifierState.Modifier.META)) View.VISIBLE else View.GONE
        modMeta?.setTextColor(0xFF1A73E8.toInt())
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }

    private fun getKeyLabel(keyCode: Int): String {
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
            else -> ""
        }
    }
}
