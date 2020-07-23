import 'dart:convert';

import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:flutter_okhttp/flutter_okhttp.dart';
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
  String _testResponse = 'Unknown';

  @override
  void initState() {
    super.initState();
    initPlatformState();
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    String platformVersion;
    String testResponse = '{}';

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
      _testResponse = testResponse;
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Center(
          child: Column(
            children: [
              Text('Running on: $_platformVersion\n'),
              Text(_testResponse),
              RaisedButton(
                onPressed: _onButtonPress,
              )
            ],
          ),
        ),
      ),
    );
  }

  Future<void> _onButtonPress() async {
    String testResponse = 'Unknown';

    FlutterOkhttp().addTrustedCaCert('example.pem');
    FlutterOkhttp().addTrustedHost('10.0.2.2');

    http.Client httpClient = FlutterOkhttp().createDartHttpClient();
    try {
      http.Response getResponse = await httpClient.get('https://10.0.2.2:4443/');
      testResponse = getResponse.body;

      setState(() {
        _testResponse = testResponse;
      });
    } catch (e) {
      setState(() {
        _testResponse = e.toString();
      });
    }
    return true;
  }
}
