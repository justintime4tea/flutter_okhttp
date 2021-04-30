# flutter_okhttp

A flutter plugin for making HTTP calls within Dart/Flutter which are executed by OkHttp under native Android.

## DISCLAIMER
This is a personal project which has been built for a very specific purpose; to fill in a gap left wide open by the Dart team. Not all of the http.Client interface is implemented. Some of the methods, which must be implemented, simply throw UnimplementedException. This project is hopefuly a shim that will remain useful up until the Dart team decides to provide _real, proper and native_ support for managing SSL/TLS/Certificate trust. In the meantime, pull requests are welcome.

## Why is this library needed?
Performing HTTP requests in Flutter is typically done using the Dio library, http/http.dart or some other library which under-the-hood uses Dio or http. Dio, http, and other libraries (at time of writing) provide no mechanism by which you can effectively establish trust or verify certificates when making HTTP calls to servers which use custom/self-signed certificates and/or IP addresses instead of hostnames or hostnames which don't match the certificate. This essentially means you cannot make verifiably trusted HTTP calls to servers which use self-signed certificates or use IP addresses from Dart or Flutter (directly).

What do you get with the existing HTTP libraries and "dart core" with regards to SSL/TLS certificates? The one thing you get is this thing called "badCertificateCallback"... This callback, already assuming your certificates are bad..., provides an X509 certificate in it's signature however it does not include the certificate chain or other intermediary certificate "bits" which would be necessary to truly establish trust for the certificate nor is there an idiomatic way or library which can perform the cryptography needed to verify a certificate. The internet has thusforth been littered with "tutorials" and code forum posts which instruct you to just bypass the verification of trust and just... well... trust whatever the heck the server gives you as long as the certificate matches the apps own copy of the same certificate... that is _not_ how you verify a certificate and that is an unacceptable solution.

```dart
// This is unnacceptable... and literally advocated for everywhere...
var ioClient = new HttpClient()
    ..badCertificateCallback = (X509Certificate cert, String host, int port) => true; // <-- WTF?

// This is also unacceptable... you don't verify a cert by just checking it against your own copy of the same cert...
var SERVER_PEM = "some_x509_certificate";
(_dio.httpClientAdapter as DefaultHttpClientAdapter).onHttpClientCreate  = (client) {
  client.badCertificateCallback=(X509Certificate cert, String host, int port){
    if(cert.pem==SERVER_PEM){ // <-- Verify the certificate? WTF is this?
      return true;
    }
    return true;
  };
};
```

## What does this library do?
This library implements, albiet only partially, the dart http.Client interface. This library allows you to make secure HTTP calls to servers which use self-signed certificates that may use IP addresses or different hostnames than what the certificate has listed. As a consumer of the library you can indicate which certificate authorities (CAs) you'd like to trust and from which hostname or IP addresses you expect to use certificates signed by those CA's. The turst is in addition to the standard trust from globally accepted CA's and does not "clear" or "remove" the trust used to make HTTP calls to "typical" servers on "the internets" for example https://google.com or any other site using "globally trusted" certificates.

## How does the library do what it does?
This library uses flutter method channels (a form of inter process communication or IPC) to pass HTTP requests made to the FlutterOkHttp implementation of http.Client to native Android. Native Android is backed by the ecosystem of Java and the robust set of network related features and functionality present. HTTP calls sent to Android are then sent to the HTTP server destination using the Android/Java OkHttp library.


## Example
* To run the example for this plugin please run the example server included in the "example-server" directory prior to running the example flutter application within "example" directory.
* The example server uses port 4443 so make sure nothing else is using that port prior to running the example server. 
* The example server uses self signed certificates and the example demonstrates how you can actually verify the and trust this certificate.
* The example Flutter application uses the OkRetryHttpClient as both a demonstration of using self-signed certificates and supporting retries.

_Note: If you have just checked out this project and have not ran `flutter pub get` then you can execute the example server by running `cd example-server/ && dart main.dart`. If you have already ran `flutter pub get` however (or some magic "auto-help-you" process has occurred) special "verison pining" files are created which somehow prevent the dart server from running, when it exists within a child directory of a flutter project, and will produce the error "Error: The specified language version is too high. The highest supported language version is X.Y". The example server obviously runs just fine as is evident by it working prior to the flutter magic commands. You may not get this message because of the "constant shifting foundation" and "it works for me!... but not for me!" issue that Dart and Flutter has or maybe you'll get this same message but for other reasons._ 


```
# Replace $PROJECT_ROOT with the full path to the directory of this project and $SOME_OTHER_DIRECTORY with a path which does _not_ exist as a child directory within a flutter project directory. 
cp $PROJECT_ROOT/example-server $SOME_OTHER_DIRECTORY
cd $SOME_OTHER_DIRECTORY
dart main.dart

# Now run the example application and make a _real_ HTTP call over TLS with self signed certificates.
```
