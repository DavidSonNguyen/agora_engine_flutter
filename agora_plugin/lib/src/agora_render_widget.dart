import 'package:flutter/material.dart';
import 'base.dart';
import 'engine.dart';

/// AgoraRenderWidget - This widget will automatically manage the native view.
///
/// Enables create native view with `uid` `mode` `local` and destroy native view automatically.
///
class AgoraRenderWidget extends StatefulWidget {
  // uid
  final int uid;

  // local flag
  final bool local;

  // local preview flag;
  final bool preview;

  /// render mode
  final VideoRenderMode mode;

  AgoraRenderWidget(
    this.uid, {
    this.mode = VideoRenderMode.Hidden,
    this.local = false,
    this.preview = false,
    Key key,
  })  : assert(uid != null),
        assert(mode != null),
        assert(local != null),
        assert(preview != null),
        super(key: key ?? Key(uid.toString()));

  @override
  State<StatefulWidget> createState() => _AgoraRenderWidgetState();
}

class _AgoraRenderWidgetState extends State<AgoraRenderWidget> {
  Widget _nativeView;

  int _viewId;

  @override
  void initState() {
    super.initState();
    _nativeView = AgoraEngine.createNativeView((viewId) {
      _viewId = viewId;
      _bindView();
    });
  }

  @override
  void dispose() {
    AgoraEngine.removeNativeView(_viewId);
    if (widget.preview) AgoraEngine.stopPreview();
    super.dispose();
  }

  @override
  void didUpdateWidget(AgoraRenderWidget oldWidget) {
    super.didUpdateWidget(oldWidget);

    if ((widget.uid != oldWidget.uid || widget.local != oldWidget.local) &&
        _viewId != null) {
      _bindView();
      return;
    }

    if (widget.mode != oldWidget.mode) {
      _changeRenderMode();
      return;
    }
  }

  void _bindView() {
    if (widget.local) {
      AgoraEngine.setupLocalVideo(_viewId, widget.mode);
      if (widget.preview) AgoraEngine.startPreview();
    } else {
      AgoraEngine.setupRemoteVideo(_viewId, widget.mode, widget.uid);
    }
  }

  void _changeRenderMode() {
    if (widget.local) {
      AgoraEngine.setLocalRenderMode(widget.mode);
    } else {
      AgoraEngine.setRemoteRenderMode(widget.uid, widget.mode);
    }
  }

  @override
  Widget build(BuildContext context) => _nativeView;
}
