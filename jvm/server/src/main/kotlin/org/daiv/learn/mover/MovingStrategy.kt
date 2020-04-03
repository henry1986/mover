package org.daiv.learn.mover

import mu.KotlinLogging


class MovingStrategy(val carProxy: CarProxy) {
    val logger = KotlinLogging.logger{}
    fun forward() {
        carProxy.forward()
    }

    fun left() {
        carProxy.left()
    }

    fun right() {
        carProxy.right()
    }

    fun run() {
        carProxy.resetVelocity(5)
        forward()
        forward()
        forward()
        right()
        forward()
        forward()
        left()
        forward()
        forward()
        forward()
        right()
        forward()
        forward()
    }
}