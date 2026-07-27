package de.tmjh.stroemwerk.hytale;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import de.tmjh.stroemwerk.flow.FlowSettings;
import de.tmjh.stroemwerk.hytale.commands.WasserbahnCommand;
import de.tmjh.stroemwerk.hytale.systems.BlockChangeSystems;
import de.tmjh.stroemwerk.hytale.systems.ItemFlowSystem;
import de.tmjh.stroemwerk.platform.BlockIds;
import java.util.ArrayList;
import java.util.List;
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
        List<String> missing = new ArrayList<>();
        BlockIds blockIds = BlockIdResolver.resolve(missing::add);

        this.runtime = new StroemwerkRuntime(FlowSettings.DEFAULT, blockIds);

        getCommandRegistry().registerCommand(new WasserbahnCommand(runtime));

        getEntityStoreRegistry().registerSystem(new BlockChangeSystems.Place(runtime));
        getEntityStoreRegistry().registerSystem(new BlockChangeSystems.Break(runtime));
        getEntityStoreRegistry().registerSystem(new BlockChangeSystems.Use(runtime));
        getEntityStoreRegistry().registerSystem(new ItemFlowSystem(runtime));

        if (missing.isEmpty()) {
            getLogger().at(Level.INFO).log("Stroemwerk geladen - Wasserbahn bereit");
        } else {
            // Ohne die Bloecke laeuft nichts, und im Spiel sieht man nur, dass
            // nichts passiert. Deshalb hier deutlich benennen, was fehlt.
            getLogger().at(Level.WARNING).log(
                    "Stroemwerk geladen, aber diese Bloecke fehlen: " + String.join(", ", missing)
                            + " - ist das Asset-Pack aktiviert?");
        }
    }

    public StroemwerkRuntime runtime() {
        return runtime;
    }
}
