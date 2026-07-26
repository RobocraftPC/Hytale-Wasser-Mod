package de.tmjh.stroemwerk.flow;

/**
 * Was an einer Position aus Sicht der Wasserbahn steht.
 */
public enum NodeType {
    /** Kein Bauteil der Wasserbahn. */
    NONE,
    /** Wasserkanal - leitet Stroemung weiter und traegt Gegenstaende. */
    CHANNEL,
    /** Wasserpumpe - erzeugt Stroemung in Blickrichtung. */
    PUMP
}
