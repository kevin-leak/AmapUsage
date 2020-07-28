package com.example.amapusage.search

interface ILocationSearchView {
    interface OnLocationSourceChange {
        fun onFocusChange(isFocus:Boolean)
        fun locationSourceCome(data: String)
        fun locationSourceChanging(data: String)
        fun beforeLocationSourceChange(toString: String)
    }
}