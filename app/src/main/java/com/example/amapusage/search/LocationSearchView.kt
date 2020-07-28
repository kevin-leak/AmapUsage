package com.example.amapusage.search

import android.content.Context
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.example.amapusage.R

class LocationSearchView : RelativeLayout, ILocationSearchView {

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attr: AttributeSet?) : this(context, attr, 0)
    constructor(context: Context, attr: AttributeSet?, defStyleAttr: Int)
            : super(context, attr, defStyleAttr)

    private var listener: ILocationSearchView.OnLocationSourceChange? = null
    private val rootLayout: View = View.inflate(context, R.layout.location_search_view, this)
    private val rlEdit = rootLayout.findViewById<RelativeLayout>(R.id.rl_edit)
    private val searchLeftIcon = rootLayout.findViewById<ImageView>(R.id.search_left_icon)
    private val searchContentEdit = rootLayout.findViewById<EditText>(R.id.search_content_edit)
    private val searchDeleteIcon = rootLayout.findViewById<ImageView>(R.id.search_delete_icon)
    private val hintLayout = rootLayout.findViewById<RelativeLayout>(R.id.hint_layout)
    private val btnCancel = rootLayout.findViewById<TextView>(R.id.btn_cancel)


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
            if (!searchContentEdit.isFocused) { //当点击搜索的时候失去了焦点，但需要改变显示状态
                hideSoftKeyboard()
                hintLayout.visibility = View.VISIBLE
                btnCancel.visibility = View.GONE
                rlEdit.visibility = View.GONE
                searchDeleteIcon.visibility = View.GONE
            }
            searchContentEdit.clearFocus()
        }
        hintLayout.setOnClickListener {
            searchContentEdit.setText("")
            searchContentEdit.requestFocus()
            openKeyboard()
        }
        searchDeleteIcon.setOnClickListener { searchContentEdit.setText("") }
        searchContentEdit.onFocusChangeListener =
            OnFocusChangeListener { _, hasFocus ->
                listener?.onFocusChange(hasFocus)
                if (hasFocus) {
                    openKeyboard()
                    rlEdit.visibility = View.VISIBLE
                    btnCancel.visibility = View.VISIBLE
                    hintLayout.visibility = View.GONE
                } else {
                    hideSoftKeyboard()
                    if (TextUtils.isEmpty(searchContentEdit.text)) {
                        hintLayout.visibility = View.VISIBLE
                        btnCancel.visibility = View.GONE
                        rlEdit.visibility = View.GONE
                        searchDeleteIcon.visibility = View.GONE
                    }
                }
            }
        searchContentEdit.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                listener?.locationSourceCome(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                listener?.beforeLocationSourceChange(s.toString())
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (TextUtils.isEmpty(s)) searchDeleteIcon.visibility = View.GONE
                else searchDeleteIcon.visibility = View.VISIBLE
                listener?.locationSourceChanging(s.toString())
            }
        })
    }

    fun setLocationSearchListener(listener: ILocationSearchView.OnLocationSourceChange) {
        this.listener = listener
    }

    private fun hideSoftKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }

    private fun openKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(searchContentEdit, InputMethodManager.RESULT_SHOWN)
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)
    }
}