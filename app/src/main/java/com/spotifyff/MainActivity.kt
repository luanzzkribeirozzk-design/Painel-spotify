package com.spotifyff

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.webkit.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView

    // ── JavaScript Bridge ──────────────────────────────────────────
    inner class AndroidBridge {

        @JavascriptInterface
        fun openFreeFire() {
            val freefirePackage = "com.dts.freefireth"
            val pm: PackageManager = packageManager

            try {
                // Verifica se o Free Fire está instalado
                pm.getPackageInfo(freefirePackage, 0)

                // Lança o Free Fire diretamente pelo Intent de LAUNCHER
                val launchIntent: Intent? = pm.getLaunchIntentForPackage(freefirePackage)
                if (launchIntent != null) {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(launchIntent)
                } else {
                    runOnUiThread {
                        Toast.makeText(
                            this@MainActivity,
                            "Free Fire não encontrado",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: PackageManager.NameNotFoundException) {
                // FF não instalado — abre Play Store
                runOnUiThread {
                    val intent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=$freefirePackage")
                    )
                    startActivity(intent)
                }
            }
        }
    }
    // ──────────────────────────────────────────────────────────────

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Esconde action bar para tela cheia
        supportActionBar?.hide()

        webView = WebView(this)
        setContentView(webView)

        // Configurações do WebView
        webView.settings.apply {
            javaScriptEnabled       = true
            domStorageEnabled       = true
            allowFileAccess         = true
            mediaPlaybackRequiresUserGesture = false
            cacheMode               = WebSettings.LOAD_DEFAULT
            mixedContentMode        = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }

        // Registra o JS Bridge com o nome "AndroidBridge"
        webView.addJavascriptInterface(AndroidBridge(), "AndroidBridge")

        // Abre links externos no browser, tudo mais no WebView
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                val url = request?.url?.toString() ?: return false
                return if (url.startsWith("file://")) {
                    false // deixa carregar normalmente
                } else {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    startActivity(intent)
                    true
                }
            }
        }

        // WebChromeClient para console.log aparecer no Logcat
        webView.webChromeClient = object : WebChromeClient() {
            override fun onConsoleMessage(msg: ConsoleMessage?): Boolean {
                android.util.Log.d("SpotifyFF_JS", msg?.message() ?: "")
                return true
            }
        }

        // Carrega o HTML da pasta assets
        webView.loadUrl("file:///android_asset/index.html")
    }

    // Back button navega no WebView se possível
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}
