package org.daiv.learn.mover

import kotlinx.html.classes
import kotlinx.html.dom.create
import kotlinx.html.id
import kotlinx.html.js.canvas
import kotlinx.html.js.div
import mu.KotlinLoggingLevel
import org.daiv.websocket.*
import org.w3c.dom.CanvasRenderingContext2D
import kotlin.browser.document
import kotlin.browser.window

fun CanvasRenderingContext2D.clearAll() = clearRect(0.0, 0.0, canvas.width.toDouble(), canvas.height.toDouble())

fun CanvasRenderingContext2D.drawRect(x: Double, y: Double) {
    beginPath()
    rect(x, y, 20.0, 20.0)
    fillStyle = "#FF0000"
    fill()
    closePath()
}


fun CanvasRenderingContext2D.drawCircle(x: Double, y: Double) {
    beginPath()
    arc(x, y, 10.0, 0.0, 2 * kotlin.math.PI)
    fillStyle = "#05FF00"
    fill()
    closePath()
}


//fun CanvasRenderingContext2D.drawCar(carPosition: CarPosition) = drawRect(carPosition.x, carPosition.y)


fun CanvasRenderingContext2D.draw(initField: InitField) {
    val mainComponents = initField.mainComponents
    initField.streetMap.map.values.forEach {
        val drawable = when (it) {
            is Street -> StreetDrawable(it, mainComponents)
            is StreetCurve -> StreetCurveDrawable(it, mainComponents)
            is GreenField -> GreenFieldDrawable(it, mainComponents)
            else -> throw RuntimeException("unknown type: $it")
        }
        drawable.draw(this)
    }
}

class CarDrawable(
    private val velocity: Double = 0.01,
    val carPosition: CarPosition,
    val mainComponents: MainComponents,
    val canvasRenderingContext2D: CanvasRenderingContext2D,
    val doneCallback: () -> Unit
) {
    val fieldSize
        get() = mainComponents.fieldSize
    val x: Int
        get() = carPosition.x
    val y: Int
        get() = carPosition.y

    inner class Movement(
        val offsetX: Double,
        val offsetY: Double,
        val block: (Double, Double) -> Boolean
    ) {
        fun CanvasRenderingContext2D.nextField(currentX: Double, currentY: Double) {
            clearAll()
            val nextX = currentX + offsetX * fieldSize
            val nextY = currentY + offsetY * fieldSize
            drawRect(nextX, nextY)
            if (block(nextX, nextY)) {
                window.requestAnimationFrame { nextField(nextX, nextY) }
            } else {
                doneCallback()
            }
        }

        fun draw(): CarDrawable {
            canvasRenderingContext2D.nextField(fieldSize * x, fieldSize * y)
            return this@CarDrawable
        }

    }

    fun move(goDirection: GoDirection) {
        val movement = carPosition.carDirection.move(goDirection)
        movement(movement.x, movement.y)
    }

    private fun movement(xDir: Int, yDir: Int) = Movement(xDir * velocity, yDir * velocity) { nextX, nextY ->
        val xGoalPosition = fieldSize * x + xDir * fieldSize
        val xCondition = if (xDir > 0) {
            (xGoalPosition) - nextX > 0
        } else {
            (xGoalPosition) - nextX < 0
        }
        val yGoalPosition = fieldSize * y + yDir * fieldSize
        val yCondition = if (yDir > 0) {
            yGoalPosition - nextY > 0
        } else {
            yGoalPosition - nextY < 0
        }
        xCondition || yCondition
    }.draw()
}

fun CanvasRenderingContext2D.drawCar(move: Move, mainComponents: MainComponents, doneCallback: () -> Unit) {
    CarDrawable(move.velocity, move.carPosition, mainComponents, this, doneCallback).move(move.goDirection)
}

class Gui(val initField: InitField) {

    val body = document.getElementById("mainBody") ?: throw RuntimeException("body is null")
    val canvas = document.create.canvas {
        classes = setOf("streetMap")
        id = "canvas1"
        height = "800"
        width = "1024"
    }
    val canvas2 = document.create.canvas {
        classes = setOf("streetMap")
        id = "canvas2"
        height = "800"
        width = "1024"
    }

    val head = document.create.div {
        classes = setOf("head")
    }

    init {
//    canvas.appendChild(canvas2)
        body.appendChild(head)
        body.appendChild(canvas)
        body.appendChild(canvas2)
        val ctx: CanvasRenderingContext2D = canvas.getContext("2d") as CanvasRenderingContext2D
        ctx.draw(initField)
    }

    val ctx2: CanvasRenderingContext2D = canvas2.getContext("2d") as CanvasRenderingContext2D

    fun moveCar(move: Move, doneCallback: () -> Unit) {
        ctx2.drawCar(move, initField.mainComponents, doneCallback)
    }

    fun message(moverMessage: MoverMessage) {
        head.innerHTML = "<p><font color=\"${moverMessage.color}\">${moverMessage.message}</font></p>"
    }
}


fun configureLogging() {
    val loggerFilterList = listOf(
        LoggingFilterRule("org.daiv.websocket.DataSender", KotlinLoggingLevel.INFO),
        LoggingFilterRule("org.daiv.websocket.Translater", KotlinLoggingLevel.INFO)
    )
    LoggingSettings.setLogging(loggerFilterList, KotlinLoggingLevel.TRACE, true)

}

fun main() {
//    document.write("Hello, world!")
    configureLogging()
    EBDataHandler(
        EBWebsocket(),
        moverModule,
        listOf(tranlaterWithEB(InitField.serializer(), context = moverModule) { event, eb ->
            val gui = Gui(event)
            eb.currentTranslaters = {
                listOf<Translater<out WSEvent>>(translater(Move.serializer(), moverModule) {
                    gui.moveCar(it) {
                        eb.send(MovementResponse.serializer(), MovementResponse(), MovementResponse.serializer()) {}
                    }
                }, translater(MoverMessage.serializer(), moverModule) {
                    gui.message(it)
                })
            }
        })
    )

}
