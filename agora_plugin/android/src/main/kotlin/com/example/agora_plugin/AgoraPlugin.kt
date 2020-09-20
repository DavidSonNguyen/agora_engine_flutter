package com.example.agora_plugin

import android.os.Handler
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.FlutterPlugin.FlutterPluginBinding
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry.Registrar
import io.flutter.plugin.common.StandardMessageCodec

/** AgoraPlugin */
class AgoraPlugin : FlutterPlugin {
  private var methodHandler: MethodCallHandlerImpl? = null
  private var eventHandler: RtcEventHandlerImpl? = null
  private var fac: AgoraRenderViewFactory? = null
  override fun onAttachedToEngine(binding: FlutterPluginBinding) {
    fac = AgoraRenderViewFactory(StandardMessageCodec.INSTANCE)
    binding.platformViewRegistry.registerViewFactory("AgoraRendererView", fac)
    eventHandler = RtcEventHandlerImpl()
    eventHandler!!.startListening(binding.binaryMessenger)
    methodHandler = MethodCallHandlerImpl(binding.applicationContext, fac!!, eventHandler!!)
    methodHandler!!.startListening(binding.binaryMessenger)
  }

  override fun onDetachedFromEngine(binding: FlutterPluginBinding) {
    fac = null
    methodHandler = null
    eventHandler = null
  }

  private class MethodResultWrapper internal constructor(private val mResult: Result, private val mHandler: Handler) : Result {
    override fun success(result: Any?) {
      mHandler.post { mResult.success(result) }
    }

    override fun error(errorCode: String, errorMessage: String?,
                       errorDetails: Any?) {
      mHandler.post { mResult.error(errorCode, errorMessage, errorDetails) }
    }

    override fun notImplemented() {
      mHandler.post { mResult.notImplemented() }
    }
  }

  companion object {
    /**
     * Plugin registration.
     */
    fun registerWith(registrar: Registrar) {
      val fac = AgoraRenderViewFactory(StandardMessageCodec.INSTANCE)
      registrar.platformViewRegistry().registerViewFactory("AgoraRendererView", fac)
      val eventHandler = RtcEventHandlerImpl()
      eventHandler.startListening(registrar.messenger())
      val handler = MethodCallHandlerImpl(if (registrar.activity() != null) registrar.activity() else registrar.context(), fac, eventHandler)
      handler.startListening(registrar.messenger())
    }
  }
}
