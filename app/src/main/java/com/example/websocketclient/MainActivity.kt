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

            if (client?.isOpen == true) {
                Toast.makeText(this, "Already connected.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            client = TestClient(uri)
            client?.setWebSocketCallback(object : TestClient.WebsocketCallback {
                override fun onMessageReceived(message: String) {
                    printLog(message)
                }
            })

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    client?.connect()
                    Log.d("MainActivity", "### connect")
                } catch (e: Exception) {
                    Log.d("MainActivity", "### connect error: ${e.message}")
                }
            }
        }

        disconnectButton.setOnClickListener {
            Log.d("MainActivity", "### close")
            client?.close()
            client?.setWebSocketCallback(null)
            printLog("[client]connection closed")
        }

        sendButton.setOnClickListener {
            val message = message.text.toString()
            if (message.isEmpty()) {
                Toast.makeText(this, "Please enter a message.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            try {
                client?.send(message)
            } catch (e: Exception) {
                printLog(e.message ?: "null")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        client?.close()
        client?.setWebSocketCallback(null)
    }

    fun printLog(message: String) {
        Log.d("MainActivity", message)
        CoroutineScope(Dispatchers.Main).launch {
            logTextView.append(message)
            logTextView.append("\n")
        }
    }
}