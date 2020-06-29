package tech.jgross.flutter_okhttp

import android.app.Activity
import android.os.AsyncTask
import okhttp3.Headers
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.util.function.Consumer

class AsyncHttpResponse<T>(val data: T?, val error: Exception?)
class AsyncHttpRequest(val method: String, val url: String, val httpClient: OkHttpClient, val headers: HashMap<*, *>?, val body: String?)

typealias AsyncHttpRequestCallback = (AsyncHttpResponse<Any>) -> Unit


class HttpAsyncTask constructor(private val request: AsyncHttpRequest, private val responseCallback: AsyncHttpRequestCallback) : AsyncTask<Void?, Void?, AsyncHttpResponse<Any>>() {
    override fun doInBackground(vararg params: Void?): AsyncHttpResponse<Any>? {
        return try {
            AsyncHttpResponse(makeHttpRequest(request.method, request.url, request.httpClient, request.headers, request.body), null)
        } catch (e: IOException) {
            AsyncHttpResponse(null, e)
        }
    }

    override fun onPostExecute(response: AsyncHttpResponse<Any>) {
        super.onPostExecute(response)
        this.responseCallback(response)
    }

    private fun makeHttpRequest(method: String, url: String, httpClient: OkHttpClient, headers: HashMap<*, *>?, body: String?): HashMap<*, *> {
        val requestBuilder = Request.Builder().url(url)

        if (headers != null) {
            for ((key, value) in headers) {
                if (value != null) {
                    requestBuilder.addHeader((key as String?)!!, (value as String?)!!)
                }
            }
        }

        if (method.isMethodRequiringBody()) {
            if (body == null) {
                throw IllegalArgumentException("Body of request cannot be null!")
            }

            if (method == methodHttpPost) {
                requestBuilder.postJson(body);
            }

            if (method == methodHttpPut) {
                requestBuilder.putJson(body);
            }

            if (method == methodHttpPatch) {
                requestBuilder.patchJson(body);
            }
        }

        val request = requestBuilder.build()
        httpClient.newCall(request).execute().use { response: Response ->
            val resultPayload: HashMap<Any?, Any?> = HashMap()
            val responseHeaders: HashMap<Any?, Any?> = HashMap()

            val rawHeaders: Headers = response.headers
            rawHeaders.forEach(
                    Consumer<Pair<String?, String?>?> { pair ->
                        if (pair != null) {
                            responseHeaders[pair.component1()] = pair.component2()
                        }
                    })

            resultPayload["code"] = response.code
            resultPayload["message"] = response.message
            resultPayload["body"] = response.body?.string()
            resultPayload["headers"] = responseHeaders

            response.body?.close()
            return resultPayload
        }
    }

}
