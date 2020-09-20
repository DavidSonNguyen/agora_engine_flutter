package com.agora.agora_plugin;

import android.content.Context;
import android.view.SurfaceView;

import java.util.HashMap;

import io.agora.rtc.RtcEngine;
import io.flutter.plugin.common.MessageCodec;
import io.flutter.plugin.platform.PlatformView;
import io.flutter.plugin.platform.PlatformViewFactory;

public class AgoraRenderViewFactory extends PlatformViewFactory {
    private HashMap<String, SurfaceView> mRendererViews = new HashMap<>();

    public void addView(SurfaceView view, int id) {
        mRendererViews.put("" + id, view);
    }

    public void removeView(int id) {
        mRendererViews.remove("" + id);
    }

    public SurfaceView getView(int id) {
        return mRendererViews.get("" + id);
    }

    public AgoraRenderViewFactory(MessageCodec<Object> createArgsCodec) {
        super(createArgsCodec);
    }

    @Override
    public PlatformView create(Context context, int id, Object o) {
        SurfaceView view = RtcEngine.CreateRendererView(context.getApplicationContext());
        AgoraRendererView rendererView = new AgoraRendererView(view, id);
        addView(view, id);
        return rendererView;
    }
}
