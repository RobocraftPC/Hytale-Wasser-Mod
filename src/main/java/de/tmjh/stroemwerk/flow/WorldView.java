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
            case NONE, PUMP -> false;
        };
    }
}
