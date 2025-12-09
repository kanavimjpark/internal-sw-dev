package com.example.a3dmodelsample.retrofit

import android.util.Log
import okhttp3.*
import org.json.JSONObject

class TickerWsClient(
    private val token: String,
    private val listener: (symbol: String, price: Double, ts: Long) -> Unit
) : WebSocketListener() {

    private var webSocket: WebSocket? = null
    private val client = OkHttpClient()

    fun connect() {
        val req = Request.Builder()
            .url("wss://ws.finnhub.io?token=$token")
            .build()
        webSocket = client.newWebSocket(req, this)
    }

    fun subscribe(symbol: String) {
//        webSocket?.send("""{"type":"subscribe","symbol":"$symbol"}""")

        val msg = """{"type":"subscribe","symbol":"$symbol"}"""
        val ok = webSocket?.send(msg) ?: false

        if (ok) {
            Log.d("WS", "📤 Subscribe sent: $msg")
        } else {
            Log.e("WS", "⚠️ Failed to send subscribe: socket is null/closed")
        }
    }

    fun unsubscribe(symbol: String) {
        webSocket?.send("""{"type":"unsubscribe","symbol":"$symbol"}""")
    }

    override fun onOpen(webSocket: WebSocket, response: Response) {
        super.onOpen(webSocket, response)
        Log.d("WS", "✅ onOpen: $response")

        val symbols = listOf("AAPL", "AMZN", "BINANCE:BTCUSDT", "IC MARKETS:1")
        symbols.forEach { s ->
            val msg = """{"type":"subscribe","symbol":"$s"}"""
            val ok = webSocket.send(msg)
            Log.d("WS", "📤 subscribe sent=$ok, msg=$msg")
        }
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        // JSON 파싱해서 listener 호출 (예: kotlinx.serialization 또는 org.json 사용)
        // { "type":"trade", "data":[ { "s":"AAPL","p":182.3,"t":..., "v":... } ] }
        Log.d("WS", "📩 raw onMessage: $text")
        val json = JSONObject(text)
        when (json.getString("type")) {
            "ping" -> Log.d("WS", "🏓 ping")
            "trade" -> {
                val arr = json.getJSONArray("data")
                for (i in 0 until arr.length()) {
                    val t = arr.getJSONObject(i)
                    val s = t.getString("s")
                    val p = t.getDouble("p")
                    val ts = t.getLong("t")
                    Log.d("WS", "💹 trade: $s $p")

                    listener.invoke(s,p,ts)
                }
            }
        }
    }

    override fun onFailure(ws: WebSocket, t: Throwable, r: Response?) {
        Log.e("WS", "🔥 onFailure: ${t.message}", t)
        Log.e("WS", "response=$r")
    }

    override fun onClosed(ws: WebSocket, code: Int, reason: String) {
        Log.w("WS", "🔌 onClosed: code=$code, reason=$reason")
    }


    fun close() { webSocket?.close(1000, null) }
}
