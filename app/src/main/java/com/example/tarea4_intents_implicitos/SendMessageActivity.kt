package com.example.tarea4_intents_implicitos

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
class SendMessageActivity : AppCompatActivity() {

    private lateinit var textboxSend : EditText
    private lateinit var btnSend : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_send_message)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        textboxSend = findViewById(R.id.textbox_send)
        btnSend = findViewById(R.id.btn_send)

        btnSend.setOnClickListener { _ ->
            val sendText: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, textboxSend.text.toString())
                type = "text/plain"
            }

            val shareText = Intent.createChooser(sendText, null)
            startActivity(shareText)
        }
    }
}