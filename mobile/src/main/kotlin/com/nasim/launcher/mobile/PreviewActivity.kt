package com.nasim.launcher.mobile

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputFilter
import android.text.InputType
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Switch
import android.widget.TextView
import com.nasim.launcher.common.CityPresets
import com.nasim.launcher.common.OpenMeteo
import com.nasim.launcher.common.ThemeConfig
import com.nasim.launcher.common.ThemeStore
import java.util.concurrent.Executors

/**
 * شاشة الموبايل: تعدّل الإعدادات وتشوف المعاينة فوراً (TvPreviewView)،
 * وبعدين "تطبيق على الشاشة" يبعتها للتلفزيون عبر RemoteClient.
 */
class PreviewActivity : Activity() {

    private lateinit var store: ThemeStore
    private lateinit var preview: TvPreviewView
    private lateinit var rbJer: RadioButton
    private lateinit var rbCyber: RadioButton
    private lateinit var swTime: Switch
    private lateinit var swWeather: Switch
    private lateinit var statusText: TextView
    private lateinit var resultText: TextView
    private lateinit var etSearch: EditText

    private var cfg = ThemeConfig()
    private var suppress = false
    private val io = Executors.newSingleThreadExecutor()
    private val main = Handler(Looper.getMainLooper())

    private val host: String get() = store.prefs.getString("host", "") ?: ""
    private val pin: String get() = store.prefs.getString("pin", "") ?: ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preview)
        store = ThemeStore(this)
        cfg = store.load()

        preview = findViewById(R.id.preview)
        rbJer = findViewById(R.id.rbJer)
        rbCyber = findViewById(R.id.rbCyber)
        swTime = findViewById(R.id.swTime)
        swWeather = findViewById(R.id.swWeather)
        statusText = findViewById(R.id.statusText)
        resultText = findViewById(R.id.resultText)
        etSearch = findViewById(R.id.etSearch)

        // --- Theme switcher ---
        findViewById<RadioGroup>(R.id.themeGroup).setOnCheckedChangeListener { _, id ->
            if (suppress) return@setOnCheckedChangeListener
            val t = if (id == R.id.rbCyber) ThemeConfig.THEME_CYBER else ThemeConfig.THEME_JERUSALEM
            cfg = cfg.copy(themeId = t)
            changed()
        }
        swTime.setOnCheckedChangeListener { _, on ->
            if (!suppress) { cfg = cfg.copy(use24h = on); changed() }
        }
        swWeather.setOnCheckedChangeListener { _, on ->
            if (!suppress) { cfg = cfg.copy(showWeather = on); changed() }
        }

        // --- المدن الجاهزة ---
        val chips = findViewById<LinearLayout>(R.id.cityChips)
        for (city in CityPresets.list) {
            chips.addView(Button(this).apply {
                text = city.name
                setOnClickListener {
                    cfg = cfg.copy(cityName = city.name, lat = city.lat, lon = city.lon)
                    changed()
                }
            })
        }
        findViewById<Button>(R.id.btnSearch).setOnClickListener { searchCity() }
        findViewById<Button>(R.id.btnConnect).setOnClickListener { showConnect() }
        findViewById<Button>(R.id.btnApply).setOnClickListener { applyToTv() }

        syncUi()
        updateConn()
        if (host.isNotEmpty()) testConnection()
    }

    override fun onDestroy() {
        io.shutdownNow()
        super.onDestroy()
    }

    /** أي تغيير: يتحفظ محلياً + المعاينة تتحدث فوراً. */
    private fun changed() {
        store.save(cfg)
        preview.render(cfg)
        resultText.text = ""
    }

    private fun syncUi() {
        suppress = true
        rbJer.isChecked = cfg.themeId == ThemeConfig.THEME_JERUSALEM
        rbCyber.isChecked = cfg.themeId == ThemeConfig.THEME_CYBER
        swTime.isChecked = cfg.use24h
        swWeather.isChecked = cfg.showWeather
        suppress = false
        preview.render(cfg)
    }

    // ------------------------------------------------------------ بحث مدينة
    private fun searchCity() {
        val q = etSearch.text.toString().trim()
        if (q.isEmpty()) return
        resultText.text = "جاري البحث…"
        io.execute {
            val r = OpenMeteo.geocode(q)
            main.post {
                if (isDestroyed) return@post
                if (r.isEmpty()) {
                    resultText.text = "لم يتم العثور على نتائج (تأكد من الإنترنت)"
                    return@post
                }
                resultText.text = ""
                AlertDialog.Builder(this)
                    .setTitle("اختر المدينة")
                    .setItems(r.map { it.name }.toTypedArray()) { _, i ->
                        cfg = cfg.copy(cityName = r[i].name.substringBefore("،"), lat = r[i].lat, lon = r[i].lon)
                        changed()
                    }
                    .show()
            }
        }
    }

    // ------------------------------------------------------------ الاتصال والإرسال
    private fun updateConn() {
        statusText.text = if (host.isEmpty()) "غير متصل — اضغط «الاتصال بالشاشة»" else "الشاشة: $host"
    }

    private fun testConnection() {
        val h = host
        io.execute {
            val msg = try {
                val r = RemoteClient.hello(h)
                "متصل بالشاشة: ${r.optString("name")} ($h)"
            } catch (e: Exception) {
                "تعذر الوصول للشاشة ($h)"
            }
            main.post { if (!isDestroyed) statusText.text = msg }
        }
    }

    private fun applyToTv() {
        if (host.isEmpty() || pin.isEmpty()) {
            showConnect()
            return
        }
        val h = host
        val p = pin
        val c = cfg
        resultText.text = "جاري الإرسال…"
        io.execute {
            val msg = try {
                val r = RemoteClient.apply(h, p, c)
                when {
                    r.optBoolean("ok") -> "تم التطبيق على الشاشة ✓"
                    r.optString("error") == "bad_pin" -> "الـ PIN غير صحيح — راجعه من إعدادات الشاشة"
                    else -> "رفضت الشاشة الطلب"
                }
            } catch (e: Exception) {
                "تعذر الوصول للشاشة — تأكد إن الهاتف والشاشة على نفس الـ Wi-Fi"
            }
            main.post { if (!isDestroyed) resultText.text = msg }
        }
    }

    private fun showConnect() {
        val d = resources.displayMetrics.density
        val pad = (16 * d).toInt()
        val found = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val etHost = EditText(this).apply {
            hint = "IP الشاشة (مثال 192.168.1.20)"
            inputType = InputType.TYPE_CLASS_TEXT
            setText(host)
        }
        val etPin = EditText(this).apply {
            hint = "PIN (6 أرقام) — يظهر في إعدادات الشاشة"
            inputType = InputType.TYPE_CLASS_NUMBER
            filters = arrayOf(InputFilter.LengthFilter(6))
            setText(pin)
        }
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(pad, pad, pad, 0)
            addView(TextView(this@PreviewActivity).apply { text = "الشاشات المكتشفة على الشبكة:" })
            addView(found)
            addView(etHost, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            addView(etPin, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
        val seen = HashSet<String>()
        val disc = NsdDiscovery(this) { ip ->
            if (ip.isNotEmpty() && seen.add(ip)) {
                found.addView(Button(this).apply {
                    text = "شاشة: $ip"
                    setOnClickListener { etHost.setText(ip) }
                })
                if (etHost.text.isNullOrBlank()) etHost.setText(ip)
            }
        }
        disc.start()
        AlertDialog.Builder(this)
            .setTitle("الاتصال بالشاشة")
            .setView(root)
            .setPositiveButton("حفظ") { _, _ ->
                store.prefs.edit()
                    .putString("host", etHost.text.toString().trim())
                    .putString("pin", etPin.text.toString().trim())
                    .apply()
                updateConn()
                testConnection()
            }
            .setNegativeButton("إلغاء", null)
            .setOnDismissListener { disc.stop() }
            .show()
    }
}
