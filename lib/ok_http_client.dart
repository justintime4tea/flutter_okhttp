import 'dart:typed_data';

import 'dart:convert';

import 'package:http/http.dart' as http;

import 'flutter_okhttp_client.dart';
import 'okhttp_post_request.dart';
import 'okhttp_request.dart';
import 'okhttp_response.dart';

class OkHttpClient implements http.Client {
  static final OkHttpClient _instance = OkHttpClient._internal();

  factory OkHttpClient() {
    return _instance;
  }

  OkHttpClient._internal();

  @override
  void close() {
    // TODO: implement close
  }

  @override
  Future<http.Response> delete(url, {Map<String, String> headers}) async {
    final okRequest = OkHttpRequest(
      url: url,
      headers: headers,
    );
    OkHttpResponse okResponse = await FlutterOkHttpClient().delete(okRequest);

    return http.Response(okResponse.body, okResponse.code, headers: okResponse.headers);
  }

  @override
  Future<http.Response> get(url, {Map<String, String> headers}) async {
    final okRequest = OkHttpRequest(
      url: url,
      headers: headers,
    );
    OkHttpResponse okResponse = await FlutterOkHttpClient().get(okRequest);

    return http.Response(okResponse.body, okResponse.code, headers: okResponse.headers);
  }

  @override
  Future<http.Response> head(url, {Map<String, String> headers}) async {
    final okRequest = OkHttpRequest(
      url: url,
      headers: headers,
    );
    OkHttpResponse okResponse = await FlutterOkHttpClient().get(okRequest);

    return http.Response(okResponse.body, okResponse.code, headers: okResponse.headers);
  }

  @override
  Future<http.Response> patch(url, {Map<String, String> headers, body, Encoding encoding}) async {
    final okRequest = OkHttpRequestWithPayload(
      url: url,
      headers: headers,
      body: body
    );
    OkHttpResponse okResponse = await FlutterOkHttpClient().patch(okRequest);

    return http.Response(okResponse.body, okResponse.code, headers: okResponse.headers);
  }

  @override
  Future<http.Response> post(url, {Map<String, String> headers, body, Encoding encoding}) async {
    final okRequest = OkHttpRequestWithPayload(
      url: url,
      headers: headers,
      body: body
    );
    OkHttpResponse okResponse = await FlutterOkHttpClient().post(okRequest);

    return http.Response(okResponse.body, okResponse.code, headers: okResponse.headers);
  }

  @override
  Future<http.Response> put(url, {Map<String, String> headers, body, Encoding encoding}) async {
    final okRequest = OkHttpRequestWithPayload(
      url: url,
      headers: headers,
      body: body
    );
    OkHttpResponse okResponse = await FlutterOkHttpClient().put(okRequest);

    return http.Response(okResponse.body, okResponse.code, headers: okResponse.headers);
  }

  @override
  Future<String> read(url, {Map<String, String> headers}) {
    // TODO: implement read
    throw UnimplementedError();
  }

  @override
  Future<Uint8List> readBytes(url, {Map<String, String> headers}) {
    // TODO: implement readBytes
    throw UnimplementedError();
  }

  @override
  Future<http.StreamedResponse> send(http.BaseRequest request) {
    // TODO: implement send
    throw UnimplementedError();
  }
}
