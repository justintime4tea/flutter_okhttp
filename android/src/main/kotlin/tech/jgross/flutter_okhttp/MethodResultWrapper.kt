package tech.jgross.flutter_okhttp

import android.os.Handler
import android.os.Looper
import io.flutter.plugin.common.MethodChannel.Result


class MethodResultWrapper(result: Result) : Result {
    private var methodResult: Result? = result
    private var handler: Handler? = null

    init {
        methodResult = result
        handler = Handler(Looper.getMainLooper())
    }

    override fun notImplemented() {
        handler!!.post { methodResult!!.notImplemented() }
    }

    override fun error(errorCode: String?, errorMessage: String?, errorDetails: Any?) {
        handler!!.post { methodResult!!.error(errorCode, errorMessage, errorDetails) }
    }

    override fun success(result: Any?) {
        handler!!.post { methodResult!!.success(result) }
    }

}