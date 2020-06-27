package tech.jgross.flutter_okhttp

import android.content.res.AssetManager
import okhttp3.OkHttpClient
import java.io.InputStream
import java.security.KeyStore
import java.security.cert.Certificate
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.HashMap
import javax.net.ssl.*

fun OkHttpClient.Builder.addTrustedCerts(caOverrideCertPaths: ArrayList<String>, assets: AssetManager): OkHttpClient.Builder {
    // Load CAs from an InputStream
    // (could be from a resource or ByteArrayInputStream or ...)
    val cf: CertificateFactory = CertificateFactory.getInstance("X.509")
    val certificates: HashMap<String, Certificate> = HashMap()

    for (certPath in caOverrideCertPaths) {
        val cert: Certificate
        val caInput: InputStream = assets.open(certPath)
        try {
            cert = cf.generateCertificate(caInput)
            println("Trusting ca=" + (cert as X509Certificate).subjectDN)
        } finally {
            caInput.close()
        }

        certificates[certPath] = cert;
    }

    if (certificates.isEmpty()) {
        return this
    }

    // Create a KeyStore containing our trusted CAs
    val keyStoreType: String = KeyStore.getDefaultType()
    val keyStore: KeyStore = KeyStore.getInstance(keyStoreType)
    keyStore.load(null, null)

    for ((key, value) in certificates) {
        keyStore.setCertificateEntry(key, value)
    }

    // Create a TrustManager that trusts the CAs in our KeyStore
    val tmfAlgorithm: String = TrustManagerFactory.getDefaultAlgorithm()
    val tmf: TrustManagerFactory = TrustManagerFactory.getInstance(tmfAlgorithm)
    tmf.init(keyStore)

    // Create an SSLContext that uses our TrustManager
    val context: SSLContext = SSLContext.getInstance("TLS")
    context.init(null, tmf.trustManagers, null)
    val trustManager: X509TrustManager = tmf.trustManagers[0] as X509TrustManager
    this.sslSocketFactory(context.socketFactory, trustManager)
    return this
}

fun OkHttpClient.Builder.addTrustedHostnames(hostnames: ArrayList<String>): OkHttpClient.Builder {
    this.hostnameVerifier(WhiteListHostnameVerifier(hostnames))
    return this
}
