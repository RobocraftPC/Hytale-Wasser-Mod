# Server-Bibliothek

Hier gehoert `HytaleServer.jar` hin. Die Datei wird **nicht** eingecheckt -
sie kommt aus der eigenen Hytale-Installation.

Zu finden ist sie im Installationsverzeichnis des Servers, unter Windows
typischerweise:

    %APPDATA%\Hytale\Server\HytaleServer.jar

Kopie hierher legen:

    libraries/HytaleServer.jar

Solange sie fehlt, ueberspringt der Build das Paket
`de.tmjh.stroemwerk.hytale` und baut nur die Stroemungslogik samt Tests.
`./gradlew test` laeuft also auch ohne installiertes Hytale.
