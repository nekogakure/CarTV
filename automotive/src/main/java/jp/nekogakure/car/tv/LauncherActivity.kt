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
import android.widget.LinearLayout
import android.widget.Button
import android.view.Gravity
import android.graphics.Color

const val tver_url = "https://tver.jp/"
const val youtube_url = "https://www.youtube.com/"

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

        val tabHeightPx = (56 * resources.displayMetrics.density).toInt()
        wv.setPadding(0, 0, 0, tabHeightPx)

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

        // Bottom tabs (TVer / YouTube)
        val tabs = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                tabHeightPx,
                Gravity.BOTTOM
            )
            setBackgroundColor(Color.DKGRAY)
        }

        val tverButton = Button(this).apply {
            text = "TVer"
            setTextColor(Color.WHITE)
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f)
            setBackgroundColor(Color.TRANSPARENT)
        }

        val ytButton = Button(this).apply {
            text = "YouTube"
            setTextColor(Color.LTGRAY)
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f)
            setBackgroundColor(Color.TRANSPARENT)
        }

        tabs.addView(tverButton)
        tabs.addView(ytButton)

        fun updateSelection(selected: Button) {
            if (selected === tverButton) {
                tverButton.setTextColor(Color.WHITE)
                ytButton.setTextColor(Color.LTGRAY)
            } else {
                tverButton.setTextColor(Color.LTGRAY)
                ytButton.setTextColor(Color.WHITE)
            }
        }

        tverButton.setOnClickListener {
            try {
                wv.loadUrl(tver_url)
                updateSelection(tverButton)
            } catch (e: Exception) {
                errorView.text = "Failed: ${e.message}"
                errorView.visibility = View.VISIBLE
                wv.visibility = View.GONE
            }
        }

        ytButton.setOnClickListener {
            try {
                wv.loadUrl(youtube_url)
                updateSelection(ytButton)
            } catch (e: Exception) {
                errorView.text = "Failed: ${e.message}"
                errorView.visibility = View.VISIBLE
                wv.visibility = View.GONE
            }
        }

        // loading default URL (TVer)
        try {
            wv.loadUrl(tver_url)
            updateSelection(tverButton)
        } catch (e: Exception) {
            errorView.text = "Failed: ${e.message}"
            errorView.visibility = View.VISIBLE
            wv.visibility = View.GONE
        }

        root.addView(wv)
        root.addView(errorView)
        root.addView(tabs)
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
