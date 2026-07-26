package de.tmjh.stroemwerk.hytale;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import de.tmjh.stroemwerk.flow.FlowSettings;
import de.tmjh.stroemwerk.hytale.commands.WasserbahnCommand;
import de.tmjh.stroemwerk.platform.BlockIds;
import java.util.logging.Level;
import javax.annotation.Nonnull;

/**
 * Einstiegspunkt des Mods.
 *
 * <p>Erstes Feature ist die Wasserbahn: Pumpen erzeugen Stroemung, Kanaele
 * leiten sie weiter und tragen Gegenstaende mit.
 */
public class StroemwerkPlugin extends JavaPlugin {

    private StroemwerkRuntime runtime;

    public StroemwerkPlugin(@Nonnull JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        // ANPASSEN: Block-IDs muessen aus der Item-Registry aufgeloest werden,
        // sobald der Aufruf gegen die echte Server-Jar feststeht. Siehe README,
        // Abschnitt "Offene Anbindung".
        BlockIds blockIds = BlockIds.NONE_RESOLVED;

        this.runtime = new StroemwerkRuntime(FlowSettings.DEFAULT, blockIds);

        getCommandRegistry().registerCommand(new WasserbahnCommand(runtime));

        if (blockIds.anyResolved()) {
            getLogger().at(Level.INFO).log("Stroemwerk geladen - Wasserbahn bereit");
        } else {
            // Sonst sucht man im Spiel lange nach dem Grund, warum nichts fliesst.
            getLogger().at(Level.WARNING).log(
                    "Stroemwerk geladen, aber keine Block-IDs aufgeloest - die Wasserbahn bleibt untaetig.");
        }
    }

    public StroemwerkRuntime runtime() {
        return runtime;
    }
}
