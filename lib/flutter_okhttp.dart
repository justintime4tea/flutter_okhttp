import 'method_channel_flutter_okhttp.dart';

class FlutterOkhttp extends MethodChannelFlutterOkHttp {
  static Future<String> get platformVersion async => MethodChannelFlutterOkHttp.platformVersion;
}
