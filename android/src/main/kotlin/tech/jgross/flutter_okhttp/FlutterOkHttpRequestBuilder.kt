package tech.jgross.flutter_okhttp

import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.HashMap

fun Request.Builder.addHeaders(headers: HashMap<String, String>): Request.Builder {
    for ((key, value) in headers) {
        this.addHeader((key as String?)!!, (value as String?)!!)
    }
    return this
}

fun Request.Builder.postJson(body: String): Request.Builder {
   return postPutPatch(body)
}

fun Request.Builder.putJson(body: String): Request.Builder {
    return postPutPatch(body)
}

fun Request.Builder.patchJson(body: String): Request.Builder {
    return postPutPatch(body)
}

private fun Request.Builder.postPutPatch(body: String): Request.Builder {
    val mediaType: MediaType = "application/json".toMediaType();
    this.post(body.toRequestBody(mediaType))
    return this
}