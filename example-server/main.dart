import 'dart:io';

String certificateChain = 'example.crt';
String serverKey = 'example.key';

Future main() async {
  var serverContext = SecurityContext();
  serverContext.useCertificateChain(certificateChain);
  serverContext.usePrivateKey(serverKey);

  var server = await HttpServer.bindSecure(
    '0.0.0.0',
    4443,
    serverContext,
  );
  print('Listening on localhost:${server.port}');
  await for (HttpRequest request in server) {
    request.response.headers.contentType = new ContentType("application", "json", charset: "utf-8");
    request.response.write("{\"message\": \"Hello world!\"}");
    await request.response.close();
  }
}
