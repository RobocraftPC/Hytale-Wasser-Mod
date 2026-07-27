package de.tmjh.stroemwerk.hytale.commands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.universe.Universe;
import de.tmjh.stroemwerk.flow.FlowNetwork;
import de.tmjh.stroemwerk.hytale.StroemwerkRuntime;
import de.tmjh.stroemwerk.platform.ChannelNetworkService;
import javax.annotation.Nonnull;

/**
 * {@code /wasserbahn} - zeigt an, was das Stroemungsnetz gerade berechnet hat.
 *
 * <p>Beim Bauen laengerer Strecken ist es sonst kaum nachvollziehbar, warum ein
 * Kanal stillsteht.
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

        context.sendMessage(Message.raw("Wasserbahn"));
        context.sendMessage(Message.raw("  Pumpen:              " + service.knownPumps().size()));
        context.sendMessage(Message.raw("  Fliessende Kanaele:  " + network.nodes().size()));
        context.sendMessage(Message.raw("  Blockiert:           " + network.contestedPositions().size()));
        context.sendMessage(Message.raw("  Geschlossene Schleusen: " + runtime.gates().closedCount()));
        context.sendMessage(Message.raw("  Grundreichweite:     " + network.maxStrength() + " Bloecke"));
        context.sendMessage(Message.raw("  Neuberechnungen:     " + service.rebuildCount()));

        // Bei mehreren Pumpen ist der staerkste Druck der aussagekraeftigste
        // Wert - daran sieht man, ob die Wasserraeder greifen.
        network.nodes().values().stream()
                .mapToInt(node -> node.strength())
                .max()
                .ifPresent(peak -> {
                    if (peak > network.maxStrength()) {
                        context.sendMessage(Message.raw(
                                "  Mit Wasserrad:       " + peak + " Bloecke"));
                    }
                });

        if (!runtime.blockIds().anyResolved()) {
            context.sendMessage(Message.raw(
                    "  Achtung: keine Block-IDs aufgeloest, die Wasserbahn ist untaetig."));
        }

        if (!network.contestedPositions().isEmpty()) {
            context.sendMessage(Message.raw("  Gegenstroemung an: "
                    + network.contestedPositions().stream()
                            .limit(5)
                            .map(Object::toString)
                            .reduce((a, b) -> a + "; " + b)
                            .orElse("-")));
        }
    }
}
