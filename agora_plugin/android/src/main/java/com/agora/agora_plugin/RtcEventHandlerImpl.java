package com.agora.agora_plugin;

import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.models.UserInfo;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.EventChannel;

import static android.content.ContentValues.TAG;

public class RtcEventHandlerImpl extends IRtcEngineEventHandler implements EventChannel.StreamHandler {

    private final Handler mEventHandler = new Handler(Looper.getMainLooper());
    private EventChannel.EventSink sink;
    private EventChannel channel;

    public void startListening(BinaryMessenger messenger) {
        if (channel != null) {
            Log.wtf(TAG, "Setting a method call handler before the last was disposed.");
            stopListening();
        }

        channel = new EventChannel(messenger, "agora_event_channel");
        channel.setStreamHandler(this);
    }

    public void stopListening() {
        if (channel == null) {
            Log.d(TAG, "Tried to stop listening when no EventChannel had been initialized.");
            return;
        }

        channel.setStreamHandler(null);
        channel = null;
    }

    public void sendEvent(final String eventName, final HashMap<String, Object> map) {
        map.put("event", eventName);
        mEventHandler.post(new Runnable() {
            @Override
            public void run() {
                if (sink != null) {
                    sink.success(map);
                }
            }
        });
    }

    @Override
    public void onListen(Object arguments, EventChannel.EventSink events) {
        this.sink = events;
    }

    @Override
    public void onCancel(Object arguments) {
        this.sink = null;
    }

    @Override
    public void onWarning(int warn) {
        super.onWarning(warn);
        HashMap<String, Object> map = new HashMap<>();
        map.put("warn", warn);
        sendEvent("onWarning", map);
    }

    @Override
    public void onError(int err) {
        super.onError(err);
        HashMap<String, Object> map = new HashMap<>();
        map.put("errorCode", err);
        sendEvent("onError", map);
    }

    @Override
    public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
        super.onJoinChannelSuccess(channel, uid, elapsed);
        HashMap<String, Object> map = new HashMap<>();
        map.put("channel", channel);
        map.put("uid", uid);
        map.put("elapsed", elapsed);
        sendEvent("onJoinChannelSuccess", map);
    }

    @Override
    public void onRejoinChannelSuccess(String channel, int uid, int elapsed) {
        super.onRejoinChannelSuccess(channel, uid, elapsed);
        HashMap<String, Object> map = new HashMap<>();
        map.put("channel", channel);
        map.put("uid", uid);
        map.put("elapsed", elapsed);
        sendEvent("onRejoinChannelSuccess", map);
    }

    @Override
    public void onLeaveChannel(RtcStats stats) {
        super.onLeaveChannel(stats);
        HashMap<String, Object> map = new HashMap<>();
        map.put("stats", mapFromStats(stats));
        sendEvent("onLeaveChannel", map);
    }

    @Override
    public void onClientRoleChanged(int oldRole, int newRole) {
        super.onClientRoleChanged(oldRole, newRole);
        HashMap<String, Object> map = new HashMap<>();
        map.put("oldRole", oldRole);
        map.put("newRole", newRole);
        sendEvent("onClientRoleChanged", map);
    }

    @Override
    public void onUserJoined(int uid, int elapsed) {
        super.onUserJoined(uid, elapsed);
        HashMap<String, Object> map = new HashMap<>();
        map.put("uid", uid);
        map.put("elapsed", elapsed);
        sendEvent("onUserJoined", map);
    }

    @Override
    public void onUserOffline(int uid, int reason) {
        super.onUserOffline(uid, reason);
        HashMap<String, Object> map = new HashMap<>();
        map.put("uid", uid);
        map.put("reason", reason);
        sendEvent("onUserOffline", map);
    }

    @Override
    public void onLocalUserRegistered(int uid, String userAccount) {
        super.onLocalUserRegistered(uid, userAccount);
        HashMap<String, Object> map = new HashMap<>();
        map.put("uid", uid);
        map.put("userAccount", userAccount);
        sendEvent("onRegisteredLocalUser", map);
    }

    @Override
    public void onUserInfoUpdated(int uid, UserInfo userInfo) {
        super.onUserInfoUpdated(uid, userInfo);
        HashMap<String, Object> map = new HashMap<>();
        map.put("uid", uid);
        HashMap<String, Object> userInfoMap = new HashMap<>();
        userInfoMap.put("uid", userInfo.uid);
        userInfoMap.put("userAccount", userInfo.userAccount);
        map.put("userInfo", userInfoMap);
        sendEvent("onUpdatedUserInfo", map);
    }

    @Override
    public void onConnectionStateChanged(int state, int reason) {
        super.onConnectionStateChanged(state, reason);
        HashMap<String, Object> map = new HashMap<>();
        map.put("state", state);
        map.put("reason", reason);
        sendEvent("onConnectionStateChanged", map);
    }

    @Override
    public void onNetworkTypeChanged(int type) {
        super.onNetworkTypeChanged(type);
        HashMap<String, Object> map = new HashMap<>();
        map.put("type", type);
        sendEvent("onNetworkTypeChanged", map);
    }

    @Override
    public void onConnectionLost() {
        super.onConnectionLost();
        sendEvent("onConnectionLost", null);
    }

    @Override
    public void onApiCallExecuted(int error, String api, String result) {
        super.onApiCallExecuted(error, api, result);
        HashMap<String, Object> map = new HashMap<>();
        map.put("errorCode", error);
        map.put("api", api);
        map.put("result", result);
        sendEvent("onApiCallExecuted", map);
    }

    @Override
    public void onTokenPrivilegeWillExpire(String token) {
        super.onTokenPrivilegeWillExpire(token);
        HashMap<String, Object> map = new HashMap<>();
        map.put("token", token);
        sendEvent("onTokenPrivilegeWillExpire", map);
    }

    @Override
    public void onRequestToken() {
        super.onRequestToken();
        sendEvent("onRequestToken", null);
    }

    @Override
    public void onAudioVolumeIndication(AudioVolumeInfo[] speakers, int totalVolume) {
        super.onAudioVolumeIndication(speakers, totalVolume);
        HashMap<String, Object> map = new HashMap<>();
        map.put("totalVolume", totalVolume);
        map.put("speakers", arrayFromSpeakers(speakers));
        sendEvent("onAudioVolumeIndication", map);
    }

    @Override
    public void onActiveSpeaker(int uid) {
        super.onActiveSpeaker(uid);
        HashMap<String, Object> map = new HashMap<>();
        map.put("uid", uid);
        sendEvent("onActiveSpeaker", map);
    }

    @Override
    public void onFirstLocalAudioFrame(int elapsed) {
        super.onFirstLocalAudioFrame(elapsed);
        HashMap<String, Object> map = new HashMap<>();
        map.put("elapsed", elapsed);
        sendEvent("onFirstLocalAudioFrame", map);
    }

    @Override
    public void onFirstRemoteAudioFrame(int uid, int elapsed) {
        super.onFirstRemoteAudioFrame(uid, elapsed);
        HashMap<String, Object> map = new HashMap<>();
        map.put("uid", uid);
        map.put("elapsed", elapsed);
        sendEvent("onFirstRemoteAudioFrame", map);
    }

    @Override
    public void onFirstRemoteAudioDecoded(int uid, int elapsed) {
        super.onFirstRemoteAudioDecoded(uid, elapsed);
        HashMap<String, Object> map = new HashMap<>();
        map.put("uid", uid);
        map.put("elapsed", elapsed);
        sendEvent("onFirstRemoteAudioDecoded", map);
    }

    @Override
    public void onFirstLocalVideoFrame(int width, int height, int elapsed) {
        super.onFirstLocalVideoFrame(width, height, elapsed);
        HashMap<String, Object> map = new HashMap<>();
        map.put("width", width);
        map.put("height", height);
        map.put("elapsed", elapsed);
        sendEvent("onFirstLocalVideoFrame", map);
    }

    @Override
    public void onFirstRemoteVideoFrame(int uid, int width, int height, int elapsed) {
        super.onFirstRemoteVideoFrame(uid, width, height, elapsed);
        HashMap<String, Object> map = new HashMap<>();
        map.put("uid", uid);
        map.put("width", width);
        map.put("height", height);
        map.put("elapsed", elapsed);
        sendEvent("onFirstRemoteVideoFrame", map);
    }

    @Override
    public void onUserMuteAudio(int uid, boolean muted) {
        super.onUserMuteAudio(uid, muted);
        HashMap<String, Object> map = new HashMap<>();
        map.put("uid", uid);
        map.put("muted", muted);
        sendEvent("onUserMuteAudio", map);
    }

//        @Override
//        public void onUserMuteVideo(int uid , boolean muted) {
//            super.onUserMuteAudio(uid , muted);
//            HashMap<String, Object> map = new HashMap<>();
//            map.put("uid" , uid);
//            map.put("muted" , muted);
//            sendEvent("onUserMuteVideo" , map);
//        }

    @Override
    public void onVideoSizeChanged(int uid, int width, int height, int rotation) {
        super.onVideoSizeChanged(uid, width, height, rotation);
        HashMap<String, Object> map = new HashMap<>();
        map.put("uid", uid);
        map.put("width", width);
        map.put("height", height);
        map.put("rotation", rotation);
        sendEvent("onVideoSizeChanged", map);
    }

    @Override
    public void onRemoteVideoStateChanged(int uid,
                                          int state,
                                          int reason,
                                          int elapsed) {
        super.onRemoteVideoStateChanged(uid, state, reason, elapsed);
        HashMap<String, Object> map = new HashMap<>();
        map.put("uid", uid);
        map.put("state", state);
        map.put("reason", reason);
        map.put("elapsed", elapsed);
        sendEvent("onRemoteVideoStateChanged", map);
    }

    @Override
    public void onLocalVideoStateChanged(int state, int error) {
        super.onLocalVideoStateChanged(state, error);
        HashMap<String, Object> map = new HashMap<>();
        map.put("localVideoState", state);
        map.put("errorCode", error);
        sendEvent("onLocalVideoStateChanged", map);
    }

    @Override
    public void onRemoteAudioStateChanged(int uid, int state, int reason, int elapsed) {
        super.onRemoteAudioStateChanged(uid, state, reason, elapsed);
        HashMap<String, Object> map = new HashMap<>();
        map.put("uid", uid);
        map.put("state", state);
        map.put("reason", reason);
        map.put("elapsed", elapsed);
        sendEvent("onRemoteAudioStateChanged", map);
    }

    @Override
    public void onLocalAudioStateChanged(int state, int error) {
        super.onLocalAudioStateChanged(state, error);
        HashMap<String, Object> map = new HashMap<>();
        map.put("state", state);
        map.put("errorCode", error);
        sendEvent("onLocalAudioStateChanged", map);
    }

    @Override
    public void onLocalPublishFallbackToAudioOnly(boolean isFallbackOrRecover) {
        super.onLocalPublishFallbackToAudioOnly(isFallbackOrRecover);
        HashMap<String, Object> map = new HashMap<>();
        map.put("isFallbackOrRecover", isFallbackOrRecover);
        sendEvent("onLocalPublishFallbackToAudioOnly", map);
    }

    @Override
    public void onRemoteSubscribeFallbackToAudioOnly(int uid, boolean isFallbackOrRecover) {
        super.onRemoteSubscribeFallbackToAudioOnly(uid, isFallbackOrRecover);
        HashMap<String, Object> map = new HashMap<>();
        map.put("uid", uid);
        map.put("isFallbackOrRecover", isFallbackOrRecover);
        sendEvent("onRemoteSubscribeFallbackToAudioOnly", map);
    }

    @Override
    public void onAudioRouteChanged(int routing) {
        super.onAudioRouteChanged(routing);
        HashMap<String, Object> map = new HashMap<>();
        map.put("routing", routing);
        sendEvent("onAudioRouteChanged", map);
    }

    @Override
    public void onCameraFocusAreaChanged(Rect rect) {
        super.onCameraFocusAreaChanged(rect);
        HashMap<String, Object> map = new HashMap<>();
        map.put("rect", mapFromRect(rect));
        sendEvent("onCameraFocusAreaChanged", map);
    }

    @Override
    public void onCameraExposureAreaChanged(Rect rect) {
        super.onCameraExposureAreaChanged(rect);
        HashMap<String, Object> map = new HashMap<>();
        map.put("rect", mapFromRect(rect));
        sendEvent("onCameraExposureAreaChanged", map);
    }

    @Override
    public void onRtcStats(RtcStats stats) {
        super.onRtcStats(stats);
        HashMap<String, Object> map = new HashMap<>();
        map.put("stats", mapFromStats(stats));
        sendEvent("onRtcStats", map);
    }

    @Override
    public void onLastmileQuality(int quality) {
        super.onLastmileQuality(quality);
        HashMap<String, Object> map = new HashMap<>();
        map.put("quality", quality);
        sendEvent("onLastmileQuality", map);
    }

    @Override
    public void onNetworkQuality(int uid, int txQuality, int rxQuality) {
        super.onNetworkQuality(uid, txQuality, rxQuality);
        HashMap<String, Object> map = new HashMap<>();
        map.put("uid", uid);
        map.put("txQuality", txQuality);
        map.put("rxQuality", rxQuality);
        sendEvent("onNetworkQuality", map);
    }

    @Override
    public void onLastmileProbeResult(LastmileProbeResult result) {
        super.onLastmileProbeResult(result);
        HashMap<String, Object> map = new HashMap<>();
        map.put("state", result.state);
        map.put("rtt", result.rtt);
        HashMap<String, Object> uplinkReport = new HashMap<>();
        map.put("uplinkReport", uplinkReport);
        uplinkReport.put("availableBandwidth", result.uplinkReport.availableBandwidth);
        uplinkReport.put("jitter", result.uplinkReport.jitter);
        uplinkReport.put("packetLossRate", result.uplinkReport.packetLossRate);
        HashMap<String, Object> downlinkReport = new HashMap<>();
        map.put("downlinkReport", downlinkReport);
        uplinkReport.put("availableBandwidth", result.downlinkReport.availableBandwidth);
        uplinkReport.put("jitter", result.downlinkReport.jitter);
        uplinkReport.put("packetLossRate", result.downlinkReport.packetLossRate);
        sendEvent("onLastmileProbeTestResult", map);
    }

    @Override
    public void onLocalVideoStats(LocalVideoStats stats) {
        super.onLocalVideoStats(stats);
        HashMap<String, Object> map = new HashMap<>();
        map.put("stats", mapFromLocalVideoStats(stats));
        sendEvent("onLocalVideoStats", map);
    }

    @Override
    public void onLocalAudioStats(LocalAudioStats stats) {
        super.onLocalAudioStats(stats);
        HashMap<String, Object> map = new HashMap<>();
        map.put("stats", mapFromLocalAudioStats(stats));
        sendEvent("onLocalAudioStats", map);
    }

    @Override
    public void onRemoteVideoStats(RemoteVideoStats stats) {
        super.onRemoteVideoStats(stats);
        HashMap<String, Object> map = new HashMap<>();
        map.put("stats", mapFromRemoteVideoStats(stats));
        sendEvent("onRemoteVideoStats", map);
    }

    @Override
    public void onRemoteAudioStats(RemoteAudioStats stats) {
        super.onRemoteAudioStats(stats);
        HashMap<String, Object> map = new HashMap<>();
        map.put("stats", mapFromRemoteAudioStats(stats));
        sendEvent("onRemoteAudioStats", map);
    }

    @Override
    public void onAudioMixingStateChanged(int state, int errorCode) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("state", state);
        map.put("errorCode", errorCode);
        sendEvent("onLocalAudioMixingStateChanged", map);
    }

    @Override
    public void onAudioEffectFinished(int soundId) {
        super.onAudioEffectFinished(soundId);
        HashMap<String, Object> map = new HashMap<>();
        map.put("soundId", soundId);
        sendEvent("onAudioEffectFinished", map);
    }

    @Override
    public void onStreamPublished(String url, int error) {
        super.onStreamPublished(url, error);
        HashMap<String, Object> map = new HashMap<>();
        map.put("url", url);
        map.put("errorCode", error);
        sendEvent("onStreamPublished", map);
    }

    @Override
    public void onStreamUnpublished(String url) {
        super.onStreamUnpublished(url);
        HashMap<String, Object> map = new HashMap<>();
        map.put("url", url);
        sendEvent("onStreamUnpublished", map);
    }

    @Override
    public void onTranscodingUpdated() {
        super.onTranscodingUpdated();
        sendEvent("onTranscodingUpdated", null);
    }

    @Override
    public void onRtmpStreamingStateChanged(String url,
                                            int state,
                                            int errCode) {
        super.onRtmpStreamingStateChanged(url, state, errCode);
        HashMap<String, Object> map = new HashMap<>();
        map.put("url", url);
        map.put("state", state);
        map.put("errorCode", errCode);
        sendEvent("onRtmpStreamingStateChanged", map);
    }


    @Override
    public void onStreamInjectedStatus(String url, int uid, int status) {
        super.onStreamInjectedStatus(url, uid, status);
        HashMap<String, Object> map = new HashMap<>();
        map.put("url", url);
        map.put("uid", uid);
        map.put("status", status);
        sendEvent("onStreamInjectedStatus", map);
    }

    @Override
    public void onMediaEngineLoadSuccess() {
        super.onMediaEngineLoadSuccess();
        sendEvent("onMediaEngineLoadSuccess", null);
    }

    @Override
    public void onMediaEngineStartCallSuccess() {
        super.onMediaEngineStartCallSuccess();
        sendEvent("onMediaEngineStartCallSuccess", null);
    }

    @Override
    public void onChannelMediaRelayStateChanged(int state, int code) {
        super.onChannelMediaRelayStateChanged(state, code);
        HashMap<String, Object> map = new HashMap<>();
        map.put("state", state);
        map.put("errorCode", code);
        sendEvent("onChannelMediaRelayChanged", map);
    }

    @Override
    public void onChannelMediaRelayEvent(int code) {
        super.onChannelMediaRelayEvent(code);
        HashMap<String, Object> map = new HashMap<>();
        map.put("event", code);
        sendEvent("onReceivedChannelMediaRelayEvent", map);
    }

    @Override
    public void onAudioMixingFinished() {
        super.onAudioMixingFinished();
        sendEvent("onAudioMixingFinished", null);
    }

    private HashMap<String, Object> mapFromStats(RtcStats stats) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("totalDuration", stats.totalDuration);
        map.put("txBytes", stats.txBytes);
        map.put("rxBytes", stats.rxBytes);
        map.put("txAudioBytes", stats.txAudioBytes);
        map.put("txVideoBytes", stats.txVideoBytes);
        map.put("rxAudioBytes", stats.rxAudioBytes);
        map.put("rxVideoBytes", stats.rxVideoBytes);
        map.put("txKBitrate", stats.txKBitRate);
        map.put("rxKBitrate", stats.rxKBitRate);
        map.put("txAudioKBitrate", stats.txAudioKBitRate);
        map.put("rxAudioKBitrate", stats.rxAudioKBitRate);
        map.put("txVideoKBitrate", stats.txVideoKBitRate);
        map.put("rxVideoKBitrate", stats.rxVideoKBitRate);
        map.put("lastmileDelay", stats.lastmileDelay);
        map.put("txPacketLossRate", stats.txPacketLossRate);
        map.put("rxPacketLossRate", stats.rxPacketLossRate);
        map.put("users", stats.users);
        map.put("cpuAppUsage", stats.cpuAppUsage);
        map.put("cpuTotalUsage", stats.cpuTotalUsage);
        return map;
    }

    private HashMap<String, Object> mapFromRect(Rect rect) {
        HashMap<String, Object> map = new HashMap<>();

        map.put("x", rect.left);
        map.put("y", rect.top);
        map.put("width", rect.width());
        map.put("height", rect.height());
        return map;
    }

    private HashMap<String, Object> mapFromLocalVideoStats(LocalVideoStats stats) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("sentBitrate", stats.sentBitrate);
        map.put("sentFrameRate", stats.sentFrameRate);
        map.put("encoderOutputFrameRate", stats.encoderOutputFrameRate);
        map.put("rendererOutputFrameRate", stats.rendererOutputFrameRate);
        map.put("sentTargetBitrate", stats.targetBitrate);
        map.put("sentTargetFrameRate", stats.targetFrameRate);
        map.put("qualityAdaptIndication", stats.targetBitrate);
        map.put("encodedBitrate", stats.targetBitrate);
        map.put("encodedFrameWidth", stats.targetBitrate);
        map.put("encodedFrameHeight", stats.encodedFrameHeight);
        map.put("encodedFrameCount", stats.encodedFrameCount);
        map.put("codecType", stats.codecType);
        return map;
    }

    private HashMap<String, Object> mapFromLocalAudioStats(LocalAudioStats stats) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("numChannels", stats.numChannels);
        map.put("sentSampleRate", stats.sentSampleRate);
        map.put("sentBitrate", stats.sentBitrate);
        return map;
    }

    private HashMap<String, Object> mapFromRemoteVideoStats(RemoteVideoStats stats) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("uid", stats.uid);
        map.put("width", stats.width);
        map.put("height", stats.height);
        map.put("receivedBitrate", stats.receivedBitrate);
        map.put("decoderOutputFrameRate", stats.decoderOutputFrameRate);
        map.put("rendererOutputFrameRate", stats.rendererOutputFrameRate);
        map.put("packetLossRate", stats.packetLossRate);
        map.put("rxStreamType", stats.rxStreamType);
        map.put("totalFrozenTime", stats.totalFrozenTime);
        map.put("frozenRate", stats.frozenRate);
        return map;
    }

    private HashMap<String, Object> mapFromRemoteAudioStats(RemoteAudioStats stats) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("uid", stats.uid);
        map.put("quality", stats.quality);
        map.put("networkTransportDelay", stats.networkTransportDelay);
        map.put("jitterBufferDelay", stats.jitterBufferDelay);
        map.put("audioLossRate", stats.audioLossRate);
        map.put("numChannels", stats.numChannels);
        map.put("receivedSampleRate", stats.receivedSampleRate);
        map.put("receivedBitrate", stats.receivedBitrate);
        map.put("totalFrozenTime", stats.totalFrozenTime);
        map.put("frozenRate", stats.frozenRate);
        return map;
    }

    private ArrayList<HashMap<String, Object>> arrayFromSpeakers(AudioVolumeInfo[] speakers) {
        ArrayList<HashMap<String, Object>> list = new ArrayList<>();

        for (AudioVolumeInfo info : speakers) {
            HashMap<String, Object> map = new HashMap<>();
            map.put("uid", info.uid);
            map.put("volume", info.volume);

            list.add(map);
        }

        return list;
    }
}
