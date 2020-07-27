package com.example.amapusage

import android.app.Application
import android.content.Context

class App : Application() {
    companion object{
        private lateinit var appContext:Context
        fun getAppContext():Context{
            return appContext
        }
    }

    override fun onCreate() {
        super.onCreate()
        appContext = this
    }
}