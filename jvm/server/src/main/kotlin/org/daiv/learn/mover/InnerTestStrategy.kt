package org.daiv.learn.mover

import mu.KotlinLogging

interface MovingInterface{
    val carProxy:CarProxy
    fun forward() {
        carProxy.forward()
    }

    fun left() {
        carProxy.left()
    }

    fun right() {
        carProxy.right()
    }

    fun run()
}

class InnerTestStrategy(override val carProxy: CarProxy) : MovingInterface{
    val logger = KotlinLogging.logger{}
    init {
        carProxy.resetVelocity(5)
    }
    fun forwardTillEnd(){
        while(carProxy.isThisFieldForwardOnly()){
            forward()
        }
    }

    override fun run() {
        forward()
        forwardTillEnd()
        right()
        forwardTillEnd()
        left()
        forwardTillEnd()
        right()
        forwardTillEnd()
    }
}
