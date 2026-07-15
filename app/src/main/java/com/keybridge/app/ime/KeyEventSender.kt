package com.keybridge.app.ime

import android.view.KeyEvent
import android.view.inputmethod.InputConnection

/**
 * 按键事件发送器
 * 负责将按键事件通过 InputConnection 发送到当前输入框
 */
class KeyEventSender {

    /**
     * 发送按键事件
     */
    fun sendKeyEvent(
        inputConnection: InputConnection?,
        keyCode: Int,
        metaState: Int = 0,
        keyChar: Char? = null
    ) {
        inputConnection ?: return

        val now = System.currentTimeMillis()

        // 发送按下事件
        val downEvent = KeyEvent(
            now, now,
            KeyEvent.ACTION_DOWN,
            keyCode,
            0,
            metaState
        )
        inputConnection.sendKeyEvent(downEvent)

        // 发送抬起事件
        val upEvent = KeyEvent(
            now, now,
            KeyEvent.ACTION_UP,
            keyCode,
            0,
            metaState
        )
        inputConnection.sendKeyEvent(upEvent)
    }

    /**
     * 发送字符输入（对于普通字符键）
     */
    fun sendChar(
        inputConnection: InputConnection?,
        char: Char,
        metaState: Int = 0
    ) {
        inputConnection ?: return

        // 优先使用 commitText，更可靠
        inputConnection.commitText(char.toString(), 1)
    }

    /**
     * 发送文本字符串
     */
    fun sendText(
        inputConnection: InputConnection?,
        text: String
    ) {
        inputConnection ?: return
        inputConnection.commitText(text, 1)
    }

    /**
     * 处理修饰键组合（如 Ctrl+C）
     * 返回是否已处理（即是否为组合键）
     */
    fun handleModifierCombo(
        inputConnection: InputConnection?,
        keyCode: Int,
        ctrlActive: Boolean,
        altActive: Boolean
    ): Boolean {
        if (!ctrlActive && !altActive) return false

        inputConnection ?: return false

        val now = System.currentTimeMillis()
        var meta = 0
        if (ctrlActive) meta = meta or KeyEvent.META_CTRL_ON
        if (altActive) meta = meta or KeyEvent.META_ALT_ON

        // 发送组合键
        sendKeyEvent(inputConnection, keyCode, meta)
        return true
    }
}
