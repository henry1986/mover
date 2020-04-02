package org.daiv.learn.mover

import org.w3c.dom.CanvasRenderingContext2D

interface Drawable {
    fun draw(canvasRenderingContext2D: CanvasRenderingContext2D)
}

//data class Street(val x: Int, val y: Int, val streetConfig: StreetConfig = StreetConfig.StraightVertical) : Drawable {
//    override fun draw(canvasRenderingContext2D: CanvasRenderingContext2D, mainComponents: MainComponents) {
//        StreetDrawable(x, y, mainComponents, streetConfig).draw(canvasRenderingContext2D)
//    }
//}


class GreenFieldDrawable(val greenField: GreenField, val mainComponents: MainComponents) :Drawable{
    val x: Int
        get() = greenField.x
    val y: Int
        get() = greenField.y
    val fieldSize
        get() = mainComponents.fieldSize

    private fun CanvasRenderingContext2D.draw() {
        beginPath()
        rect(x * fieldSize, y * fieldSize, fieldSize, fieldSize)
        fillStyle = mainComponents.greenColor
        fill()
        closePath()
    }

    override fun draw(canvasRenderingContext2D: CanvasRenderingContext2D) {
        canvasRenderingContext2D.draw()
    }
}

class StreetCurveDrawable(val streetCurve: StreetCurve, val mainComponents: MainComponents):Drawable {
    val x
        get() = streetCurve.x
    val y: Int
        get() = streetCurve.y
    val curve: Curve
        get() = streetCurve.curve
    val fieldSize
        get() = mainComponents.fieldSize

    val greenColor
        get() = mainComponents.greenColor
    val stripeWidth
        get() = mainComponents.stripeWidth

    fun CanvasRenderingContext2D.drawCurve() {
        beginPath()
        val curvePosition = curve.curvePosition
        arc(
            fieldSize * x + curvePosition.x * fieldSize,
            fieldSize * y + curvePosition.y * fieldSize,
            fieldSize - (0.5 * stripeWidth) * fieldSize,
            curvePosition.piStart * kotlin.math.PI,
            curvePosition.piEnd * kotlin.math.PI
        )
        strokeStyle = greenColor
        lineWidth = fieldSize * stripeWidth
        stroke()
//    fill()
        closePath()
    }

    fun CanvasRenderingContext2D.drawInnerCurve() {
        beginPath()
        val curvePosition = curve.curvePosition
        arc(
            fieldSize * x + curvePosition.x * fieldSize,
            fieldSize * y + curvePosition.y * fieldSize,
            (0.5 * stripeWidth) * fieldSize,
            curvePosition.piStart * kotlin.math.PI,
            curvePosition.piEnd * kotlin.math.PI
        )
        strokeStyle = greenColor
        lineWidth = fieldSize * stripeWidth
        stroke()
//    fill()
        closePath()
    }

    fun CanvasRenderingContext2D.drawMiddleCurve() {
        beginPath()
        val curvePosition = curve.curvePosition
        arc(
            fieldSize * x + curvePosition.x * fieldSize,
            fieldSize * y + curvePosition.y * fieldSize,
            fieldSize / 2.0,
            curvePosition.piStart * kotlin.math.PI,
            curvePosition.piEnd * kotlin.math.PI
        )
        strokeStyle = mainComponents.middleStripeColor
        lineWidth = fieldSize * mainComponents.middleStripeWidth
        stroke()
        closePath()
    }

    override fun draw(canvasRenderingContext2D: CanvasRenderingContext2D) {
        canvasRenderingContext2D.drawInnerCurve()
        canvasRenderingContext2D.drawMiddleCurve()
        canvasRenderingContext2D.drawCurve()
    }
}


class StreetDrawable(val street: Street, val mainComponents: MainComponents) :Drawable{
    val x
        get() = street.x
    val y
        get() = street.y
    val streetConfig
        get() = street.streetConfig

    private val stripeWidth
        get() = mainComponents.stripeWidth
    private val middleStripeWidth
        get() = mainComponents.middleStripeWidth
    private val fieldSize
        get() = mainComponents.fieldSize
    private val mainStripe = (1.0 - stripeWidth * 2.0 - middleStripeWidth) / 2.0
    private val distance = mainStripe + stripeWidth
    private val direction = if (streetConfig.isHorizontal) HorizontalStreet() else VerticalStreet()

    private abstract inner class Direction {
        abstract val xOffSet: Double
        abstract val yOffSet: Double
        abstract val xMultiplier: Double
        abstract val yMultiplier: Double

        abstract fun middleX(sLength: Double): Double
        abstract fun middleY(sLength: Double): Double
        abstract val xMiddleStripeOffSet: Double
        abstract val yMiddleStripeOffSet: Double

        fun middleStripeRect(sLength: Double, canvasRenderingContext2D: CanvasRenderingContext2D) {
            canvasRenderingContext2D.rect(middleX(sLength), middleY(sLength), xMiddleStripeOffSet, yMiddleStripeOffSet)
        }

        private fun CanvasRenderingContext2D.drawSideStripe() {
            beginPath()

            if (!streetConfig.firstOpen)
                rect(x * fieldSize, y * fieldSize, xMultiplier * fieldSize, yMultiplier * fieldSize)
            if (!streetConfig.secondOpen)
                rect(x * fieldSize + xOffSet, y * fieldSize + yOffSet, xMultiplier * fieldSize, yMultiplier * fieldSize)

            fillStyle = mainComponents.greenColor
            fill()
            closePath()
        }


        fun CanvasRenderingContext2D.drawInnerCurve(curve: Curve) {
            beginPath()
            val curvePosition = curve.curvePosition
            arc(
                fieldSize * x + curvePosition.x * fieldSize,
                fieldSize * y + curvePosition.y * fieldSize,
                (0.5 * stripeWidth) * fieldSize,
                curvePosition.piStart * kotlin.math.PI,
                curvePosition.piEnd * kotlin.math.PI
            )
            strokeStyle = mainComponents.greenColor
            lineWidth = fieldSize * stripeWidth
            stroke()
//    fill()
            closePath()
        }

        abstract fun drawInnerCurves(canvasRenderingContext2D: CanvasRenderingContext2D)

        fun drawSideStripe(canvasRenderingContext2D: CanvasRenderingContext2D) {
            drawInnerCurves(canvasRenderingContext2D)
            canvasRenderingContext2D.drawSideStripe()
        }
    }

    private inner class VerticalStreet : Direction() {
        override val xOffSet = fieldSize - stripeWidth * fieldSize
        override val yOffSet = 0.0
        override val xMultiplier = stripeWidth
        override val yMultiplier = 1.0
        override fun middleX(sLength: Double) = x * fieldSize + distance * fieldSize
        override fun middleY(sLength: Double) = y * fieldSize + sLength * fieldSize
        override val xMiddleStripeOffSet: Double = middleStripeWidth * fieldSize
        override val yMiddleStripeOffSet: Double = fieldSize * mainComponents.middleStripeLength
        override fun drawInnerCurves(canvasRenderingContext2D: CanvasRenderingContext2D) {
            if (streetConfig.firstOpen) {
                canvasRenderingContext2D.drawInnerCurve(Curve.DownToLeft)
                canvasRenderingContext2D.drawInnerCurve(Curve.UpToLeft)
            }
            if (streetConfig.secondOpen) {
                canvasRenderingContext2D.drawInnerCurve(Curve.DownToRight)
                canvasRenderingContext2D.drawInnerCurve(Curve.UpToRight)
            }
        }
    }

    private inner class HorizontalStreet : Direction() {
        override val xOffSet = 0.0
        override val yOffSet = fieldSize - stripeWidth * fieldSize
        override val xMultiplier = 1.0
        override val yMultiplier = stripeWidth
        override fun middleX(sLength: Double) = x * fieldSize + sLength * fieldSize
        override fun middleY(sLength: Double) = y * fieldSize + distance * fieldSize
        override val xMiddleStripeOffSet: Double = fieldSize * mainComponents.middleStripeLength
        override val yMiddleStripeOffSet: Double = middleStripeWidth * fieldSize
        override fun drawInnerCurves(canvasRenderingContext2D: CanvasRenderingContext2D) {
            if (streetConfig.firstOpen) {
                canvasRenderingContext2D.drawInnerCurve(Curve.DownToLeft)
                canvasRenderingContext2D.drawInnerCurve(Curve.DownToRight)
            }
            if (streetConfig.secondOpen) {
                canvasRenderingContext2D.drawInnerCurve(Curve.UpToLeft)
                canvasRenderingContext2D.drawInnerCurve(Curve.UpToRight)
            }
        }
    }


    fun CanvasRenderingContext2D.middleStripeRect(length: Double) {
        this@StreetDrawable.direction.middleStripeRect(length, this)
    }

    private fun CanvasRenderingContext2D.drawMiddleStripe() {
        beginPath()
        middleStripeRect(0.0)
        middleStripeRect(0.25)
        middleStripeRect(0.5)
        middleStripeRect(0.75)
        fillStyle = mainComponents.middleStripeColor
        fill()
        closePath()
    }

    private fun CanvasRenderingContext2D.drawStreet() {
        this@StreetDrawable.direction.drawSideStripe(this)
        drawMiddleStripe()
    }

    override fun draw(ctx: CanvasRenderingContext2D) {
        ctx.drawStreet()
    }
}

