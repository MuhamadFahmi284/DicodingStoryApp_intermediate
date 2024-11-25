package com.dicoding.dicodingstoryapp.component

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.Patterns
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.widget.AppCompatEditText

class MyEmailEditText : AppCompatEditText {

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    private fun init() {
        textAlignment = TEXT_ALIGNMENT_VIEW_START
        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (Patterns.EMAIL_ADDRESS.matcher(s.toString()).matches()) {
                    error = null
                } else {
                    error = "Format email tidak valid"
                }
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })
    }

}