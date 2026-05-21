package com.kali.ui.sos

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.webkit.*
import androidx.appcompat.app.AppCompatActivity
import com.kali.databinding.ActivityTrackingBinding
import com.kali.ui.auth.LoginActivity
import com.kali.util.SessionManager

class TrackingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTrackingBinding
    private val session by lazy { SessionManager(this) }
    private var injectionCompleted = false

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTrackingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val role = intent.getStringExtra("ROLE") ?: session.getRole() ?: "Guardian"
        binding.tvDashboardTitle.text = if (role == "Admin") "POLICE ADMIN CONSOLE" else "FAMILY SAFETY DASHBOARD"

        // Set up exit/logout click action
        binding.btnExitDashboard.setOnClickListener {
            session.clear()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        // Configure standard WebView settings for maximum local performance
        binding.webViewDashboard.apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.databaseEnabled = true
            settings.loadWithOverviewMode = true
            settings.useWideViewPort = true
            
            // Enable hardware acceleration
            setLayerType(View.LAYER_TYPE_HARDWARE, null)
            
            // Handle standard client behaviors
            webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                    binding.pbLoading.visibility = View.VISIBLE
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    binding.pbLoading.visibility = View.GONE

                    // Inject JWT token and auto-route inside WebView
                    if (url != null && url.contains("login.html") && !injectionCompleted) {
                        val token = session.getToken() ?: ""
                        val name = session.getName() ?: "Authorized User"
                        val userRole = session.getRole() ?: "Guardian"
                        
                        val script = """
                            localStorage.setItem('token', '$token');
                            localStorage.setItem('role', '$userRole');
                            localStorage.setItem('name', '$name');
                            var target = '$userRole' === 'Admin' ? 'police/index.html' : 'guardian/index.html';
                            window.location.href = target;
                        """.trimIndent()

                        injectionCompleted = true
                        view?.evaluateJavascript(script, null)
                    }
                }

                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                    val url = request?.url?.toString() ?: return false
                    // Keep navigation inside our secure KALI WebView
                    if (url.contains("10.0.2.2") || url.contains("localhost")) {
                        return false
                    }
                    return true
                }
            }

            webChromeClient = object : WebChromeClient() {
                override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                    // Forward WebView errors to Android logcat for debugging
                    android.util.Log.d("KALI_WEBVIEW", "${consoleMessage?.message()} -- From line ${consoleMessage?.lineNumber()} of ${consoleMessage?.sourceId()}")
                    return true
                }
            }

            // Start by loading the login page to safely initialize shared localStorage context
            loadUrl("http://10.0.2.2:8080/login.html")
        }
    }

    override fun onBackPressed() {
        if (binding.webViewDashboard.canGoBack()) {
            binding.webViewDashboard.goBack()
        } else {
            super.onBackPressed()
        }
    }
}
