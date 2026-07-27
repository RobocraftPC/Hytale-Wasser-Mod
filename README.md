# Strömwerk

Ein Hytale-Mod über Transport mit Wasserkraft — in der Richtung von *Create*,
aber statt Zahnrädern und Wellen läuft alles über Strömung.

Erstes Feature: die **Wasserbahn**. Eine Pumpe drückt Wasser in einen Kanal,
die Strömung breitet sich über die verbundene Strecke aus und trägt
Gegenstände mit.

## Spielregeln der Wasserbahn

- Die **Wasserpumpe** drückt in den Kanal, an dem sie hängt. Einfach Pumpe und
  Kanal nebeneinander setzen — eine Ausrichtung muss man nicht einstellen.
  Grenzen mehrere Kanäle an, entscheidet eine feste Reihenfolge
  (Nord, Ost, Süd, West, Oben, Unten).
- Der **Wasserkanal** leitet die Strömung weiter. Pro Block sinkt der Druck um
  eine Stufe, ab Werk reicht eine Pumpe 32 Blöcke weit.
- Kurven und Abzweige funktionieren. An einer Gabelung hat **geradeaus
  Vorrang**; der Abzweig wird trotzdem versorgt, nur eine Stufe schwächer.
- Kanäle dürfen senkrecht laufen — Wasser trägt Gegenstände auch nach oben.
- Treffen zwei Pumpen aufeinander, gewinnt der **höhere Restdruck**. Bei
  Gleichstand blockieren sie sich und der Kanal steht still.
- Je weiter weg von der Pumpe, desto **langsamer** der Transport: von 4 Blöcken
  pro Sekunde bei vollem Druck auf 1 am Ende der Reichweite.
- Gegenstände werden zur Kanalmitte gezogen, damit sie nicht an den Wänden
  hängenbleiben.

### Schleuse

Die **Schleuse** ist ein Kanal, der sich schließen lässt. Offen verhält sie
sich wie ein normaler Kanalstück, geschlossen sperrt sie — die Strecke
dahinter fällt trocken und Gegenstände bleiben davor liegen. Frisch gesetzt
steht sie offen.

Damit lassen sich drei Dinge bauen, die vorher nicht gingen:

- Eine Strecke **abstellen**, ohne Blöcke abzubauen.
- An einer Gabelung **umleiten**: geradeaus hat normalerweise Vorrang, mit
  geschlossener Schleuse nimmt die Strömung den Abzweig.
- Eine **Pattsituation auflösen**, in der zwei gleich starke Pumpen sich
  gegenseitig blockieren.

`/wasserbahn` zeigt an, was das Netz gerade berechnet hat — Anzahl Pumpen,
fließende Kanäle, geschlossene Schleusen und blockierte Stellen. Bei langen
Strecken ist das die schnellste Art herauszufinden, warum irgendwo nichts
läuft.

## Bauen

```bash
./gradlew build          # baut, testet und legt die Jar in out/ ab
./gradlew test           # nur Tests
```

Gebraucht wird ein JDK 25 (Hytale-Plugins laufen darauf). Ist keins
installiert, laedt Gradle sich selbst eins herunter.

Die Jar landet unter `build/libs/` und zusaetzlich durchnummeriert unter
`out/Stroemwerk-<version>-b<nummer>.jar`. Der Zaehler steht in
`out/build-number.txt`; beides ist nicht eingecheckt, die Nummern gelten also
nur auf dem eigenen Rechner. Aus einem roten Build entsteht keine Jar in
`out/`.

Für den vollständigen Build gehört `HytaleServer.jar` nach `libraries/`,
siehe [libraries/README.md](libraries/README.md). **Ohne** die Jar baut und
testet das Projekt trotzdem — dann bleibt nur das Paket `hytale` außen vor.

Installiert wird die fertige Jar im `mods/`-Verzeichnis des Servers.
C:\Users\<Username>\AppData\Roaming\Hytale\UserData\Mods

## Aufbau

Der Mod ist bewusst in zwei Hälften geteilt:

| Paket | Inhalt | Server nötig |
|---|---|---|
| `de.tmjh.stroemwerk.flow` | Strömungsberechnung und Transportphysik | nein |
| `de.tmjh.stroemwerk.platform` | Netzverwaltung, Zwischenspeicher | nein |
| `de.tmjh.stroemwerk.hytale` | Anbindung an die Server-API | ja |

Die eigentliche Mechanik — wie sich Druck ausbreitet, wo Strömungen sich
aufheben, wie schnell ein Gegenstand treibt — hängt an keiner einzigen
Hytale-Klasse. Sie arbeitet gegen das schmale Interface `WorldView` und ist
damit ohne laufenden Server testbar. Deshalb gibt es 47 Tests, die in
Sekunden durchlaufen, statt jede Änderung im Spiel nachstellen zu müssen.

Der Preis dafür ist eine Übersetzungsschicht in `hytale`. Die ist klein und
genau der Teil, den man anfassen muss, wenn sich die Server-API ändert.

## Offene Anbindung

Der Mod kompiliert gegen eine echte `HytaleServer.jar`, lädt im Spiel und
meldet sich im Log. Block-IDs und Bau-Events sind inzwischen angeschlossen —
Bauen und Abbauen aktualisiert das Strömungsnetz, `/wasserbahn` zeigt echte
Zahlen. Was noch fehlt:

1. **Item-Transport** — `StroemwerkRuntime.velocityAt` liefert die fertige,
   getestete Geschwindigkeit für jeden Punkt im Kanal. Es fehlt das
   Tick-System, das die Gegenstände im Kanal einsammelt und diese
   Geschwindigkeit auf sie anwendet. Bis dahin fließt Wasser, aber nichts
   fährt darin.
2. **Schleuse schalten** — `StroemwerkRuntime.toggleGate` ist fertig, es fehlt
   die Anbindung an `UseBlockEvent.Pre`, damit ein Rechtsklick sie umlegt.
3. **Blockrotation lesen** (`BlockFacing`) — nicht mehr dringend, seit die
   Pumpe sich ihren Kanal selbst sucht. Mit auslesbarer Rotation ließe sich
   die Richtung genauer steuern.

Einige Importe im Paket `hytale` sind mit `PRUEFEN` markiert: sie stammen aus
der API-Referenz, nicht aus einem Build gegen die Server-Jar. Stimmt ein Paket
nicht, lässt sich der Import in der IDE auffüllen — an der Logik ändert das
nichts.

Ebenfalls noch offen: Die Blöcke benutzen Platzhaltertexturen
(`tools/make_textures.py`) und einfache Würfelmodelle. Ein Kanal sollte ein
Trog-Modell bekommen. Die Kreativ-Kategorie `Blocks.Rocks` ist geliehen und
gehört auf eine eigene umgestellt.

## Als Nächstes

- Trichter, der Gegenstände aus einer Kiste in den Kanal wirft
- Abscheider am Streckenende, der wieder in eine Kiste einsortiert
- Wasserrad als Antrieb für die Pumpe, statt sie einfach laufen zu lassen
- Konfigurierbare Reichweite und Geschwindigkeit über die Plugin-Config
- Schleusen dauerhaft speichern, damit sie einen Serverneustart überstehen
