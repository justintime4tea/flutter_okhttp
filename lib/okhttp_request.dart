import 'mappable.dart';

class OkHttpRequest implements Mappable {
  final String url;
  final Map<String, String> headers;

  const OkHttpRequest({this.url, this.headers});

  @override
  Map<String, dynamic> toMap() {
    return <String, dynamic>{
      'url': url,
      'headers': headers,
    };
  }
}
