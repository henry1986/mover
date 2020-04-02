package org.daiv.learn.mover

import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.DefaultHeaders
import io.ktor.http.content.files
import io.ktor.http.content.static
import io.ktor.response.respondFile
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.websocket.WebSockets
import io.ktor.websocket.webSocket
import org.daiv.websocket.*
import java.io.File
import java.time.Duration
import kotlin.reflect.KClass

class MServer {
    fun start() {
        embeddedServer(Netty, port = 8080) {
            install(DefaultHeaders)
            install(CallLogging)
            install(WebSockets) {
                pingPeriod = Duration.ofMinutes(1)
            }
            routing {
                webSocket("/ws") {
                    val controlChannel = ControlledChannelImpl(outgoing, "")
                    val handler = MoverSessionHandler(controlChannel)
                    val map: Map<KClass<out WSEvent>, MessageReceiver<out WSEvent>> = mapOf(
                        MoverCommunication::class to handler
                    )
                    val manager = SessionHandlerManager(map)
                    EventBusReceiver(incoming, manager).handle()
                }
                get("/*") {
                    call.respondFile(File("js/gui/build/resources/main/index.html"))
                }
                static {
                    files("js/gui/build/resources/main")
                }
            }
        }.start(true)
    }
}

fun main() {
    System.setProperty("logback.configurationFile", "logback.xml")
    MServer().start()
}