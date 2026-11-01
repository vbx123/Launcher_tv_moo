package com.nasim.launcher.tv

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.os.Build
import android.util.Log
import com.nasim.launcher.common.Protocol
import com.nasim.launcher.common.ThemeConfig
import com.nasim.launcher.common.ThemeStore
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.net.ServerSocket
import java.net.Socket
import java.security.MessageDigest

/**
 * سيرفر TCP صغير جداً على الشاشة (منفذ 8765).
 * الموبايل يبعت سطر JSON واحد، والشاشة ترد بسطر JSON واحد.
 *   {"type":"hello"}                                  -> يرجع الاسم والإعدادات الحالية
 *   {"type":"apply","pin":"123456","config":{...}}    -> يحفظ الإعدادات (بعد التحقق من الـ PIN)
 * الحفظ في SharedPreferences بيشغّل Listener في الـ LauncherActivity فيتطبق الثيم فوراً.
 */
class RemoteServer(private val ctx: Context, private val store: ThemeStore) {
    @Volatile private var running = false
    private var nsd: NsdManager? = null
    private var regListener: NsdManager.RegistrationListener? = null

    @Synchronized
    fun start() {
        if (running) return
        running = true
        Thread({ acceptLoop() }, "remote-server").apply { isDaemon = true }.start()
    }

    private fun acceptLoop() {
        var ss: ServerSocket? = null
        try {
            ss = ServerSocket(Protocol.PORT)
            registerNsd(ss.localPort)
            while (!ss.isClosed) {
                handle(ss.accept()) // طلبات صغيرة، فالمعالجة المتتالية كفاية وبتخفف الحمل على الجهاز الضعيف
            }
        } catch (e: Exception) {
            Log.w(TAG, "server stopped: $e")
        } finally {
            try { ss?.close() } catch (_: Exception) {}
            unregisterNsd()
            running = false
        }
    }

    private fun handle(s: Socket) {
        try {
            s.soTimeout = 4000
            s.use { sock ->
                val line = readLine(sock.getInputStream()) ?: return
                val req = JSONObject(line)
                val res = when (req.optString("type")) {
                    "hello" -> JSONObject().put("ok", true).put("name", Build.MODEL)
                        .put("config", store.load().toJson())
                    "apply" -> {
                        val ok = MessageDigest.isEqual(
                            req.optString("pin").toByteArray(), store.pin().toByteArray()
                        )
                        if (!ok) {
                            Thread.sleep(700) // تبطيء محاولات تخمين الـ PIN
                            JSONObject().put("ok", false).put("error", "bad_pin")
                        } else {
                            store.save(ThemeConfig.fromJson(req.getJSONObject("config")))
                            JSONObject().put("ok", true)
                        }
                    }
                    else -> JSONObject().put("ok", false).put("error", "unknown")
                }
                sock.getOutputStream().apply {
                    write((res.toString() + "\n").toByteArray())
                    flush()
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "client error: $e")
        }
    }

    private fun readLine(input: InputStream): String? {
        val buf = ByteArrayOutputStream()
        while (buf.size() < Protocol.MAX_LINE) {
            val b = input.read()
            if (b == -1 || b == '\n'.code) break
            buf.write(b)
        }
        return if (buf.size() == 0) null else buf.toString("UTF-8")
    }

    // ---- الإعلان عن الشاشة على الشبكة (عشان الموبايل يلاقيها تلقائياً) ----
    private fun registerNsd(port: Int) {
        try {
            val info = NsdServiceInfo().apply {
                serviceName = "NasimTV"
                serviceType = Protocol.SERVICE_TYPE
                setPort(port)
            }
            val m = ctx.getSystemService(Context.NSD_SERVICE) as NsdManager
            val l = object : NsdManager.RegistrationListener {
                override fun onServiceRegistered(i: NsdServiceInfo) {}
                override fun onRegistrationFailed(i: NsdServiceInfo, e: Int) { Log.w(TAG, "nsd reg failed $e") }
                override fun onServiceUnregistered(i: NsdServiceInfo) {}
                override fun onUnregistrationFailed(i: NsdServiceInfo, e: Int) {}
            }
            m.registerService(info, NsdManager.PROTOCOL_DNS_SD, l)
            nsd = m
            regListener = l
        } catch (e: Exception) {
            Log.w(TAG, "nsd error: $e")
        }
    }

    private fun unregisterNsd() {
        try { regListener?.let { nsd?.unregisterService(it) } } catch (_: Exception) {}
        regListener = null
    }

    companion object { private const val TAG = "RemoteServer" }
}
