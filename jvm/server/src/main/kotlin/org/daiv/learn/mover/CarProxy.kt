package org.daiv.learn.mover

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.KSerializer
import mu.KotlinLogging

interface CarProxyCom {
    fun <T : MoverCommunication> send(serializer: KSerializer<T>, t: T, response: () -> Unit)
}


class CarProxy(val carProxyCom: CarProxyCom, val streetMap: StreetMap) {
    val logger = KotlinLogging.logger {}
    private var carPosition = CarPosition(1, 1, CarDirection.East)
    private var velocity: Double = 0.01
    private fun send(goDirection: GoDirection) {
        val channel = Channel<Int>()
        val move = Move(carPosition, goDirection, velocity)
        logger.trace { "move: $move" }
        carProxyCom.send(Move.serializer(), move) {
            runBlocking {
                channel.send(5)
            }
        }
        runBlocking {
            channel.receive()
        }
        val prev = streetMap[carPosition]
        carPosition = carPosition.move(goDirection)
        val after = streetMap[carPosition]
        if (prev == null || after == null || !prev.isTransitionPossible(after)) {
            carProxyCom.send(MoverMessage.serializer(), MoverMessage("error -> you have left the street", "red")) {

            }
            throw RuntimeException("error")
        }
    }

    fun resetVelocity(velocity: Int) {
        this.velocity = velocity * 0.01
    }

    fun forward() {
        send(GoDirection.Forward)
    }

    fun right() {
        send(GoDirection.Right)
    }

    fun left() {
        send(GoDirection.Left)
    }
}