package de.tmjh.stroemwerk.flow;

/**
 * Minimaler Lesezugriff auf die Welt, den {@link FlowNetwork} braucht.
 *
 * <p>Im Spiel liefert der Adapter aus dem {@code platform}-Paket diese Daten,
 * in Tests eine einfache Map. Dadurch bleibt die Stroemungsberechnung frei von
 * Server-Abhaengigkeiten.
 */
public interface WorldView {

    /**
     * Welches Wasserbahn-Bauteil steht an dieser Position.
     */
    NodeType typeAt(BlockPos pos);

    /**
     * Richtung, in die eine Pumpe drueckt. Nur fuer {@link NodeType#PUMP} definiert.
     */
    Direction pumpFacing(BlockPos pos);

    /**
     * Ob eine Schleuse offen steht. Nur fuer {@link NodeType#GATE} definiert.
     *
     * <p>Standardmaessig offen, damit einfache Implementierungen ohne
     * Schleusen nichts ueberschreiben muessen.
     */
    default boolean isGateOpen(BlockPos pos) {
        return true;
    }

    /**
     * Ob an dieser Position Stroemung weitergeleitet wird - also ein Kanal
     * liegt oder eine offene Schleuse.
     */
    default boolean conducts(BlockPos pos) {
        return switch (typeAt(pos)) {
            case CHANNEL -> true;
            case GATE -> isGateOpen(pos);
            case NONE, PUMP, WHEEL -> false;
        };
    }

    /**
     * Zaehlt die Wasserraeder, die direkt an dieser Pumpe anliegen.
     */
    default int wheelsAround(BlockPos pump) {
        int count = 0;
        for (Direction direction : Direction.values()) {
            if (typeAt(pump.offset(direction)) == NodeType.WHEEL) {
                count++;
            }
        }
        return count;
    }

    /**
     * Richtung, in die eine Pumpe tatsaechlich drueckt.
     *
     * <p>Ist keine Ausrichtung hinterlegt, ergibt sie sich aus der Nachbarschaft:
     * eine Pumpe drueckt in den Kanal, an dem sie haengt. Damit funktioniert sie
     * auch dann, wenn die Blockrotation nicht ausgelesen werden kann - man baut
     * einfach Pumpe und Kanal nebeneinander.
     *
     * <p>Haengen mehrere Kanaele an, gewinnt die erste Richtung in der
     * Reihenfolge von {@link Direction}. Das ist beliebig, aber vorhersagbar;
     * wer es genau steuern will, setzt die Ausrichtung ausdruecklich.
     */
    default Direction effectivePumpFacing(BlockPos pos) {
        Direction explicit = pumpFacing(pos);
        if (explicit != null) {
            return explicit;
        }
        for (Direction candidate : Direction.values()) {
            if (conducts(pos.offset(candidate))) {
                return candidate;
            }
        }
        return null;
    }
}
