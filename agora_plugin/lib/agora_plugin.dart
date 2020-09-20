
import 'dart:async';

import 'package:flutter/services.dart';

class AgoraPlugin {
  static const MethodChannel _channel =
      const MethodChannel('agora_plugin');

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }
}
