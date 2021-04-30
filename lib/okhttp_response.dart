class OkHttpResponse {
  final num code;
  final String body;
  final String message;
  final Map<String, String> headers;

  const OkHttpResponse({
    required this.code,
    required this.body,
    required this.message,
    required this.headers,
  });
}
