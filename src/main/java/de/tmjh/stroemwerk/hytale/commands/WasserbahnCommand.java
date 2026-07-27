package de.tmjh.stroemwerk.hytale.commands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.universe.Universe;
import de.tmjh.stroemwerk.flow.FlowNetwork;
import de.tmjh.stroemwerk.hytale.BlockIdLookup;
import de.tmjh.stroemwerk.hytale.StroemwerkRuntime;
import de.tmjh.stroemwerk.platform.BlockIds;
import de.tmjh.stroemwerk.platform.ChannelNetworkService;
import javax.annotation.Nonnull;

/**
 * {@code /wasserbahn} - zeigt an, was das Stroemungsnetz gerade berechnet hat.
 *
 * <p>Der Befehl beantwortet vor allem die Frage "warum passiert nichts?" und
 * geht dabei die Kette von hinten durch: Sind die Bloecke bekannt? Kommen die
 * Bau-Ereignisse an? Laeuft das Tick-System? Erst danach kommen die Zahlen zum
 * Netz selbst.
 */
public class WasserbahnCommand extends CommandBase {

    private final StroemwerkRuntime runtime;

    public WasserbahnCommand(StroemwerkRuntime runtime) {
        super("wasserbahn", "Zeigt den Zustand der Wasserbahn in dieser Welt");
        this.runtime = runtime;
    }

    @Override
    protected void executeSync(@Nonnull CommandContext context) {
        ChannelNetworkService service = runtime.networkFor(Universe.get().getDefaultWorld());
        FlowNetwork network = service.network();
        BlockIdLookup lookup = runtime.blockIds();
        BlockIds ids = lookup.get();

        context.sendMessage(Message.raw("Wasserbahn"));

        // 1. Kennt der Mod seine eigenen Bloecke?
        if (lookup.isComplete()) {
            context.sendMessage(Message.raw("  Bloecke erkannt:  Kanal=" + ids.channel()
                    + " Pumpe=" + ids.pump()
                    + " Schleuse=" + ids.gate()
                    + " Rad=" + ids.wheel()));
        } else {
            context.sendMessage(Message.raw("  FEHLT: " + String.join(", ", lookup.missing())));
            context.sendMessage(Message.raw("  Ohne erkannte Bloecke bleibt jede Bahn untaetig."));
            context.sendMessage(Message.raw("  Versuche bisher: " + lookup.attempts()));
        }

        // 2. Kommen die Ereignisse ueberhaupt an?
        context.sendMessage(Message.raw("  Ereignisse:       gebaut=" + runtime.placedEvents()
                + " abgebaut=" + runtime.removedEvents()
                + " benutzt=" + runtime.useEvents()));
        context.sendMessage(Message.raw("  Gegenstaende:     geprueft=" + runtime.itemTicks()
                + " getragen=" + runtime.itemsPushed()));

        // 3. Und was hat das Netz daraus gemacht?
        context.sendMessage(Message.raw("  Pumpen:           " + service.knownPumps().size()));
        context.sendMessage(Message.raw("  Fliessende Kanaele: " + network.nodes().size()));
        context.sendMessage(Message.raw("  Blockiert:        " + network.contestedPositions().size()));
        context.sendMessage(Message.raw("  Geschlossene Schleusen: " + runtime.gates().closedCount()));
        context.sendMessage(Message.raw("  Grundreichweite:  " + network.maxStrength() + " Bloecke"));
        context.sendMessage(Message.raw("  Neuberechnungen:  " + service.rebuildCount()));

        network.nodes().values().stream()
                .mapToInt(node -> node.strength())
                .max()
                .ifPresent(peak -> {
                    if (peak > network.maxStrength()) {
                        context.sendMessage(Message.raw("  Mit Wasserrad:    " + peak + " Bloecke"));
                    }
                });

        if (lookup.isComplete() && runtime.placedEvents() == 0) {
            context.sendMessage(Message.raw(
                    "  Hinweis: Es ist noch kein Bau-Ereignis angekommen."));
        }
    }
}
