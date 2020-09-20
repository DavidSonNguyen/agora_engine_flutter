package com.agora.agora_plugin;

import android.os.Handler;

import androidx.annotation.NonNull;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry;
import io.flutter.plugin.common.PluginRegistry.Registrar;
import io.flutter.plugin.common.StandardMessageCodec;

/** AgoraPlugin */
public class AgoraPlugin implements FlutterPlugin {
  private MethodCallHandlerImpl methodHandler;
  private RtcEventHandlerImpl eventHandler;
  private AgoraRenderViewFactory fac;

  /**
   * Plugin registration.
   */
  public static void registerWith(PluginRegistry.Registrar registrar) {
    AgoraRenderViewFactory fac = new AgoraRenderViewFactory(StandardMessageCodec.INSTANCE);
    registrar.platformViewRegistry().registerViewFactory("AgoraRendererView", fac);

    RtcEventHandlerImpl eventHandler = new RtcEventHandlerImpl();
    eventHandler.startListening(registrar.messenger());

    MethodCallHandlerImpl handler =
            new MethodCallHandlerImpl((registrar.activity() != null) ? registrar.activity() : registrar.context(), fac, eventHandler);
    handler.startListening(registrar.messenger());
  }

  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding binding) {
    fac = new AgoraRenderViewFactory(StandardMessageCodec.INSTANCE);
    binding.getPlatformViewRegistry().registerViewFactory("AgoraRendererView", fac);
    eventHandler = new RtcEventHandlerImpl();
    eventHandler.startListening(binding.getBinaryMessenger());
    methodHandler = new MethodCallHandlerImpl(binding.getApplicationContext(), fac, eventHandler);
    methodHandler.startListening(binding.getBinaryMessenger());
  }

  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
    fac = null;
    methodHandler = null;
    eventHandler = null;
  }

  private static class MethodResultWrapper implements MethodChannel.Result {
    private MethodChannel.Result mResult;
    private Handler mHandler;

    MethodResultWrapper(MethodChannel.Result result, Handler handler) {
      this.mResult = result;
      this.mHandler = handler;
    }

    @Override
    public void success(final Object result) {
      mHandler.post(new Runnable() {
        @Override
        public void run() {
          mResult.success(result);
        }
      });
    }

    @Override
    public void error(final String errorCode, final String errorMessage,
                      final Object errorDetails) {
      mHandler.post(new Runnable() {
        @Override
        public void run() {
          mResult.error(errorCode, errorMessage, errorDetails);
        }
      });
    }

    @Override
    public void notImplemented() {
      mHandler.post(new Runnable() {
        @Override
        public void run() {
          mResult.notImplemented();
        }
      });
    }
  }
}
