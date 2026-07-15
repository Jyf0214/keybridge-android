package com.keybridge.app.data

import android.view.KeyEvent

/**
 * 修饰键状态管理器
 * 跟踪 Shift、Ctrl、Alt、Meta 的锁定和临时按压状态
 */
class ModifierState {

    enum class Modifier {
        SHIFT, CTRL, ALT, META
    }

    private val locked = mutableMapOf(
        Modifier.SHIFT to false,
        Modifier.CTRL to false,
        Modifier.ALT to false,
        Modifier.META to false
    )

    private val temporary = mutableMapOf(
        Modifier.SHIFT to false,
        Modifier.CTRL to false,
        Modifier.ALT to false,
        Modifier.META to false
    )

    /**
     * 切换修饰键锁定状态（点击修饰键时调用）
     */
    fun toggleLock(modifier: Modifier) {
        locked[modifier] = !(locked[modifier] ?: false)
        temporary[modifier] = false
    }

    /**
     * 设置临时按压状态（长按修饰键时调用）
     */
    fun setTemporary(modifier: Modifier, active: Boolean) {
        temporary[modifier] = active
    }

    /**
     * 检查修饰键是否处于激活状态（锁定或临时按压）
     */
    fun isActive(modifier: Modifier): Boolean {
        return (locked[modifier] ?: false) || (temporary[modifier] ?: false)
    }

    /**
     * 检查修饰键是否处于锁定状态
     */
    fun isLocked(modifier: Modifier): Boolean {
        return locked[modifier] ?: false
    }

    /**
     * 获取当前修饰键状态下的 KeyEvent 模式标志位
     */
    fun getMetaState(): Int {
        var meta = 0
        if (isActive(Modifier.SHIFT)) meta = meta or KeyEvent.META_SHIFT_ON
        if (isActive(Modifier.CTRL)) meta = meta or KeyEvent.META_CTRL_ON
        if (isActive(Modifier.ALT)) meta = meta or KeyEvent.META_ALT_ON
        if (isActive(Modifier.META)) meta = meta or KeyEvent.META_META_ON
        return meta
    }

    /**
     * 字符输入后，如果是单次 Shift 则自动关闭（像物理键盘一样）
     */
    fun onCharacterInput() {
        if (temporary[Modifier.SHIFT] == true) {
            temporary[Modifier.SHIFT] = false
        }
        // 非锁定的修饰键在字符输入后不自动关闭 Ctrl/Alt/Meta
        // 因为用户可能需要 Ctrl+C 这样的组合键
    }

    /**
     * 重置所有状态
     */
    fun reset() {
        keys.forEach { modifier ->
            locked[modifier] = false
            temporary[modifier] = false
        }
    }

    companion object {
        private val keys = Modifier.entries
    }
}
