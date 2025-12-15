package dev.mgks.swv.sample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView
// Import your library placeholder to prove connection works
import dev.mgks.swv.placeholder.Placeholder

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Test the library connection
        val lib = Placeholder()
        val textView = findViewById<TextView>(R.id.text_status) // Assuming you give ID in xml

        // This proves the app can see the library module
        textView.text = "Template App Running.\nLibrary says: ${lib.greet()}"
    }
}