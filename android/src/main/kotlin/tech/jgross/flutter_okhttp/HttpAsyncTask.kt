package tech.jgross.flutter_okhttp

import android.os.AsyncTask
import okhttp3.*
import okhttp3.FormBody
import org.json.JSONObject
import java.io.IOException
import java.io.PrintWriter
import java.io.StringWriter
import java.net.SocketTimeoutException
import java.util.*
import java.util.concurrent.TimeoutException
import java.util.function.Consumer
import kotlin.collections.HashMap


class AsyncHttpResponse<T>(val data: T?, val error: Exception?)
class AsyncHttpRequest(val method: String, val url: String, val httpClient: OkHttpClient, val headers: HashMap<*, *>?, val body: String?)

typealias AsyncHttpRequestCallback = (AsyncHttpResponse<Any>) -> Unit

const val FORM_POST = "application/x-www-form-urlencoded"

class HttpAsyncTask constructor(private val request: AsyncHttpRequest, private val responseCallback: AsyncHttpRequestCallback) : AsyncTask<Void?, Void?, AsyncHttpResponse<Any>>() {
    override fun doInBackground(vararg params: Void?): AsyncHttpResponse<Any>? {
        return try {
            AsyncHttpResponse(makeHttpRequest(request.method, request.url, request.httpClient, request.headers, request.body), null)
        } catch (e: Exception) {
            AsyncHttpResponse(null, e)
        }
    }

    override fun onPostExecute(response: AsyncHttpResponse<Any>) {
        super.onPostExecute(response)
        this.responseCallback(response)
    }

    private fun makeHttpRequest(method: String, url: String, httpClient: OkHttpClient, headers: HashMap<*, *>?, body: String?): HashMap<*, *> {
        val requestBuilder = Request.Builder().url(url)
        var contentType: String? = ""

        if (headers != null) {
            for ((key, value) in headers) {
                if (key is String && key.toLowerCase(Locale.getDefault()) == "content-type") {
                    contentType = value as String?
                }
                if (value != null) {
                    requestBuilder.addHeader((key as String?)!!, (value as String?)!!)
                }
            }
        }

        if (method.isMethodRequiringBody()) {
            if (body == null) {
                throw IllegalArgumentException("Body of request cannot be null!")
            }

            if (contentType != "" && contentType is String && contentType.toLowerCase(Locale.getDefault()) == FORM_POST) {
                val bodyAsJson = JSONObject(body)
                val formBodyBuilder: FormBody.Builder = FormBody.Builder()

                for (key in bodyAsJson.keys()) {
                    val value = bodyAsJson.get(key)

                    if (value is String) {
                        formBodyBuilder.add(key, value)
                    }
                }

                requestBuilder.post(formBodyBuilder.build())
            } else {
                if (method == methodHttpPost) {
                    requestBuilder.postJson(body)
                }

                if (method == methodHttpPut) {
                    requestBuilder.putJson(body)
                }

                if (method == methodHttpPatch) {
                    requestBuilder.patchJson(body)
                }
            }
        }

        try {
            val request = requestBuilder.build()
            httpClient.newCall(request).execute().use { response: Response ->
                val responseHeaders: HashMap<Any?, Any?> = HashMap()
                val rawHeaders: Headers = response.headers
                rawHeaders.forEach(
                        Consumer<Pair<String?, String?>?> { pair ->
                            if (pair != null) {
                                responseHeaders[pair.component1()] = pair.component2()
                            }
                        })

                val resultPayload: HashMap<Any?, Any?> = HashMap()
                resultPayload["code"] = response.code
                resultPayload["message"] = response.message
                resultPayload["body"] = response.body?.string()
                resultPayload["headers"] = responseHeaders

                response.body?.close()
                return resultPayload
            }
        } catch (e: SocketTimeoutException) {
            val resultPayload: HashMap<Any?, Any?> = HashMap()
            resultPayload["code"] = 504
            resultPayload["message"] = "Client Socket Timeout"
            resultPayload["body"] = ""
            val responseHeaders = HashMap<Any?, Any?>()

            val stringWriter = StringWriter()
            e.printStackTrace(PrintWriter(stringWriter))
            val exceptionString = stringWriter.toString()

            responseHeaders["e-stacktrace"] = exceptionString
            resultPayload["headers"] = responseHeaders
            return resultPayload
        } catch (e: TimeoutException) {
            val resultPayload: HashMap<Any?, Any?> = HashMap()
            resultPayload["code"] = 504
            resultPayload["message"] = "Client Async Timeout"
            resultPayload["body"] = ""

            val responseHeaders = HashMap<Any?, Any?>()
            val stringWriter = StringWriter()
            e.printStackTrace(PrintWriter(stringWriter))
            val exceptionString = stringWriter.toString()

            responseHeaders["e-stacktrace"] = exceptionString
            resultPayload["headers"] = responseHeaders
            return resultPayload
        }
    }

}
