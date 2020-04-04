package org.daiv.learn.mover

class MovingStrategy2(override val carProxy: CarProxy) : MovingInterface {

    fun isThisFieldForwardOnly(): Boolean {
        return carProxy.isThisFieldForwardOnly()
    }

    fun forward(number: Int) {
        var i = 0
        while (i < number) {
            forward()
            i = i + 1
        }
    }

    fun forwardTillEnd() {

    }

    /**
     * Aufgabe: Die [forwardTillEnd] Funktion soll so funktionieren, dass das Auto bis zur nächsten Kurve oder Kreuzung fährt.
     *
     * Die run Methode ist bereits vollständig und korrekt implementiert, hier musst Du nichts mehr ändern.
     *
     * Was Du jetzt machen musst, ist die [forwardTillEnd] Funktion entsprechend zu implementieren.
     *
     * Als wichtige Hilfe hast Du hierbei die [isThisFieldForwardOnly] Methode.
     *
     * Diese Methode gibt Dir dabei an, ob Du gerade auf einer Geraden stehst (true) oder nicht (false). Wenn Du also auf
     *
     * Kurve oder einer Kreuzung stehst, gibt diese Methode false wieder.
     *
     */
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