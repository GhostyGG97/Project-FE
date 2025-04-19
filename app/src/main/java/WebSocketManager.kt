import okhttp3.*
import okio.ByteString

object WebSocketManager {
    private lateinit var webSocket: WebSocket
    private val client = OkHttpClient()

    private val request = Request.Builder()
        .url("ws://10.0.2.2:5000/ws") // IP para localhost desde el emulador
        .build()

    var onMessageReceived: ((String) -> Unit)? = null

    fun connect() {
        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                println("WebSocket conectado")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                onMessageReceived?.invoke(text)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                println("❌ Error de conexión: ${t.message}")
                response?.let { println("Código HTTP: ${it.code}") }
                println("Error WebSocket: ${t.message}")
            }
        })
    }

    fun sendMessage(message: String) {
        webSocket.send(message)
    }
}
