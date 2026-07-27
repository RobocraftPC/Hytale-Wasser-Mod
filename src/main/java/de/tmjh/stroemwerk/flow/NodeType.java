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
    PUMP,
    /** Schleuse - Kanal, der sich schliessen laesst und dann sperrt. */
    GATE,
    /** Wasserrad - treibt eine angrenzende Pumpe an und erhoeht deren Druck. */
    WHEEL
}
