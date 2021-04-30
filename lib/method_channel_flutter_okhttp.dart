import 'package:flutter/services.dart';
import 'package:http/http.dart';

import 'flutter_okhttp_platform.dart';
import 'ok_http_client.dart';
import 'okhttp_post_request.dart';
import 'okhttp_request.dart';
import 'okhttp_response.dart';

const MethodChannel _channel = MethodChannel('tech.jgross.flutter_okhttp');

class MethodChannelFlutterOkHttp implements FlutterOkHttpPlatform {
  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  @override
  Client createDartHttpClient() {
    return OkHttpClient();
  }

  @override
  Future<void> addTrustedCaCert(String filename) {
    return _channel.invokeMethod('ADD_TRUSTED_CERT', {'filename': filename});
  }

  @override
  Future<void> removeTrustedCaCert(String filename) {
    return _channel.invokeMethod('REMOVE_TRUSTED_CERT', {'filename': filename});
  }

  @override
  Future<void> addTrustedHost(String host) {
    return _channel.invokeMethod('ADD_TRUSTED_HOST', {'host': host});
  }

  @override
  Future<void> removeTrustedHost(String host) {
    return _channel.invokeMethod('REMOVE_TRUSTED_HOST', {'host': host});
  }

  @override
  Future<OkHttpResponse> get(OkHttpRequest request) async {
    final Map<dynamic, dynamic> result = await _channel.invokeMethod('GET', request.toMap());

    return OkHttpResponse(
      code: result['code'],
      message: result['message'],
      body: result['body'],
      headers: result['headers']?.cast<String, String>(),
    );
  }

  @override
  Future<OkHttpResponse> post(OkHttpRequestWithPayload request) async {
    final Map<dynamic, dynamic> result = await _channel.invokeMethod('POST', request.toMap());

    return OkHttpResponse(
      code: result['code'],
      message: result['message'],
      body: result['body'],
      headers: result['headers']?.cast<String, String>(),
    );
  }

  @override
  Future<OkHttpResponse> delete(OkHttpRequest request) async {
    final Map<dynamic, dynamic> result = await _channel.invokeMethod('DEL', request.toMap());

    return OkHttpResponse(
      code: result['code'],
      message: result['message'],
      body: result['body'],
      headers: result['headers']?.cast<String, String>(),
    );
  }

  @override
  Future<OkHttpResponse> patch(OkHttpRequestWithPayload request) async {
    final Map<dynamic, dynamic> result = await _channel.invokeMethod('PATCH', request.toMap());

    return OkHttpResponse(
      code: result['code'],
      message: result['message'],
      body: result['body'],
      headers: result['headers']?.cast<String, String>(),
    );
  }

  @override
  Future<OkHttpResponse> put(OkHttpRequestWithPayload request) async {
    final Map<dynamic, dynamic> result = await _channel.invokeMethod('PUT', request.toMap());

    return OkHttpResponse(
      code: result['code'],
      message: result['message'],
      body: result['body'],
      headers: result['headers']?.cast<String, String>(),
    );
  }
}
