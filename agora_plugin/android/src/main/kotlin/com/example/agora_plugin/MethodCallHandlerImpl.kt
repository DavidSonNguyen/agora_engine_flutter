package com.example.agora_plugin

import android.content.ContentValues
import android.content.Context
import android.util.Log
import android.view.SurfaceView
import io.agora.rtc.RtcEngine
import io.agora.rtc.internal.LastmileProbeConfig
import io.agora.rtc.live.LiveInjectStreamConfig
import io.agora.rtc.live.LiveTranscoding
import io.agora.rtc.live.LiveTranscoding.*
import io.agora.rtc.models.UserInfo
import io.agora.rtc.video.*
import io.agora.rtc.video.VideoEncoderConfiguration.ORIENTATION_MODE
import io.agora.rtc.video.VideoEncoderConfiguration.VideoDimensions
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import java.util.*

class MethodCallHandlerImpl(private val context: Context, agoraRenderViewFactory: AgoraRenderViewFactory, mRtcEventHandler: RtcEventHandlerImpl) : MethodCallHandler {
    private val mRtcEventHandler: RtcEventHandlerImpl
    private val agoraRenderViewFactory: AgoraRenderViewFactory
    private var channel: MethodChannel? = null
    private var mRtcEngine: RtcEngine? = null
    fun addView(view: SurfaceView?, id: Int) {
        view?.let { agoraRenderViewFactory.addView(it, id) }
    }

    private fun removeView(id: Int) {
        agoraRenderViewFactory.removeView(id)
    }

    private fun getView(id: Int): SurfaceView? {
        return agoraRenderViewFactory.getView(id)
    }

    fun startListening(messenger: BinaryMessenger?) {
        if (channel != null) {
            Log.wtf(ContentValues.TAG, "Setting a method call handler before the last was disposed.")
            stopListening()
        }
        channel = MethodChannel(messenger, "agora_method_channel")
        channel!!.setMethodCallHandler(this)
    }

    fun stopListening() {
        if (channel == null) {
            Log.d(ContentValues.TAG, "Tried to stop listening when no MethodChannel had been initialized.")
            return
        }
        channel!!.setMethodCallHandler(null)
        channel = null
    }

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        when (call.method) {
            "create" -> {
                try {
                    val appId = call.argument<String>("appId")
                    mRtcEngine = RtcEngine.create(context, appId, mRtcEventHandler)
                    result.success(null)
                } catch (e: Exception) {
                    throw RuntimeException("NEED TO check rtc sdk init fatal error\n")
                }
            }
            "destroy" -> {
                RtcEngine.destroy()
                result.success(null)
            }
            "setChannelProfile" -> {
                if (mRtcEngine == null) {
                    result.success(null)
                    return
                }
                val profile = call.argument<Int>("profile")!!
                mRtcEngine!!.setChannelProfile(profile)
                result.success(null)
            }
            "setClientRole" -> {
                if (mRtcEngine == null) {
                    result.success(null)
                    return
                }
                val role = call.argument<Int>("role")!!
                mRtcEngine!!.setClientRole(role)
                result.success(null)
            }
            "joinChannel" -> {
                if (mRtcEngine == null) {
                    result.success(null)
                    return
                }
                val token = call.argument<String>("token")
                val channel = call.argument<String>("channelId")
                val info = call.argument<String>("info")
                val uid = call.argument<Int>("uid")!!
                result.success(mRtcEngine!!.joinChannel(token, channel, info, uid) >= 0)
            }
            "leaveChannel" -> {
                if (mRtcEngine == null) {
                    result.success(null)
                    return
                }
                result.success(mRtcEngine!!.leaveChannel() >= 0)
            }
            "switchChannel" -> {
                if (mRtcEngine == null) {
                    result.success(null)
                    return
                }
                val token = call.argument<String>("token")
                val channel = call.argument<String>("channelId")
                result.success(mRtcEngine!!.switchChannel(token, channel) >= 0)
            }
            "renewToken" -> {
                if (mRtcEngine == null) {
                    result.success(null)
                    return
                }
                val token = call.argument<String>("token")
                mRtcEngine!!.renewToken(token)
                result.success(null)
            }
            "enableWebSdkInteroperability" -> {
                if (mRtcEngine == null) {
                    result.success(null)
                    return
                }
                val enabled = call.argument<Boolean>("enabled")!!
                mRtcEngine!!.enableWebSdkInteroperability(enabled)
                result.success(null)
            }
            "getConnectionState" -> {
                if (mRtcEngine == null) {
                    result.success(null)
                    return
                }
                val state = mRtcEngine!!.connectionState
                result.success(state)
            }
            "registerLocalUserAccount" -> {
                if (mRtcEngine == null) {
                    result.success(null)
                    return
                }
                val appId = call.argument<String>("appId")
                val userAccount = call.argument<String>("userAccount")
                val state = mRtcEngine!!.registerLocalUserAccount(appId, userAccount)
                result.success(state == 0)
            }
            "joinChannelByUserAccount" -> {
                if (mRtcEngine == null) {
                    result.success(null)
                    return
                }
                val token = call.argument<String>("token")
                val userAccount = call.argument<String>("userAccount")
                val channelId = call.argument<String>("channelId")
                val state = mRtcEngine!!.joinChannelWithUserAccount(token, channelId, userAccount)
                result.success(state == 0)
            }
            "getUserInfoByUserAccount" -> {
                if (mRtcEngine == null) {
                    result.success(null)
                    return
                }
                val userAccount = call.argument<String>("userAccount")
                val info = UserInfo()
                val code = mRtcEngine!!.getUserInfoByUserAccount(userAccount, info)
                if (code == 0) {
                    val map = HashMap<String, Any>()
                    map["uid"] = info.uid
                    map["userAccount"] = info.userAccount
                    result.success(map)
                } else {
                    result.error("getUserInfoByUserAccountError", "get user info failed", code)
                }
            }
            "getUserInfoByUid" -> {
                if (mRtcEngine == null) {
                    result.success(null)
                    return
                }
                val uid = call.argument<Int>("uid")!!
                val info = UserInfo()
                val code = mRtcEngine!!.getUserInfoByUid(uid, info)
                if (code == 0) {
                    val map = HashMap<String, Any>()
                    map["uid"] = info.uid
                    map["userAccount"] = info.userAccount
                    result.success(map)
                } else {
                    result.error("getUserInfoByUid", "get user info failed", code)
                }
            }
            "enableAudio" -> {
                if (mRtcEngine == null) {
                    result.success(null)
                    return
                }
                mRtcEngine!!.enableAudio()
                result.success(null)
            }
            "disableAudio" -> {
                if (mRtcEngine == null) {
                    result.success(null)
                    return
                }
                mRtcEngine!!.disableAudio()
                result.success(null)
            }
            "setAudioProfile" -> {
                if (mRtcEngine == null) {
                    result.success(null)
                    return
                }
                val profile = call.argument<Int>("profile")!!
                val scenario = call.argument<Int>("scenario")!!
                mRtcEngine!!.setAudioProfile(profile, scenario)
                result.success(null)
            }
            "adjustRecordingSignalVolume" -> {
                if (mRtcEngine == null) {
                    result.success(null)
                    return
                }
                val volume = call.argument<Int>("volume")!!
                mRtcEngine!!.adjustRecordingSignalVolume(volume)
                result.success(null)
            }
            "adjustPlaybackSignalVolume" -> {
                if (mRtcEngine == null) {
                    result.success(null)
                    return
                }
                val volume = call.argument<Int>("volume")!!
                mRtcEngine!!.adjustPlaybackSignalVolume(volume)
                result.success(null)
            }
            "enableAudioVolumeIndication" -> {
                if (mRtcEngine == null) {
                    result.success(null)
                    return
                }
                val interval = call.argument<Int>("interval")!!
                val smooth = call.argument<Int>("smooth")!!
                val vad = call.argument<Boolean>("vad")!!
                mRtcEngine!!.enableAudioVolumeIndication(interval, smooth, vad)
                result.success(null)
            }
            "enableLocalAudio" -> {
                if (mRtcEngine == null) {
                    result.success(null)
                    return
                }
                val enabled = call.argument<Boolean>("enabled")!!
                mRtcEngine!!.enableLocalAudio(enabled)
                result.success(null)
            }
            "muteLocalAudioStream" -> {
                if (mRtcEngine == null) {
                    result.success(null)
                    return
                }
                val muted = call.argument<Boolean>("muted")!!
                mRtcEngine!!.muteLocalAudioStream(muted)
                result.success(null)
            }
            "muteRemoteAudioStream" -> {
                if (mRtcEngine == null) {
                    result.success(null)
                    return
                }
                val uid = call.argument<Int>("uid")!!
                val muted = call.argument<Boolean>("muted")!!
                mRtcEngine!!.muteRemoteAudioStream(uid, muted)
                result.success(null)
            }
            "muteAllRemoteAudioStreams" -> {
                if (mRtcEngine == null) {
                    result.success(null)
                    return
                }
                val muted = call.argument<Boolean>("muted")!!
                mRtcEngine!!.muteAllRemoteAudioStreams(muted)
                result.success(null)
            }
            "setDefaultMuteAllRemoteAudioStreams" -> {
                if (mRtcEngine == null) {
                    result.success(null)
                    return
                }
                val muted = call.argument<Boolean>("muted")!!
                mRtcEngine!!.setDefaultMuteAllRemoteAudioStreams(muted)
                result.success(null)
            }
            "setBeautyEffectOptions" -> {
                if (mRtcEngine == null) {
                    result.success(null)
                    return
                }
                val enabled = call.argument<Boolean>("enabled")!!
                val optionsMap = call.argument<HashMap<String, Any>>("options")!!
                val options = beautyOptionsFromMap(optionsMap)
                mRtcEngine!!.setBeautyEffectOptions(enabled, options)
                result.success(null)
            }
            "enableVideo" -> {
                if (mRtcEngine == null) {
                    result.success(null)
                    return
                }
                mRtcEngine!!.enableVideo()
                result.success(null)
            }
            "disableVideo" -> {
                if (mRtcEngine == null) {
                    result.success(null)
                    return
                }
                mRtcEngine!!.disableVideo()
                result.success(null)
            }
            "setVideoEncoderConfiguration" -> {
                if (mRtcEngine == null) {
                    result.success(null)
                    return
                }
                val configDic = call.argument<HashMap<String, Any>>("config")!!
                val config = videoEncoderConfigurationFromMap(configDic)
                mRtcEngine!!.setVideoEncoderConfiguration(config)
                result.success(null)
            }
            "removeNativeView" -> {
                if (mRtcEngine == null) {
                    result.success(null)
                    return
                }
                val viewId = call.argument<Int>("viewId")!!
                removeView(viewId)
                result.success(null)
            }
            "setupLocalVideo" -> {
                if (mRtcEngine == null) {
                    result.success(null)
                    return
                }
                val localViewId = call.argument<Int>("viewId")!!
                val localView = getView(localViewId)
                val localRenderMode = call.argument<Int>("renderMode")!!
                val localCanvas = VideoCanvas(localView)
                localCanvas.renderMode = localRenderMode
                mRtcEngine!!.setupLocalVideo(localCanvas)
                result.success(null)
            }
            "setupRemoteVideo" -> {
                if (mRtcEngine == null) {
                    result.success(null)
                    return
                }
                val remoteViewId = call.argument<Int>("viewId")!!
                val view = getView(remoteViewId)
                val remoteRenderMode = call.argument<Int>("renderMode")!!
                val remoteUid = call.argument<Int>("uid")!!
                mRtcEngine!!.setupRemoteVideo(VideoCanvas(view, remoteRenderMode, remoteUid))
                result.success(null)
            }
            "setLocalRenderMode" -> {
                if (mRtcEngine == null) {
                    result.success(null)
                    return
                }
                val mode = call.argument<Int>("mode")!!
                mRtcEngine!!.setLocalRenderMode(mode)
                result.success(null)
            }
            "setRemoteRenderMode" -> {
                if (mRtcEngine == null) {
                    result.success(null)
                    return
                }
                val uid = call.argument<Int>("uid")!!
                val mode = call.argument<Int>("mode")!!
                mRtcEngine!!.setRemoteRenderMode(uid, mode)
                result.success(null)
            }
            "startPreview" -> {
                if (mRtcEngine == null) {
                    result.success(null)
                    return
                }
                mRtcEngine!!.startPreview()
                result.success(null)
            }
            "stopPreview" -> {
                if (mRtcEngine == null) {
                    result.success(null)
                    return
                }
                mRtcEngine!!.stopPreview()
                result.success(null)
            }
            "enableLocalVideo" -> {
                if (mRtcEngine == null) {
                    result.success(null)
                    return
                }
                val enabled = call.argument<Boolean>("enabled")!!
                mRtcEngine!!.enableLocalVideo(enabled)
                result.success(null)
            }
            "muteLocalVideoStream" -> {
                if (mRtcEngine == null) {
                    result.success(null)
                    return
                }
                val muted = call.argument<Boolean>("muted")!!
                mRtcEngine!!.muteLocalVideoStream(muted)
                result.success(null)
            }
            "muteRemoteVideoStream" -> {
                val uid = call.argument<Int>("uid")!!
                val muted = call.argument<Boolean>("muted")!!
                mRtcEngine!!.muteRemoteVideoStream(uid, muted)
                result.success(null)
            }
            "muteAllRemoteVideoStreams" -> {
                if (mRtcEngine == null) {
                    result.success(null)
                    return
                }
                val muted = call.argument<Boolean>("muted")!!
                mRtcEngine!!.muteAllRemoteVideoStreams(muted)
                result.success(null)
            }
            "setDefaultMuteAllRemoteVideoStreams" -> {
                if (mRtcEngine == null) {
                    result.success(null)
                    return
                }
                val muted = call.argument<Boolean>("muted")!!
                mRtcEngine!!.setDefaultMuteAllRemoteVideoStreams(muted)
                result.success(null)
            }
            "setLocalVoiceChanger" -> {
                if (mRtcEngine == null) {
                    result.success(null)
                    return
                }
                val changer = call.argument<Int>("changer")!!
                mRtcEngine!!.setLocalVoiceChanger(changer)
                result.success(null)
            }
            "setLocalVoicePitch" -> {
                if (mRtcEngine == null) {
                    result.success(null)
                    return
                }
                val pitch = call.argument<Double>("pitch")!!
                mRtcEngine!!.setLocalVoicePitch(pitch)
                result.success(null)
            }
            "setLocalVoiceEqualizationOfBandFrequency" -> {
                if (mRtcEngine == null) {
                    result.success(null)
                    return
                }
                val bandFrequency = call.argument<Int>("bandFrequency")!!
                val gain = call.argument<Int>("gain")!!
                mRtcEngine!!.setLocalVoiceEqualization(bandFrequency, gain)
                result.success(null)
            }
            "setLocalVoiceReverbOfType" -> {
                if (mRtcEngine == null) {
                    result.success(null)
                    return
                }
                val reverbType = call.argument<Int>("reverbType")!!
                val value = call.argument<Int>("value")!!
                mRtcEngine!!.setLocalVoiceReverb(reverbType, value)
                result.success(null)
            }
            "setLocalVoiceReverbPreset" -> {
                if (mRtcEngine == null) {
                    result.success(null)
                    return
                }
                val reverbType = call.argument<Int>("reverbType")!!
                mRtcEngine!!.setLocalVoiceReverbPreset(reverbType)
                result.success(null)
            }
            "enableSoundPositionIndication" -> {
                if (mRtcEngine == null) {
                    result.success(null)
                    return
                }
                val enabled = call.argument<Boolean>("enabled")!!
                mRtcEngine!!.enableSoundPositionIndication(enabled)
                result.success(null)
            }
            "setRemoteVoicePosition" -> {
                if (mRtcEngine == null) {
                    result.success(null)
                    return
                }
                val uid = call.argument<Int>("uid")!!
                val pan = call.argument<Double>("pan")!!
                val gain = call.argument<Int>("gain")!!
                mRtcEngine!!.setRemoteVoicePosition(uid, pan, gain.toDouble())
                result.success(null)
            }
            "setDefaultAudioRouteToSpeaker" -> {
                if (mRtcEngine == null) {
                    result.success(null)
                    return
                }
                val defaultToSpeaker = call.argument<Boolean>("defaultToSpeaker")!!
                mRtcEngine!!.setDefaultAudioRoutetoSpeakerphone(defaultToSpeaker)
                result.success(null)
            }
            "setEnableSpeakerphone" -> {
                if (mRtcEngine == null) {
                    result.success(null)
                    return
                }
                val enabled = call.argument<Boolean>("enabled")!!
                mRtcEngine!!.setEnableSpeakerphone(enabled)
                result.success(null)
            }
            "isSpeakerphoneEnabled" -> {
                if (mRtcEngine == null) {
                    result.success(null)
                    return
                }
                val enabled = mRtcEngine!!.isSpeakerphoneEnabled
                result.success(enabled)
            }
            "setRemoteUserPriority" -> {
                run {
                    if (mRtcEngine == null) {
                        result.success(null)
                        return
                    }
                    val uid = call.argument<Int>("uid")!!
                    val userPriority = call.argument<Int>("userPriority")!!
                    mRtcEngine!!.setRemoteUserPriority(uid, userPriority)
                    result.success(null)
                }
                run {
                    if (mRtcEngine == null) {
                        result.success(null)
                        return
                    }
                    val option = call.argument<Int>("option")!!
                    mRtcEngine!!.setLocalPublishFallbackOption(option)
                    result.success(null)
                }
            }
            "setLocalPublishFallbackOption" -> {
                if (mRtcEngine == null) {
                    result.success(null)
                    return
                }
                val option = call.argument<Int>("option")!!
                mRtcEngine!!.setLocalPublishFallbackOption(option)
                result.success(null)
            }
            "setRemoteSubscribeFallbackOption" -> {
                if (mRtcEngine == null) {
                    result.success(null)
                    return
                }
                val option = call.argument<Int>("option")!!
                mRtcEngine!!.setRemoteSubscribeFallbackOption(option)
                result.success(null)
            }
            "enableDualStreamMode" -> {
                if (mRtcEngine == null) {
                    result.success(null)
                    return
                }
                val enabled = call.argument<Boolean>("enabled")!!
                mRtcEngine!!.enableDualStreamMode(enabled)
                result.success(null)
            }
            "setRemoteVideoStreamType" -> {
                if (mRtcEngine == null) {
                    result.success(null)
                    return
                }
                val uid = call.argument<Int>("uid")!!
                val streamType = call.argument<Int>("streamType")!!
                mRtcEngine!!.setRemoteVideoStreamType(uid, streamType)
                result.success(null)
            }
            "setRemoteDefaultVideoStreamType" -> {
                if (mRtcEngine == null) {
                    result.success(null)
                    return
                }
                val streamType = call.argument<Int>("streamType")!!
                mRtcEngine!!.setRemoteDefaultVideoStreamType(streamType)
                result.success(null)
            }
            "setLiveTranscoding" -> {
                if (mRtcEngine == null) {
                    result.success(null)
                    return
                }
                val transcoding = LiveTranscoding()
                val params = call.argument<Map<*, *>>("transcoding")!!
                if (params["width"] != null && params["height"] != null) {
                    transcoding.width = params["width"] as Int
                    transcoding.height = params["height"] as Int
                }
                if (params["videoBitrate"] != null) {
                    transcoding.videoBitrate = params["videoBitrate"] as Int
                }
                if (params["videoFramerate"] != null) {
                    transcoding.videoFramerate = params["videoFramerate"] as Int
                }
                if (params["videoGop"] != null) {
                    transcoding.videoGop = params["videoGop"] as Int
                }
                if (params["videoCodecProfile"] != null) {
                    val videoCodecProfile = params["videoCodecProfile"] as Int
                    for (profileType in VideoCodecProfileType.values()) {
                        if (VideoCodecProfileType.getValue(profileType) == videoCodecProfile) {
                            transcoding.videoCodecProfile = profileType
                            break
                        }
                    }
                }
                if (params["audioCodecProfile"] != null) {
                    val audioCodecProfile = params["audioCodecProfile"] as Int
                    for (profileType in AudioCodecProfileType.values()) {
                        if (AudioCodecProfileType.getValue(profileType) == audioCodecProfile) {
                            transcoding.audioCodecProfile = profileType
                            break
                        }
                    }
                }
                if (params["audioSampleRate"] != null) {
                    val audioSampleRate = params["audioSampleRate"] as Int
                    for (rateType in AudioSampleRateType.values()) {
                        if (AudioSampleRateType.getValue(rateType) == audioSampleRate) {
                            transcoding.audioSampleRate = rateType
                            break
                        }
                    }
                }
                if (params["watermark"] != null) {
                    val image = params["watermark"] as Map<*, *>?
                    val watermarkMap = HashMap<String, Any?>()
                    watermarkMap["url"] = image!!["url"]
                    watermarkMap["x"] = image["x"]
                    watermarkMap["y"] = image["y"]
                    watermarkMap["width"] = image["width"]
                    watermarkMap["height"] = image["height"]
                    transcoding.watermark = createAgoraImage(watermarkMap)
                }
                if (params["backgroundImage"] != null) {
                    val image = params["backgroundImage"] as Map<*, *>?
                    val backgroundImageMap = HashMap<String, Any?>()
                    backgroundImageMap["url"] = image!!["url"]
                    backgroundImageMap["x"] = image["x"]
                    backgroundImageMap["y"] = image["y"]
                    backgroundImageMap["width"] = image["width"]
                    backgroundImageMap["height"] = image["height"]
                    transcoding.backgroundImage = createAgoraImage(backgroundImageMap)
                }
                if (params["backgroundColor"] != null) {
                    transcoding.setBackgroundColor(params["backgroundColor"] as Int)
                }
                if (params["audioBitrate"] != null) {
                    transcoding.audioBitrate = params["audioBitrate"] as Int
                }
                if (params["audioChannels"] != null) {
                    transcoding.audioChannels = params["audioChannels"] as Int
                }
                if (params["transcodingUsers"] != null) {
                    val users = ArrayList<TranscodingUser>()
                    val transcodingUsers = params["transcodingUsers"] as ArrayList<*>?
                    var i = 0
                    while (i < transcodingUsers!!.size) {
                        val optionUser = transcodingUsers[i] as Map<*, *>
                        val user = TranscodingUser()
                        user.uid = optionUser["uid"] as Int
                        user.x = optionUser["x"] as Int
                        user.y = optionUser["y"] as Int
                        user.width = optionUser["width"] as Int
                        user.height = optionUser["height"] as Int
                        user.zOrder = optionUser["zOrder"] as Int
                        user.alpha = (optionUser["alpha"] as Double?)!!.toFloat()
                        user.audioChannel = optionUser["audioChannel"] as Int
                        users.add(user)
                        i++
                    }
                    transcoding.users = users
                }
                if (params["transcodingExtraInfo"] != null) {
                    transcoding.userConfigExtraInfo = params["transcodingExtraInfo"] as String?
                }
                result.success(mRtcEngine!!.setLiveTranscoding(transcoding))
            }
            "addPublishStreamUrl" -> {
                if (mRtcEngine == null) {
                    result.success(null)
                    return
                }
                val url = call.argument<String>("url")
                val enable = call.argument<Boolean>("enable")!!
                result.success(mRtcEngine!!.addPublishStreamUrl(url, enable))
            }
            "removePublishStreamUrl" -> {
                if (mRtcEngine == null) {
                    result.success(null)
                    return
                }
                val url = call.argument<String>("url")
                result.success(mRtcEngine!!.removePublishStreamUrl(url))
            }
            "addInjectStreamUrl" -> {
                if (mRtcEngine == null) {
                    result.success(null)
                    return
                }
                val url = call.argument<String>("url")
                val config = call.argument<Map<*, *>>("config")!!
                val streamConfig = LiveInjectStreamConfig()
                if (config["width"] != null && config["height"] != null) {
                    streamConfig.width = config["width"] as Int
                    streamConfig.height = config["height"] as Int
                }
                if (config["videoGop"] != null) {
                    streamConfig.videoGop = config["videoGop"] as Int
                }
                if (config["videoFramerate"] != null) {
                    streamConfig.videoFramerate = config["videoFramerate"] as Int
                }
                if (config["videoBitrate"] != null) {
                    streamConfig.videoBitrate = config["videoBitrate"] as Int
                }
                if (config["audioBitrate"] != null) {
                    streamConfig.audioBitrate = config["audioBitrate"] as Int
                }
                if (config["audioChannels"] != null) {
                    streamConfig.audioChannels = config["audioChannels"] as Int
                }
                if (config["audioSampleRate"] != null) {
                    val audioSampleRate = config["audioSampleRate"] as Int
                    for (rateType in LiveInjectStreamConfig.AudioSampleRateType.values()) {
                        if (LiveInjectStreamConfig.AudioSampleRateType.getValue(rateType) == audioSampleRate) {
                            streamConfig.audioSampleRate = rateType
                            break
                        }
                    }
                }
                result.success(mRtcEngine!!.addInjectStreamUrl(url, streamConfig))
            }
            "removeInjectStreamUrl" -> {
                if (mRtcEngine == null) {
                    result.success(null)
                    return
                }
                val url = call.argument<String>("url")
                result.success(mRtcEngine!!.removeInjectStreamUrl(url))
            }
            "setEncryptionSecret" -> {
                if (mRtcEngine == null) {
                    result.success(null)
                    return
                }
                val secret = call.argument<String>("secret")
                mRtcEngine!!.setEncryptionSecret(secret)
                result.success(null)
            }
            "setEncryptionMode" -> {
                if (mRtcEngine == null) {
                    result.success(null)
                    return
                }
                val encryptionMode = call.argument<String>("encryptionMode")
                mRtcEngine!!.setEncryptionMode(encryptionMode)
                result.success(null)
            }
            "startEchoTestWithInterval" -> {
                if (mRtcEngine == null) {
                    result.success(null)
                    return
                }
                val interval = call.argument<Int>("interval")!!
                mRtcEngine!!.startEchoTest(interval)
                result.success(null)
            }
            "stopEchoTest" -> {
                if (mRtcEngine == null) {
                    result.success(null)
                    return
                }
                mRtcEngine!!.stopEchoTest()
                result.success(null)
            }
            "enableLastmileTest" -> {
                if (mRtcEngine == null) {
                    result.success(null)
                    return
                }
                mRtcEngine!!.enableLastmileTest()
                result.success(null)
            }
            "disableLastmileTest" -> {
                if (mRtcEngine == null) {
                    result.success(null)
                    return
                }
                mRtcEngine!!.disableLastmileTest()
                result.success(null)
            }
            "startLastmileProbeTest" -> {
                if (mRtcEngine == null) {
                    result.success(null)
                    return
                }
                val probeConfig = call.argument<HashMap<String, Any>>("config")!!
                val config = LastmileProbeConfig()
                config.expectedDownlinkBitrate = probeConfig["expectedDownlinkBitrate"] as Int
                config.expectedUplinkBitrate = probeConfig["expectedUplinkBitrate"] as Int
                config.probeDownlink = probeConfig["probeDownlink"] as Boolean
                config.probeUplink = probeConfig["probeUplink"] as Boolean
                mRtcEngine!!.startLastmileProbeTest(config)
                result.success(null)
            }
            "stopLastmileProbeTest" -> {
                if (mRtcEngine == null) {
                    result.success(null)
                    return
                }
                mRtcEngine!!.stopLastmileProbeTest()
                result.success(null)
            }
            "addVideoWatermark" -> {
                if (mRtcEngine == null) {
                    result.success(null)
                    return
                }
                val encryptionMode = call.argument<String>("encryptionMode")
                val url = call.argument<String>("url")
                val watermarkOptions = call.argument<HashMap<String, Any>>("options")!!
                val positionLandscapeOptions = watermarkOptions["positionInPortraitMode"] as HashMap<String, Any>?
                val options = WatermarkOptions()
                val landscapePosition = WatermarkOptions.Rectangle()
                landscapePosition.height = positionLandscapeOptions!!["height"] as Int
                landscapePosition.width = positionLandscapeOptions["width"] as Int
                landscapePosition.x = positionLandscapeOptions["x"] as Int
                landscapePosition.y = positionLandscapeOptions["y"] as Int
                val positionPortraitOptions = watermarkOptions["positionInPortraitMode"] as HashMap<String, Any>?
                val portraitPosition = WatermarkOptions.Rectangle()
                portraitPosition.height = positionPortraitOptions!!["height"] as Int
                portraitPosition.width = positionPortraitOptions["width"] as Int
                portraitPosition.x = positionPortraitOptions["x"] as Int
                portraitPosition.y = positionPortraitOptions["y"] as Int
                options.positionInLandscapeMode = landscapePosition
                options.visibleInPreview = watermarkOptions["visibleInPreview"] as Boolean
                options.positionInPortraitMode = portraitPosition
                mRtcEngine!!.addVideoWatermark(url, options)
                result.success(null)
            }
            "clearVideoWatermarks" -> {
                if (mRtcEngine == null) {
                    result.success(null)
                    return
                }
                mRtcEngine!!.clearVideoWatermarks()
                result.success(null)
            }
            "startAudioMixing" -> {
                if (mRtcEngine == null) {
                    result.success(null)
                    return
                }
                val filepath = call.argument<String>("filepath")
                val loopback = call.argument<Boolean>("loopback")!!
                val replace = call.argument<Boolean>("replace")!!
                val cycle = call.argument<Int>("cycle")!!
                mRtcEngine!!.startAudioMixing(filepath, loopback, replace, cycle)
                result.success(null)
            }
            "stopAudioMixing" -> {
                if (mRtcEngine == null) {
                    result.success(null)
                    return
                }
                mRtcEngine!!.stopAudioMixing()
                result.success(null)
            }
            "pauseAudioMixing" -> {
                if (mRtcEngine == null) {
                    result.success(null)
                    return
                }
                mRtcEngine!!.pauseAudioMixing()
                result.success(null)
            }
            "resumeAudioMixing" -> {
                if (mRtcEngine == null) {
                    result.success(null)
                    return
                }
                mRtcEngine!!.resumeAudioMixing()
                result.success(null)
            }
            "adjustAudioMixingVolume" -> {
                if (mRtcEngine == null) {
                    result.success(null)
                    return
                }
                val volume = call.argument<Int>("volume")!!
                mRtcEngine!!.adjustAudioMixingVolume(volume)
                result.success(null)
            }
            "adjustAudioMixingPlayoutVolume" -> {
                if (mRtcEngine == null) {
                    result.success(null)
                    return
                }
                val volume = call.argument<Int>("volume")!!
                mRtcEngine!!.adjustAudioMixingPlayoutVolume(volume)
                result.success(null)
            }
            "adjustAudioMixingPublishVolume" -> {
                if (mRtcEngine == null) {
                    result.success(null)
                    return
                }
                val volume = call.argument<Int>("volume")!!
                mRtcEngine!!.adjustAudioMixingPublishVolume(volume)
                result.success(null)
            }
            "getAudioMixingPlayoutVolume" -> {
                if (mRtcEngine == null) {
                    result.success(null)
                    return
                }
                val res = mRtcEngine!!.audioMixingPlayoutVolume
                result.success(res)
            }
            "getAudioMixingPublishVolume" -> {
                if (mRtcEngine == null) {
                    result.success(null)
                    return
                }
                val res = mRtcEngine!!.audioMixingPublishVolume
                result.success(res)
            }
            "getAudioMixingDuration" -> {
                if (mRtcEngine == null) {
                    result.success(null)
                    return
                }
                val res = mRtcEngine!!.audioMixingDuration
                result.success(res)
            }
            "getAudioMixingCurrentPosition" -> {
                if (mRtcEngine == null) {
                    result.success(null)
                    return
                }
                val res = mRtcEngine!!.audioMixingCurrentPosition
                result.success(res)
            }
            "setAudioMixingPosition" -> {
                if (mRtcEngine == null) {
                    result.success(null)
                    return
                }
                val pos = call.argument<Int>("pos")!!
                mRtcEngine!!.setAudioMixingPosition(pos)
                result.success(null)
            }
            "getEffectsVolume" -> {
                if (mRtcEngine == null) {
                    result.success(null)
                    return
                }
                val volume = mRtcEngine!!.audioEffectManager.effectsVolume
                result.success(volume)
            }
            "setEffectsVolume" -> {
                if (mRtcEngine == null) {
                    result.success(null)
                    return
                }
                val volume = call.argument<Double>("volume")!!
                mRtcEngine!!.audioEffectManager.effectsVolume = volume
                result.success(null)
            }
            "setVolumeOfEffect" -> {
                if (mRtcEngine == null) {
                    result.success(null)
                    return
                }
                val volume = call.argument<Double>("volume")!!
                val soundId = call.argument<Int>("soundId")!!
                mRtcEngine!!.audioEffectManager.setVolumeOfEffect(soundId, volume)
                result.success(null)
            }
            "playEffect" -> {
                if (mRtcEngine == null) {
                    result.success(null)
                    return
                }
                val soundId = call.argument<Int>("soundId")!!
                val filepath = call.argument<String>("filepath")
                val loopback = call.argument<Int>("loopback")!!
                val pitch = call.argument<Double>("pitch")!!
                val pan = call.argument<Double>("pan")!!
                val gain = call.argument<Double>("gain")!!
                val publish = call.argument<Boolean>("publish")!!
                mRtcEngine!!.audioEffectManager.playEffect(soundId, filepath, loopback, pitch, pan, gain, publish)
                result.success(null)
            }
            "stopEffect" -> {
                if (mRtcEngine == null) {
                    result.success(null)
                    return
                }
                val soundId = call.argument<Int>("soundId")!!
                mRtcEngine!!.audioEffectManager.stopEffect(soundId)
                result.success(null)
            }
            "stopAllEffects" -> {
                if (mRtcEngine == null) {
                    result.success(null)
                    return
                }
                mRtcEngine!!.audioEffectManager.stopAllEffects()
                result.success(null)
            }
            "preloadEffect" -> {
                if (mRtcEngine == null) {
                    result.success(null)
                    return
                }
                val soundId = call.argument<Int>("soundId")!!
                val filepath = call.argument<String>("filepath")
                mRtcEngine!!.audioEffectManager.preloadEffect(soundId, filepath)
                result.success(null)
            }
            "unloadEffect" -> {
                if (mRtcEngine == null) {
                    result.success(null)
                    return
                }
                val soundId = call.argument<Int>("soundId")!!
                mRtcEngine!!.audioEffectManager.unloadEffect(soundId)
                result.success(null)
            }
            "pauseEffect" -> {
                if (mRtcEngine == null) {
                    result.success(null)
                    return
                }
                val soundId = call.argument<Int>("soundId")!!
                mRtcEngine!!.audioEffectManager.pauseEffect(soundId)
                result.success(null)
            }
            "pauseAllEffects" -> {
                if (mRtcEngine == null) {
                    result.success(null)
                    return
                }
                mRtcEngine!!.audioEffectManager.pauseAllEffects()
                result.success(null)
            }
            "resumeEffect" -> {
                if (mRtcEngine == null) {
                    result.success(null)
                    return
                }
                val soundId = call.argument<Int>("soundId")!!
                mRtcEngine!!.audioEffectManager.resumeEffect(soundId)
                result.success(null)
            }
            "resumeAllEffects" -> {
                if (mRtcEngine == null) {
                    result.success(null)
                    return
                }
                mRtcEngine!!.audioEffectManager.resumeAllEffects()
                result.success(null)
            }
            "startChannelMediaRelay" -> {
                if (mRtcEngine == null) {
                    result.success(null)
                    return
                }
                val config = ChannelMediaRelayConfiguration()
                val src = config.srcChannelMediaInfo
                val options = call.argument<HashMap<String, Any>>("config")!!
                if (options["src"] != null) {
                    val srcOption = options["src"] as HashMap<String, Any>?
                    if (srcOption!!["token"] != null) {
                        src.token = srcOption["token"] as String?
                    }
                    if (srcOption["channelName"] != null) {
                        src.channelName = srcOption["channelName"] as String?
                    }
                }
                val dstMediaInfo: List<HashMap<String, Any>>? = options["channels"] as List<HashMap<String, Any>>?
                var i = 0
                while (i < dstMediaInfo!!.size) {
                    val dst = dstMediaInfo[i]
                    var channelName: String? = null
                    var token: String? = null
                    var uid = 0
                    if (dst["token"] != null) {
                        token = token
                    }
                    if (dst["channelName"] != null) {
                        channelName = dst["channelName"] as String?
                    }
                    if (dst["uid"] != null) {
                        uid = dst["uid"] as Int
                    }
                    config.setDestChannelInfo(channelName, ChannelMediaInfo(channelName, token, uid))
                    i++
                }
                mRtcEngine!!.startChannelMediaRelay(config)
                result.success(null)
            }
            "removeChannelMediaRelay" -> {
                if (mRtcEngine == null) {
                    result.success(null)
                    return
                }
                val config = ChannelMediaRelayConfiguration()
                val src = config.srcChannelMediaInfo
                val options = call.argument<HashMap<String, Any>>("config")!!
                if (options["src"] != null) {
                    val srcOption = options["src"] as HashMap<String, Any>?
                    if (srcOption!!["token"] != null) {
                        src.token = srcOption["token"] as String?
                    }
                    if (srcOption["channelName"] != null) {
                        src.channelName = srcOption["channelName"] as String?
                    }
                }
                val dstMediaInfo: List<HashMap<String, Any>>? = options["channels"] as List<HashMap<String, Any>>?
                var i = 0
                while (i < dstMediaInfo!!.size) {
                    val dst = dstMediaInfo[i]
                    var channelName: String? = null
                    if (dst["channelName"] != null) {
                        channelName = dst["channelName"] as String?
                    }
                    config.removeDestChannelInfo(channelName)
                    i++
                }
                mRtcEngine!!.updateChannelMediaRelay(config)
                result.success(null)
            }
            "updateChannelMediaRelay" -> {
                if (mRtcEngine == null) {
                    result.success(null)
                    return
                }
                val config = ChannelMediaRelayConfiguration()
                val src = config.srcChannelMediaInfo
                val options = call.argument<HashMap<String, Any>>("config")!!
                if (options["src"] != null) {
                    val srcOption = options["src"] as HashMap<String, Any>?
                    if (srcOption!!["token"] != null) {
                        src.token = srcOption["token"] as String?
                    }
                    if (srcOption["channelName"] != null) {
                        src.channelName = srcOption["channelName"] as String?
                    }
                }
                val dstMediaInfo: List<HashMap<String, Any>>? = options["channels"] as List<HashMap<String, Any>>?
                var i = 0
                while (i < dstMediaInfo!!.size) {
                    val dst = dstMediaInfo[i]
                    var channelName: String? = null
                    var token: String? = null
                    var uid = 0
                    if (dst["token"] != null) {
                        token = token
                    }
                    if (dst["channelName"] != null) {
                        channelName = dst["channelName"] as String?
                    }
                    if (dst["uid"] != null) {
                        uid = dst["uid"] as Int
                    }
                    config.setDestChannelInfo(channelName, ChannelMediaInfo(channelName, token, uid))
                    i++
                }
                mRtcEngine!!.updateChannelMediaRelay(config)
                result.success(null)
            }
            "stopChannelMediaRelay" -> {
                if (mRtcEngine == null) {
                    result.success(null)
                    return
                }
                mRtcEngine!!.stopChannelMediaRelay()
                result.success(null)
            }
            "enableInEarMonitoring" -> {
                if (mRtcEngine == null) {
                    result.success(null)
                    return
                }
                val enabled = call.argument<Boolean>("enabled")!!
                mRtcEngine!!.enableInEarMonitoring(enabled)
                result.success(null)
            }
            "setInEarMonitoringVolume" -> {
                if (mRtcEngine == null) {
                    result.success(null)
                    return
                }
                val volume = call.argument<Int>("volume")!!
                mRtcEngine!!.setInEarMonitoringVolume(volume)
                result.success(null)
            }
            "switchCamera" -> {
                if (mRtcEngine == null) {
                    result.success(null)
                    return
                }
                mRtcEngine!!.switchCamera()
                result.success(null)
            }
            "getSdkVersion" -> {
                if (mRtcEngine == null) {
                    result.success(null)
                    return
                }
                val version = RtcEngine.getSdkVersion()
                result.success(version)
            }
            "setLogFile" -> {
                if (mRtcEngine == null) {
                    result.success(null)
                    return
                }
                val filePath = call.argument<String>("filePath")
                mRtcEngine!!.setLogFile(filePath)
                result.success(null)
            }
            "setLogFilter" -> {
                if (mRtcEngine == null) {
                    result.success(null)
                    return
                }
                val filter = call.argument<Int>("filter")!!
                mRtcEngine!!.setLogFilter(filter)
                result.success(null)
            }
            "setLogFileSize" -> {
                if (mRtcEngine == null) {
                    result.success(null)
                    return
                }
                val fileSizeInKBytes = call.argument<Int>("fileSizeInKBytes")!!
                mRtcEngine!!.setLogFileSize(fileSizeInKBytes)
                result.success(null)
            }
            "setParameters" -> {
                if (mRtcEngine == null) {
                    result.success(null)
                    return
                }
                val params = call.argument<String>("params")
                val res = mRtcEngine!!.setParameters(params)
                result.success(res)
            }
            "getParameters" -> {
                if (mRtcEngine == null) {
                    result.success(null)
                    return
                }
                val params = call.argument<String>("params")
                val args = call.argument<String>("args")
                val res = mRtcEngine!!.getParameter(params, args)
                result.success(res)
            }
            else -> result.notImplemented()
        }
    }

    private fun beautyOptionsFromMap(map: HashMap<String, Any>): BeautyOptions {
        val options = BeautyOptions()
        options.lighteningContrastLevel = (map["lighteningContrastLevel"] as Double?)!!.toInt()
        options.lighteningLevel = (map["lighteningLevel"] as Double?)!!.toFloat()
        options.smoothnessLevel = (map["smoothnessLevel"] as Double?)!!.toFloat()
        options.rednessLevel = (map["rednessLevel"] as Double?)!!.toFloat()
        return options
    }

    private fun videoEncoderConfigurationFromMap(map: HashMap<String, Any>): VideoEncoderConfiguration {
        val width = map["width"] as Int
        val height = map["height"] as Int
        val frameRate = map["frameRate"] as Int
        val bitrate = map["bitrate"] as Int
        val minBitrate = map["minBitrate"] as Int
        val orientationMode = map["orientationMode"] as Int
        val configuration = VideoEncoderConfiguration()
        configuration.dimensions = VideoDimensions(width, height)
        configuration.frameRate = frameRate
        configuration.bitrate = bitrate
        configuration.minBitrate = minBitrate
        configuration.orientationMode = orientationFromValue(orientationMode)
        return configuration
    }

    private fun orientationFromValue(value: Int): ORIENTATION_MODE {
        return when (value) {
            0 -> ORIENTATION_MODE.ORIENTATION_MODE_ADAPTIVE
            1 -> ORIENTATION_MODE.ORIENTATION_MODE_FIXED_LANDSCAPE
            2 -> ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT
            else -> ORIENTATION_MODE.ORIENTATION_MODE_ADAPTIVE
        }
    }

    private fun createAgoraImage(options: HashMap<String, Any?>): AgoraImage {
        val image = AgoraImage()
        image.url = options["url"] as String?
        image.height = options["height"] as Int
        image.width = options["width"] as Int
        image.x = options["x"] as Int
        image.y = options["y"] as Int
        return image
    }

    init {
        this.agoraRenderViewFactory = agoraRenderViewFactory
        this.mRtcEventHandler = mRtcEventHandler
    }
}
