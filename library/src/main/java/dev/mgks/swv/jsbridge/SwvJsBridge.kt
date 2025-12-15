package dev.mgks.swv.jsbridge

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebView
import org.json.JSONObject

class SwvJsBridge(private val webView: WebView) {

    private val requestHandlers = HashMap<String, (data: Any?, callback: BridgeCallback) -> Unit>()
    private val pendingResponses = HashMap<String, (result: Any?) -> Unit>()
    private val mainHandler = Handler(Looper.getMainLooper())

    init {
        webView.addJavascriptInterface(this, "AndroidBridge")
        // Try to inject immediately (might fail if WebView not ready, but safe to try)
        injectJsShim()
    }

    fun register(name: String, handler: (data: Any?, callback: BridgeCallback) -> Unit) {
        requestHandlers[name] = handler
    }

    fun callJs(handlerName: String, data: Any? = null, onSuccess: ((Any?) -> Unit)? = null) {
        val reqId = "native_req_" + System.currentTimeMillis()
        if (onSuccess != null) {
            pendingResponses[reqId] = onSuccess
        }

        val payload = JSONObject()
        payload.put("type", "request")
        payload.put("handler", handlerName)
        payload.put("id", reqId)
        if (data != null) payload.put("data", data)

        runOnMain {
            // Check if bridge exists before calling
            val script = """
                if (window.SmartBridge) {
                    window.SmartBridge._handleNativeMessage(${payload.toString()});
                } else {
                    console.error("SmartBridge not ready yet for call: $handlerName");
                }
            """
            webView.evaluateJavascript(script, null)
        }
    }

    interface BridgeCallback {
        fun resolve(data: Any?)
        fun reject(errorMessage: String)
    }

    @JavascriptInterface
    fun postMessage(message: String) {
        try {
            val json = JSONObject(message)
            val type = json.optString("type")
            val id = json.optString("id")

            if (type == "request") {
                val handlerName = json.optString("handler")
                val data = json.opt("data")
                val handler = requestHandlers[handlerName]

                if (handler != null) {
                    val callback = object : BridgeCallback {
                        override fun resolve(result: Any?) { replyToJs(id, result, true) }
                        override fun reject(error: String) { replyToJs(id, error, false) }
                    }
                    runOnMain { handler(data, callback) }
                } else {
                    Log.e("SwvJsBridge", "No Kotlin handler registered for: $handlerName")
                    replyToJs(id, "Handler not found: $handlerName", false)
                }
            } else if (type == "response") {
                val callback = pendingResponses.remove(id)
                val data = json.opt("data")
                runOnMain { callback?.invoke(data) }
            }
        } catch (e: Exception) {
            Log.e("SwvJsBridge", "Failed to parse message", e)
        }
    }

    private fun replyToJs(reqId: String, data: Any?, success: Boolean) {
        val response = JSONObject()
        response.put("type", "response")
        response.put("id", reqId)
        response.put("status", if (success) "success" else "error")
        response.put("data", data)

        runOnMain {
            webView.evaluateJavascript("window.SmartBridge._handleNativeMessage(${response.toString()})", null)
        }
    }

    private fun runOnMain(block: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) block() else mainHandler.post(block)
    }

    fun injectJsShim() {
        val jsCode = """
            (function() {
                if (window.SmartBridge) return;

                window.SmartBridge = {
                    handlers: {},
                    pending: {},
                    
                    register: function(name, callback) {
                        this.handlers[name] = callback;
                    },

                    call: function(handler, data) {
                        return new Promise((resolve, reject) => {
                            const id = 'js_req_' + Date.now() + Math.floor(Math.random() * 1000);
                            this.pending[id] = { resolve: resolve, reject: reject };
                            const msg = { type: 'request', handler: handler, id: id, data: data };
                            if (window.AndroidBridge) {
                                window.AndroidBridge.postMessage(JSON.stringify(msg));
                            } else {
                                reject("AndroidBridge not found");
                            }
                        });
                    },

                    _handleNativeMessage: function(msg) {
                        if (msg.type === 'response') {
                            const p = this.pending[msg.id];
                            if (p) {
                                if (msg.status === 'success') p.resolve(msg.data);
                                else p.reject(msg.data);
                                delete this.pending[msg.id];
                            }
                        } else if (msg.type === 'request') {
                            const handler = this.handlers[msg.handler];
                            if (handler) {
                                Promise.resolve(handler(msg.data)).then(result => {
                                    this._replyNative(msg.id, result, true);
                                }).catch(err => {
                                    this._replyNative(msg.id, err, false);
                                });
                            } else {
                                console.error("No JS handler registered for: " + msg.handler);
                                this._replyNative(msg.id, "Handler not found", false);
                            }
                        }
                    },

                    _replyNative: function(id, data, success) {
                        const msg = { type: 'response', id: id, status: success ? 'success' : 'error', data: data };
                        if (window.AndroidBridge) window.AndroidBridge.postMessage(JSON.stringify(msg));
                    }
                };
                console.log("SmartBridge initialized");
                
                // DISPATCH EVENT so the page knows we are ready
                window.dispatchEvent(new Event("SmartBridgeReady"));
            })();
        """.trimIndent()

        webView.evaluateJavascript(jsCode, null)
    }
}