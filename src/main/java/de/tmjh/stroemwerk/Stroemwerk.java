package de.tmjh.stroemwerk;

/**
 * Feste Kennungen des Mods. Muessen zu den Dateinamen unter
 * {@code src/main/resources/Server/Item/Items/} passen.
 */
public final class Stroemwerk {

    public static final String MOD_ID = "stroemwerk";

    /** Wasserkanal - traegt Gegenstaende. */
    public static final String CHANNEL_ITEM_ID = "Stroemwerk_Wasserkanal";

    /** Wasserpumpe - erzeugt Stroemung in Blickrichtung. */
    public static final String PUMP_ITEM_ID = "Stroemwerk_Wasserpumpe";

    /** Schleuse - Kanal, der sich schliessen laesst. */
    public static final String GATE_ITEM_ID = "Stroemwerk_Schleuse";

    private Stroemwerk() {
    }
}
