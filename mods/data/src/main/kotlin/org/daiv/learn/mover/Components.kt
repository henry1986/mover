package org.daiv.learn.mover

import kotlinx.serialization.Serializable

@Serializable
data class MainComponents(
    val fieldSize: Double,
    val fieldSizeHalf: Double,
    val greenColor: String,
    val middleStripeColor: String,
    val stripeWidth: Double,
    val middleStripeWidth: Double,
    val middleStripeLength: Double
)

enum class Transition {
    NorthSouth, EastWest, SouthNorth, WestEast
}

fun StreetElement.transition(after: StreetElement): Transition {
    val transition = if (this.x == after.x) {
        if (this.y > after.y) {
            Transition.SouthNorth
        } else {
            Transition.NorthSouth
        }
    } else {
        if (this.x > after.x) {
            Transition.EastWest
        } else {
            Transition.WestEast
        }
    }
    return transition
}
