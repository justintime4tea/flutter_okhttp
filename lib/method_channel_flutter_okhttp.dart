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
  Future<OkHttpResponse> get(OkHttpRequest request) async {
    final Map<dynamic, dynamic> result = await _channel.invokeMethod('GET', request.toMap());

    if (result == null) {
      return null;
    }

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

    if (result == null) {
      return null;
    }

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

    if (result == null) {
      return null;
    }

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

    if (result == null) {
      return null;
    }

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

    if (result == null) {
      return null;
    }

    return OkHttpResponse(
      code: result['code'],
      message: result['message'],
      body: result['body'],
      headers: result['headers']?.cast<String, String>(),
    );
  }
}
