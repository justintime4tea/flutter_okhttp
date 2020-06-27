package tech.jgross.flutter_okhttp

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.annotation.NonNull
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry.ActivityResultListener
import io.flutter.plugin.common.PluginRegistry.Registrar
import okhttp3.*
import java.io.IOException
import java.security.KeyManagementException
import java.security.KeyStoreException
import java.security.NoSuchAlgorithmException
import java.security.cert.CertificateException
import java.util.*
import kotlin.collections.HashMap

/** FlutterOkHttpPlugin */
class FlutterOkHttpPlugin : FlutterPlugin, MethodCallHandler, ActivityResultListener, ActivityAware {
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private lateinit var channel: MethodChannel
    private var applicationContext: Context? = null
    private var mainActivity: Activity? = null
    private var pendingOperation: PendingOperation? = null

    private var certAssetsPath: ArrayList<String> = arrayListOf("ca-override.pem")
    private var hostsAllowedToUseCertSignedByCaOverride: ArrayList<String> = arrayListOf("10.0.2.2", "192.168.0.149")
    private var okHttpClient: OkHttpClient? = null

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        onAttachedToEngine(flutterPluginBinding.applicationContext, flutterPluginBinding.binaryMessenger);
    }

    // This static function is optional and equivalent to onAttachedToEngine. It supports the old
    // pre-Flutter-1.12 Android projects. You are encouraged to continue supporting
    // plugin registration via this function while apps migrate to use the new Android APIs
    // post-flutter-1.12 via https://flutter.dev/go/android-project-migration.
    //
    // It is encouraged to share logic between onAttachedToEngine and registerWith to keep
    // them functionally equivalent. Only one of onAttachedToEngine or registerWith will be called
    // depending on the user's project. onAttachedToEngine or registerWith must both be defined
    // in the same class.
    companion object {
        @JvmStatic
        fun registerWith(registrar: Registrar) {
            val plugin = FlutterOkHttpPlugin()
            plugin.setActivity(registrar.activity());
            plugin.onAttachedToEngine(registrar.context(), registrar.messenger());
            registrar.addActivityResultListener(plugin);
        }
    }

    private fun setActivity(flutterActivity: Activity) {
        mainActivity = flutterActivity
    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull rawResult: Result) {
        val result: Result = MethodResultWrapper(rawResult)
        val arguments = call.arguments<Map<String, Any>>()

        if (arguments != null && arguments.isHttpArgs()) {
            return try {
                performHttpRequest(call.method, arguments, rawResult)
            } catch (ex: Exception) {
                finishWithError(methodOnHttpError, ex.localizedMessage)
            }
        }

        return when (call.method) {
            methodGetPlatformVersion -> rawResult.success("Android ${android.os.Build.VERSION.RELEASE}")
            else -> result.notImplemented()
        }
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        return false;
    }

    override fun onDetachedFromActivity() {
        this.mainActivity = null;
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        binding.addActivityResultListener(this)
        mainActivity = binding.activity
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        binding.addActivityResultListener(this)
        mainActivity = binding.activity
    }

    override fun onDetachedFromActivityForConfigChanges() {
        mainActivity = null
    }

    private fun onAttachedToEngine(context: Context, binaryMessenger: BinaryMessenger) {
        applicationContext = context
        val channel = MethodChannel(binaryMessenger, "tech.jgross.flutter_okhttp")
        channel.setMethodCallHandler(this)
    }

    private fun checkAndSetPendingOperation(method: String, result: Result) {
        check(pendingOperation == null) { "Concurrent operations detected: " + pendingOperation?.method + ", " + method }
        pendingOperation = PendingOperation(method, result)
    }

    private fun finishWithSuccess(data: Any) {
        if (pendingOperation != null) {
            pendingOperation!!.result.success(data)
            pendingOperation = null
        }
    }

    private fun finishWithError(errorCode: String, errorMessage: String?) {
        if (pendingOperation != null) {
            pendingOperation!!.result.error(errorCode, errorMessage, null)
            pendingOperation = null
        }
    }

    private fun onResponseReceived(response: AsyncHttpResponse<Any>?) {
        if (response != null && response.error == null) {
            finishWithSuccess(response.data as HashMap<*, *>)
        } else {
            finishWithError(methodOnHttpError, response?.error?.message)
        }
    }


    private fun performHttpRequest(method: String, arguments: Map<String, Any>, result: Result) {
        if (!method.isHttpMethod()) {
            return
        }

        val url = arguments["url"] as? String?
        val headers: HashMap<*, *>? = arguments["headers"] as? HashMap<*, *>?
        val body: String? = arguments["body"] as? String

        if (url == null) {
            return
        }

        if (okHttpClient == null) {
            okHttpClient = createOkHttpClient()
        }

        if (okHttpClient == null) {
            throw IOException("Could not create OkHttpClient!")
        }

        checkAndSetPendingOperation(method, result)

        val httpClient = okHttpClient!!
        val request = AsyncHttpRequest(method, url, httpClient, headers, body)
        HttpAsyncTask(request, this::onResponseReceived).execute()
    }


    @Throws(NoSuchAlgorithmException::class, KeyStoreException::class, CertificateException::class, IOException::class, KeyManagementException::class)
    private fun createOkHttpClient(): OkHttpClient? {
        if (mainActivity != null) {
            return OkHttpClient.Builder()
                    .addTrustedCerts(certAssetsPath, mainActivity!!.assets)
                    .addTrustedHostnames(hostsAllowedToUseCertSignedByCaOverride)
                    .build()
        }

        return null
    }

    private class PendingOperation internal constructor(val method: String, val result: Result)
}
