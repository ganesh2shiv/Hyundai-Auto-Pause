package com.custom.app.util

import android.graphics.Paint
import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.text.TextPaint
import android.text.TextUtils
import android.text.style.ImageSpan
import android.text.style.TypefaceSpan

class SpannyText : SpannableStringBuilder {

    private val flag = SPAN_EXCLUSIVE_EXCLUSIVE

    constructor() : super("")

    constructor(text: CharSequence?) : super(if (!TextUtils.isEmpty(text)) text else "")

    constructor(text: CharSequence?, vararg spans: Any?) : super(if (!TextUtils.isEmpty(text)) text else "") {
        if (spans.isNotEmpty()) {
            for (span in spans) {
                setSpan(span, 0, length)
            }
        }
    }

    constructor(text: CharSequence?, span: Any?) : super(if (!TextUtils.isEmpty(text)) text else "") {
        setSpan(span, 0, text?.length)
    }

    fun append(text: CharSequence?, vararg spans: Any?): SpannyText {
        append(text)
        if (text != null && spans.isNotEmpty()) {
            for (span in spans) {
                setSpan(span, length - text.length, length)
            }
        }
        return this
    }

    fun append(text: CharSequence?, span: Any?): SpannyText {
        if (text != null) {
            append(text)
            setSpan(span, length - text.length, length)
        }
        return this
    }

    fun append(text: CharSequence?, imageSpan: ImageSpan?): SpannyText {
        var text = text
        text = ".$text"
        append(text)
        setSpan(imageSpan, length - text.length, length - text.length + 1)
        return this
    }

    override fun append(text: CharSequence?): SpannyText {
        super.append(text)
        return this
    }

    private fun setSpan(span: Any?, start: Int?, end: Int?) {
        if (span != null && start != null && end != null) {
            setSpan(span, start, end, flag)
        }
    }
}

class SpannyTypeface : TypefaceSpan {
    private val newType: Typeface

    constructor(type: Typeface) : super("") {
        newType = type
    }

    constructor(family: String?, type: Typeface) : super(family) {
        newType = type
    }

    override fun updateDrawState(textPaint: TextPaint) {
        applyCustomTypeFace(textPaint, newType)
    }

    override fun updateMeasureState(paint: TextPaint) {
        applyCustomTypeFace(paint, newType)
    }

    companion object {
        private fun applyCustomTypeFace(paint: Paint, tf: Typeface) {
            val oldStyle: Int
            val old = paint.typeface
            oldStyle = old?.style ?: 0
            val fake = oldStyle and tf.style.inv()
            if (fake and Typeface.BOLD != 0) {
                paint.isFakeBoldText = true
            }
            if (fake and Typeface.ITALIC != 0) {
                paint.textSkewX = -0.25f
            }
            paint.typeface = tf
        }
    }
}
