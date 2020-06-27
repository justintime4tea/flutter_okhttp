package tech.jgross.flutter_okhttp

import org.apache.http.conn.ssl.StrictHostnameVerifier
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLSession

class WhiteListHostnameVerifier(private val hostnames: ArrayList<String>) : HostnameVerifier {
    override fun verify(hostname: String?, session: SSLSession?): Boolean {
        return hostnames.contains(hostname) || StrictHostnameVerifier().verify(hostname, session)
    }
}