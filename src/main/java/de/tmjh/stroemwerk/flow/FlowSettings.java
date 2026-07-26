package de.tmjh.stroemwerk.flow;

/**
 * Stellschrauben der Wasserbahn. Werden spaeter aus der Plugin-Config geladen.
 *
 * @param maxStrength   Startdruck einer Pumpe und damit ihre Reichweite in Bloecken
 * @param maxSpeed      Transportgeschwindigkeit bei vollem Druck, Bloecke pro Sekunde
 * @param minSpeed      Transportgeschwindigkeit am Ende der Reichweite
 * @param centering     wie stark Gegenstaende zur Kanalmitte gezogen werden
 * @param maxCentering  Deckel fuer die Zentrierung, damit nichts zappelt
 */
public record FlowSettings(int maxStrength, double maxSpeed, double minSpeed,
                           double centering, double maxCentering) {

    public static final FlowSettings DEFAULT =
            new FlowSettings(FlowNetwork.DEFAULT_MAX_STRENGTH, 4.0, 1.0, 4.0, 1.5);

    public FlowSettings {
        if (maxStrength < 1) {
            throw new IllegalArgumentException("maxStrength muss mindestens 1 sein, war " + maxStrength);
        }
        if (minSpeed > maxSpeed) {
            throw new IllegalArgumentException("minSpeed darf nicht groesser als maxSpeed sein");
        }
    }
}
