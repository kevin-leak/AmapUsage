package com.example.amapusage.sheet

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.amapusage.R
import kotlinx.android.synthetic.main.fragment_bottom_sheet.*


class BottomSheetFragment(val manager: FragmentManager) : DialogFragment() {

    private var tvCancelVisibility: Int = View.VISIBLE
    private var titleVisibility: Int = View.VISIBLE
    private var root: View? = null
    private var window: Window? = null
    private var adapter: BottomSheetAdapter = BottomSheetAdapter()
    private var resId = R.layout.fragment_bottom_sheet

    constructor(manager: FragmentManager, actions: MutableList<out SheetAction>) : this(manager) {
        this.adapter = BottomSheetAdapter(actions)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
        root = inflater.inflate(R.layout.fragment_bottom_sheet, null)
        return root
    }

    fun setSheetLayout(resId: Int) {
        this.resId = resId
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tvCancel.setOnClickListener { this.dismiss() }
        sheetTitle.visibility = titleVisibility
        tvCancel.visibility = tvCancelVisibility
        recycleView.layoutManager = LinearLayoutManager(this.context)
        recycleView.adapter = adapter
        adapter.setColumnClick(object : SheetClickListener {
            override fun onColumnClickListener(position: Int, sheetAction: SheetAction) {
                onColumnClick(position, sheetAction)
                dismiss()
            }
        })
        recycleView.isNestedScrollingEnabled = false
    }

    private fun onColumnClick(position: Int, sheetAction: SheetAction) {
        sheetAction.commonAction()
    }

    override fun onStart() {
        super.onStart()
        // 下面这些设置必须在此方法(onStart())中才有效
        window = dialog!!.window
        // 如果不设置这句代码, 那么弹框就会与四边都有一定的距离
        window?.setBackgroundDrawableResource(R.color.transparent)
        // 设置动画
        window?.setWindowAnimations(R.style.bottomDialogAnimation)
        val params: WindowManager.LayoutParams = window!!.attributes
        params.gravity = Gravity.BOTTOM
        // 如果不设置宽度,那么即使你在布局中设置宽度为 match_parent 也不会起作用
        params.width = resources.displayMetrics.widthPixels
        window?.attributes = params
    }

    private fun setTitleAction(action: SheetAction) {
        sheetTitle.visibility = View.VISIBLE
        sheetTitle.text = action.description
        sheetTitle.setOnClickListener { action.commonAction() }
    }

    fun setVisibleCancel(visibility: Int) {
        if (tvCancel == null) tvCancelVisibility = visibility
        else tvCancel.visibility = tvCancelVisibility
    }

    fun setVisibleTitle(visibility: Int) {
        if (tvCancel == null) titleVisibility = visibility
        else tvCancel.visibility = titleVisibility
    }

    fun setSheets(sheetsAction: MutableList<out SheetAction>): BottomSheetFragment {
        Log.e("kyle-map", "setSheets: $sheetsAction")
        adapter.setAction(sheetsAction)
        return this
    }

    fun setFocusSheet(holder: BottomSheetAdapter.SheetHolder) {
        adapter.focusHolder = holder
    }

    fun setFocusSheet(sheetsAction: SheetAction, holder: BottomSheetAdapter.SheetHolder) {
        adapter.focusHolder = holder
        adapter.setFocusAction(sheetsAction)
    }

    fun show() {
        super.show(manager, "BottomSheetFragment")
    }
}