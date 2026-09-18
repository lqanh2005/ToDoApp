package com.lqanh.todoandroid.ui.widgets

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.widget.FrameLayout
import kotlin.math.abs

class SwipeRevealLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private var foregroundView: View? = null
    private var actionsView: View? = null
    private var actionsWidth = 0f
    private var contentOffset = 0f
    private var downX = 0f
    private var downY = 0f
    private var startOffset = 0f
    private var dragging = false
    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop

    init {
        clipChildren = true
        clipToPadding = true
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        if (childCount >= 2) {
            actionsView = getChildAt(0)
            foregroundView = getChildAt(1)
            actionsView?.elevation = 0f
            foregroundView?.elevation = 0f
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val fg = foregroundView
        val actions = actionsView
        if (fg == null || actions == null) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            return
        }
        measureChild(fg, widthMeasureSpec, heightMeasureSpec)
        val heightSpec = MeasureSpec.makeMeasureSpec(fg.measuredHeight, MeasureSpec.EXACTLY)
        val widthSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
        actions.measure(widthSpec, heightSpec)
        actionsWidth = actions.measuredWidth.toFloat()
        setMeasuredDimension(
            View.resolveSize(fg.measuredWidth, widthMeasureSpec),
            fg.measuredHeight
        )
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        val w = right - left
        val h = bottom - top
        val aw = actionsView?.measuredWidth ?: 0
        foregroundView?.layout(0, 0, w, h)
        actionsView?.layout(w, 0, w + aw, h)
        applyOffset(contentOffset)
    }

    private fun applyOffset(offset: Float) {
        contentOffset = offset.coerceIn(-actionsWidth, 0f)
        foregroundView?.translationX = contentOffset
        actionsView?.translationX = contentOffset
    }

    private fun animateOffset(target: Float) {
        val from = contentOffset
        val to = target.coerceIn(-actionsWidth, 0f)
        foregroundView?.animate()?.cancel()
        actionsView?.animate()?.cancel()
        if (from == to) {
            applyOffset(to)
            return
        }
        foregroundView?.animate()
            ?.translationX(to)
            ?.setDuration(180)
            ?.setUpdateListener {
                val fgTx = foregroundView?.translationX ?: to
                actionsView?.translationX = fgTx
                contentOffset = fgTx
            }
            ?.withEndAction { applyOffset(to) }
            ?.start()
        actionsView?.animate()
            ?.translationX(to)
            ?.setDuration(180)
            ?.start()
    }

    fun close(animate: Boolean = true) {
        if (animate) animateOffset(0f) else applyOffset(0f)
        if (openLayout === this) openLayout = null
    }

    fun open(animate: Boolean = true) {
        if (actionsWidth <= 0f) {
            actionsWidth = (actionsView?.measuredWidth ?: 0).toFloat()
        }
        openLayout?.takeIf { it !== this }?.close()
        openLayout = this
        if (animate) animateOffset(-actionsWidth) else applyOffset(-actionsWidth)
    }

    fun isOpen(): Boolean = contentOffset < -actionsWidth / 2f

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                downX = ev.x
                downY = ev.y
                startOffset = contentOffset
                dragging = false
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = ev.x - downX
                val dy = ev.y - downY
                if (!dragging && abs(dx) > touchSlop && abs(dx) > abs(dy)) {
                    dragging = true
                    parent?.requestDisallowInterceptTouchEvent(true)
                    return true
                }
            }
        }
        return dragging
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                downX = event.x
                downY = event.y
                startOffset = contentOffset
                dragging = false
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = event.x - downX
                val dy = event.y - downY
                if (!dragging && abs(dx) > touchSlop && abs(dx) > abs(dy)) {
                    dragging = true
                    parent?.requestDisallowInterceptTouchEvent(true)
                    openLayout?.takeIf { it !== this }?.close()
                }
                if (dragging) {
                    if (actionsWidth <= 0f) {
                        actionsWidth = (actionsView?.measuredWidth ?: 0).toFloat()
                    }
                    applyOffset(startOffset + dx)
                    return true
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (dragging) {
                    if (contentOffset < -actionsWidth / 2f) open() else close()
                    dragging = false
                    return true
                } else if (isOpen() && event.x < width + contentOffset) {
                    close()
                    return true
                }
            }
        }
        return super.onTouchEvent(event) || dragging
    }

    companion object {
        private var openLayout: SwipeRevealLayout? = null

        fun closeOpen() {
            openLayout?.close()
            openLayout = null
        }
    }
}
