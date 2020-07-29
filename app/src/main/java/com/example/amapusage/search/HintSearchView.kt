package com.example.amapusage.search

import android.content.Context
import android.os.SystemClock
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.inputmethod.InputMethodManager
import android.widget.*
import com.example.amapusage.R

class HintSearchView : FrameLayout, IHintSearchView {
    private val TAG = "HintSearchView"

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attr: AttributeSet?) : this(context, attr, 0)
    constructor(context: Context, attr: AttributeSet?, defStyleAttr: Int)
            : super(context, attr, defStyleAttr, defStyleAttr)

    var listener: IHintSearchView.OnSearchChangeListener? = null
    private val rootLayout: View = View.inflate(context, R.layout.location_search_view, this)
    private val rlEdit: RelativeLayout = rootLayout.findViewById(R.id.rl_edit)
    private val searchLeftIcon = rootLayout.findViewById<ImageView>(R.id.search_left_icon)
    private val searchContentEdit = rootLayout.findViewById<EditText>(R.id.search_content_edit)
    private val searchDeleteIcon = rootLayout.findViewById<ImageView>(R.id.search_delete_icon)

    // 在各自的点击事件里面取消文字.
    private val hintLayout = rootLayout.findViewById<RelativeLayout>(R.id.hint_layout)
    private val btnCancel = rootLayout.findViewById<TextView>(R.id.btn_cancel) // 需要失去焦点

    var enterEditMode = false // 失去了焦点但是还存在文字，也认为是enter
        private set

    init {
        rlEdit.visibility = View.GONE
        hintLayout.visibility = View.VISIBLE
        btnCancel.visibility = View.GONE
        searchDeleteIcon.visibility = View.GONE
        initListener()
    }

    private fun initListener() {
        // 第一次取消是关闭键盘，第二次是清楚数据, 同时发生坍塌
        btnCancel.setOnClickListener {
            searchContentEdit.setText("")
            searchContentEdit.clearFocus()
        }
        hintLayout.setOnClickListener { flashyEditClick() } //模拟searchContentEdit发生点击.
        searchDeleteIcon.setOnClickListener {
            searchContentEdit.setText("")
            enterEditMode()
        }
        searchContentEdit.setOnClickListener { if (searchContentEdit.isFocused) enterEditMode() }
        searchContentEdit.onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
            if (hasFocus) enterEditMode()
            else outEditMode()
        }
        searchContentEdit.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                listener?.sourceCome(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                listener?.beforeSourceChange(s.toString())
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (TextUtils.isEmpty(s)) searchDeleteIcon.visibility = View.GONE
                else searchDeleteIcon.visibility = View.VISIBLE
                listener?.sourceChanging(s.toString())
            }
        })
    }

    override fun outEditMode() {
        if (searchContentEdit.isFocused) {
            searchContentEdit.clearFocus()
            return
        }
        enterEditMode = false
        listener?.onEnterModeChange(enterEditMode) // 防止遮盖，先调用
        hideSoftKeyboard()
        if (TextUtils.isEmpty(searchContentEdit.text)) { // 在提交后，有的失去了焦点但是还会存在文字.
            hintLayout.visibility = View.VISIBLE
            btnCancel.visibility = View.GONE
            rlEdit.visibility = View.GONE
            searchDeleteIcon.visibility = View.GONE
        }
    }

    override fun enterEditMode() {
        if (!searchContentEdit.isFocused) {
            searchContentEdit.requestFocus()
            return
        }
        enterEditMode = true
        listener?.onEnterModeChange(enterEditMode) // 防止遮盖，先调用
        openKeyboard()
        rlEdit.visibility = View.VISIBLE
        btnCancel.visibility = View.VISIBLE
        hintLayout.visibility = View.GONE
    }

    private fun flashyEditClick() { // 模拟一个searchContentEdit的点击事件
        searchContentEdit.dispatchTouchEvent(
            MotionEvent.obtain(
                SystemClock.uptimeMillis(),
                SystemClock.uptimeMillis(),
                MotionEvent.ACTION_DOWN,
                searchContentEdit.left + 5f,
                searchContentEdit.top + 5f,
                0
            )
        )
        searchContentEdit.dispatchTouchEvent(
            MotionEvent.obtain(
                SystemClock.uptimeMillis(),
                SystemClock.uptimeMillis(),
                MotionEvent.ACTION_UP,
                searchContentEdit.left + 5f,
                searchContentEdit.top + 5f,
                0
            )
        )
    }

    override fun setSearchListener(listener: IHintSearchView.OnSearchChangeListener) {
        this.listener = listener
    }

    override fun getEditView(): EditText {
        return searchContentEdit
    }

    private fun hideSoftKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
        Log.d(TAG, "openKeyboard: " + "imm.isActive")
    }

    private fun openKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(searchContentEdit, InputMethodManager.RESULT_SHOWN)
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)
        Log.d(TAG, "openKeyboard: " + "imm.isActive")
    }

    open class OnSearchChangeListenerIml : IHintSearchView.OnSearchChangeListener {
        override fun onEnterModeChange(isEnter: Boolean) {}
        override fun sourceCome(data: String) {}
        override fun sourceChanging(data: String) {}
        override fun beforeSourceChange(toString: String) {}
    }
}