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
}
