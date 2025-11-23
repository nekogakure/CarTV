package jp.nekogakure.car.tv

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import android.widget.FrameLayout
import android.view.ViewGroup
import android.view.View
import android.widget.TextView

const val url = "https://tver.jp/"

class LauncherActivity : ComponentActivity() {
    private var webView: WebView? = null

    @SuppressLint("SetJavaScriptEnabled", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = FrameLayout(this)
        root.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        val wv = WebView(this)
        wv.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )

        // Debugging
        WebView.setWebContentsDebuggingEnabled(true)
        wv.webChromeClient = WebChromeClient()

        // Error view
        val errorView = TextView(this).apply {
            visibility = View.GONE
            textSize = 16f
            setPadding(32, 32, 32, 32)
        }

        wv.webViewClient = object : WebViewClient() {
            @SuppressLint("SetTextI18n")
            override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
                if (request.isForMainFrame) {
                    errorView.text = "error: [${error.errorCode}] ${error.description}"
                    errorView.visibility = View.VISIBLE
                    view.visibility = View.GONE
                }
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                errorView.visibility = View.GONE
                view?.visibility = View.VISIBLE
            }
        }

        // WebView settings
        val settings: WebSettings = wv.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.loadWithOverviewMode = true
        settings.useWideViewPort = true
        settings.userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36"
        settings.mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE

        // Cookies
        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)
        cookieManager.setAcceptThirdPartyCookies(wv, true)

        // loading URL
        try {
            wv.loadUrl(url)
        } catch (e: Exception) {
            errorView.text = "Failed: ${e.message}"
            errorView.visibility = View.VISIBLE
            wv.visibility = View.GONE
        }

        root.addView(wv)
        root.addView(errorView)
        setContentView(root)
        webView = wv
    }

    override fun onDestroy() {
        webView?.apply {
            (parent as? ViewGroup)?.removeView(this)
            stopLoading()
            clearHistory()

            // load a blank page to free up js and other resources
            try {
                loadUrl("about:blank")
            } catch (_: Exception) {}

            removeAllViews()
            destroy()
        }
        webView = null
        super.onDestroy()
    }
}
