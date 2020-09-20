package com.agora.agora_plugin;

import android.view.SurfaceView;
import android.view.View;

import io.flutter.plugin.platform.PlatformView;

public class AgoraRendererView implements PlatformView {
    private SurfaceView mSurfaceView;
    private final long uid;

    AgoraRendererView(SurfaceView surfaceView, int uid) {
        this.mSurfaceView = surfaceView;
        this.uid = uid;
    }

    @Override
    public View getView() {
        return mSurfaceView;
    }

    @Override
    public void dispose() {
        mSurfaceView = null;
    }
}
