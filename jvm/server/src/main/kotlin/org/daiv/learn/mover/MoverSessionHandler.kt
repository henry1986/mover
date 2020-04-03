package org.daiv.learn.mover

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.KSerializer
import kotlinx.serialization.modules.SerialModule
import mu.KotlinLogging
import org.daiv.websocket.ControlledChannel
import org.daiv.websocket.MessageReceiver
import org.daiv.websocket.MessageSender

class MoverSessionHandler(override val controlledChannel: ControlledChannel) : MessageReceiver<MoverCommunication>,
    MessageSender, CarProxyCom {

    override val context: SerialModule = moverModule

    val logger = KotlinLogging.logger {}
    val carProxy: CarProxy

    init {
        val list = listOf(
            StreetCurve(1, 1, Curve.UpToRight),
            Street(2, 1, StreetConfig.StraightHorizontal),
            Street(3, 1, StreetConfig.StraightHorizontal),
            Street(4, 1, StreetConfig.StraightHorizontalDown),
            Street(5, 1, StreetConfig.StraightHorizontal),
            Street(6, 1, StreetConfig.StraightHorizontal),
            Street(7, 1, StreetConfig.StraightHorizontal),
            StreetCurve(8, 1, Curve.UpToLeft),

            Street(1, 2),
            GreenField(2, 2),
            GreenField(3, 2),
            Street(4, 2),
            GreenField(5, 2),
            GreenField(6, 2),
            GreenField(7, 2),
            Street(8, 2),

            Street(1, 3),
            GreenField(2, 3),
            GreenField(3, 3),
            Street(4, 3),
            GreenField(5, 3),
            GreenField(6, 3),
            GreenField(7, 3),
            Street(8, 3),

            Street(1, 4),
            GreenField(2, 4),
            StreetCurve(3, 4, Curve.UpToRight),
            Street(4, 4, StreetConfig.Cross),
            Street(5, 4, StreetConfig.StraightHorizontal),
            Street(6, 4, StreetConfig.StraightHorizontal),
            Street(7, 4, StreetConfig.StraightHorizontal),
            Street(8, 4, StreetConfig.StraightVerticalLeft),

            Street(1, 5, StreetConfig.StraightVerticalRight),
            Street(2, 5, StreetConfig.StraightHorizontal),
            StreetCurve(3, 5, Curve.DownToLeft),
            Street(4, 5),
            GreenField(5, 5),
            GreenField(6, 5),
            GreenField(7, 5),
            Street(8, 5),

            Street(1, 6),
            GreenField(2, 6),
            GreenField(3, 6),
            Street(4, 6),
            GreenField(5, 6),
            GreenField(6, 6),
            GreenField(7, 6),
            Street(8, 6),

            StreetCurve(1, 7, Curve.DownToRight),
            Street(2, 7, StreetConfig.StraightHorizontal),
            Street(3, 7, StreetConfig.StraightHorizontal),
            Street(4, 7, StreetConfig.StraightHorizontalUp),
            Street(5, 7, StreetConfig.StraightHorizontal),
            Street(6, 7, StreetConfig.StraightHorizontal),
            Street(7, 7, StreetConfig.StraightHorizontal),
            StreetCurve(8, 7, Curve.DownToLeft)
        )
        val streetMap = StreetMap(list.map { it.toStreetCoordinate() to it }.toMap())
        val main = MainComponents(100.0, 50.0, "#2b541f", "white", 0.1, 0.05, 0.1)
        carProxy = CarProxy(this, streetMap)
        toFrontend(InitField.serializer(), InitField(main, streetMap))
        GlobalScope.launch {
            MovingStrategy(carProxy).run()
//            InnerTestStrategy(carProxy).run()
        }
    }

    private var response: () -> Unit = {}

    override fun <T : MoverCommunication> send(serializer: KSerializer<T>, t: T, response: () -> Unit) {
        this.response = response
        toFrontend(serializer, t)
    }

    override suspend fun onMessage(event: MoverCommunication) {
        when (event) {
            is MovementResponse -> {
                response()
            }
        }
    }
}