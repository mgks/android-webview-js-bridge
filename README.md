# Android Smart JS Bridge

[![](https://jitpack.io/v/mgks/android-webview-js-bridge.svg)](https://jitpack.io/#mgks/android-webview-js-bridge)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](https://opensource.org/licenses/MIT)

A lightweight, two-way, **Promise-based** bridge between Android (Kotlin/Java) and JavaScript in WebViews.

It solves the problem of getting data *back* from asynchronous calls.

Extracted from the core of **[Android Smart WebView](https://github.com/mgks/Android-SmartWebView)**.

<img src="https://github.com/mgks/android-webview-js-bridge/blob/main/preview.gif?raw=true" width="200">

## Features
*   🔄 **Two-Way:** Call Kotlin from JS, and JS from Kotlin.
*   🤝 **Promises:** JS calls return a `Promise`. No more callback hell.
*   ⚡ **Zero Dependencies:** Uses standard `JSONObject`. Lightweight.
*   ✅ **Clean API:** `register()` and `call()` methods on both sides.

## Installation (JitPack)

```groovy
implementation 'com.github.mgks:android-webview-js-bridge:1.0.0'
```

## Usage

### 1. Initialize
```kotlin
val bridge = SwvJsBridge(webView)

webView.webViewClient = object : WebViewClient() {
    override fun onPageFinished(view: WebView?, url: String?) {
        bridge.injectJsShim() // Ensure JS is ready
    }
}
```

### 2. JavaScript calls Kotlin
**Kotlin:**
```kotlin
bridge.register("getUserInfo") { data, callback ->
    // Do work...
    callback.resolve("Ghazi Khan")
}
```
**JavaScript:**
```javascript
window.SmartBridge.call("getUserInfo").then(name => {
    console.log("User is:", name);
});
```

### 3. Kotlin calls JavaScript
**JavaScript:**
```javascript
window.SmartBridge.register("updateTitle", (data) => {
    document.title = data.newTitle;
    return "Title Updated!";
});
```
**Kotlin:**
```kotlin
val data = mapOf("newTitle" to "Hello World")
bridge.callJs("updateTitle", JSONObject(data)) { result ->
    Log.d("Bridge", "JS finished: $result")
}
```

## License
MIT License