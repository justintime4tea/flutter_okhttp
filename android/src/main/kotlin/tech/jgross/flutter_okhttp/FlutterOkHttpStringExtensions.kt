package tech.jgross.flutter_okhttp

const val methodHttpGet = "GET"
const val methodHttpPost = "POST"
const val methodHttpPut = "PUT"
const val methodHttpDelete = "DEL"
const val methodHttpPatch = "PATCH"
const val methodGetPlatformVersion = "getPlatformVersion"
const val methodOnHttpError = "ON_HTTP_ERROR"
val methodsWithBody = arrayOf(methodHttpPost, methodHttpPut, methodHttpPatch)
val httpMethods = arrayOf(methodHttpGet, methodHttpPost, methodHttpPut, methodHttpDelete, methodHttpPatch)

fun Map<String, Any>.isHttpArgs(): Boolean {
    return this.contains("url") && this.contains("headers")
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