import 'package:http/http.dart';

import 'method_channel_flutter_okhttp.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'okhttp_post_request.dart';
import 'okhttp_request.dart';
import 'okhttp_response.dart';

abstract class FlutterOkHttpPlatform extends PlatformInterface {
  FlutterOkHttpPlatform() : super(token: _token);

  /// The default instance of [FlutterOkHttpPlatform] to use.
  ///
  /// Defaults to [MethodChannelFlutterAppAuth].
  static FlutterOkHttpPlatform get instance => _instance;

  static FlutterOkHttpPlatform _instance = MethodChannelFlutterOkHttp();

  static final Object _token = Object();

  static set instance(FlutterOkHttpPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Client createDartHttpClient() {
    throw UnimplementedError();
  }

  Future<void> addTrustedCaCert(String filename) {
    throw UnimplementedError('addCertOverride() has not been implemented.');
  }

  Future<void> removeTrustedCaCert(String filename) {
    throw UnimplementedError('removeCertOverride() has not been implemented.');
  }

  Future<void> addTrustedHost(String host) {
    throw UnimplementedError('addTrustedHost() has not been implemented.');
  }

  Future<void> removeTrustedHost(String host) {
    throw UnimplementedError('removeTrustedHost() has not been implemented.');
  }

  Future<OkHttpResponse> get(OkHttpRequest request) {
    throw UnimplementedError('get() has not been implemented');
  }

  Future<OkHttpResponse> post(OkHttpRequestWithPayload request) {
    throw UnimplementedError('post() has not been implemented');
  }

  Future<OkHttpResponse> put(OkHttpRequestWithPayload request) {
    throw UnimplementedError('put() has not been implemented');
  }

  Future<OkHttpResponse> patch(OkHttpRequestWithPayload request) {
    throw UnimplementedError('patch() has not been implemented');
  }

  Future<OkHttpResponse> delete(OkHttpRequest request) {
    throw UnimplementedError('delete() has not been implemented');
  }
}
