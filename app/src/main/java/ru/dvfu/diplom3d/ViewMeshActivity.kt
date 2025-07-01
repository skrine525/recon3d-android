package ru.dvfu.diplom3d

import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity

class ViewMeshActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val serverUrl = prefs.getString("server_url", null)
        val meshId = intent.getStringExtra("mesh_id")
        val x = intent.getDoubleExtra("x", Double.NaN)
        val y = intent.getDoubleExtra("y", Double.NaN)
        val z = intent.getDoubleExtra("z", Double.NaN)
        if (serverUrl.isNullOrEmpty() || meshId.isNullOrEmpty()) {
            finish()
            return
        }
        val webView = WebView(this)
        webView.settings.javaScriptEnabled = true
        webView.webViewClient = WebViewClient()
        setContentView(webView)
        var url = "$serverUrl/mesh/$meshId/"
        if (!x.isNaN() && !y.isNaN() && !z.isNaN()) {
            url += "?x=$x&y=$y&z=$z"
        }
        webView.loadUrl(url)
    }
} 