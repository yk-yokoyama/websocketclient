package com.example.websocketclient

import android.util.Log
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI

class TestClient(uri: URI) : WebSocketClient(uri) {

    interface WebsocketCallback {
        fun onMessageReceived(message: String)
    }

    private var callback: WebsocketCallback? = null

    fun setWebSocketCallback(callback: WebsocketCallback?) {
        this.callback = callback
    }

    override fun onOpen(handshakedata: ServerHandshake?) {
        Log.d("WebSocketClient", "### onOpen")
        callback?.onMessageReceived("[client]connection opened")
    }

    override fun onMessage(message: String?) {
        // サーバーからのメッセージを受信
        Log.d("WebSocketClient", "### onMessage: $message")
        callback?.onMessageReceived("[server]${message}")
    }

    override fun onClose(code: Int, reason: String?, remote: Boolean) {
        Log.d("WebSocketClient", "### onClose")
        callback?.onMessageReceived("[client]connection closed")
    }

    override fun onError(ex: Exception?) {
        Log.d("WebSocketClient", "### onError: ${ex?.message}")
        callback?.onMessageReceived(ex?.message ?: "connection error")
    }
}