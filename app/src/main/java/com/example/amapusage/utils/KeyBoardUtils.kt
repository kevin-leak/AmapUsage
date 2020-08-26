package com.example.amapusage.utils

import android.content.Context
import android.os.IBinder
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import com.example.amapusage.App

object KeyBoardUtils {

    fun openKeyboard(mEditText: EditText) {
        val imm =
            App.appContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(mEditText, InputMethodManager.RESULT_SHOWN)
        imm.toggleSoftInput(
            InputMethodManager.SHOW_FORCED,
            InputMethodManager.HIDE_IMPLICIT_ONLY
        )
    }

    fun closeKeyboard(windowToken: IBinder) {
        val imm =
            App.appContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }
}