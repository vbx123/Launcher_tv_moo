package com.nasim.launcher.mobile

import com.nasim.launcher.common.Protocol
import com.nasim.launcher.common.ThemeConfig
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.InetSocketAddress
import java.net.Socket

/** عميل TCP: يفتح اتصال، يبعت سطر JSON، ويقرا الرد. لازم يتنادى من Thread خلفي. */
object RemoteClient {
    private fun request(host: String, payload: JSONObject): JSONObject {
        Socket().use { s ->
            s.connect(InetSocketAddress(host, Protocol.PORT), 3000)
            s.soTimeout = 5000
            val out = s.getOutputStream()
            out.write((payload.toString() + "\n").toByteArray(Charsets.UTF_8))
            out.flush()
            val line = BufferedReader(InputStreamReader(s.getInputStream(), Charsets.UTF_8)).readLine()
                ?: throw IOException("no reply")
            return JSONObject(line)
        }
    }

    fun hello(host: String): JSONObject = request(host, JSONObject().put("type", "hello"))

    fun apply(host: String, pin: String, cfg: ThemeConfig): JSONObject = request(
        host, JSONObject().put("type", "apply").put("pin", pin).put("config", cfg.toJson())
    )
}
