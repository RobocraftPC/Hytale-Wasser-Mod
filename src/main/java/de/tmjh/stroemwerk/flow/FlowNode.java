package de.tmjh.stroemwerk.flow;

/**
 * Ergebnis der Stroemungsberechnung fuer einen einzelnen Kanalblock.
 *
 * @param direction Richtung, in die Gegenstaende geschoben werden
 * @param strength  Restdruck; faellt pro Block um 1 und bestimmt bei
 *                  konkurrierenden Pumpen, welche gewinnt
 * @param source    Position der Pumpe, die diesen Block speist
 */
public record FlowNode(Direction direction, int strength, BlockPos source) {

    /**
     * Geschwindigkeit in Bloecken pro Sekunde, mit der Gegenstaende
     * transportiert werden. Voller Druck ist am schnellsten, am Ende der
     * Reichweite trudelt es aus.
     */
    public double speed(int maxStrength, double maxSpeed, double minSpeed) {
        if (maxStrength <= 1) {
            return maxSpeed;
        }
        // Wasserraeder koennen den Druck ueber den Grundwert heben. Das soll
        // die Reichweite verlaengern, nicht das Tempo ins Unermessliche treiben.
        double ratio = Math.min(1.0, (double) (strength - 1) / (double) (maxStrength - 1));
        return minSpeed + (maxSpeed - minSpeed) * ratio;
    }
}
