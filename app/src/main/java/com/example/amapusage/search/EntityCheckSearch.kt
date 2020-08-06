package com.example.amapusage.search

import android.annotation.SuppressLint
import android.content.Context
import android.os.IBinder
import android.os.SystemClock
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import com.example.amapusage.R

@SuppressLint("NewApi")
class EntityCheckSearch(context: Context, attr: AttributeSet?, defStyleAttr: Int) :
    FrameLayout(context, attr, defStyleAttr, defStyleAttr), IEntityCheckSearch {
    private val TAG = "HintSearchView"

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attr: AttributeSet?) : this(context, attr, 0)

    var listeners: MutableList<IEntityCheckSearch.OnSearchListener> = mutableListOf()
    private val rootLayout: View = View.inflate(context, R.layout.location_search_view, this)
    private val rlEdit: RelativeLayout = rootLayout.findViewById(R.id.rl_edit)
    private val searchLeftIcon = rootLayout.findViewById<ImageView>(R.id.search_left_icon)
    private val searchContentEdit = rootLayout.findViewById<EditText>(R.id.search_content_edit)
    private val searchDeleteIcon = rootLayout.findViewById<ImageView>(R.id.search_delete_icon)

    // 在各自的点击事件里面取消文字.
    private val hintLayout = rootLayout.findViewById<RelativeLayout>(R.id.hint_layout)
    private val btnCancel = rootLayout.findViewById<TextView>(R.id.btn_cancel) // 需要失去焦点
    var isEnterMode = false // 失去了焦点但是还存在文字，也认为是enter
        private set
    var isSearch = false
        private set

    init {
        rlEdit.visibility = View.GONE
        hintLayout.visibility = View.VISIBLE
        btnCancel.visibility = View.GONE
        searchDeleteIcon.visibility = View.GONE
        initListener()
        attr?.let {
            initStyle(attr, defStyleAttr)
        }
    }

    private fun initStyle(attrs: AttributeSet, defStyleAttr: Int) {
        val a =
            context.obtainStyledAttributes(attrs, R.styleable.EntityCheckSearch, defStyleAttr, 0)
        if (a.hasValue(R.styleable.EntityCheckSearch_text)) {
            val queryText = a.getString(R.styleable.EntityCheckSearch_text)
            searchContentEdit.setText(queryText)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initListener() {
        // 第一次取消是关闭键盘，第二次是清楚数据, 同时发生坍塌
        btnCancel.setOnClickListener {
            listeners.forEach { it.beforeSearchModeChange(isSearch) } // 防止遮盖，先调用
            searchContentEdit.setText("") // 可能存在没有本身在搜索完就没有焦点的状态
            if (!searchContentEdit.isFocused) exitEditMode()
            else searchContentEdit.clearFocus()
            isSearch = false
            listeners.forEach { it.onSearchModeChange(isSearch) }
        }
        hintLayout.setOnClickListener {
            listeners.forEach { it.beforeSearchModeChange(isSearch) } // 防止遮盖，先调用
            flashyEditClick()
            isSearch = true
            listeners.forEach { it.onSearchModeChange(isSearch) }
        } //模拟searchContentEdit发生点击.
        searchDeleteIcon.setOnClickListener {
            searchContentEdit.setText("")
            enterEditMode()
        }
        searchContentEdit.setOnTouchListener { _, _ ->
            if (searchContentEdit.isFocused) enterEditMode()
            false
        }
        searchContentEdit.onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
            if (hasFocus) enterEditMode()
            else exitEditMode()
        }
        searchContentEdit.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                listeners.forEach { it.beforeSourceChange(s.toString()) }
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (TextUtils.isEmpty(s)) searchDeleteIcon.visibility = View.GONE
                else searchDeleteIcon.visibility = View.VISIBLE
                listeners.forEach { it.sourceChanging(s.toString()) }
            }
        })
        searchContentEdit.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                if (TextUtils.isEmpty(searchContentEdit.text)) searchContentEdit.requestFocus()
                listeners.forEach { it.sourceCome(searchContentEdit.text.toString()) }
            }
            false
        }
    }

    override fun exitEditMode() {
        if (searchContentEdit.isFocused) {
            searchContentEdit.clearFocus()
            return
        }
        isEnterMode = false
        hideSoftKeyboard()
        listeners.forEach { it.onEnterModeChange(isEnterMode) } // 防止遮盖，先调用
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
        isEnterMode = true
        listeners.forEach { it.onEnterModeChange(isEnterMode) }
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

    override fun addSearchListener(lt: IEntityCheckSearch.OnSearchListener){
        listeners.add(lt)
    }

    override fun getWindowToken(): IBinder = searchContentEdit.windowToken
    override fun getText(): Editable? = searchContentEdit.text

    private fun hideSoftKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(searchContentEdit.windowToken, 0)
        Log.d(TAG, "openKeyboard: " + "imm.isActive")
    }

    private fun openKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(searchContentEdit, InputMethodManager.RESULT_SHOWN)
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)
        Log.d(TAG, "openKeyboard: " + "imm.isActive")
    }

    open class OnSearchListenerIml : IEntityCheckSearch.OnSearchListener {
        override fun onEnterModeChange(isEnter: Boolean) {}
        override fun sourceCome(data: String) {}
        override fun sourceChanging(data: String) {}
        override fun beforeSourceChange(toString: String) {}
        override fun onSearchModeChange(isSearch: Boolean) {}
        override fun beforeSearchModeChange(isSearch: Boolean) {}
    }

    override fun setText(text: String) {
        enterEditMode()
        searchContentEdit.setText(text)
    }
}