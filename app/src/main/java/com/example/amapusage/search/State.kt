package com.example.amapusage.search

import androidx.lifecycle.MutableLiveData

open class State {
    open lateinit var list: MutableLiveData<MutableList<CheckModel>>
    open var lastPosition: Int = 0
    open val size: Int
        get() {
            return list.value?.size ?: 0
        }

    open fun save() {}
    open fun reSet() {}
    open fun action(position: Int) {}
}