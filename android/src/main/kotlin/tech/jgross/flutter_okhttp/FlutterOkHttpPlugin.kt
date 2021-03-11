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
import org.apache.http.conn.ssl.StrictHostnameVerifier
import java.io.IOException
import java.io.InputStream
import java.nio.channels.AsynchronousFileChannel.open
import java.security.*
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.*
import javax.net.ssl.*
import javax.security.cert.Certificate
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
    private var pendingOperations: HashMap<String, PendingOperation> = HashMap()

    private var certFilenames: ArrayList<String> = ArrayList<String>()
    private var hostsAllowedToUseCertSignedByCaOverride: ArrayList<String> = arrayListOf(
            "10.0.2.2"
    )

    private var okHttpClient: OkHttpClient? = null

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        onAttachedToEngine(flutterPluginBinding.applicationContext, flutterPluginBinding.binaryMessenger)
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
            val activity = registrar.activity();
            if (activity != null) {
                val plugin = FlutterOkHttpPlugin()
                plugin.setActivity(registrar.activity())
                plugin.updateCertTrust()
                plugin.onAttachedToEngine(registrar.context(), registrar.messenger())
                registrar.addActivityResultListener(plugin)
            }
        }
    }

    private fun setActivity(flutterActivity: Activity) {
        mainActivity = flutterActivity
    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull rawResult: Result) {
        val result: Result = MethodResultWrapper(rawResult)
        val arguments = call.arguments<Map<String, Any>>()

        val requestId = arguments["requestId"] as? String?

        if (arguments != null && arguments.isHttpArgs()) {
            return try {
                performHttpRequest(call.method, arguments, rawResult)
            } catch (ex: Exception) {
                finishWithError(methodOnHttpError, ex.localizedMessage, requestId)
            }
        }

        return when (call.method) {
            methodGetPlatformVersion -> rawResult.success("Android ${android.os.Build.VERSION.RELEASE}")
            methodAddTrustedCert -> {
                if (arguments.hasFilenameArg()) {
                    addTrustedCert(arguments["filename"] as String)
                    return result.success(null)
                }
                return result.error(null, "Filename must be provided", null)
            }
            methodRemoveTrustedCert -> {
                if (arguments.hasFilenameArg()) {
                    removeTrustedCert(arguments["filename"] as String)
                    return result.success(null)
                }
                return result.error(null, "Filename must be provided.", null)
            }
            methodAddTrustedHost -> {
                if (arguments.hasHostArg()) {
                    addTrustedHost(arguments["host"] as String)
                    return result.success(null)
                }
                return result.error(null, "Host must be provided.", null)
            }
            methodRemoveTrustedHost -> {
                if (arguments.hasHostArg()) {
                    removeTrustedHost(arguments["host"] as String)
                    return result.success(null)
                }
                return result.error(null, "Host must be provided.", null)
            }
            else -> result.notImplemented()
        }
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        return false
    }

    override fun onDetachedFromActivity() {
        this.mainActivity = null
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
        channel = MethodChannel(binaryMessenger, "tech.jgross.flutter_okhttp")
        channel.setMethodCallHandler(this)
    }

    private fun checkAndSetPendingOperation(method: String, result: Result, requestId: String?) {
        if (requestId is String) {
            val pendingOperation: PendingOperation? = pendingOperations[requestId]
            check(pendingOperation == null) { "Concurrent operations detected: " + pendingOperation?.method + ", " + method + ", " + requestId }
            pendingOperations[requestId] = PendingOperation(method, result)
        } else {
            check(pendingOperation == null) { "Concurrent operations detected: " + pendingOperation?.method + ", " + method }
            pendingOperation = PendingOperation(method, result)
        }
    }

    private fun finishWithSuccess(data: Any, requestId: String?) {
        if (requestId is String) {
            val pendingOperation: PendingOperation? = pendingOperations[requestId]
            if (pendingOperation is PendingOperation) {
                pendingOperation.result.success(data)
                pendingOperations.remove(requestId)
            }
        }

        if (pendingOperation != null) {
            pendingOperation!!.result.success(data)
            pendingOperation = null
        }
    }

    private fun finishWithError(errorCode: String, errorMessage: String?, requestId: String?) {
        if (requestId is String) {
            val pendingOperation: PendingOperation? = pendingOperations[requestId]

            if (pendingOperation is PendingOperation) {
                pendingOperation.result.error(errorCode, errorMessage, null)
                pendingOperations.remove(requestId)
            }
        }

        if (pendingOperation != null) {
            pendingOperation!!.result.error(errorCode, errorMessage, null)
            pendingOperation = null
        }
    }

    private fun onResponseReceived(response: AsyncHttpResponse<Any>?, requestId: String?) {
        if (response != null && response.error == null) {
            finishWithSuccess(response.data as HashMap<*, *>, requestId)
        } else {
            finishWithError(methodOnHttpError, response?.error?.message, requestId)
        }
    }

    private fun addTrustedCert(filename: String) {
        certFilenames.add(filename)
        updateCertTrust()
        okHttpClient = null
    }

    private fun removeTrustedCert(filename: String) {
        certFilenames.remove(filename)
        updateCertTrust()
        okHttpClient = null
    }

    private fun addTrustedHost(host: String) {
        hostsAllowedToUseCertSignedByCaOverride.add(host)
        updateCertTrust()
        okHttpClient = null
    }

    private fun removeTrustedHost(host: String) {
        hostsAllowedToUseCertSignedByCaOverride.remove(host)
        updateCertTrust()
        okHttpClient = null
    }

    private fun updateCertTrust() {
        if (this.mainActivity != null) {
            // Load CAs from an InputStream
            // (could be from a resource or ByteArrayInputStream or ...)
            val cf: CertificateFactory = CertificateFactory.getInstance("X.509")
            val cert: java.security.cert.Certificate

            if (certFilenames.size < 1) {
                certFilenames.add("ca-override.pem")
            }

            val fileName: String = certFilenames[0]
            val caInput: InputStream = this.mainActivity!!.assets.open(fileName)

            try {
                cert = cf.generateCertificate(caInput)
                println("Trusting ca=" + (cert as X509Certificate).subjectDN)
            } finally {
                caInput.close()
            }

            // Create a KeyStore containing our trusted CAs
            val keyStoreType: String = KeyStore.getDefaultType()
            val keyStore: KeyStore = KeyStore.getInstance(keyStoreType)
            keyStore.load(null, null)

            keyStore.setCertificateEntry("ca", cert)

            // Create a TrustManager that trusts the CAs in our KeyStore
            val tmfAlgorithm: String = TrustManagerFactory.getDefaultAlgorithm()
            val tmf: TrustManagerFactory = TrustManagerFactory.getInstance(tmfAlgorithm)
            tmf.init(keyStore)

            // Create an SSLContext that uses our TrustManager
            val context: SSLContext = SSLContext.getInstance("TLS")
            context.init(null, tmf.trustManagers, SecureRandom())
            HttpsURLConnection.setDefaultSSLSocketFactory(context.socketFactory)
            HttpsURLConnection.setDefaultHostnameVerifier(WhiteListHostnameVerifier(hostsAllowedToUseCertSignedByCaOverride))
        }
    }

    class WhiteListHostnameVerifier(private val hostnames: ArrayList<String>) : HostnameVerifier {
        override fun verify(hostname: String?, session: SSLSession?): Boolean {
            return hostnames.contains(hostname) || StrictHostnameVerifier().verify(hostname, session)
        }
    }

    private fun performHttpRequest(method: String, arguments: Map<String, Any>, result: Result) {
        if (!method.isHttpMethod()) {
            return
        }

        val requestId = arguments["requestId"] as? String?
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

        checkAndSetPendingOperation(method, result, requestId)

        val httpClient = okHttpClient!!
        val request = AsyncHttpRequest(method, url, httpClient, headers, body)
        val onResponse = { response: AsyncHttpResponse<Any>? -> this.onResponseReceived(response, requestId)}
        HttpAsyncTask(request, onResponse).execute()
    }


    @Throws(NoSuchAlgorithmException::class, KeyStoreException::class, CertificateException::class, IOException::class, KeyManagementException::class)
    private fun createOkHttpClient(): OkHttpClient? {
        if (mainActivity != null) {
            return OkHttpClient.Builder()
                    .addTrustedCerts(certFilenames, mainActivity!!.assets)
                    .addTrustedHostnames(hostsAllowedToUseCertSignedByCaOverride)
                    .build()
        }

        return null
    }

    private class PendingOperation internal constructor(val method: String, val result: Result)
}
