package com.agora.agora_plugin;

import android.content.Context;
import android.util.Log;
import android.view.SurfaceView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.agora.rtc.RtcEngine;
import io.agora.rtc.internal.LastmileProbeConfig;
import io.agora.rtc.live.LiveInjectStreamConfig;
import io.agora.rtc.live.LiveTranscoding;
import io.agora.rtc.models.UserInfo;
import io.agora.rtc.video.AgoraImage;
import io.agora.rtc.video.BeautyOptions;
import io.agora.rtc.video.ChannelMediaInfo;
import io.agora.rtc.video.ChannelMediaRelayConfiguration;
import io.agora.rtc.video.VideoCanvas;
import io.agora.rtc.video.VideoEncoderConfiguration;
import io.agora.rtc.video.WatermarkOptions;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;

import static android.content.ContentValues.TAG;

final public class MethodCallHandlerImpl implements MethodChannel.MethodCallHandler {
    private final Context context;
    private final RtcEventHandlerImpl mRtcEventHandler;
    private final AgoraRenderViewFactory agoraRenderViewFactory;

    @Nullable
    private MethodChannel channel;

    private RtcEngine mRtcEngine;

    public MethodCallHandlerImpl(Context context, AgoraRenderViewFactory agoraRenderViewFactory, RtcEventHandlerImpl mRtcEventHandler) {
        this.context = context;
        this.agoraRenderViewFactory = agoraRenderViewFactory;
        this.mRtcEventHandler = mRtcEventHandler;
    }

    void addView(SurfaceView view, int id) {
        agoraRenderViewFactory.addView(view, id);
    }

    private void removeView(int id) {
        agoraRenderViewFactory.removeView(id);
    }

    private SurfaceView getView(int id) {
        return agoraRenderViewFactory.getView(id);
    }

    void startListening(BinaryMessenger messenger) {
        if (channel != null) {
            Log.wtf(TAG, "Setting a method call handler before the last was disposed.");
            stopListening();
        }

        channel = new MethodChannel(messenger, "agora_method_channel");
        channel.setMethodCallHandler(this);
    }

    void stopListening() {
        if (channel == null) {
            Log.d(TAG, "Tried to stop listening when no MethodChannel had been initialized.");
            return;
        }

        channel.setMethodCallHandler(null);
        channel = null;
    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull MethodChannel.Result result) {
        switch (call.method) {
            // Core Methods
            case "create": {
                try {
                    String appId = call.argument("appId");
                    mRtcEngine = RtcEngine.create(context, appId, mRtcEventHandler);
                    result.success(null);
                } catch (Exception e) {
                    throw new RuntimeException("NEED TO check rtc sdk init fatal error\n");
                }
            }
            break;
            case "destroy": {
                RtcEngine.destroy();
                result.success(null);
            }
            break;
            case "setChannelProfile": {
                if (mRtcEngine == null) {
                    result.success(null);
                    return;
                }
                int profile = call.argument("profile");
                mRtcEngine.setChannelProfile(profile);
                result.success(null);
            }
            break;
            case "setClientRole": {
                if (mRtcEngine == null) {
                    result.success(null);
                    return;
                }
                int role = call.argument("role");
                mRtcEngine.setClientRole(role);
                result.success(null);
            }
            break;
            case "joinChannel": {
                if (mRtcEngine == null) {
                    result.success(null);
                    return;
                }
                String token = call.argument("token");
                String channel = call.argument("channelId");
                String info = call.argument("info");
                int uid = call.argument("uid");
                result.success(mRtcEngine.joinChannel(token, channel, info, uid) >= 0);
            }
            break;
            case "leaveChannel": {
                if (mRtcEngine == null) {
                    result.success(null);
                    return;
                }
                result.success(mRtcEngine.leaveChannel() >= 0);
            }
            break;
            case "switchChannel": {
                if (mRtcEngine == null) {
                    result.success(null);
                    return;
                }
                String token = call.argument("token");
                String channel = call.argument("channelId");
                result.success(mRtcEngine.switchChannel(token, channel) >= 0);
            }
            break;
            case "renewToken": {
                if (mRtcEngine == null) {
                    result.success(null);
                    return;
                }
                String token = call.argument("token");
                mRtcEngine.renewToken(token);
                result.success(null);
            }
            break;
            case "enableWebSdkInteroperability": {
                if (mRtcEngine == null) {
                    result.success(null);
                    return;
                }
                boolean enabled = call.argument("enabled");
                mRtcEngine.enableWebSdkInteroperability(enabled);
                result.success(null);
            }
            break;
            case "getConnectionState": {
                if (mRtcEngine == null) {
                    result.success(null);
                    return;
                }
                int state = mRtcEngine.getConnectionState();
                result.success(state);
            }
            break;
            case "registerLocalUserAccount": {
                if (mRtcEngine == null) {
                    result.success(null);
                    return;
                }
                String appId = call.argument("appId");
                String userAccount = call.argument("userAccount");
                int state = mRtcEngine.registerLocalUserAccount(appId, userAccount);
                result.success(state == 0);
            }
            break;
            case "joinChannelByUserAccount": {
                if (mRtcEngine == null) {
                    result.success(null);
                    return;
                }
                String token = call.argument("token");
                String userAccount = call.argument("userAccount");
                String channelId = call.argument("channelId");
                int state = mRtcEngine.joinChannelWithUserAccount(token, channelId, userAccount);
                result.success(state == 0);
            }
            break;
            case "getUserInfoByUserAccount": {
                if (mRtcEngine == null) {
                    result.success(null);
                    return;
                }
                String userAccount = call.argument("userAccount");
                UserInfo info = new UserInfo();
                int code = mRtcEngine.getUserInfoByUserAccount(userAccount, info);
                if (code == 0) {
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("uid", info.uid);
                    map.put("userAccount", info.userAccount);
                    result.success(map);
                } else {
                    result.error("getUserInfoByUserAccountError", "get user info failed", code);
                }
            }
            break;
            case "getUserInfoByUid": {
                if (mRtcEngine == null) {
                    result.success(null);
                    return;
                }
                int uid = call.argument("uid");
                UserInfo info = new UserInfo();
                int code = mRtcEngine.getUserInfoByUid(uid, info);
                if (code == 0) {
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("uid", info.uid);
                    map.put("userAccount", info.userAccount);
                    result.success(map);
                } else {
                    result.error("getUserInfoByUid", "get user info failed", code);
                }
            }
            break;
            // Core Audio
            case "enableAudio": {
                if (mRtcEngine == null) {
                    result.success(null);
                    return;
                }
                mRtcEngine.enableAudio();
                result.success(null);
            }
            break;
            case "disableAudio": {
                if (mRtcEngine == null) {
                    result.success(null);
                    return;
                }
                mRtcEngine.disableAudio();
                result.success(null);
            }
            break;
            case "setAudioProfile": {
                if (mRtcEngine == null) {
                    result.success(null);
                    return;
                }
                int profile = call.argument("profile");
                int scenario = call.argument("scenario");
                mRtcEngine.setAudioProfile(profile, scenario);
                result.success(null);
            }
            break;
            case "adjustRecordingSignalVolume": {
                if (mRtcEngine == null) {
                    result.success(null);
                    return;
                }
                int volume = call.argument("volume");
                mRtcEngine.adjustRecordingSignalVolume(volume);
                result.success(null);
            }
            break;
            case "adjustPlaybackSignalVolume": {
                if (mRtcEngine == null) {
                    result.success(null);
                    return;
                }
                int volume = call.argument("volume");
                mRtcEngine.adjustPlaybackSignalVolume(volume);
                result.success(null);
            }
            break;
            case "enableAudioVolumeIndication": {
                if (mRtcEngine == null) {
                    result.success(null);
                    return;
                }
                int interval = call.argument("interval");
                int smooth = call.argument("smooth");
                boolean vad = call.argument("vad");
                mRtcEngine.enableAudioVolumeIndication(interval, smooth, vad);
                result.success(null);
            }
            break;
            case "enableLocalAudio": {
                if (mRtcEngine == null) {
                    result.success(null);
                    return;
                }
                boolean enabled = call.argument("enabled");
                mRtcEngine.enableLocalAudio(enabled);
                result.success(null);
            }
            break;
            case "muteLocalAudioStream": {
                if (mRtcEngine == null) {
                    result.success(null);
                    return;
                }
                boolean muted = call.argument("muted");
                mRtcEngine.muteLocalAudioStream(muted);
                result.success(null);
            }
            break;
            case "muteRemoteAudioStream": {
                if (mRtcEngine == null) {
                    result.success(null);
                    return;
                }
                int uid = call.argument("uid");
                boolean muted = call.argument("muted");
                mRtcEngine.muteRemoteAudioStream(uid, muted);
                result.success(null);
            }
            break;
            case "muteAllRemoteAudioStreams": {
                if (mRtcEngine == null) {
                    result.success(null);
                    return;
                }
                boolean muted = call.argument("muted");
                mRtcEngine.muteAllRemoteAudioStreams(muted);
                result.success(null);
            }
            break;
            case "setDefaultMuteAllRemoteAudioStreams": {
                if (mRtcEngine == null) {
                    result.success(null);
                    return;
                }
                boolean muted = call.argument("muted");
                mRtcEngine.setDefaultMuteAllRemoteAudioStreams(muted);
                result.success(null);
            }
            break;
            // Video Pre-process and Post-process
            case "setBeautyEffectOptions": {
                if (mRtcEngine == null) {
                    result.success(null);
                    return;
                }
                boolean enabled = call.argument("enabled");
                HashMap<String, Object> optionsMap = call.argument("options");
                BeautyOptions options = beautyOptionsFromMap(optionsMap);
                mRtcEngine.setBeautyEffectOptions(enabled, options);
                result.success(null);
            }
            break;
            // Core Video
            case "enableVideo": {
                if (mRtcEngine == null) {
                    result.success(null);
                    return;
                }
                mRtcEngine.enableVideo();
                result.success(null);
            }
            break;
            case "disableVideo": {
                if (mRtcEngine == null) {
                    result.success(null);
                    return;
                }
                mRtcEngine.disableVideo();
                result.success(null);
            }
            break;
            case "setVideoEncoderConfiguration": {
                if (mRtcEngine == null) {
                    result.success(null);
                    return;
                }
                HashMap<String, Object> configDic = call.argument("config");
                VideoEncoderConfiguration config = videoEncoderConfigurationFromMap(configDic);
                mRtcEngine.setVideoEncoderConfiguration(config);
                result.success(null);
            }
            break;
            case "removeNativeView": {
                if (mRtcEngine == null) {
                    result.success(null);
                    return;
                }
                int viewId = call.argument("viewId");
                removeView(viewId);
                result.success(null);
            }
            break;
            case "setupLocalVideo": {
                if (mRtcEngine == null) {
                    result.success(null);
                    return;
                }
                int localViewId = call.argument("viewId");
                SurfaceView localView = getView(localViewId);
                int localRenderMode = call.argument("renderMode");
                VideoCanvas localCanvas = new VideoCanvas(localView);
                localCanvas.renderMode = localRenderMode;
                mRtcEngine.setupLocalVideo(localCanvas);
                result.success(null);
            }
            break;
            case "setupRemoteVideo": {
                if (mRtcEngine == null) {
                    result.success(null);
                    return;
                }
                int remoteViewId = call.argument("viewId");
                SurfaceView view = getView(remoteViewId);
                int remoteRenderMode = call.argument("renderMode");
                int remoteUid = call.argument("uid");
                mRtcEngine.setupRemoteVideo(new VideoCanvas(view, remoteRenderMode, remoteUid));
                result.success(null);
            }
            break;
            // TODO: HERE
            case "setLocalRenderMode": {
                if (mRtcEngine == null) {
                    result.success(null);
                    return;
                }
                int mode = call.argument("mode");
                mRtcEngine.setLocalRenderMode(mode);
                result.success(null);
            }
            break;
            case "setRemoteRenderMode": {
                if (mRtcEngine == null) {
                    result.success(null);
                    return;
                }
                int uid = call.argument("uid");
                int mode = call.argument("mode");
                mRtcEngine.setRemoteRenderMode(uid, mode);
                result.success(null);
            }
            break;
            case "startPreview": {
                if (mRtcEngine == null) {
                    result.success(null);
                    return;
                }
                mRtcEngine.startPreview();
                result.success(null);
            }
            break;
            case "stopPreview": {
                if (mRtcEngine == null) {
                    result.success(null);
                    return;
                }
                mRtcEngine.stopPreview();
                result.success(null);
            }
            break;
            case "enableLocalVideo": {
                if (mRtcEngine == null) {
                    result.success(null);
                    return;
                }
                boolean enabled = call.argument("enabled");
                mRtcEngine.enableLocalVideo(enabled);
                result.success(null);
            }
            break;
            case "muteLocalVideoStream": {
                if (mRtcEngine == null) {
                    result.success(null);
                    return;
                }
                boolean muted = call.argument("muted");
                mRtcEngine.muteLocalVideoStream(muted);
                result.success(null);
            }
            break;
            case "muteRemoteVideoStream": {
                int uid = call.argument("uid");
                boolean muted = call.argument("muted");
                mRtcEngine.muteRemoteVideoStream(uid, muted);
                result.success(null);
            }
            break;
            case "muteAllRemoteVideoStreams": {
                if (mRtcEngine == null) {
                    result.success(null);
                    return;
                }
                boolean muted = call.argument("muted");
                mRtcEngine.muteAllRemoteVideoStreams(muted);
                result.success(null);
            }
            break;
            case "setDefaultMuteAllRemoteVideoStreams": {
                if (mRtcEngine == null) {
                    result.success(null);
                    return;
                }
                boolean muted = call.argument("muted");
                mRtcEngine.setDefaultMuteAllRemoteVideoStreams(muted);
                result.success(null);
            }
            break;

            // Voice
            case "setLocalVoiceChanger": {
                if (mRtcEngine == null) {
                    result.success(null);
                    return;
                }
                int changer = call.argument("changer");
                mRtcEngine.setLocalVoiceChanger(changer);
                result.success(null);
            }
            break;

            case "setLocalVoicePitch": {
                if (mRtcEngine == null) {
                    result.success(null);
                    return;
                }
                double pitch = call.argument("pitch");
                mRtcEngine.setLocalVoicePitch(pitch);
                result.success(null);
            }
            break;
            case "setLocalVoiceEqualizationOfBandFrequency": {
                if (mRtcEngine == null) {
                    result.success(null);
                    return;
                }
                int bandFrequency = call.argument("bandFrequency");
                int gain = call.argument("gain");
                mRtcEngine.setLocalVoiceEqualization(bandFrequency, gain);
                result.success(null);
            }
            break;
            case "setLocalVoiceReverbOfType": {
                if (mRtcEngine == null) {
                    result.success(null);
                    return;
                }
                int reverbType = call.argument("reverbType");
                int value = call.argument("value");
                mRtcEngine.setLocalVoiceReverb(reverbType, value);
                result.success(null);
            }
            break;
            case "setLocalVoiceReverbPreset": {
                if (mRtcEngine == null) {
                    result.success(null);
                    return;
                }
                int reverbType = call.argument("reverbType");
                mRtcEngine.setLocalVoiceReverbPreset(reverbType);
                result.success(null);
            }
            break;
            case "enableSoundPositionIndication": {
                if (mRtcEngine == null) {
                    result.success(null);
                    return;
                }
                boolean enabled = call.argument("enabled");
                mRtcEngine.enableSoundPositionIndication(enabled);
                result.success(null);
            }
            break;
            case "setRemoteVoicePosition": {
                if (mRtcEngine == null) {
                    result.success(null);
                    return;
                }
                int uid = call.argument("uid");
                double pan = call.argument("pan");
                int gain = call.argument("gain");
                mRtcEngine.setRemoteVoicePosition(uid, pan, gain);
                result.success(null);
            }
            break;

            // Audio Routing Controller
            case "setDefaultAudioRouteToSpeaker": {
                if (mRtcEngine == null) {
                    result.success(null);
                    return;
                }
                boolean defaultToSpeaker = call.argument("defaultToSpeaker");
                mRtcEngine.setDefaultAudioRoutetoSpeakerphone(defaultToSpeaker);
                result.success(null);
            }
            break;
            case "setEnableSpeakerphone": {
                if (mRtcEngine == null) {
                    result.success(null);
                    return;
                }
                boolean enabled = call.argument("enabled");
                mRtcEngine.setEnableSpeakerphone(enabled);
                result.success(null);
            }
            break;
            case "isSpeakerphoneEnabled": {
                if (mRtcEngine == null) {
                    result.success(null);
                    return;
                }
                boolean enabled = mRtcEngine.isSpeakerphoneEnabled();
                result.success(enabled);
            }
            break;

            // Stream Fallback
            case "setRemoteUserPriority": {
                if (mRtcEngine == null) {
                    result.success(null);
                    return;
                }
                int uid = call.argument("uid");
                int userPriority = call.argument("userPriority");
                mRtcEngine.setRemoteUserPriority(uid, userPriority);
                result.success(null);
            }
            case "setLocalPublishFallbackOption": {
                if (mRtcEngine == null) {
                    result.success(null);
                    return;
                }
                int option = call.argument("option");
                mRtcEngine.setLocalPublishFallbackOption(option);
                result.success(null);
            }
            break;
            case "setRemoteSubscribeFallbackOption": {
                if (mRtcEngine == null) {
                    result.success(null);
                    return;
                }
                int option = call.argument("option");
                mRtcEngine.setRemoteSubscribeFallbackOption(option);
                result.success(null);
            }
            break;

            // Dual-stream Mode
            case "enableDualStreamMode": {
                if (mRtcEngine == null) {
                    result.success(null);
                    return;
                }
                boolean enabled = call.argument("enabled");
                mRtcEngine.enableDualStreamMode(enabled);
                result.success(null);
            }
            break;
            case "setRemoteVideoStreamType": {
                if (mRtcEngine == null) {
                    result.success(null);
                    return;
                }
                int uid = call.argument("uid");
                int streamType = call.argument("streamType");
                mRtcEngine.setRemoteVideoStreamType(uid, streamType);
                result.success(null);
            }
            break;
            case "setRemoteDefaultVideoStreamType": {
                if (mRtcEngine == null) {
                    result.success(null);
                    return;
                }
                int streamType = call.argument("streamType");
                mRtcEngine.setRemoteDefaultVideoStreamType(streamType);
                result.success(null);
            }
            break;

            case "setLiveTranscoding": {
                if (mRtcEngine == null) {
                    result.success(null);
                    return;
                }
                LiveTranscoding transcoding = new LiveTranscoding();
                Map params = call.argument("transcoding");
                if (params.get("width") != null && params.get("height") != null) {
                    transcoding.width = (int) params.get("width");
                    transcoding.height = (int) params.get("height");
                }
                if (params.get("videoBitrate") != null) {
                    transcoding.videoBitrate = (int) params.get("videoBitrate");
                }
                if (params.get("videoFramerate") != null) {
                    transcoding.videoFramerate = (int) params.get("videoFramerate");
                }
                if (params.get("videoGop") != null) {
                    transcoding.videoGop = (int) params.get("videoGop");
                }
                if (params.get("videoCodecProfile") != null) {
                    int videoCodecProfile = (int) params.get("videoCodecProfile");
                    for (LiveTranscoding.VideoCodecProfileType profileType : LiveTranscoding.VideoCodecProfileType.values()) {
                        if (LiveTranscoding.VideoCodecProfileType.getValue(profileType) == videoCodecProfile) {
                            transcoding.videoCodecProfile = profileType;
                            break;
                        }
                    }
                }
                if (params.get("audioCodecProfile") != null) {
                    int audioCodecProfile = (int) params.get("audioCodecProfile");
                    for (LiveTranscoding.AudioCodecProfileType profileType : LiveTranscoding.AudioCodecProfileType.values()) {
                        if (LiveTranscoding.AudioCodecProfileType.getValue(profileType) == audioCodecProfile) {
                            transcoding.audioCodecProfile = profileType;
                            break;
                        }
                    }
                }
                if (params.get("audioSampleRate") != null) {
                    int audioSampleRate = (int) params.get("audioSampleRate");
                    for (LiveTranscoding.AudioSampleRateType rateType : LiveTranscoding.AudioSampleRateType.values()) {
                        if (LiveTranscoding.AudioSampleRateType.getValue(rateType) == audioSampleRate) {
                            transcoding.audioSampleRate = rateType;
                            break;
                        }
                    }
                }
                if (params.get("watermark") != null) {
                    Map image = (Map) params.get("watermark");
                    Map watermarkMap = new HashMap();
                    watermarkMap.put("url", image.get("url"));
                    watermarkMap.put("x", image.get("x"));
                    watermarkMap.put("y", image.get("y"));
                    watermarkMap.put("width", image.get("width"));
                    watermarkMap.put("height", image.get("height"));
                    transcoding.watermark = this.createAgoraImage(watermarkMap);
                }
                if (params.get("backgroundImage") != null) {
                    Map image = (Map) params.get("backgroundImage");
                    Map backgroundImageMap = new HashMap();
                    backgroundImageMap.put("url", image.get("url"));
                    backgroundImageMap.put("x", image.get("x"));
                    backgroundImageMap.put("y", image.get("y"));
                    backgroundImageMap.put("width", image.get("width"));
                    backgroundImageMap.put("height", image.get("height"));
                    transcoding.backgroundImage = this.createAgoraImage(backgroundImageMap);
                }
                if (params.get("backgroundColor") != null) {
                    transcoding.setBackgroundColor((int) params.get("backgroundColor"));
                }
                if (params.get("audioBitrate") != null) {
                    transcoding.audioBitrate = (int) params.get("audioBitrate");
                }
                if (params.get("audioChannels") != null) {
                    transcoding.audioChannels = (int) params.get("audioChannels");
                }
                if (params.get("transcodingUsers") != null) {
                    ArrayList<LiveTranscoding.TranscodingUser> users = new ArrayList<LiveTranscoding.TranscodingUser>();
                    ArrayList transcodingUsers = (ArrayList) params.get("transcodingUsers");
                    for (int i = 0; i < transcodingUsers.size(); i++) {
                        Map optionUser = (Map) transcodingUsers.get(i);
                        LiveTranscoding.TranscodingUser user = new LiveTranscoding.TranscodingUser();
                        user.uid = (int) optionUser.get("uid");
                        user.x = (int) optionUser.get("x");
                        user.y = (int) optionUser.get("y");
                        user.width = (int) optionUser.get("width");
                        user.height = (int) optionUser.get("height");
                        user.zOrder = (int) optionUser.get("zOrder");
                        user.alpha = ((Double) optionUser.get("alpha")).floatValue();
                        user.audioChannel = (int) optionUser.get("audioChannel");
                        users.add(user);
                    }
                    transcoding.setUsers(users);
                }
                if (params.get("transcodingExtraInfo") != null) {
                    transcoding.userConfigExtraInfo = (String) params.get("transcodingExtraInfo");
                }
                result.success(mRtcEngine.setLiveTranscoding(transcoding));
            }
            break;
            case "addPublishStreamUrl": {
                if (mRtcEngine == null) {
                    result.success(null);
                    return;
                }
                String url = call.argument("url");
                boolean enable = call.argument("enable");
                result.success(mRtcEngine.addPublishStreamUrl(url, enable));
            }
            break;
            case "removePublishStreamUrl": {
                if (mRtcEngine == null) {
                    result.success(null);
                    return;
                }
                String url = call.argument("url");
                result.success(mRtcEngine.removePublishStreamUrl(url));
            }
            break;
            case "addInjectStreamUrl": {
                if (mRtcEngine == null) {
                    result.success(null);
                    return;
                }
                String url = call.argument("url");
                Map config = call.argument("config");
                LiveInjectStreamConfig streamConfig = new LiveInjectStreamConfig();
                if (config.get("width") != null && config.get("height") != null) {
                    streamConfig.width = (int) config.get("width");
                    streamConfig.height = (int) config.get("height");
                }

                if (config.get("videoGop") != null) {
                    streamConfig.videoGop = (int) config.get("videoGop");
                }

                if (config.get("videoFramerate") != null) {
                    streamConfig.videoFramerate = (int) config.get("videoFramerate");
                }

                if (config.get("videoBitrate") != null) {
                    streamConfig.videoBitrate = (int) config.get("videoBitrate");
                }

                if (config.get("audioBitrate") != null) {
                    streamConfig.audioBitrate = (int) config.get("audioBitrate");
                }

                if (config.get("audioChannels") != null) {
                    streamConfig.audioChannels = (int) config.get("audioChannels");
                }

                if (config.get("audioSampleRate") != null) {
                    int audioSampleRate = (int) config.get("audioSampleRate");
                    for (LiveInjectStreamConfig.AudioSampleRateType rateType : LiveInjectStreamConfig.AudioSampleRateType.values()) {
                        if (LiveInjectStreamConfig.AudioSampleRateType.getValue(rateType) == audioSampleRate) {
                            streamConfig.audioSampleRate = rateType;
                            break;
                        }
                    }
                }
                result.success(mRtcEngine.addInjectStreamUrl(url, streamConfig));
            }
            break;
            case "removeInjectStreamUrl": {
                if (mRtcEngine == null) {
                    result.success(null);
                    return;
                }
                String url = call.argument("url");
                result.success(mRtcEngine.removeInjectStreamUrl(url));
            }
            break;

            // Encryption
            case "setEncryptionSecret": {
                if (mRtcEngine == null) {
                    result.success(null);
                    return;
                }
                String secret = call.argument("secret");
                mRtcEngine.setEncryptionSecret(secret);
                result.success(null);
            }
            break;
            case "setEncryptionMode": {
                if (mRtcEngine == null) {
                    result.success(null);
                    return;
                }
                String encryptionMode = call.argument("encryptionMode");
                mRtcEngine.setEncryptionMode(encryptionMode);
                result.success(null);
            }
            break;

            case "startEchoTestWithInterval": {
                if (mRtcEngine == null) {
                    result.success(null);
                    return;
                }
                int interval = call.argument("interval");
                mRtcEngine.startEchoTest(interval);
                result.success(null);
            }
            break;

            case "stopEchoTest": {
                if (mRtcEngine == null) {
                    result.success(null);
                    return;
                }
                mRtcEngine.stopEchoTest();
                result.success(null);
            }
            break;

            case "enableLastmileTest": {
                if (mRtcEngine == null) {
                    result.success(null);
                    return;
                }
                mRtcEngine.enableLastmileTest();
                result.success(null);
            }
            break;

            case "disableLastmileTest": {
                if (mRtcEngine == null) {
                    result.success(null);
                    return;
                }
                mRtcEngine.disableLastmileTest();
                result.success(null);
            }
            break;

            case "startLastmileProbeTest": {
                if (mRtcEngine == null) {
                    result.success(null);
                    return;
                }
                HashMap<String, Object> probeConfig = call.argument("config");
                LastmileProbeConfig config = new LastmileProbeConfig();
                config.expectedDownlinkBitrate = (int) probeConfig.get("expectedDownlinkBitrate");
                config.expectedUplinkBitrate = (int) probeConfig.get("expectedUplinkBitrate");
                config.probeDownlink = (boolean) probeConfig.get("probeDownlink");
                config.probeUplink = (boolean) probeConfig.get("probeUplink");

                mRtcEngine.startLastmileProbeTest(config);
                result.success(null);
            }
            break;

            case "stopLastmileProbeTest": {
                if (mRtcEngine == null) {
                    result.success(null);
                    return;
                }
                mRtcEngine.stopLastmileProbeTest();
                result.success(null);
            }
            break;

            case "addVideoWatermark": {
                if (mRtcEngine == null) {
                    result.success(null);
                    return;
                }
                String encryptionMode = call.argument("encryptionMode");
                String url = call.argument("url");
                HashMap<String, Object> watermarkOptions = call.argument("options");
                HashMap<String, Object> positionLandscapeOptions = (HashMap<String, Object>) watermarkOptions.get("positionInPortraitMode");
                WatermarkOptions options = new WatermarkOptions();
                WatermarkOptions.Rectangle landscapePosition = new WatermarkOptions.Rectangle();
                landscapePosition.height = (int) positionLandscapeOptions.get("height");
                landscapePosition.width = (int) positionLandscapeOptions.get("width");
                landscapePosition.x = (int) positionLandscapeOptions.get("x");
                landscapePosition.y = (int) positionLandscapeOptions.get("y");

                HashMap<String, Object> positionPortraitOptions = (HashMap<String, Object>) watermarkOptions.get("positionInPortraitMode");
                WatermarkOptions.Rectangle portraitPosition = new WatermarkOptions.Rectangle();
                portraitPosition.height = (int) positionPortraitOptions.get("height");
                portraitPosition.width = (int) positionPortraitOptions.get("width");
                portraitPosition.x = (int) positionPortraitOptions.get("x");
                portraitPosition.y = (int) positionPortraitOptions.get("y");

                options.positionInLandscapeMode = landscapePosition;
                options.visibleInPreview = (boolean) watermarkOptions.get("visibleInPreview");
                options.positionInPortraitMode = portraitPosition;
                mRtcEngine.addVideoWatermark(url, options);
                result.success(null);
            }
            break;

            case "clearVideoWatermarks": {
                if (mRtcEngine == null) {
                    result.success(null);
                    return;
                }
                mRtcEngine.clearVideoWatermarks();
                result.success(null);
            }
            break;

            case "startAudioMixing": {
                if (mRtcEngine == null) {
                    result.success(null);
                    return;
                }
                String filepath = call.argument("filepath");
                boolean loopback = call.argument("loopback");
                boolean replace = call.argument("replace");
                int cycle = call.argument("cycle");
                mRtcEngine.startAudioMixing(filepath, loopback, replace, cycle);
                result.success(null);
            }
            break;

            case "stopAudioMixing": {
                if (mRtcEngine == null) {
                    result.success(null);
                    return;
                }
                mRtcEngine.stopAudioMixing();
                result.success(null);
            }
            break;

            case "pauseAudioMixing": {
                if (mRtcEngine == null) {
                    result.success(null);
                    return;
                }
                mRtcEngine.pauseAudioMixing();
                result.success(null);
            }
            break;

            case "resumeAudioMixing": {
                if (mRtcEngine == null) {
                    result.success(null);
                    return;
                }
                mRtcEngine.resumeAudioMixing();
                result.success(null);
            }
            break;

            case "adjustAudioMixingVolume": {
                if (mRtcEngine == null) {
                    result.success(null);
                    return;
                }
                int volume = call.argument("volume");
                mRtcEngine.adjustAudioMixingVolume(volume);
                result.success(null);
            }
            break;

            case "adjustAudioMixingPlayoutVolume": {
                if (mRtcEngine == null) {
                    result.success(null);
                    return;
                }
                int volume = call.argument("volume");
                mRtcEngine.adjustAudioMixingPlayoutVolume(volume);
                result.success(null);
            }
            break;

            case "adjustAudioMixingPublishVolume": {
                if (mRtcEngine == null) {
                    result.success(null);
                    return;
                }
                int volume = call.argument("volume");
                mRtcEngine.adjustAudioMixingPublishVolume(volume);
                result.success(null);
            }
            break;

            case "getAudioMixingPlayoutVolume": {
                if (mRtcEngine == null) {
                    result.success(null);
                    return;
                }
                int res = mRtcEngine.getAudioMixingPlayoutVolume();
                result.success(res);
            }
            break;

            case "getAudioMixingPublishVolume": {
                if (mRtcEngine == null) {
                    result.success(null);
                    return;
                }
                int res = mRtcEngine.getAudioMixingPublishVolume();
                result.success(res);
            }
            break;

            case "getAudioMixingDuration": {
                if (mRtcEngine == null) {
                    result.success(null);
                    return;
                }
                int res = mRtcEngine.getAudioMixingDuration();
                result.success(res);
            }
            break;

            case "getAudioMixingCurrentPosition": {
                if (mRtcEngine == null) {
                    result.success(null);
                    return;
                }
                int res = mRtcEngine.getAudioMixingCurrentPosition();
                result.success(res);
            }
            break;

            case "setAudioMixingPosition": {
                if (mRtcEngine == null) {
                    result.success(null);
                    return;
                }
                int pos = call.argument("pos");
                mRtcEngine.setAudioMixingPosition(pos);
                result.success(null);
            }
            break;

            case "getEffectsVolume": {
                if (mRtcEngine == null) {
                    result.success(null);
                    return;
                }
                double volume = mRtcEngine.getAudioEffectManager().getEffectsVolume();
                result.success(volume);
            }
            break;

            case "setEffectsVolume": {
                if (mRtcEngine == null) {
                    result.success(null);
                    return;
                }
                double volume = call.argument("volume");
                mRtcEngine.getAudioEffectManager().setEffectsVolume(volume);
                result.success(null);
            }
            break;

            case "setVolumeOfEffect": {
                if (mRtcEngine == null) {
                    result.success(null);
                    return;
                }
                double volume = call.argument("volume");
                int soundId = call.argument("soundId");
                mRtcEngine.getAudioEffectManager().setVolumeOfEffect(soundId, volume);
                result.success(null);
            }
            break;

            case "playEffect": {
                if (mRtcEngine == null) {
                    result.success(null);
                    return;
                }
                int soundId = call.argument("soundId");
                String filepath = call.argument("filepath");
                int loopback = call.argument("loopback");
                double pitch = call.argument("pitch");
                double pan = call.argument("pan");
                double gain = call.argument("gain");
                boolean publish = call.argument("publish");
                mRtcEngine.getAudioEffectManager().playEffect(soundId, filepath, loopback, pitch, pan, gain, publish);
                result.success(null);
            }
            break;

            case "stopEffect": {
                if (mRtcEngine == null) {
                    result.success(null);
                    return;
                }
                int soundId = call.argument("soundId");
                mRtcEngine.getAudioEffectManager().stopEffect(soundId);
                result.success(null);
            }
            break;

            case "stopAllEffects": {
                if (mRtcEngine == null) {
                    result.success(null);
                    return;
                }
                mRtcEngine.getAudioEffectManager().stopAllEffects();
                result.success(null);
            }
            break;

            case "preloadEffect": {
                if (mRtcEngine == null) {
                    result.success(null);
                    return;
                }
                int soundId = call.argument("soundId");
                String filepath = call.argument("filepath");
                mRtcEngine.getAudioEffectManager().preloadEffect(soundId, filepath);
                result.success(null);
            }
            break;

            case "unloadEffect": {
                if (mRtcEngine == null) {
                    result.success(null);
                    return;
                }
                int soundId = call.argument("soundId");
                mRtcEngine.getAudioEffectManager().unloadEffect(soundId);
                result.success(null);
            }
            break;

            case "pauseEffect": {
                if (mRtcEngine == null) {
                    result.success(null);
                    return;
                }
                int soundId = call.argument("soundId");
                mRtcEngine.getAudioEffectManager().pauseEffect(soundId);
                result.success(null);
            }
            break;

            case "pauseAllEffects": {
                if (mRtcEngine == null) {
                    result.success(null);
                    return;
                }
                mRtcEngine.getAudioEffectManager().pauseAllEffects();
                result.success(null);
            }
            break;

            case "resumeEffect": {
                if (mRtcEngine == null) {
                    result.success(null);
                    return;
                }
                int soundId = call.argument("soundId");
                mRtcEngine.getAudioEffectManager().resumeEffect(soundId);
                result.success(null);
            }
            break;

            case "resumeAllEffects": {
                if (mRtcEngine == null) {
                    result.success(null);
                    return;
                }
                mRtcEngine.getAudioEffectManager().resumeAllEffects();
                result.success(null);
            }
            break;

            case "startChannelMediaRelay": {
                if (mRtcEngine == null) {
                    result.success(null);
                    return;
                }
                ChannelMediaRelayConfiguration config = new ChannelMediaRelayConfiguration();
                ChannelMediaInfo src = config.getSrcChannelMediaInfo();
                HashMap<String, Object> options = call.argument("config");
                if (options.get("src") != null) {
                    HashMap<String, Object> srcOption = (HashMap<String, Object>) options.get("src");
                    if (srcOption.get("token") != null) {
                        src.token = (String) srcOption.get("token");
                    }
                    if (srcOption.get("channelName") != null) {
                        src.channelName = (String) srcOption.get("channelName");
                    }
                }
                List<HashMap<String, Object>> dstMediaInfo = (List) options.get("channels");
                for (int i = 0; i < dstMediaInfo.size(); i++) {
                    HashMap<String, Object> dst = dstMediaInfo.get(i);
                    String channelName = null;
                    String token = null;
                    Integer uid = 0;
                    if (dst.get("token") != null) {
                        token = token;
                    }
                    if (dst.get("channelName") != null) {
                        channelName = (String) dst.get("channelName");
                    }
                    if (dst.get("uid") != null) {
                        uid = (int) dst.get("uid");
                    }
                    config.setDestChannelInfo(channelName, new ChannelMediaInfo(channelName, token, uid));
                }
                mRtcEngine.startChannelMediaRelay(config);
                result.success(null);
            }
            break;

            case "removeChannelMediaRelay": {
                if (mRtcEngine == null) {
                    result.success(null);
                    return;
                }
                ChannelMediaRelayConfiguration config = new ChannelMediaRelayConfiguration();
                ChannelMediaInfo src = config.getSrcChannelMediaInfo();
                HashMap<String, Object> options = call.argument("config");
                if (options.get("src") != null) {
                    HashMap<String, Object> srcOption = (HashMap<String, Object>) options.get("src");
                    if (srcOption.get("token") != null) {
                        src.token = (String) srcOption.get("token");
                    }
                    if (srcOption.get("channelName") != null) {
                        src.channelName = (String) srcOption.get("channelName");
                    }
                }
                List<HashMap<String, Object>> dstMediaInfo = (List) options.get("channels");
                for (int i = 0; i < dstMediaInfo.size(); i++) {
                    HashMap<String, Object> dst = dstMediaInfo.get(i);
                    String channelName = null;
                    if (dst.get("channelName") != null) {
                        channelName = (String) dst.get("channelName");
                    }
                    config.removeDestChannelInfo(channelName);
                }
                mRtcEngine.updateChannelMediaRelay(config);
                result.success(null);
            }
            break;

            case "updateChannelMediaRelay": {
                if (mRtcEngine == null) {
                    result.success(null);
                    return;
                }
                ChannelMediaRelayConfiguration config = new ChannelMediaRelayConfiguration();
                ChannelMediaInfo src = config.getSrcChannelMediaInfo();
                HashMap<String, Object> options = call.argument("config");
                if (options.get("src") != null) {
                    HashMap<String, Object> srcOption = (HashMap<String, Object>) options.get("src");
                    if (srcOption.get("token") != null) {
                        src.token = (String) srcOption.get("token");
                    }
                    if (srcOption.get("channelName") != null) {
                        src.channelName = (String) srcOption.get("channelName");
                    }
                }
                List<HashMap<String, Object>> dstMediaInfo = (List) options.get("channels");
                for (int i = 0; i < dstMediaInfo.size(); i++) {
                    HashMap<String, Object> dst = dstMediaInfo.get(i);
                    String channelName = null;
                    String token = null;
                    Integer uid = 0;
                    if (dst.get("token") != null) {
                        token = token;
                    }
                    if (dst.get("channelName") != null) {
                        channelName = (String) dst.get("channelName");
                    }
                    if (dst.get("uid") != null) {
                        uid = (int) dst.get("uid");
                    }
                    config.setDestChannelInfo(channelName, new ChannelMediaInfo(channelName, token, uid));
                }
                mRtcEngine.updateChannelMediaRelay(config);
                result.success(null);
            }
            break;

            case "stopChannelMediaRelay": {
                if (mRtcEngine == null) {
                    result.success(null);
                    return;
                }
                mRtcEngine.stopChannelMediaRelay();
                result.success(null);
            }
            break;

            case "enableInEarMonitoring": {
                if (mRtcEngine == null) {
                    result.success(null);
                    return;
                }
                boolean enabled = call.argument("enabled");
                mRtcEngine.enableInEarMonitoring(enabled);
                result.success(null);
            }
            break;

            case "setInEarMonitoringVolume": {
                if (mRtcEngine == null) {
                    result.success(null);
                    return;
                }
                int volume = call.argument("volume");
                mRtcEngine.setInEarMonitoringVolume(volume);
                result.success(null);
            }
            break;

            // Camera Control
            case "switchCamera": {
                if (mRtcEngine == null) {
                    result.success(null);
                    return;
                }
                mRtcEngine.switchCamera();
                result.success(null);
            }
            break;

            // Miscellaneous Methods
            case "getSdkVersion": {
                if (mRtcEngine == null) {
                    result.success(null);
                    return;
                }
                String version = RtcEngine.getSdkVersion();
                result.success(version);
            }
            break;

            case "setLogFile": {
                if (mRtcEngine == null) {
                    result.success(null);
                    return;
                }
                String filePath = call.argument("filePath");
                mRtcEngine.setLogFile(filePath);
                result.success(null);
            }
            break;

            case "setLogFilter": {
                if (mRtcEngine == null) {
                    result.success(null);
                    return;
                }
                int filter = call.argument("filter");
                mRtcEngine.setLogFilter(filter);
                result.success(null);
            }
            break;

            case "setLogFileSize": {
                if (mRtcEngine == null) {
                    result.success(null);
                    return;
                }
                int fileSizeInKBytes = call.argument("fileSizeInKBytes");
                mRtcEngine.setLogFileSize(fileSizeInKBytes);
                result.success(null);
            }
            break;

            case "setParameters": {
                if (mRtcEngine == null) {
                    result.success(null);
                    return;
                }
                String params = call.argument("params");
                int res = mRtcEngine.setParameters(params);
                result.success(res);
            }
            break;

            case "getParameters": {
                if (mRtcEngine == null) {
                    result.success(null);
                    return;
                }
                String params = call.argument("params");
                String args = call.argument("args");
                String res = mRtcEngine.getParameter(params, args);
                result.success(res);
            }
            break;

            default:
                result.notImplemented();
        }
    }

    private BeautyOptions beautyOptionsFromMap(HashMap<String, Object> map) {
        BeautyOptions options = new BeautyOptions();
        options.lighteningContrastLevel =
                ((Double) (map.get("lighteningContrastLevel"))).intValue();
        options.lighteningLevel = ((Double) (map.get("lighteningLevel"))).floatValue();
        options.smoothnessLevel = ((Double) (map.get("smoothnessLevel"))).floatValue();
        options.rednessLevel = ((Double) (map.get("rednessLevel"))).floatValue();
        return options;
    }

    private VideoEncoderConfiguration videoEncoderConfigurationFromMap(HashMap<String, Object> map) {
        int width = (int) (map.get("width"));
        int height = (int) (map.get("height"));
        int frameRate = (int) (map.get("frameRate"));
        int bitrate = (int) (map.get("bitrate"));
        int minBitrate = (int) (map.get("minBitrate"));
        int orientationMode = (int) (map.get("orientationMode"));

        VideoEncoderConfiguration configuration = new VideoEncoderConfiguration();
        configuration.dimensions = new VideoEncoderConfiguration.VideoDimensions(width, height);
        configuration.frameRate = frameRate;
        configuration.bitrate = bitrate;
        configuration.minBitrate = minBitrate;
        configuration.orientationMode = orientationFromValue(orientationMode);

        return configuration;
    }

    private VideoEncoderConfiguration.ORIENTATION_MODE orientationFromValue(int value) {
        switch (value) {
            case 0:
                return VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_ADAPTIVE;
            case 1:
                return VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_LANDSCAPE;
            case 2:
                return VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT;
            default:
                return VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_ADAPTIVE;
        }
    }

    private AgoraImage createAgoraImage(Map<String, Object> options) {
        AgoraImage image = new AgoraImage();
        image.url = (String) options.get("url");
        image.height = (int) options.get("height");
        image.width = (int) options.get("width");
        image.x = (int) options.get("x");
        image.y = (int) options.get("y");
        return image;
    }
}
