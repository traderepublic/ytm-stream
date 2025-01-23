package application.plugins

import application.YtmProvider
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.pingPeriod
import io.ktor.server.websocket.timeout
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.CloseReason
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

fun Application.configureSockets(ytmProvider: YtmProvider) {
    install(WebSockets) {
        pingPeriod = 10.seconds
        timeout = 15.seconds
        maxFrameSize = 1024
        masking = false
    }

    /**
     * Websocket endpoint to subscribe to Yield to Maturity updates for a given ISIN.
     * The ISIN is passed as a parameter in the URL.
     * Example: ws://localhost:8080/ytm/DE0001102440
     */
    routing {
        webSocket("/ytm/{isin}") {
            val isin = call.parameters["isin"] ?: return@webSocket close(
                CloseReason(
                    CloseReason.Codes.CANNOT_ACCEPT,
                    "Missing isin parameter"
                )
            )
            runCatching {
                launch {
                    ytmProvider.subscribe(isin).collect {
                        send(Frame.Text(it))
                    }
                }

                try {
                    closeReason.await()
                } finally {
                    ytmProvider.unsubscribe(isin)
                }
            }.onFailure {
                ytmProvider.closeAll()
            }
        }
    }
}