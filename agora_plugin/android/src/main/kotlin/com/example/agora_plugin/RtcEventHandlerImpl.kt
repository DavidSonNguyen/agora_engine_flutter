package com.example.agora_plugin

import android.content.ContentValues
import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.util.Log
import io.agora.rtc.IRtcEngineEventHandler
import io.agora.rtc.models.UserInfo
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.EventChannel.EventSink
import java.util.*

class RtcEventHandlerImpl : IRtcEngineEventHandler(), EventChannel.StreamHandler {
    private val mEventHandler = Handler(Looper.getMainLooper())
    private var sink: EventSink? = null
    private var channel: EventChannel? = null
    fun startListening(messenger: BinaryMessenger?) {
        if (channel != null) {
            Log.wtf(ContentValues.TAG, "Setting a method call handler before the last was disposed.")
            stopListening()
        }
        channel = EventChannel(messenger, "agora_event_channel")
        channel!!.setStreamHandler(this)
    }

    fun stopListening() {
        if (channel == null) {
            Log.d(ContentValues.TAG, "Tried to stop listening when no EventChannel had been initialized.")
            return
        }
        channel!!.setStreamHandler(null)
        channel = null
    }

    fun sendEvent(eventName: String?, map: HashMap<String?, Any?>?) {
        map!!["event"] = eventName
        mEventHandler.post {
            if (sink != null) {
                sink!!.success(map)
            }
        }
    }

    override fun onListen(arguments: Any, events: EventSink) {
        sink = events
    }

    override fun onCancel(arguments: Any) {
        sink = null
    }

    override fun onWarning(warn: Int) {
        super.onWarning(warn)
        val map = HashMap<String?, Any?>()
        map["warn"] = warn
        sendEvent("onWarning", map)
    }

    override fun onError(err: Int) {
        super.onError(err)
        val map = HashMap<String?, Any?>()
        map["errorCode"] = err
        sendEvent("onError", map)
    }

    override fun onJoinChannelSuccess(channel: String, uid: Int, elapsed: Int) {
        super.onJoinChannelSuccess(channel, uid, elapsed)
        val map = HashMap<String?, Any?>()
        map["channel"] = channel
        map["uid"] = uid
        map["elapsed"] = elapsed
        sendEvent("onJoinChannelSuccess", map)
    }

    override fun onRejoinChannelSuccess(channel: String, uid: Int, elapsed: Int) {
        super.onRejoinChannelSuccess(channel, uid, elapsed)
        val map = HashMap<String?, Any?>()
        map["channel"] = channel
        map["uid"] = uid
        map["elapsed"] = elapsed
        sendEvent("onRejoinChannelSuccess", map)
    }

    override fun onLeaveChannel(stats: RtcStats) {
        super.onLeaveChannel(stats)
        val map = HashMap<String?, Any?>()
        map["stats"] = mapFromStats(stats)
        sendEvent("onLeaveChannel", map)
    }

    override fun onClientRoleChanged(oldRole: Int, newRole: Int) {
        super.onClientRoleChanged(oldRole, newRole)
        val map = HashMap<String?, Any?>()
        map["oldRole"] = oldRole
        map["newRole"] = newRole
        sendEvent("onClientRoleChanged", map)
    }

    override fun onUserJoined(uid: Int, elapsed: Int) {
        super.onUserJoined(uid, elapsed)
        val map = HashMap<String?, Any?>()
        map["uid"] = uid
        map["elapsed"] = elapsed
        sendEvent("onUserJoined", map)
    }

    override fun onUserOffline(uid: Int, reason: Int) {
        super.onUserOffline(uid, reason)
        val map = HashMap<String?, Any?>()
        map["uid"] = uid
        map["reason"] = reason
        sendEvent("onUserOffline", map)
    }

    override fun onLocalUserRegistered(uid: Int, userAccount: String) {
        super.onLocalUserRegistered(uid, userAccount)
        val map = HashMap<String?, Any?>()
        map["uid"] = uid
        map["userAccount"] = userAccount
        sendEvent("onRegisteredLocalUser", map)
    }

    override fun onUserInfoUpdated(uid: Int, userInfo: UserInfo) {
        super.onUserInfoUpdated(uid, userInfo)
        val map = HashMap<String?, Any?>()
        map["uid"] = uid
        val userInfoMap = HashMap<String, Any>()
        userInfoMap["uid"] = userInfo.uid
        userInfoMap["userAccount"] = userInfo.userAccount
        map["userInfo"] = userInfoMap
        sendEvent("onUpdatedUserInfo", map)
    }

    override fun onConnectionStateChanged(state: Int, reason: Int) {
        super.onConnectionStateChanged(state, reason)
        val map = HashMap<String?, Any?>()
        map["state"] = state
        map["reason"] = reason
        sendEvent("onConnectionStateChanged", map)
    }

    override fun onNetworkTypeChanged(type: Int) {
        super.onNetworkTypeChanged(type)
        val map = HashMap<String?, Any?>()
        map["type"] = type
        sendEvent("onNetworkTypeChanged", map)
    }

    override fun onConnectionLost() {
        super.onConnectionLost()
        sendEvent("onConnectionLost", null)
    }

    override fun onApiCallExecuted(error: Int, api: String, result: String) {
        super.onApiCallExecuted(error, api, result)
        val map = HashMap<String?, Any?>()
        map["errorCode"] = error
        map["api"] = api
        map["result"] = result
        sendEvent("onApiCallExecuted", map)
    }

    override fun onTokenPrivilegeWillExpire(token: String) {
        super.onTokenPrivilegeWillExpire(token)
        val map = HashMap<String?, Any?>()
        map["token"] = token
        sendEvent("onTokenPrivilegeWillExpire", map)
    }

    override fun onRequestToken() {
        super.onRequestToken()
        sendEvent("onRequestToken", null)
    }

    override fun onAudioVolumeIndication(speakers: Array<AudioVolumeInfo>, totalVolume: Int) {
        super.onAudioVolumeIndication(speakers, totalVolume)
        val map = HashMap<String?, Any?>()
        map["totalVolume"] = totalVolume
        map["speakers"] = arrayFromSpeakers(speakers)
        sendEvent("onAudioVolumeIndication", map)
    }

    override fun onActiveSpeaker(uid: Int) {
        super.onActiveSpeaker(uid)
        val map = HashMap<String?, Any?>()
        map["uid"] = uid
        sendEvent("onActiveSpeaker", map)
    }

    override fun onFirstLocalAudioFrame(elapsed: Int) {
        super.onFirstLocalAudioFrame(elapsed)
        val map = HashMap<String?, Any?>()
        map["elapsed"] = elapsed
        sendEvent("onFirstLocalAudioFrame", map)
    }

    override fun onFirstRemoteAudioFrame(uid: Int, elapsed: Int) {
        super.onFirstRemoteAudioFrame(uid, elapsed)
        val map = HashMap<String?, Any?>()
        map["uid"] = uid
        map["elapsed"] = elapsed
        sendEvent("onFirstRemoteAudioFrame", map)
    }

    override fun onFirstRemoteAudioDecoded(uid: Int, elapsed: Int) {
        super.onFirstRemoteAudioDecoded(uid, elapsed)
        val map = HashMap<String?, Any?>()
        map["uid"] = uid
        map["elapsed"] = elapsed
        sendEvent("onFirstRemoteAudioDecoded", map)
    }

    override fun onFirstLocalVideoFrame(width: Int, height: Int, elapsed: Int) {
        super.onFirstLocalVideoFrame(width, height, elapsed)
        val map = HashMap<String?, Any?>()
        map["width"] = width
        map["height"] = height
        map["elapsed"] = elapsed
        sendEvent("onFirstLocalVideoFrame", map)
    }

    override fun onFirstRemoteVideoFrame(uid: Int, width: Int, height: Int, elapsed: Int) {
        super.onFirstRemoteVideoFrame(uid, width, height, elapsed)
        val map = HashMap<String?, Any?>()
        map["uid"] = uid
        map["width"] = width
        map["height"] = height
        map["elapsed"] = elapsed
        sendEvent("onFirstRemoteVideoFrame", map)
    }

    override fun onUserMuteAudio(uid: Int, muted: Boolean) {
        super.onUserMuteAudio(uid, muted)
        val map = HashMap<String?, Any?>()
        map["uid"] = uid
        map["muted"] = muted
        sendEvent("onUserMuteAudio", map)
    }

    //        @Override
    //        public void onUserMuteVideo(int uid , boolean muted) {
    //            super.onUserMuteAudio(uid , muted);
    //            HashMap<String, Object> map = new HashMap<>();
    //            map.put("uid" , uid);
    //            map.put("muted" , muted);
    //            sendEvent("onUserMuteVideo" , map);
    //        }
    override fun onVideoSizeChanged(uid: Int, width: Int, height: Int, rotation: Int) {
        super.onVideoSizeChanged(uid, width, height, rotation)
        val map = HashMap<String?, Any?>()
        map["uid"] = uid
        map["width"] = width
        map["height"] = height
        map["rotation"] = rotation
        sendEvent("onVideoSizeChanged", map)
    }

    override fun onRemoteVideoStateChanged(uid: Int,
                                           state: Int,
                                           reason: Int,
                                           elapsed: Int) {
        super.onRemoteVideoStateChanged(uid, state, reason, elapsed)
        val map = HashMap<String?, Any?>()
        map["uid"] = uid
        map["state"] = state
        map["reason"] = reason
        map["elapsed"] = elapsed
        sendEvent("onRemoteVideoStateChanged", map)
    }

    override fun onLocalVideoStateChanged(state: Int, error: Int) {
        super.onLocalVideoStateChanged(state, error)
        val map = HashMap<String?, Any?>()
        map["localVideoState"] = state
        map["errorCode"] = error
        sendEvent("onLocalVideoStateChanged", map)
    }

    override fun onRemoteAudioStateChanged(uid: Int, state: Int, reason: Int, elapsed: Int) {
        super.onRemoteAudioStateChanged(uid, state, reason, elapsed)
        val map = HashMap<String?, Any?>()
        map["uid"] = uid
        map["state"] = state
        map["reason"] = reason
        map["elapsed"] = elapsed
        sendEvent("onRemoteAudioStateChanged", map)
    }

    override fun onLocalAudioStateChanged(state: Int, error: Int) {
        super.onLocalAudioStateChanged(state, error)
        val map = HashMap<String?, Any?>()
        map["state"] = state
        map["errorCode"] = error
        sendEvent("onLocalAudioStateChanged", map)
    }

    override fun onLocalPublishFallbackToAudioOnly(isFallbackOrRecover: Boolean) {
        super.onLocalPublishFallbackToAudioOnly(isFallbackOrRecover)
        val map = HashMap<String?, Any?>()
        map["isFallbackOrRecover"] = isFallbackOrRecover
        sendEvent("onLocalPublishFallbackToAudioOnly", map)
    }

    override fun onRemoteSubscribeFallbackToAudioOnly(uid: Int, isFallbackOrRecover: Boolean) {
        super.onRemoteSubscribeFallbackToAudioOnly(uid, isFallbackOrRecover)
        val map = HashMap<String?, Any?>()
        map["uid"] = uid
        map["isFallbackOrRecover"] = isFallbackOrRecover
        sendEvent("onRemoteSubscribeFallbackToAudioOnly", map)
    }

    override fun onAudioRouteChanged(routing: Int) {
        super.onAudioRouteChanged(routing)
        val map = HashMap<String?, Any?>()
        map["routing"] = routing
        sendEvent("onAudioRouteChanged", map)
    }

    override fun onCameraFocusAreaChanged(rect: Rect) {
        super.onCameraFocusAreaChanged(rect)
        val map = HashMap<String?, Any?>()
        map["rect"] = mapFromRect(rect)
        sendEvent("onCameraFocusAreaChanged", map)
    }

    override fun onCameraExposureAreaChanged(rect: Rect) {
        super.onCameraExposureAreaChanged(rect)
        val map = HashMap<String?, Any?>()
        map["rect"] = mapFromRect(rect)
        sendEvent("onCameraExposureAreaChanged", map)
    }

    override fun onRtcStats(stats: RtcStats) {
        super.onRtcStats(stats)
        val map = HashMap<String?, Any?>()
        map["stats"] = mapFromStats(stats)
        sendEvent("onRtcStats", map)
    }

    override fun onLastmileQuality(quality: Int) {
        super.onLastmileQuality(quality)
        val map = HashMap<String?, Any?>()
        map["quality"] = quality
        sendEvent("onLastmileQuality", map)
    }

    override fun onNetworkQuality(uid: Int, txQuality: Int, rxQuality: Int) {
        super.onNetworkQuality(uid, txQuality, rxQuality)
        val map = HashMap<String?, Any?>()
        map["uid"] = uid
        map["txQuality"] = txQuality
        map["rxQuality"] = rxQuality
        sendEvent("onNetworkQuality", map)
    }

    override fun onLastmileProbeResult(result: LastmileProbeResult) {
        super.onLastmileProbeResult(result)
        val map = HashMap<String?, Any?>()
        map["state"] = result.state
        map["rtt"] = result.rtt
        val uplinkReport = HashMap<String, Any>()
        map["uplinkReport"] = uplinkReport
        uplinkReport["availableBandwidth"] = result.uplinkReport.availableBandwidth
        uplinkReport["jitter"] = result.uplinkReport.jitter
        uplinkReport["packetLossRate"] = result.uplinkReport.packetLossRate
        val downlinkReport = HashMap<String, Any>()
        map["downlinkReport"] = downlinkReport
        uplinkReport["availableBandwidth"] = result.downlinkReport.availableBandwidth
        uplinkReport["jitter"] = result.downlinkReport.jitter
        uplinkReport["packetLossRate"] = result.downlinkReport.packetLossRate
        sendEvent("onLastmileProbeTestResult", map)
    }

    override fun onLocalVideoStats(stats: LocalVideoStats) {
        super.onLocalVideoStats(stats)
        val map = HashMap<String?, Any?>()
        map["stats"] = mapFromLocalVideoStats(stats)
        sendEvent("onLocalVideoStats", map)
    }

    override fun onLocalAudioStats(stats: LocalAudioStats) {
        super.onLocalAudioStats(stats)
        val map = HashMap<String?, Any?>()
        map["stats"] = mapFromLocalAudioStats(stats)
        sendEvent("onLocalAudioStats", map)
    }

    override fun onRemoteVideoStats(stats: RemoteVideoStats) {
        super.onRemoteVideoStats(stats)
        val map = HashMap<String?, Any?>()
        map["stats"] = mapFromRemoteVideoStats(stats)
        sendEvent("onRemoteVideoStats", map)
    }

    override fun onRemoteAudioStats(stats: RemoteAudioStats) {
        super.onRemoteAudioStats(stats)
        val map = HashMap<String?, Any?>()
        map["stats"] = mapFromRemoteAudioStats(stats)
        sendEvent("onRemoteAudioStats", map)
    }

    override fun onAudioMixingStateChanged(state: Int, errorCode: Int) {
        val map = HashMap<String?, Any?>()
        map["state"] = state
        map["errorCode"] = errorCode
        sendEvent("onLocalAudioMixingStateChanged", map)
    }

    override fun onAudioEffectFinished(soundId: Int) {
        super.onAudioEffectFinished(soundId)
        val map = HashMap<String?, Any?>()
        map["soundId"] = soundId
        sendEvent("onAudioEffectFinished", map)
    }

    override fun onStreamPublished(url: String, error: Int) {
        super.onStreamPublished(url, error)
        val map = HashMap<String?, Any?>()
        map["url"] = url
        map["errorCode"] = error
        sendEvent("onStreamPublished", map)
    }

    override fun onStreamUnpublished(url: String) {
        super.onStreamUnpublished(url)
        val map = HashMap<String?, Any?>()
        map["url"] = url
        sendEvent("onStreamUnpublished", map)
    }

    override fun onTranscodingUpdated() {
        super.onTranscodingUpdated()
        sendEvent("onTranscodingUpdated", null)
    }

    override fun onRtmpStreamingStateChanged(url: String,
                                             state: Int,
                                             errCode: Int) {
        super.onRtmpStreamingStateChanged(url, state, errCode)
        val map = HashMap<String?, Any?>()
        map["url"] = url
        map["state"] = state
        map["errorCode"] = errCode
        sendEvent("onRtmpStreamingStateChanged", map)
    }

    override fun onStreamInjectedStatus(url: String, uid: Int, status: Int) {
        super.onStreamInjectedStatus(url, uid, status)
        val map = HashMap<String?, Any?>()
        map["url"] = url
        map["uid"] = uid
        map["status"] = status
        sendEvent("onStreamInjectedStatus", map)
    }

    override fun onMediaEngineLoadSuccess() {
        super.onMediaEngineLoadSuccess()
        sendEvent("onMediaEngineLoadSuccess", null)
    }

    override fun onMediaEngineStartCallSuccess() {
        super.onMediaEngineStartCallSuccess()
        sendEvent("onMediaEngineStartCallSuccess", null)
    }

    override fun onChannelMediaRelayStateChanged(state: Int, code: Int) {
        super.onChannelMediaRelayStateChanged(state, code)
        val map = HashMap<String?, Any?>()
        map["state"] = state
        map["errorCode"] = code
        sendEvent("onChannelMediaRelayChanged", map)
    }

    override fun onChannelMediaRelayEvent(code: Int) {
        super.onChannelMediaRelayEvent(code)
        val map = HashMap<String?, Any?>()
        map["event"] = code
        sendEvent("onReceivedChannelMediaRelayEvent", map)
    }

    override fun onAudioMixingFinished() {
        super.onAudioMixingFinished()
        sendEvent("onAudioMixingFinished", null)
    }

    private fun mapFromStats(stats: RtcStats): HashMap<String, Any> {
        val map = HashMap<String, Any>()
        map["totalDuration"] = stats.totalDuration
        map["txBytes"] = stats.txBytes
        map["rxBytes"] = stats.rxBytes
        map["txAudioBytes"] = stats.txAudioBytes
        map["txVideoBytes"] = stats.txVideoBytes
        map["rxAudioBytes"] = stats.rxAudioBytes
        map["rxVideoBytes"] = stats.rxVideoBytes
        map["txKBitrate"] = stats.txKBitRate
        map["rxKBitrate"] = stats.rxKBitRate
        map["txAudioKBitrate"] = stats.txAudioKBitRate
        map["rxAudioKBitrate"] = stats.rxAudioKBitRate
        map["txVideoKBitrate"] = stats.txVideoKBitRate
        map["rxVideoKBitrate"] = stats.rxVideoKBitRate
        map["lastmileDelay"] = stats.lastmileDelay
        map["txPacketLossRate"] = stats.txPacketLossRate
        map["rxPacketLossRate"] = stats.rxPacketLossRate
        map["users"] = stats.users
        map["cpuAppUsage"] = stats.cpuAppUsage
        map["cpuTotalUsage"] = stats.cpuTotalUsage
        return map
    }

    private fun mapFromRect(rect: Rect): HashMap<String, Any> {
        val map = HashMap<String, Any>()
        map["x"] = rect.left
        map["y"] = rect.top
        map["width"] = rect.width()
        map["height"] = rect.height()
        return map
    }

    private fun mapFromLocalVideoStats(stats: LocalVideoStats): HashMap<String, Any> {
        val map = HashMap<String, Any>()
        map["sentBitrate"] = stats.sentBitrate
        map["sentFrameRate"] = stats.sentFrameRate
        map["encoderOutputFrameRate"] = stats.encoderOutputFrameRate
        map["rendererOutputFrameRate"] = stats.rendererOutputFrameRate
        map["sentTargetBitrate"] = stats.targetBitrate
        map["sentTargetFrameRate"] = stats.targetFrameRate
        map["qualityAdaptIndication"] = stats.targetBitrate
        map["encodedBitrate"] = stats.targetBitrate
        map["encodedFrameWidth"] = stats.targetBitrate
        map["encodedFrameHeight"] = stats.encodedFrameHeight
        map["encodedFrameCount"] = stats.encodedFrameCount
        map["codecType"] = stats.codecType
        return map
    }

    private fun mapFromLocalAudioStats(stats: LocalAudioStats): HashMap<String, Any> {
        val map = HashMap<String, Any>()
        map["numChannels"] = stats.numChannels
        map["sentSampleRate"] = stats.sentSampleRate
        map["sentBitrate"] = stats.sentBitrate
        return map
    }

    private fun mapFromRemoteVideoStats(stats: RemoteVideoStats): HashMap<String, Any> {
        val map = HashMap<String, Any>()
        map["uid"] = stats.uid
        map["width"] = stats.width
        map["height"] = stats.height
        map["receivedBitrate"] = stats.receivedBitrate
        map["decoderOutputFrameRate"] = stats.decoderOutputFrameRate
        map["rendererOutputFrameRate"] = stats.rendererOutputFrameRate
        map["packetLossRate"] = stats.packetLossRate
        map["rxStreamType"] = stats.rxStreamType
        map["totalFrozenTime"] = stats.totalFrozenTime
        map["frozenRate"] = stats.frozenRate
        return map
    }

    private fun mapFromRemoteAudioStats(stats: RemoteAudioStats): HashMap<String, Any> {
        val map = HashMap<String, Any>()
        map["uid"] = stats.uid
        map["quality"] = stats.quality
        map["networkTransportDelay"] = stats.networkTransportDelay
        map["jitterBufferDelay"] = stats.jitterBufferDelay
        map["audioLossRate"] = stats.audioLossRate
        map["numChannels"] = stats.numChannels
        map["receivedSampleRate"] = stats.receivedSampleRate
        map["receivedBitrate"] = stats.receivedBitrate
        map["totalFrozenTime"] = stats.totalFrozenTime
        map["frozenRate"] = stats.frozenRate
        return map
    }

    private fun arrayFromSpeakers(speakers: Array<AudioVolumeInfo>): ArrayList<HashMap<String, Any>> {
        val list = ArrayList<HashMap<String, Any>>()
        for (info in speakers) {
            val map = HashMap<String, Any>()
            map["uid"] = info.uid
            map["volume"] = info.volume
            list.add(map)
        }
        return list
    }
}