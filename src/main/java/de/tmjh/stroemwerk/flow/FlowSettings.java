package de.tmjh.stroemwerk.flow;

/**
 * Stellschrauben der Wasserbahn. Werden spaeter aus der Plugin-Config geladen.
 *
 * @param maxStrength   Startdruck einer Pumpe und damit ihre Reichweite in Bloecken
 * @param maxSpeed      Transportgeschwindigkeit bei vollem Druck, Bloecke pro Sekunde
 * @param minSpeed      Transportgeschwindigkeit am Ende der Reichweite
 * @param centering     wie stark Gegenstaende zur Kanalmitte gezogen werden
 * @param maxCentering  Deckel fuer die Zentrierung, damit nichts zappelt
 * @param wheelBonus    zusaetzliche Reichweite je Wasserrad an der Pumpe
 * @param maxWheels     wie viele Wasserraeder eine Pumpe hoechstens nutzt
 * @param costUp        Druckverlust je Block nach oben; bergauf kostet Kraft
 * @param costDown      Druckverlust je Block nach unten; bergab traegt die Schwerkraft
 * @param maxNodes      Obergrenze fuer die Groesse eines Netzes, damit eine
 *                      ausufernde Anlage den Server nicht lahmlegt
 */
public record FlowSettings(int maxStrength, double maxSpeed, double minSpeed,
                           double centering, double maxCentering,
                           int wheelBonus, int maxWheels,
                           int costUp, int costDown, int maxNodes) {

    public static final FlowSettings DEFAULT =
            new FlowSettings(FlowNetwork.DEFAULT_MAX_STRENGTH, 4.0, 1.0, 4.0, 1.5, 16, 2,
                    2, 0, 4096);

    /**
     * Dieselben Einstellungen mit anderer Grundreichweite.
     */
    public FlowSettings withMaxStrength(int newMaxStrength) {
        return new FlowSettings(newMaxStrength, maxSpeed, minSpeed, centering, maxCentering,
                wheelBonus, maxWheels, costUp, costDown, maxNodes);
    }

    public FlowSettings {
        if (maxStrength < 1) {
            throw new IllegalArgumentException("maxStrength muss mindestens 1 sein, war " + maxStrength);
        }
        if (minSpeed > maxSpeed) {
            throw new IllegalArgumentException("minSpeed darf nicht groesser als maxSpeed sein");
        }
    }
}
