package org.daiv.learn.mover

interface MovingInterface{
    fun run()
}

class InnerTestStrategy(val carProxy: CarProxy) : MovingInterface{
    init {
        carProxy.resetVelocity(5)
    }
    private fun forward() = carProxy.forward()
    private fun left() = carProxy.left()
    private fun right() = carProxy.right()

    override fun run() {
        forward()
        left()
    }
}