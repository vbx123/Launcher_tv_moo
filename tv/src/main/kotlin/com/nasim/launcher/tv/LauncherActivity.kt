package com.nasim.launcher.tv

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Typeface
import android.media.tv.TvContract
import android.media.tv.TvInputInfo
import android.media.tv.TvInputManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.os.StatFs
import android.os.SystemClock
import android.view.View
import android.widget.ProgressBar
import android.widget.TextClock
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nasim.launcher.common.OpenMeteo
import com.nasim.launcher.common.ThemeConfig
import com.nasim.launcher.common.ThemeStore
import com.nasim.launcher.common.ThemeStyle
import com.nasim.launcher.common.ThemeStyles
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors
import com.nasim.launcher.common.R as CR

/**
 * الشاشة الرئيسية (Launcher).
 * مصدر الحقيقة الوحيد للإعدادات = SharedPreferences (ThemeStore).
 * سواء غيّرت من إعدادات الشاشة أو جه تحديث من الموبايل، الاتنين بيكتبوا في نفس المكان،
 * والـ prefsListener هو اللي بيطبّق التغيير فوراً.
 */
class LauncherActivity : Activity() {

    private lateinit var store: ThemeStore
    private lateinit var repo: AppRepository
    private lateinit var bg: android.widget.ImageView
    private lateinit var clock: TextClock
    private lateinit var dateText: TextView
    private lateinit var weatherText: TextView
    private lateinit var statusRow: View
    private lateinit var wifiText: TextView
    private lateinit var storageText: TextView
    private lateinit var storageBar: ProgressBar
    private lateinit var settingsBtn: TextView
    private lateinit var rv: RecyclerView
    private lateinit var adapter: TileAdapter

    private val main = Handler(Looper.getMainLooper())
    private val io = Executors.newSingleThreadExecutor()
    private val dateFmt = SimpleDateFormat("EEEE، d MMMM yyyy", Locale.forLanguageTag("ar-u-nu-latn"))

    private var config = ThemeConfig()
    private var style: ThemeStyle = ThemeStyles.of(ThemeConfig.THEME_JERUSALEM)
    private var typeface: Typeface? = null
    private var allApps: List<InstalledApp> = emptyList()
    private var lastWeatherAt = 0L
    private var lastBgRes = 0
    private var openDialog: Dialog? = null

    // لازم نحتفظ بالـ listener في متغير (SharedPreferences بتمسكه Weak)
    private val prefsListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (key == ThemeStore.KEY_CONFIG) applyConfig(store.load())
    }

    private val tick = object : Runnable {
        override fun run() {
            dateText.text = dateFmt.format(Date())
            if (style.showStatusBar) updateStatus()
            if (SystemClock.elapsedRealtime() - lastWeatherAt > 15 * 60_000L) fetchWeather()
            main.postDelayed(this, 30_000L)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tv)
        store = ThemeStore(this)
        repo = AppRepository(this)

        bg = findViewById(R.id.bg)
        clock = findViewById(R.id.clock)
        dateText = findViewById(R.id.dateText)
        weatherText = findViewById(R.id.weatherText)
        statusRow = findViewById(R.id.statusRow)
        wifiText = findViewById(R.id.wifiText)
        storageText = findViewById(R.id.storageText)
        storageBar = findViewById(R.id.storageBar)
        settingsBtn = findViewById(R.id.settingsBtn)
        rv = findViewById(R.id.rv)

        val d = resources.displayMetrics.density
        adapter = TileAdapter((150 * d).toInt(), (116 * d).toInt(), (8 * d).toInt())
        rv.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        rv.adapter = adapter
        rv.itemAnimator = null              // بدون animations إضافية = أخف على Mali-450
        rv.setHasFixedSize(true)
        rv.setItemViewCacheSize(8)

        settingsBtn.setOnClickListener {
            openDialog = SettingsDialog(this, store, typeface).also { it.show() }
        }
        settingsBtn.setOnFocusChangeListener { v, has ->
            v.animate().scaleX(if (has) 1.08f else 1f).scaleY(if (has) 1.08f else 1f).setDuration(120).start()
        }

        applyConfig(store.load())
        loadApps()
    }

    override fun onResume() {
        super.onResume()
        (application as TvApp).server.start()   // يتأكد إن السيرفر شغال
        store.prefs.registerOnSharedPreferenceChangeListener(prefsListener)
        val c = store.load()
        if (c != config) applyConfig(c)
        main.removeCallbacks(tick)
        tick.run()
    }

    override fun onPause() {
        store.prefs.unregisterOnSharedPreferenceChangeListener(prefsListener)
        main.removeCallbacks(tick)
        super.onPause()
    }

    override fun onDestroy() {
        main.removeCallbacksAndMessages(null)
        io.shutdownNow()
        super.onDestroy()
    }

    /** زر Home وإحنا جوه اللانشر: يقفل أي نافذة ويرجع لأول كارت. */
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        openDialog?.dismiss()
        rv.scrollToPosition(0)
        focusFirst()
    }

    @Deprecated("Launcher: Back لا يعمل شيئاً")
    override fun onBackPressed() { /* الـ Launcher مبيقفلش */ }

    // ------------------------------------------------------------------ theme
    private fun applyConfig(c: ThemeConfig) {
        val old = config
        config = c
        style = ThemeStyles.of(c.themeId)

        if (lastBgRes != style.background) {
            bg.setImageResource(style.background)
            lastBgRes = style.background
        }
        typeface = pickTypeface()
        for (t in arrayOf(clock, dateText, weatherText, wifiText, storageText, settingsBtn)) t.typeface = typeface
        clock.setTextColor(style.textColor)
        dateText.setTextColor(style.textColor)
        weatherText.setTextColor(style.subTextColor)
        wifiText.setTextColor(style.subTextColor)
        storageText.setTextColor(style.subTextColor)
        settingsBtn.setTextColor(style.textColor)
        settingsBtn.setBackgroundResource(style.cardBg)

        clock.format12Hour = if (c.use24h) "HH:mm" else "h:mm a"
        clock.format24Hour = "HH:mm"
        statusRow.visibility = if (style.showStatusBar) View.VISIBLE else View.GONE
        weatherText.visibility = if (c.showWeather) View.VISIBLE else View.GONE
        if (!c.showWeather) weatherText.text = ""

        adapter.typeface = typeface
        rebuildRow()

        dateText.text = dateFmt.format(Date())
        if (style.showStatusBar) updateStatus()
        if (old.lat != c.lat || old.lon != c.lon || old.cityName != c.cityName || lastWeatherAt == 0L) {
            fetchWeather()
        }
    }

    /** لو حطيت ملف خط في tv/src/main/res/font/theme_font.ttf هيتستخدم تلقائياً. */
    private fun pickTypeface(): Typeface {
        val id = resources.getIdentifier("theme_font", "font", packageName)
        if (id != 0) try { return resources.getFont(id) } catch (_: Exception) {}
        return if (style.useSerif) Typeface.create(Typeface.SERIF, Typeface.BOLD)
        else Typeface.create("sans-serif-light", Typeface.NORMAL)
    }

    // ------------------------------------------------------------------ apps row
    private fun loadApps() {
        io.execute {
            val apps = repo.loadAll()
            main.post {
                if (isDestroyed) return@post
                allApps = apps
                rebuildRow()
            }
        }
    }

    private fun rebuildRow() {
        val tiles = ArrayList<Tile>()
        tiles += shortcut("المتجر", CR.drawable.ic_store,
            listOf("com.android.vending", "cm.aptoide.pt", "com.apkpure.aegon"))
        tiles += shortcut("مركز الوسائط", CR.drawable.ic_media,
            listOf("org.xbmc.kodi", "com.plexapp.android", "com.mxtech.videoplayer.ad", "com.google.android.videos"))
        tiles += shortcut("يوتيوب", CR.drawable.ic_youtube,
            listOf("com.google.android.youtube.tv", "com.liskovsoft.smarttubetv", "com.google.android.youtube"))
        tiles += Tile("HDMI", getDrawable(CR.drawable.ic_hdmi)) { openHdmi() }
        tiles += Tile("المزيد", getDrawable(CR.drawable.ic_more)) { showAllApps() }
        adapter.submit(tiles, style)
        focusFirst()
    }

    /** لو أي تطبيق من القائمة مثبت نفتحه (بأيقونته الأصلية)، وإلا نعرض رسالة. عدّل القوائم حسب جهازك. */
    private fun shortcut(label: String, fallbackIcon: Int, pkgs: List<String>): Tile {
        val app = pkgs.firstNotNullOfOrNull { p -> allApps.firstOrNull { it.pkg == p } }
        return if (app != null) Tile(label, app.icon) { launch(app) }
        else Tile(label, getDrawable(fallbackIcon)) { toast("$label غير مثبت على الجهاز") }
    }

    private fun launch(app: InstalledApp) {
        try { startActivity(app.intent) } catch (e: Exception) { toast("تعذر فتح ${app.label}") }
    }

    private fun showAllApps() {
        openDialog = AllAppsDialog(this, allApps, style, typeface) { launch(it) }.also { it.show() }
    }

    /** يفتح مدخل HDMI (Passthrough) من خلال TvInputManager. */
    private fun openHdmi() {
        try {
            val tv = getSystemService(TV_INPUT_SERVICE) as TvInputManager
            val inputs = tv.tvInputList
            val input = inputs.firstOrNull { it.type == TvInputInfo.TYPE_HDMI && it.isPassthroughInput }
                ?: inputs.firstOrNull { it.isPassthroughInput }
            if (input != null) {
                startActivity(
                    Intent(Intent.ACTION_VIEW, TvContract.buildChannelUriForPassthroughInput(input.id))
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
                return
            }
        } catch (_: Exception) {
        }
        toast("لم يتم العثور على مدخل HDMI")
    }

    private fun focusFirst() {
        rv.post {
            if (openDialog?.isShowing == true) return@post
            rv.findViewHolderForAdapterPosition(0)?.itemView?.requestFocus()
        }
    }

    // ------------------------------------------------------------------ weather / status
    private fun fetchWeather() {
        if (!config.showWeather) return
        val c = config
        lastWeatherAt = SystemClock.elapsedRealtime()
        weatherText.text = c.cityName
        io.execute {
            val w = OpenMeteo.fetchWeather(c.lat, c.lon)
            main.post {
                if (isDestroyed || !config.showWeather) return@post
                weatherText.text = if (w == null) c.cityName
                else "${c.cityName} • ${w.tempC}° ${OpenMeteo.describe(w.code)}"
            }
        }
    }

    private fun updateStatus() {
        val cm = getSystemService(ConnectivityManager::class.java)
        val net = cm.activeNetwork
        val caps = if (net == null) null else cm.getNetworkCapabilities(net)
        wifiText.text = when {
            caps == null -> "لا يوجد اتصال"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "Wi-Fi متصل"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet متصل"
            else -> "متصل"
        }
        val st = StatFs(Environment.getDataDirectory().path)
        val total = st.totalBytes
        val used = total - st.availableBytes
        storageText.text = String.format(Locale.US, "التخزين %.1f / %.1f GB", used / 1e9, total / 1e9)
        storageBar.progress = if (total > 0) ((used * 100) / total).toInt() else 0
    }

    private fun toast(s: String) = Toast.makeText(this, s, Toast.LENGTH_SHORT).show()
}
