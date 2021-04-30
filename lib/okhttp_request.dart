import 'mappable.dart';

class OkHttpRequest implements Mappable {
  final String requestId;
  final String url;
  final Map<String, String>? headers;

  const OkHttpRequest({
    required this.requestId,
    required this.url,
    this.headers,
  });

  @override
  Map<String, dynamic> toMap() {
    return <String, dynamic>{
      'requestId': requestId,
      'url': url,
      'headers': headers,
    };
  }
}
