import 'okhttp_request.dart';

class OkHttpRequestWithPayload extends OkHttpRequest {
  final Object? body;

  const OkHttpRequestWithPayload(
      {required String requestId, required String url, Map<String, String>? headers, required this.body})
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
