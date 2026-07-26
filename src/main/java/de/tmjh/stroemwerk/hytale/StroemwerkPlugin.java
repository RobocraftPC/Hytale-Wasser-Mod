package de.tmjh.stroemwerk.hytale;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import de.tmjh.stroemwerk.flow.FlowSettings;
import de.tmjh.stroemwerk.hytale.commands.WasserbahnCommand;
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
        int channelBlockId = -1;
        int pumpBlockId = -1;

        this.runtime = new StroemwerkRuntime(FlowSettings.DEFAULT, channelBlockId, pumpBlockId);

        getCommandRegistry().registerCommand(new WasserbahnCommand(runtime));

        getLogger().at(Level.INFO).log("Stroemwerk geladen - Wasserbahn bereit");
    }

    public StroemwerkRuntime runtime() {
        return runtime;
    }
}
