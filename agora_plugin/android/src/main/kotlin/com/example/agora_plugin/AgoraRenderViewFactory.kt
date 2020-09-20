package com.example.agora_plugin

import android.content.Context
import android.view.SurfaceView
import io.agora.rtc.RtcEngine
import io.flutter.plugin.common.MessageCodec
import io.flutter.plugin.platform.PlatformView
import io.flutter.plugin.platform.PlatformViewFactory
import java.util.*

class AgoraRenderViewFactory(createArgsCodec: MessageCodec<Any?>?) : PlatformViewFactory(createArgsCodec) {
    private val mRendererViews = HashMap<String, SurfaceView>()
    fun addView(view: SurfaceView, id: Int) {
        mRendererViews["" + id] = view
    }

    fun removeView(id: Int) {
        mRendererViews.remove("" + id)
    }

    fun getView(id: Int): SurfaceView? {
        return mRendererViews["" + id]
    }

    override fun create(context: Context, id: Int, o: Any): PlatformView {
        val view = RtcEngine.CreateRendererView(context.applicationContext)
        val rendererView = AgoraRendererView(view, id)
        addView(view, id)
        return rendererView
    }
}