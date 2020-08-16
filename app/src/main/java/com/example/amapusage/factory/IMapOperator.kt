package com.example.amapusage.factory

import android.widget.ImageButton
import com.amap.api.maps.AMap
import com.amap.api.maps.TextureMapView

interface IMapOperator {
    interface Operator {
        fun preWork(tMV: TextureMapView, lt: LocationSourceLister): Operator  // 不持有tMV
        fun buildMapBaseConfig(): AMap      // 配置
        fun bindCurrentButton(btn: ImageButton, state:LocateCurrentState): Operator
        fun clearCenterMark(): Operator
        fun setUpCenterMark(): Operator
        fun getMap(): AMap
        fun moveToCurrent()                 // 定位
        fun endOperate()
        fun initAction()
        fun queryByText(queryText: String)
    }

    interface LocationSourceLister {
        fun moveCameraFinish()
        fun onMoveChange()
        fun startLoadNewData()
        fun loadDataDone()
    }

    interface LocateCurrentState{
       fun performLocateCurrent(isOnAnimation: Boolean):Boolean // 传入进行判断是否需要消费，返回是否消费
    }
}