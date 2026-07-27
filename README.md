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
- Kanäle dürfen senkrecht laufen, aber **Höhe kostet**: bergauf zwei Stufen je
  Block, bergab keine. Ein Fallschacht ist also gratis und speist die Strecke
  darunter mit vollem Druck — bergauf reicht dieselbe Pumpe nur halb so weit.
- Treffen zwei Pumpen aufeinander, gewinnt der **höhere Restdruck**. Bei
  Gleichstand blockieren sie sich und der Kanal steht still.
- Je weiter weg von der Pumpe, desto **langsamer** der Transport: von 4 Blöcken
  pro Sekunde bei vollem Druck auf 1 am Ende der Reichweite.
- Gegenstände werden zur Kanalmitte gezogen, damit sie nicht an den Wänden
  hängenbleiben.

### Wasserrad

Das **Wasserrad** treibt eine Pumpe an, an der es direkt anliegt — egal an
welcher der sechs Seiten. Jedes Rad verlängert die Reichweite dieser Pumpe um
16 Blöcke, höchstens zwei Räder zählen. Aus 32 Blöcken werden so bis zu 64.

Mehr Druck macht **nicht schneller**, nur weiter: das Tempo bleibt bei
4 Blöcken pro Sekunde gedeckelt. Ein Rad ist selbst kein Kanal — Strömung
fließt nicht hindurch.

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

Ein **Rechtsklick** auf eine Schleuse legt sie um.

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

### Automatisch in den Server kopieren

Damit nach dem Bauen nur noch der Serverneustart bleibt, trägt man den
Mods-Ordner in eine `.env` ein:

```bash
cp .env.example .env
```

```
HYTALE_MODS_DIR=C:\Users\<Name>\AppData\Roaming\Hytale\UserData\Mods
```

`./gradlew build` legt die Jar dann dort ab und räumt vorher die alten
`Stroemwerk-*.jar` weg. **Andere Mods im selben Ordner bleiben unberührt** —
gelöscht wird nur, was mit `Stroemwerk-` beginnt.

Die `.env` ist nicht eingecheckt. Ohne sie überspringt sich der Schritt mit
einem Hinweis, bauen und testen geht also auch ohne. Alternativ tut es die
Umgebungsvariable `HYTALE_MODS_DIR`. Zeigt der Pfad ins Leere, bricht der
Build ab, statt den Tippfehler stillschweigend zu schlucken.

Einzeln aufrufen lässt sich der Schritt mit `./gradlew deployMod`.

Von Hand liegt der Ordner unter:
`C:\Users\<Name>\AppData\Roaming\Hytale\UserData\Mods`

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
damit ohne laufenden Server testbar. Deshalb gibt es 60 Tests, die in
Sekunden durchlaufen, statt jede Änderung im Spiel nachstellen zu müssen.

Der Preis dafür ist eine Übersetzungsschicht in `hytale`. Die ist klein und
genau der Teil, den man anfassen muss, wenn sich die Server-API ändert.

## Offene Anbindung

Alles ist angeschlossen: Block-IDs, Bau-Events, Schleusen-Rechtsklick und der
Item-Transport. Was jetzt noch aussteht, ist **Erprobung im Spiel** — dass es
kompiliert, heißt nicht, dass es sich gut anfühlt.

Worauf beim ersten Test zu achten wäre:

- Werden Gegenstände sauber getragen, oder zappeln sie? `ItemFlowSystem` setzt
  die Geschwindigkeit hart; ein sanfteres Angleichen könnte ruhiger aussehen.
- Wie verhält sich das Zusammenspiel mit der Schwerkraft an Steigungen?
- `ItemFlowSystem` läuft bewusst nicht parallel, weil die Netzabfrage nicht
  threadsicher ist. Bei vielen Gegenständen wäre zu prüfen, ob das reicht.

Offen bleibt das Auslesen der **Blockrotation** (`BlockFacing`) — nicht
dringend, seit die Pumpe sich ihren Kanal selbst sucht. Mit auslesbarer
Rotation ließe sich die Richtung genauer steuern.

Ebenfalls noch offen: Die Blöcke benutzen Platzhaltertexturen
(`tools/make_textures.py`) und einfache Würfelmodelle. Ein Kanal sollte ein
Trog-Modell bekommen. Die Kreativ-Kategorie `Blocks.Rocks` ist geliehen und
gehört auf eine eigene umgestellt.

## Als Nächstes

- Trichter, der Gegenstände aus einer Kiste in den Kanal wirft
- Abscheider am Streckenende, der wieder in eine Kiste einsortiert
- Konfigurierbare Reichweite und Geschwindigkeit über die Plugin-Config
- Schleusen dauerhaft speichern, damit sie einen Serverneustart überstehen
