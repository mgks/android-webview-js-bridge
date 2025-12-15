package dev.mgks.swv.sample

import android.os.Bundle
import android.webkit.WebView
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.webkit.WebViewClient
import dev.mgks.swv.jsbridge.SwvJsBridge
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    private lateinit var bridge: SwvJsBridge
    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        webView = findViewById(R.id.webview)
        webView.settings.javaScriptEnabled = true

        // 1. Initialize Bridge
        bridge = SwvJsBridge(webView)

        // 2. Register a Handler (JS calls "getAppVersion" -> Kotlin returns "1.0")
        bridge.register("getAppVersion") { data, callback ->
            // We can read data from JS if needed
            Toast.makeText(this, "JS asked for version", Toast.LENGTH_SHORT).show()

            // Reply
            callback.resolve("v1.0.0-Beta")
        }

        // Setup page
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                // Ensure bridge is ready on every page load
                bridge.injectJsShim()
            }
        }

        // Load Test HTML
        val html = """
            <html>
            <body>
                <h1>JS Bridge Test</h1>
                <button onclick="askNative()">Ask Native for Version (JS > APP)</button>
                <p id="result">Waiting...</p>
                
                <script>
                    function askNative() {
                        if (!window.SmartBridge) { alert("Bridge not ready!"); return; }
                        window.SmartBridge.call("getAppVersion")
                            .then(version => {
                                document.getElementById("result").innerText = "Native says: " + version;
                            });
                    }

                    // Define our setup function
                    function setupBridge() {
                        console.log("Setting up JS handlers...");
                        window.SmartBridge.register("changeBgColor", function(data) {
                            document.body.style.backgroundColor = data.color;
                            return "Color changed to " + data.color;
                        });
                    }

                    // Check if Bridge is already ready, or wait for event
                    if (window.SmartBridge) {
                        setupBridge();
                    } else {
                        window.addEventListener("SmartBridgeReady", setupBridge);
                    }
                </script>
            </body>
            </html>
        """
        webView.loadDataWithBaseURL(null, html, "text/html", "utf-8", null)

        // 3. Test Button: Kotlin calling JS
        findViewById<Button>(R.id.btn_test).setOnClickListener {
            // Send data to JS
            val data = mapOf("color" to "#86B4F8")

            bridge.callJs("changeBgColor", JSONObject(data)) { result ->
                Toast.makeText(this, "JS Replied: $result", Toast.LENGTH_SHORT).show()
            }
        }
    }
}