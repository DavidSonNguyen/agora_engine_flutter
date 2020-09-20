package com.example.agora_plugin

import android.view.SurfaceView
import android.view.View
import io.flutter.plugin.platform.PlatformView

class AgoraRendererView internal constructor(private var mSurfaceView: SurfaceView?, uid: Int) : PlatformView {
    private val uid: Long
    override fun getView(): View? {
        return mSurfaceView
    }

    override fun dispose() {
        mSurfaceView = null
    }

    init {
        this.uid = uid.toLong()
    }
}