package com.example.amapusage.utils

import android.content.Context
import android.os.IBinder
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.widget.SearchView

object KeyBoardUtils {

    fun openKeyboard(mEditText: EditText, mContext: Context) {
        val imm = mContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(mEditText, InputMethodManager.RESULT_SHOWN)
        imm.toggleSoftInput(
            InputMethodManager.SHOW_FORCED,
            InputMethodManager.HIDE_IMPLICIT_ONLY
        )
    }

    fun closeKeyboard(windowToken: IBinder, mContext: Context) {
        val imm = mContext
            .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }


}