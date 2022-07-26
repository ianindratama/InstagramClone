package com.example.submission.customView

import android.content.Context
import android.graphics.Canvas
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.widget.AppCompatEditText
import com.example.submission.R

class PasswordEditText: AppCompatEditText, View.OnTouchListener {

    constructor(context: Context): super(context){
        init()
    }

    constructor(context: Context, attrs: AttributeSet): super(context, attrs){
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int): super(context, attrs, defStyleAttr){
        init()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        textAlignment = View.TEXT_ALIGNMENT_VIEW_START
    }

    private fun init(){

        hint = context.getString(R.string.hint_password)

        addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                // do nothing
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                error = when (p0.toString().length) {
                    in 1..5 -> {
                        context.getString(R.string.error_password)
                    }
                    else -> {
                        null
                    }
                }

            }

            override fun afterTextChanged(p0: Editable?) {
                // do nothing
            }

        })

    }

    override fun onTouch(p0: View?, p1: MotionEvent?): Boolean {
        return true
    }


}