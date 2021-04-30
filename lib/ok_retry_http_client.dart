import 'dart:async';
import 'dart:convert';
import 'dart:math' as math;
import 'dart:typed_data';

import 'package:flutter/material.dart';
import 'package:http/http.dart';

/// An HTTP client wrapper that automatically retries failed requests.
class OkRetryHttpClient implements Client {
  /// The wrapped client.
  final Client _inner;

  /// The number of times a request should be retried.
  final int _retries;

  /// The callback that, when returning true, indicates that a request should be retried.
  final bool Function(Response) _shouldRetryRequest;

  /// The callback that, when returning false, rethrows the error that was caught internally.
  /// When returning true the request is retried regardless of the error which occurred.
  final bool Function(Object, StackTrace) _shouldRetryRequestOnError;

  /// The callback that determines how long to wait before retrying a request.
  final Duration Function(int) _delay;

  /// The callback to call to indicate that a request has just finished being retried.
  final void Function(Response, int) _onRequestCompleted;

  /// Creates a client wrapping [_inner] that retries HTTP requests.
  ///
  /// This client witll retry a failing request [retries] times (3 by default). Note that
  /// `n` retries means that the request will be sent at most `n + 1` times.
  ///
  /// By default, this retries requests whose responses have status code 503
  /// Temporary Failure / Service Unavailable. If [shouldRetryRequest] is passed, it retries any request for whose
  /// response [shouldRetryRequest] returns `true`. If [shouldRetryRequestOnError] is passed, it also retries
  /// any request that throws an error for which [shouldRetryRequestOnError] returns `true`.
  ///
  /// By default, this waits 500ms between the original request and the first
  /// retry, then increases the delay by 1.5x for each subsequent retry. If
  /// [delay] is passed, it's used to determine the time to wait before the
  /// given (zero-based) retry.
  ///
  /// If [onRequestCompleted] is passed, it's called immediately after each request has completed. The
  /// `response` parameter will be null if the request was retried due to an
  /// error for which [shouldRetryRequestOnError] returned `true`.
  OkRetryHttpClient(
    this._inner, {
    int retries = 3,
    bool Function(Response) shouldRetryRequest = _defaultShouldRetryRequest,
    bool Function(Object, StackTrace) shouldRetryRequestOnError = _defaultShouldRetryOnError,
    Duration Function(int retryCount) delay = _defaultDelay,
    void Function(Response, int retryCount) onRequestCompleted,
  })  : _retries = retries,
        _shouldRetryRequest = shouldRetryRequest,
        _shouldRetryRequestOnError = shouldRetryRequestOnError,
        _delay = delay,
        _onRequestCompleted = onRequestCompleted {
    RangeError.checkNotNegative(_retries, 'retries');
  }

  /// Like [new OkRetryHttpClient], but with a pre-computed list of [delays]
  /// between each retry.
  ///
  /// This will retry a request at most `delays.length` times, using each delay
  /// in order. It will wait for `delays[0]` after the initial request,
  /// `delays[1]` after the first retry, and so on.
  OkRetryHttpClient.withDelays(
    Client inner,
    Iterable<Duration> delays, {
    bool Function(Response) shouldRetryRequest = _defaultShouldRetryRequest,
    bool Function(Object, StackTrace) shouldRetryRequestOnError = _defaultShouldRetryOnError,
    void Function(Response, int retryCount) onRequestCompleted,
  }) : this._withDelays(
          inner,
          delays.toList(),
          shouldRetryRequest: shouldRetryRequest,
          shouldRetryRequestOnError: shouldRetryRequestOnError,
          onRequestCompleted: onRequestCompleted,
        );

  OkRetryHttpClient._withDelays(
    Client inner,
    List<Duration> delays, {
    bool Function(Response) shouldRetryRequest,
    bool Function(Object, StackTrace) shouldRetryRequestOnError,
    void Function(Response, int) onRequestCompleted,
  }) : this(
          inner,
          retries: delays.length,
          delay: (retryCount) => delays[retryCount],
          shouldRetryRequest: shouldRetryRequest,
          shouldRetryRequestOnError: shouldRetryRequestOnError,
          onRequestCompleted: onRequestCompleted,
        );

  @override
  Future<StreamedResponse> send(BaseRequest request) async {
    throw UnimplementedError();
  }

  @override
  void close() => _inner.close();

  @override
  Future<Response> delete(Uri url, {Map<String, String> headers, Object body, Encoding encoding}) async {
    var i = 0;
    for (;;) {
      Response response;
      try {
        response = await _inner.delete(
          url,
          headers: headers,
          body: body,
          encoding: encoding,
        );
      } catch (error, stackTrace) {
        debugPrint("Failed ${i + 1} for DELETE $url");
        if (i == _retries || !_shouldRetryRequestOnError(error, stackTrace)) rethrow;
      }

      if (response != null) {
        if (i == _retries || !_shouldRetryRequest(response)) return response;
      }

      await Future.delayed(_delay(i));
      debugPrint("Retry ${i + 1} for DELETE $url");
      _onRequestCompleted?.call(response, i);
      i++;
    }
  }

  @override
  Future<Response> get(Uri url, {Map<String, String> headers}) async {
    var i = 0;
    for (;;) {
      Response response;
      try {
        response = await _inner.get(
          url,
          headers: headers,
        );
      } catch (error, stackTrace) {
        debugPrint("Failed ${i + 1} for GET $url");
        if (i == _retries || !_shouldRetryRequestOnError(error, stackTrace)) rethrow;
      }

      if (response != null) {
        if (i == _retries || !_shouldRetryRequest(response)) return response;
      }

      await Future.delayed(_delay(i));
      debugPrint("Retry ${i + 1} for GET $url");
      _onRequestCompleted?.call(response, i);
      i++;
    }
  }

  @override
  Future<Response> head(Uri url, {Map<String, String> headers}) async {
    var i = 0;
    for (;;) {
      Response response;
      try {
        response = await _inner.head(
          url,
          headers: headers,
        );
      } catch (error, stackTrace) {
        debugPrint("Failed ${i + 1} for HEAD $url");
        if (i == _retries || !_shouldRetryRequestOnError(error, stackTrace)) rethrow;
      }

      if (response != null) {
        if (i == _retries || !_shouldRetryRequest(response)) return response;
      }

      await Future.delayed(_delay(i));
      debugPrint("Retry ${i + 1} for HEAD $url");
      _onRequestCompleted?.call(response, i);
      i++;
    }
  }

  @override
  Future<Response> patch(Uri url, {Map<String, String> headers, Object body, Encoding encoding}) async {
    var i = 0;
    for (;;) {
      Response response;
      try {
        response = await _inner.patch(
          url,
          headers: headers,
          body: body,
          encoding: encoding,
        );
      } catch (error, stackTrace) {
        debugPrint("Failed ${i + 1} for PATCH $url");
        if (i == _retries || !_shouldRetryRequestOnError(error, stackTrace)) rethrow;
      }

      if (response != null) {
        if (i == _retries || !_shouldRetryRequest(response)) return response;
      }

      await Future.delayed(_delay(i));
      debugPrint("Retry ${i + 1} for PATCH $url");
      _onRequestCompleted?.call(response, i);
      i++;
    }
  }

  @override
  Future<Response> post(Uri url, {Map<String, String> headers, Object body, Encoding encoding}) async {
    var i = 0;
    for (;;) {
      Response response;
      try {
        response = await _inner.post(
          url,
          headers: headers,
          body: body,
          encoding: encoding,
        );
      } catch (error, stackTrace) {
        debugPrint("Failed ${i + 1} for POST $url");
        if (i == _retries || !_shouldRetryRequestOnError(error, stackTrace)) rethrow;
      }

      if (response != null) {
        if (i == _retries || !_shouldRetryRequest(response)) return response;
      }

      await Future.delayed(_delay(i));
      debugPrint("Retry ${i + 1} for POST $url");
      _onRequestCompleted?.call(response, i);
      i++;
    }
  }

  @override
  Future<Response> put(Uri url, {Map<String, String> headers, Object body, Encoding encoding}) async {
    var i = 0;
    for (;;) {
      Response response;
      try {
        response = await _inner.put(
          url,
          headers: headers,
          body: body,
          encoding: encoding,
        );
      } catch (error, stackTrace) {
        debugPrint("Failed ${i + 1} for PUT $url");
        if (i == _retries || !_shouldRetryRequestOnError(error, stackTrace)) rethrow;
      }

      if (response != null) {
        if (i == _retries || !_shouldRetryRequest(response)) return response;
      }

      await Future.delayed(_delay(i));
      debugPrint("Retry ${i + 1} for PUT $url");
      _onRequestCompleted?.call(response, i);
      i++;
    }
  }

  @override
  Future<String> read(Uri url, {Map<String, String> headers}) {
    // TODO: implement read
    throw UnimplementedError();
  }

  @override
  Future<Uint8List> readBytes(Uri url, {Map<String, String> headers}) {
    // TODO: implement readBytes
    throw UnimplementedError();
  }
}

bool _defaultShouldRetryRequest(Response response) => response.statusCode == 503;

bool _defaultShouldRetryOnError(Object error, StackTrace stackTrace) => false;

Duration _defaultDelay(int retryCount) => const Duration(milliseconds: 500) * math.pow(1.5, retryCount);
