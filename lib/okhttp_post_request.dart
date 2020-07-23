import 'okhttp_request.dart';

class OkHttpRequestWithPayload extends OkHttpRequest {
  final String body;

  const OkHttpRequestWithPayload({String requestId, String url, Map<String, String> headers, this.body})
      : super(requestId: requestId, url: url, headers: headers);

  @override
  Map<String, dynamic> toMap() {
    final mergedMap = super.toMap();
    if (body != null) {
      mergedMap.putIfAbsent('body', () => body);
    }
    return mergedMap;
  }
}
