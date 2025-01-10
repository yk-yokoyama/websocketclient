package com.example.websocketclient

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.URI
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    private val address by lazy { findViewById<EditText>(R.id.edit_address) }
    private val port by lazy { findViewById<EditText>(R.id.edit_port) }
    private val connectButton by lazy { findViewById<Button>(R.id.btn_connect) }
    private val disconnectButton by lazy { findViewById<Button>(R.id.btn_disconnect) }
    private val message by lazy { findViewById<EditText>(R.id.edit_message) }
    private val sendButton by lazy { findViewById<Button>(R.id.btn_send) }
    private val logTextView by lazy { findViewById<TextView>(R.id.logTextView) }

    private var client: TestClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        connectButton.setOnClickListener {
            val ip = address.text.toString()
            val port = port.text.toString()
            if (ip.isEmpty() || port.isEmpty()) {
                Toast.makeText(this, "Please enter a port number.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val uri = URI("ws://$ip:${port.toInt()}")

            CoroutineScope(Dispatchers.Main).launch {
                client = TestClient(uri)
                client?.setWebSocketCallback(object : TestClient.WebsocketCallback {
                    override fun onMessageReceived(message: String) {
                        printLog("[server]$message")
                    }
                })

                thread {
                    client?.connect()
                }
            }
        }

        disconnectButton.setOnClickListener {
            client?.close()
            client?.setWebSocketCallback(null)
        }

        sendButton.setOnClickListener {
            val message = message.text.toString()
            if (message.isEmpty()) {
                Toast.makeText(this, "Please enter a message.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            client?.send(message)
        }
    }

    private fun printLog(message: String) {
        Log.d("MainActivity", message)
        CoroutineScope(Dispatchers.Main).launch {
            logTextView.append(message)
            logTextView.append("\n")
        }
    }
}