package com.apexinvest.app.data.remote



import android.util.Log

import com.apexinvest.app.util.StockMetadataUtils

import kotlinx.coroutines.CoroutineScope

import kotlinx.coroutines.Dispatchers

import kotlinx.coroutines.Job

import kotlinx.coroutines.delay

import kotlinx.coroutines.flow.MutableSharedFlow

import kotlinx.coroutines.flow.SharedFlow

import kotlinx.coroutines.launch

import okhttp3.OkHttpClient

import okhttp3.Request

import okhttp3.Response

import okhttp3.WebSocket

import okhttp3.WebSocketListener

import org.json.JSONArray

import org.json.JSONObject

import java.util.UUID
import kotlin.time.Duration.Companion.milliseconds


class TradingViewWebSocketClient(private val client: OkHttpClient) {



    private var webSocket: WebSocket? = null

    private val scope = CoroutineScope(Dispatchers.IO + Job())

    private var reconnectJob: Job? = null

    private var currentSessionId: String? = null

    private var currentSymbol: String? = null



// Emits ONLY the Double price

    private val _livePrice = MutableSharedFlow<Double>(extraBufferCapacity = 10)

    val livePrice: SharedFlow<Double> = _livePrice



    private val TAG = "TradingViewWS"



    fun connect(symbol: String) {

        if (currentSymbol == symbol && webSocket != null) {

            Log.d(TAG, "Already connected to $symbol")

            return

        }



        disconnect()

        currentSymbol = symbol



        val request = Request.Builder()

            .url("wss://data.tradingview.com/socket.io/websocket")

            .header("Origin", "https://www.tradingview.com")

            .build()



        Log.d(TAG, "Initiating connection for $symbol...")

        webSocket = client.newWebSocket(request, object : WebSocketListener() {

            override fun onOpen(webSocket: WebSocket, response: Response) {

                Log.d(TAG, "Connected to TradingView WS successfully")

                val sessionId = generateSessionId()

                currentSessionId = sessionId



                val tvSymbol = getTradingViewSymbol(symbol)



// Handshake protocol

                sendMessage("quote_create_session", listOf(sessionId))

                sendMessage("quote_set_fields", listOf(sessionId, "lp", "ch", "chp", "volume"))

                sendMessage("quote_add_symbols", listOf(sessionId, tvSymbol))

                Log.i(TAG, "Subscribed to $tvSymbol (Session: $sessionId)")

            }



            override fun onMessage(webSocket: WebSocket, text: String) {

                handleIncomingMessage(text)

            }



            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {

                Log.d(TAG, "WS Closing: $code / $reason")

                if (this@TradingViewWebSocketClient.webSocket == webSocket) {
                    this@TradingViewWebSocketClient.webSocket = null
                }

            }



            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {

                Log.d(TAG, "WS Closed: $code / $reason")

                if (this@TradingViewWebSocketClient.webSocket == webSocket) {
                    this@TradingViewWebSocketClient.webSocket = null
                }

            }



            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {

                Log.e(TAG, "WS Failure: ${t.message}. Response: ${response?.message}")

                // Clear stale reference if it matches our current socket
                if (this@TradingViewWebSocketClient.webSocket == webSocket) {
                    this@TradingViewWebSocketClient.webSocket = null
                }

                reconnectJob?.cancel()

                reconnectJob = scope.launch {

                    delay(5000.milliseconds)

                    if (currentSymbol == symbol) {

                        Log.d(TAG, "Attempting reconnect for $symbol...")

                        connect(symbol)

                    }

                }

            }

        })

    }



    fun disconnect() {

        Log.d(TAG, "Disconnecting from WebSocket...")

        reconnectJob?.cancel()

        reconnectJob = null

        webSocket?.close(1000, "Disconnect requested")

        webSocket = null

        currentSessionId = null

        currentSymbol = null

    }



    private fun handleIncomingMessage(message: String) {

        if (message.contains("~h~")) {

            webSocket?.send(message)

            return

        }



        val packets = message.split("~m~")

        for (packet in packets) {

            if (packet.isBlank() || packet.all { it.isDigit() }) continue



            try {

                val json = JSONObject(packet)

                if (json.optString("m") == "qsd") {

                    val pArray = json.optJSONArray("p")

                    if (pArray != null && pArray.length() >= 2) {

                        val payload = pArray.getJSONObject(1)

                        val values = payload.optJSONObject("v")

                        if (values != null && values.has("lp")) {

                            val price = values.getDouble("lp")

                            Log.d(TAG, "Received price update: $price")

                            scope.launch {

                                _livePrice.emit(price)

                            }

                        }

                    }

                }

            } catch (_: Exception) { }

        }

    }



    private fun sendMessage(func: String, params: List<Any>) {

        val payload = JSONObject().apply {

            put("m", func)

            put("p", JSONArray(params)) // Fixes the string serialization issue

        }.toString()

        val formatted = "~m~${payload.length}~m~$payload"

        webSocket?.send(formatted)

    }



    private fun generateSessionId(): String {

        return "qs_${UUID.randomUUID().toString().replace("-", "").take(12)}"

    }



    private fun getTradingViewSymbol(symbol: String): String {

        val info = StockMetadataUtils.getExchangeInfo(symbol)

        val cleanSymbol = symbol.substringBefore(".").uppercase()



        val tvPrefix = when (val suffix = symbol.substringAfterLast(".", "").uppercase()) {

            "NS" -> "NSE"

            "BO" -> "BSE"

            "T" -> "TSE"

            "L" -> "LSE"

            "HK" -> "HKEX"

            "SS" -> "SSE"

            "SZ" -> "SZSE"

            "AX" -> "ASX"

            "DE" -> "XETR"

            "TO" -> "TSX"

            "V" -> "TSXV"

            "KS" -> "KRX"

            "SI" -> "SGX"

            "PA" -> "EURONEXT"

            "AS" -> "EURONEXT"

            "MI" -> "MIL"

            "CH" -> "SIX"

            "SR" -> "TADAWUL"

            "SA" -> "BMFBOVESPA"

            "" -> "NASDAQ"

            else -> info.tvPrefix

        }



        return "$tvPrefix:$cleanSymbol"

    }

}