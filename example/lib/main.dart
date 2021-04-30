import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:flutter_okhttp/flutter_okhttp.dart';
import 'package:flutter_okhttp/ok_retry_http_client.dart';
import 'package:http/http.dart' as http;

void main() {
  runApp(MyApp());
}

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _platformVersion = 'Unknown';
  String _messageToUser = '';

  @override
  void initState() {
    super.initState();
    initPlatformState();
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    String platformVersion = '';
    String messageToUser = '';

    // Platform messages may fail, so we use a try/catch PlatformException.
    try {
      platformVersion = await FlutterOkhttp.platformVersion;
    } on PlatformException {
      platformVersion = 'Failed to get platform version.';
    }

    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) return;

    setState(() {
      _platformVersion = platformVersion;
      _messageToUser = messageToUser;
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('FlutterOkHttp Client Test'),
        ),
        body: Center(
          child: Column(
            children: [
              Padding(
                padding: EdgeInsets.all(8),
                child: Text('Platform: $_platformVersion'),
              ),
              Padding(
                padding: EdgeInsets.all(8),
                child: Text(_messageToUser),
              ),
              Padding(
                padding: EdgeInsets.all(8),
                child: ElevatedButton(
                  onPressed: _onButtonPress,
                  child: Text("TEST REQUEST"),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Future<void> _onButtonPress() async {
    String responseBody = 'Unknown';

    FlutterOkhttp().addTrustedCaCert('example.pem');
    FlutterOkhttp().addTrustedHost('10.0.2.2');

    http.Client httpClient = FlutterOkhttp().createDartHttpClient();
    try {
      // This client will try to make an HTTP call... if it fails it will retry as many times as there are Duration listed.
      // There are 3 durations listed in the example below. The HTTP GET call will be made, at most, 4 times; once for
      // the initial call and 3 additional times for each retry with delays of 1, 3 and 5 seconds.
      OkRetryHttpClient retryClient = OkRetryHttpClient.withDelays(
        httpClient,
        [
          Duration(seconds: 1),
          Duration(seconds: 3),
          Duration(seconds: 5),
        ],
        onRequestCompleted: (response, i) {
          print("Request ${i + 1} has finished!");
        },
        shouldRetryRequestOnError: (error, stacktrace) {
          if (error is PlatformException) {
            switch (error.code) {
              case 'ON_HTTP_DNS_RESOLUTION_FAILURE':
                // If you expect occasional DNS failure then it may make sense to allow retry by return 'true' otherwise this error is non-recoverable
                return false;
              case 'ON_HTTP_ERROR':
                // In this case we want to retry when OkHttp error code is a "generic" FlutterOkHttp error
                return true;
              default:
                // In the default case, any PlatformExceptions un-matched above, do not retry because it may be a futile effort
                return false;
            }
          }

          return false;
        },
      );

      http.Response r = await retryClient.get(Uri.parse('https://10.0.2.2:4443/')).timeout(Duration(seconds: 25));
      responseBody = r.body;

      setState(() {
        _messageToUser = responseBody;
      });
    } catch (e) {
      setState(() {
        _messageToUser = e.toString();
      });
    }

    return;
  }
}
