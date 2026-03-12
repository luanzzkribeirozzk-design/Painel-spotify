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

    inner class AndroidBridge {
        @JavascriptInterface
        fun openFreeFire() {
            val pkg = "com.dts.freefireth"
            try {
                packageManager.getPackageInfo(pkg, 0)
                val intent = packageManager.getLaunchIntentForPackage(pkg)
                if (intent != null) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                }
            } catch (e: PackageManager.NameNotFoundException) {
                runOnUiThread {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$pkg")))
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()

        webView = WebView(this)
        setContentView(webView)

        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowFileAccess = true
            allowContentAccess = true
            mediaPlaybackRequiresUserGesture = false
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            cacheMode = WebSettings.LOAD_NO_CACHE
            // Essencial para YouTube IFrame API
            userAgentString = "Mozilla/5.0 (Linux; Android 11; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
        }

        webView.addJavascriptInterface(AndroidBridge(), "AndroidBridge")

        // WebChromeClient com suporte a autoplay de audio/video
        webView.webChromeClient = object : WebChromeClient() {
            override fun onConsoleMessage(msg: ConsoleMessage?): Boolean {
                android.util.Log.d("SpotifyFF", msg?.message() ?: "")
                return true
            }
        }

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val url = request?.url?.toString() ?: return false
                // Deixa YouTube e outros requests de API passarem normalmente
                if (url.contains("youtube.com") || url.contains("ytimg.com") || url.startsWith("file://")) {
                    return false
                }
                return try {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    true
                } catch (e: Exception) { false }
            }
        }

        webView.loadUrl("file:///android_asset/index.html")
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (webView.canGoBack()) webView.goBack()
        else super.onBackPressed()
    }
}
