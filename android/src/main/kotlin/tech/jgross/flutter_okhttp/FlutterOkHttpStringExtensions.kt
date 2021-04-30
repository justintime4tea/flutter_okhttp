package tech.jgross.flutter_okhttp

const val methodHttpGet = "GET"
const val methodHttpPost = "POST"
const val methodHttpPut = "PUT"
const val methodHttpDelete = "DEL"
const val methodHttpPatch = "PATCH"
const val methodGetPlatformVersion = "getPlatformVersion"
const val methodAddTrustedCert = "ADD_TRUSTED_CERT"
const val methodRemoveTrustedCert = "REMOVE_TRUSTED_CERT"
const val methodAddTrustedHost = "ADD_TRUSTED_HOST"
const val methodRemoveTrustedHost = "REMOVE_TRUSTED_HOST"
const val methodOnHttpError = "ON_HTTP_ERROR"
const val methodOnHttpDnsResolutionFailure = "ON_HTTP_DNS_RESOLUTION_FAILURE"

val methodsWithBody = arrayOf(methodHttpPost, methodHttpPut, methodHttpPatch)
val httpMethods = arrayOf(methodHttpGet, methodHttpPost, methodHttpPut, methodHttpDelete, methodHttpPatch)

fun Map<String, Any>.isHttpArgs(): Boolean {
    return this.contains("url") && this.contains("headers")
}

fun Map<String, Any>.hasFilenameArg(): Boolean {
    return this.contains("filename") && this["filename"] is String
}

fun Map<String, Any>.hasHostArg(): Boolean {
    return this.contains("host") && this["host"] is String
}

fun Map<String, Any>.isHttpArgsWithBody(): Boolean {
    return this.isHttpArgs() && this.contains("body")
}

fun String.isHttpMethod(): Boolean {
    return httpMethods.contains(this)
}

fun String.isMethodRequiringBody(): Boolean {
    return methodsWithBody.contains(this)
}