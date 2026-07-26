# Strömwerk

Ein Hytale-Mod über Transport mit Wasserkraft — in der Richtung von *Create*,
aber statt Zahnrädern und Wellen läuft alles über Strömung.

Erstes Feature: die **Wasserbahn**. Eine Pumpe drückt Wasser in einen Kanal,
die Strömung breitet sich über die verbundene Strecke aus und trägt
Gegenstände mit.

## Spielregeln der Wasserbahn

- Die **Wasserpumpe** erzeugt Strömung in ihre Blickrichtung. Vor ihr muss ein
  Kanal liegen, sonst passiert nichts.
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

`/wasserbahn` zeigt an, was das Netz gerade berechnet hat — Anzahl Pumpen,
fließende Kanäle und blockierte Stellen. Bei langen Strecken ist das die
schnellste Art herauszufinden, warum irgendwo nichts läuft.

## Bauen

```bash
./gradlew build          # baut und testet
./gradlew test           # nur Tests
```

Gebraucht wird ein JDK 25 (Hytale-Plugins laufen darauf). Die Jar landet unter
`build/libs/`.

Für den vollständigen Build gehört `HytaleServer.jar` nach `libraries/`,
siehe [libraries/README.md](libraries/README.md). **Ohne** die Jar baut und
testet das Projekt trotzdem — dann bleibt nur das Paket `hytale` außen vor.

Installiert wird die fertige Jar im `mods/`-Verzeichnis des Servers.

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
damit ohne laufenden Server testbar. Deshalb gibt es 28 Tests, die in
Sekunden durchlaufen, statt jede Änderung im Spiel nachstellen zu müssen.

Der Preis dafür ist eine Übersetzungsschicht in `hytale`. Die ist klein und
genau der Teil, den man anfassen muss, wenn sich die Server-API ändert.

## Offene Anbindung

Der Mod ist gegen die öffentlich dokumentierte Server-API geschrieben, aber
noch nicht gegen eine echte `HytaleServer.jar` kompiliert. Drei Stellen sind
deshalb noch nicht angeschlossen und im Code mit `ANPASSEN` markiert:

1. **Block-IDs auflösen** (`StroemwerkPlugin.setup()`) — die IDs von
   Wasserkanal und Wasserpumpe müssen aus der Item-Registry kommen, aktuell
   stehen dort Platzhalter.
2. **Blockrotation lesen** (`BlockFacing`) — bis klar ist, wie Hytale die
   Ausrichtung eines platzierten Blocks ablegt, merkt sich das Plugin die
   Blickrichtung beim Platzieren selbst. Das überlebt keinen Serverneustart.
3. **Bau-Events und Item-Transport** — `StroemwerkRuntime.onBlockChanged` und
   `velocityAt` sind fertig und getestet, es fehlt nur die Verdrahtung an die
   Block-Events und an die Item-Entities des Servers.

Ebenfalls noch offen: Die Blöcke benutzen Platzhaltertexturen
(`tools/make_textures.py`) und einfache Würfelmodelle. Ein Kanal sollte ein
Trog-Modell bekommen. Die Kreativ-Kategorie `Blocks.Rocks` ist geliehen und
gehört auf eine eigene umgestellt.

## Als Nächstes

- Trichter, der Gegenstände aus einer Kiste in den Kanal wirft
- Abscheider am Streckenende, der wieder in eine Kiste einsortiert
- Schleuse als schaltbarer Kanal, damit sich Strecken umleiten lassen
- Wasserrad als Antrieb für die Pumpe, statt sie einfach laufen zu lassen
- Konfigurierbare Reichweite und Geschwindigkeit über die Plugin-Config
