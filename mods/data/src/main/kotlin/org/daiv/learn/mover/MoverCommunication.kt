package org.daiv.learn.mover

import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import org.daiv.websocket.WSEvent

interface MoverCommunication : WSEvent

enum class StreetConfig(
    val isHorizontal: Boolean,
    val firstOpen: Boolean,
    val secondOpen: Boolean
) : OpenDescription {
    StraightHorizontal(true, false, false),
    StraightVertical(false, false, false),
    StraightHorizontalDown(true, false, true),
    StraightHorizontalUp(true, true, false),
    StraightVerticalRight(false, false, true),
    StraightVerticalLeft(false, true, false),
    Cross(true, true, true);

    val isStraight = !firstOpen && !secondOpen

    override val isNorthOpen: Boolean = !isHorizontal || (isHorizontal && firstOpen)
    override val isSouthOpen: Boolean = !isHorizontal || (isHorizontal && secondOpen)
    override val isLeftOpen: Boolean = isHorizontal || (!isHorizontal && firstOpen)
    override val isRightOpen: Boolean = isHorizontal || (!isHorizontal && secondOpen)

}

enum class CarDirection {
    North, East, South, West;

    fun move(goDirection: GoDirection): MovementCoordinates {
        return when (goDirection) {
            GoDirection.Forward -> when (this) {
                North -> MovementCoordinates(0, -1, this)
                East -> MovementCoordinates(1, 0, this)
                South -> MovementCoordinates(0, 1, this)
                West -> MovementCoordinates(-1, 0, this)
            }

            GoDirection.Left -> when (this) {
                North -> MovementCoordinates(-1, 0, West)
                East -> MovementCoordinates(0, -1, North)
                South -> MovementCoordinates(1, 0, East)
                West -> MovementCoordinates(0, 1, South)
            }

            GoDirection.Right -> when (this) {
                North -> MovementCoordinates(1, 0, East)
                East -> MovementCoordinates(0, 1, South)
                South -> MovementCoordinates(-1, 0, West)
                West -> MovementCoordinates(0, -1, North)
            }
        }
    }
}

data class MovementCoordinates(val x: Int, val y: Int, val carDirection: CarDirection)


enum class GoDirection {
    Forward, Left, Right;
}

@Serializable
data class Move(val carPosition: CarPosition, val goDirection: GoDirection, val velocity: Double) : MoverCommunication

@Serializable
class MovementResponse : MoverCommunication

@Serializable
data class MoverMessage(val message: String, val color: String) : MoverCommunication

@Serializable
data class CarPosition(override val x: Int, override val y: Int, val carDirection: CarDirection) : Coordinateable {
    private fun move(movementCoordinates: MovementCoordinates) =
        copy(x + movementCoordinates.x, y + movementCoordinates.y, movementCoordinates.carDirection)

    fun move(goDirection: GoDirection): CarPosition = move(carDirection.move(goDirection))

//    fun forward(): CarPosition {
//        move(carDirection.move(GoDirection.Forward))
//        return when (carDirection) {
//            CarDirection.North -> copy(x + 1)
//            CarDirection.East -> copy(y = y + 1)
//            CarDirection.South -> copy(x - 1)
//            CarDirection.West -> copy(y = y - 1)
//        }
//    }
//
//    fun goLeft(): CarPosition {
//        return when (carDirection) {
//            CarDirection.North -> copy(x - 1, y - 1, CarDirection.West)
//            CarDirection.East -> copy(x + 1, y - 1, CarDirection.North)
//            CarDirection.South -> copy(x + 1, y + 1, CarDirection.East)
//            CarDirection.West -> copy(x - 1, y + 1, CarDirection.South)
//        }
//    }
//
//    fun goRight(): CarPosition {
//        return when (carDirection) {
//            CarDirection.North -> copy(x + 1, y - 1, CarDirection.East)
//            CarDirection.East -> copy(x + 1, y + 1, CarDirection.South)
//            CarDirection.South -> copy(x - 1, y + 1, CarDirection.West)
//            CarDirection.West -> copy(x - 1, y - 1, CarDirection.North)
//        }
//    }
}

data class CurvePosition(val x: Int, val y: Int, val piStart: Double, val piEnd: Double)

interface OpenDescription {
    val isNorthOpen: Boolean
    val isSouthOpen: Boolean
    val isRightOpen: Boolean
    val isLeftOpen: Boolean
}

fun StreetElement.isTransitionPossible(after: StreetElement): Boolean {
    val transition = transition(after)
    return when (transition) {
        Transition.NorthSouth -> this.isSouthOpen && after.isNorthOpen
        Transition.EastWest -> this.isLeftOpen && after.isRightOpen
        Transition.SouthNorth -> this.isNorthOpen && after.isSouthOpen
        Transition.WestEast -> this.isRightOpen && after.isLeftOpen
    }
}


enum class Curve(
    val curvePosition: CurvePosition,
    override val isNorthOpen: Boolean,
    override val isSouthOpen: Boolean,
    override val isRightOpen: Boolean,
    override val isLeftOpen: Boolean
) : OpenDescription {
    DownToRight(CurvePosition(1, 0, 0.5, 1.0), true, false, true, false),
    DownToLeft(CurvePosition(0, 0, 0.0, 0.5), true, false, false, true),
    UpToRight(CurvePosition(1, 1, 1.0, 1.5), false, true, true, false),
    UpToLeft(CurvePosition(0, 1, 1.5, 2.0), false, true, false, true)
}

interface Coordinateable {
    val x: Int
    val y: Int
    fun toStreetCoordinate() = StreetCoordinate(x, y)
}

interface StreetElement : Coordinateable, OpenDescription


@Serializable
data class Street(
    override val x: Int,
    override val y: Int,
    val streetConfig: StreetConfig = StreetConfig.StraightVertical
) : StreetElement, OpenDescription by streetConfig

@Serializable
data class StreetCurve(override val x: Int, override val y: Int, val curve: Curve) : StreetElement,
    OpenDescription by curve

@Serializable
data class GreenField(override val x: Int, override val y: Int) : StreetElement {
    override val isNorthOpen: Boolean = false
    override val isSouthOpen: Boolean = false
    override val isLeftOpen: Boolean = false
    override val isRightOpen: Boolean = false
}

@Serializable
data class InitField(val mainComponents: MainComponents, val streetMap: StreetMap) : MoverCommunication

@Serializable
data class StreetCoordinate(override val x: Int, override val y: Int) : Coordinateable

@Serializable
data class StreetMap(val map: Map<StreetCoordinate, StreetElement>) {
    operator fun get(streetCoordinate: StreetCoordinate): StreetElement = map[streetCoordinate]!!
    operator fun get(carPosition: CarPosition): StreetElement? = map[carPosition.toStreetCoordinate()]
}

val moverModule = SerializersModule {
    polymorphic(StreetElement::class) {
        Street::class with Street.serializer()
        StreetCurve::class with StreetCurve.serializer()
        GreenField::class with GreenField.serializer()
    }
}
