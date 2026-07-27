package de.tmjh.stroemwerk.platform;

import de.tmjh.stroemwerk.flow.BlockPos;
import de.tmjh.stroemwerk.flow.Direction;
import de.tmjh.stroemwerk.flow.FlowNetwork;
import de.tmjh.stroemwerk.flow.FlowNode;
import de.tmjh.stroemwerk.flow.FlowSettings;
import de.tmjh.stroemwerk.flow.NodeType;
import de.tmjh.stroemwerk.flow.WorldView;
import java.util.Set;
import java.util.TreeSet;

/**
 * Haelt das Stroemungsnetz einer Welt aktuell.
 *
 * <p>Neu berechnet wird nur, wenn sich wirklich etwas geaendert hat - beim
 * Bauen oder Abbauen eines Bauteils. Danach liefert {@link #flowAt} bis zur
 * naechsten Aenderung aus dem Zwischenspeicher.
 *
 * <p>Kennt keine Hytale-Typen: die Welt kommt als {@link WorldView} herein.
 * Nicht threadsicher - alle Aufrufe gehoeren auf den Thread der Welt.
 */
public final class ChannelNetworkService {

    private final WorldView world;
    private final FlowSettings settings;
    private final Set<BlockPos> pumps = new TreeSet<>();

    private FlowNetwork network = FlowNetwork.empty();
    private boolean dirty = true;
    private int rebuildCount;

    public ChannelNetworkService(WorldView world, FlowSettings settings) {
        this.world = world;
        this.settings = settings;
    }

    /**
     * Meldet eine Bauteilaenderung. Pumpen werden mitgefuehrt, damit das Netz
     * nicht jedes Mal die halbe Welt absuchen muss.
     */
    public void onBlockChanged(BlockPos pos) {
        NodeType type = world.typeAt(pos);
        if (type == NodeType.PUMP) {
            pumps.add(pos);
            dirty = true;
        } else if (pumps.remove(pos)) {
            dirty = true;
        } else if (type == NodeType.WHEEL || isNextToPump(pos)) {
            // Ein Wasserrad aendert den Druck der Pumpe daneben, ohne selbst
            // Teil des Netzes zu sein. Auch sein Abbau muss daher auffallen.
            dirty = true;
        } else if (type == NodeType.CHANNEL || type == NodeType.GATE || network.at(pos) != null) {
            // Ein Kanal kam dazu oder verschwand - die Strecken verschieben sich.
            dirty = true;
        }
    }

    /**
     * Stroemung an dieser Position, notfalls nach einer Neuberechnung.
     */
    public FlowNode flowAt(BlockPos pos) {
        return network().at(pos);
    }

    public FlowNetwork network() {
        if (dirty) {
            // Zwischen den Neuberechnungen koennen Pumpen abgebaut worden sein,
            // ohne dass wir es gesehen haben (z.B. durch andere Plugins).
            pumps.removeIf(pump -> world.typeAt(pump) != NodeType.PUMP);
            network = FlowNetwork.build(world, pumps, settings);
            dirty = false;
            rebuildCount++;
        }
        return network;
    }

    /** Erzwingt eine Neuberechnung beim naechsten Zugriff. */
    public void invalidate() {
        dirty = true;
    }

    /** Welches Bauteil an dieser Position steht. */
    public NodeType typeAt(BlockPos pos) {
        return world.typeAt(pos);
    }

    /**
     * Ob an dieser Stelle eine bekannte Pumpe angrenzt. Beim Abbau eines
     * Wasserrads laesst sich nicht mehr feststellen, dass dort eines stand -
     * die Nachbarschaft verraet es trotzdem.
     */
    private boolean isNextToPump(BlockPos pos) {
        for (Direction direction : Direction.values()) {
            if (pumps.contains(pos.offset(direction))) {
                return true;
            }
        }
        return false;
    }

    public Set<BlockPos> knownPumps() {
        return Set.copyOf(pumps);
    }

    public FlowSettings settings() {
        return settings;
    }

    /** Wie oft neu gerechnet wurde - fuer /wasserbahn info. */
    public int rebuildCount() {
        return rebuildCount;
    }
}
