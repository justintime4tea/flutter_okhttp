# flutter_okhttp

A flutter plugin for making HTTP calls backed by OkHttp under native Android from within Dart/Flutter.

The cause for this projects existence is ultimately so that I can make HTTP calls from Dart/Flutter that trust certificates signed by a given custom certificate authority and to do so regardless if the hostname matches the certificate. There's a better way to describe this project and it needs to be actually documented. That will have to come later.

To manual "dummy" test this plugin please run the example server included in "example-server" before running the example flutter application within "example" directory.
