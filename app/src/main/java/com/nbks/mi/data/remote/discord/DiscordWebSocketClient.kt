package com.nbks.mi.data.remote.discord
import javax.inject.Inject
// Discord WebSocket removed
enum class DiscordConnectionStatus { DISCONNECTED, CONNECTING, CONNECTED }
class DiscordWebSocketClient @Inject constructor() {
    fun connect(token: String) {}
    fun disconnect() {}
}
