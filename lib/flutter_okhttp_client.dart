import 'flutter_okhttp_platform.dart';
import 'okhttp_post_request.dart';
import 'okhttp_request.dart';
import 'okhttp_response.dart';

class FlutterOkHttpClient {
  Future<OkHttpResponse> get(OkHttpRequest request) {
    return FlutterOkHttpPlatform.instance.get(request);
  }

  Future<OkHttpResponse> post(OkHttpRequestWithPayload request) {
    return FlutterOkHttpPlatform.instance.post(request);
  }

  Future<OkHttpResponse> put(OkHttpRequestWithPayload request) {
    return FlutterOkHttpPlatform.instance.post(request);
  }

  Future<OkHttpResponse> patch(OkHttpRequestWithPayload request) {
    return FlutterOkHttpPlatform.instance.post(request);
  }

  Future<OkHttpResponse> delete(OkHttpRequest request) {
    return FlutterOkHttpPlatform.instance.delete(request);
  }
}
