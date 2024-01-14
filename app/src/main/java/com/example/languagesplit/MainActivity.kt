package com.example.languagesplit

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.google.android.play.core.ktx.sessionId
import com.google.android.play.core.ktx.status
import com.google.android.play.core.splitcompat.SplitCompat
import com.google.android.play.core.splitinstall.SplitInstallManagerFactory
import com.google.android.play.core.splitinstall.SplitInstallRequest
import com.google.android.play.core.splitinstall.SplitInstallSessionState
import com.google.android.play.core.splitinstall.SplitInstallStateUpdatedListener
import com.google.android.play.core.splitinstall.model.SplitInstallSessionStatus
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private val tvSwitch by lazy { findViewById<TextView>(R.id.tvSwitch) }
    private val tvDisplay by lazy { findViewById<TextView>(R.id.tvDisplay) }
    private val tvKey1 by lazy { findViewById<TextView>(R.id.tvKey1) }
    private val tvKey2 by lazy { findViewById<TextView>(R.id.tvKey2) }
    private val tvKey3 by lazy { findViewById<TextView>(R.id.tvKey3) }
    private val tvKey4 by lazy { findViewById<TextView>(R.id.tvKey4) }

    private val manager by lazy {
        SplitInstallManagerFactory.create(this)
    }

    private val mHandler = Handler(Looper.getMainLooper())

    private val tasks = mutableMapOf<Int,LoadItem>()

    private val listener = object : SplitInstallStateUpdatedListener {
        override fun onStateUpdate(state: SplitInstallSessionState) {
            val sessionId = state.sessionId
            Log.e("chenglei", "onStateUpdate, ret:${
                when (state.status) {
                    SplitInstallSessionStatus.INSTALLED -> "installed"
                    SplitInstallSessionStatus.UNKNOWN -> "unknow"
                    SplitInstallSessionStatus.PENDING->"pending"
                    SplitInstallSessionStatus.REQUIRES_USER_CONFIRMATION->"requires_user_confirmation"
                    SplitInstallSessionStatus.DOWNLOADING -> "downloading"
                    SplitInstallSessionStatus.DOWNLOADED -> "downloaded"
                    SplitInstallSessionStatus.INSTALLING->"installing"
                    SplitInstallSessionStatus.INSTALLED->"installed"
                    SplitInstallSessionStatus.FAILED->"failed"
                    SplitInstallSessionStatus.CANCELING->"canceling"
                    SplitInstallSessionStatus.CANCELED->"canceled"
                    else->"unknow"
                }
            }")
            when(state.status){
                SplitInstallSessionStatus.INSTALLED->{
                    val loadItem = tasks[sessionId]
                    tasks.remove(sessionId)
                    loadItem?:return
                    val languageItem = loadItem.item
                    val appLocale = loadItem.locale
                    LanguageSP.saveLanguage(this@MainActivity, languageItem)
                    tvSwitch.setText(languageItem.name)
                    AppCompatDelegate.setApplicationLocales(appLocale)
                }
                    SplitInstallSessionStatus.CANCELED,
                    SplitInstallSessionStatus.FAILED->{
                        tasks.remove(sessionId)
                    }
                else->{}
            }
        }

    }

    private val languageItems = arrayListOf<LanguageItem>(
        LanguageItem("中文", "zh-CN"),
        LanguageItem("英文", "en-US"),
        LanguageItem("印尼语", "in-ID"),
        LanguageItem("阿拉伯语", "ar-EG"),
    )

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(newBase)
        SplitCompat.installActivity(this)
    }

    override fun onResume() {
        super.onResume()
        manager.registerListener(listener)
    }

    override fun onDestroy() {
        super.onDestroy()
        manager.unregisterListener(listener)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_activity_main)
        val primaryLocale: Locale = this.resources.configuration.locales[0]
        val locale: String = primaryLocale.displayName
        tvDisplay.setText(locale)

        tvSwitch.text = LanguageSP.language(this).name
        tvKey1.setText(R.string.string_1)
        tvKey2.setText(R.string.string_2)
        tvKey3.setText(R.string.string_3)
        tvKey4.setText(R.string.string_4)

        tvSwitch.setOnClickListener {
            val popupWindow = PopupWindow(this)
            val root = LayoutInflater.from(this).inflate(R.layout.layout_switch_language, null, false)
            popupWindow.contentView = root
            if (root is LinearLayout) {
                root.removeAllViews()
                for (languageItem in languageItems) {
                    root.addView(TextView(this@MainActivity).apply {
                        setText(languageItem.name)
                        setOnClickListener {
                            val appLocale = LocaleListCompat.forLanguageTags(languageItem.code)
                            val locale = appLocale[0]
                            val installed = SplitInstallManagerFactory.create(this@MainActivity)
                                .installedLanguages
                                .find {
                                    it == languageItem.code.split("-")[0]
                                } != null
                            if (installed) {
                                LanguageSP.saveLanguage(this@MainActivity, languageItem)
                                tvSwitch.setText(languageItem.name)
                                AppCompatDelegate.setApplicationLocales(appLocale)
                                return@setOnClickListener
                            }
                            if (tasks.values.find { it.item.code==languageItem.code } != null) {
                                Log.e("chenglei", "${languageItem.code}下载中")
                                return@setOnClickListener
                            }
                            // 请求
                            val request = SplitInstallRequest.newBuilder()
                                .addLanguage(locale)
                                .build()

                            val splitInstallManager= SplitInstallManagerFactory.create(this@MainActivity)

                            splitInstallManager.startInstall(request)
                                .addOnSuccessListener {
                                    mHandler.post {
                                        tasks.put(it, LoadItem(languageItem, appLocale))
                                    }
                                    Log.e("chenglei", "install succ:${it}")
                                }
                                .addOnCanceledListener {
                                    Log.e("chenglei", "install cancel")
                                }
                                .addOnFailureListener {
                                    Log.e("chenglei", "install failure, ret:${it.stackTraceToString()}")
                                }
                                .addOnCompleteListener {
                                    Log.e("chenglei", "install complete,${it.result}")
                                }

                            popupWindow.dismiss()
                        }
                    },LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        topMargin = 60
                    })
                }
            }
            popupWindow.showAsDropDown(tvSwitch)
        }
    }
}