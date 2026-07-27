package de.tmjh.stroemwerk.hytale;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import de.tmjh.stroemwerk.flow.FlowSettings;
import de.tmjh.stroemwerk.hytale.commands.WasserbahnCommand;
import de.tmjh.stroemwerk.hytale.systems.BlockChangeSystems;
import de.tmjh.stroemwerk.hytale.systems.ItemFlowSystem;
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
        // Nicht hier aufloesen: die Asset-Packs sind zu diesem Zeitpunkt noch
        // nicht sicher geladen. BlockIdLookup holt die IDs beim ersten Bedarf
        // nach und versucht es erneut, solange etwas fehlt.
        this.runtime = new StroemwerkRuntime(FlowSettings.DEFAULT, new BlockIdLookup());

        getCommandRegistry().registerCommand(new WasserbahnCommand(runtime));

        getEntityStoreRegistry().registerSystem(new BlockChangeSystems.Place(runtime));
        getEntityStoreRegistry().registerSystem(new BlockChangeSystems.Break(runtime));
        getEntityStoreRegistry().registerSystem(new BlockChangeSystems.Use(runtime));
        getEntityStoreRegistry().registerSystem(new ItemFlowSystem(runtime));

        getLogger().at(Level.INFO).log("Stroemwerk geladen - 4 Systeme registriert, "
                + "Bloecke werden beim ersten Zugriff gesucht. Stand pruefen mit /wasserbahn");
    }

    /**
     * Sobald der Server laeuft, sollten die Asset-Packs da sein - guter
     * Zeitpunkt, um die Bloecke einmal zu suchen und das Ergebnis zu melden.
     */
    @Override
    protected void start() {
        BlockIdLookup lookup = runtime.blockIds();
        lookup.get();

        if (lookup.isComplete()) {
            getLogger().at(Level.INFO).log("Stroemwerk: alle Bloecke erkannt - Wasserbahn bereit");
        } else {
            getLogger().at(Level.WARNING).log(
                    "Stroemwerk: diese Bloecke fehlen noch: " + String.join(", ", lookup.missing())
                            + " - ist das Asset-Pack aktiviert? Es wird weiter versucht.");
        }
    }

    public StroemwerkRuntime runtime() {
        return runtime;
    }
}
