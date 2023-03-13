package com.vaca.xutils_android

import android.app.Application
import org.xutils.BuildConfig
import org.xutils.x

class MyApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        x.Ext.init(this);
        x.Ext.setDebug(BuildConfig.DEBUG);
    }
}