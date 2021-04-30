class OkHttpResponse {
  final num code;
  final String body;
  final String message;
  final Map<String, String> headers;

  const OkHttpResponse({
    this.code,
    this.body,
    this.message,
    this.headers,
  });
}
