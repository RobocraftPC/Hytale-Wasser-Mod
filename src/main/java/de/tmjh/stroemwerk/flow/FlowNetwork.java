package de.tmjh.stroemwerk.flow;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Berechnet die Stroemung eines Kanalnetzes.
 *
 * <p>Jede Pumpe schiebt Wasser in ihre Blickrichtung. Von dort breitet sich der
 * Druck ueber verbundene Kanalbloecke aus und verliert pro Block eine Stufe.
 * Treffen zwei Pumpen aufeinander, gewinnt der hoehere Restdruck; bei
 * Gleichstand blockieren sie sich gegenseitig und der Kanal steht still.
 *
 * <p>Die Klasse ist unveraenderlich und rein rechnend - kein Weltzugriff ausser
 * ueber {@link WorldView}.
 */
public final class FlowNetwork {

    /** Wie weit eine Pumpe maximal traegt, in Bloecken. */
    public static final int DEFAULT_MAX_STRENGTH = 32;

    private final Map<BlockPos, FlowNode> nodes;
    private final Set<BlockPos> contested;
    private final int maxStrength;

    private FlowNetwork(Map<BlockPos, FlowNode> nodes, Set<BlockPos> contested, int maxStrength) {
        this.nodes = nodes;
        this.contested = contested;
        this.maxStrength = maxStrength;
    }

    public static FlowNetwork empty() {
        return new FlowNetwork(Map.of(), Set.of(), DEFAULT_MAX_STRENGTH);
    }

    public static FlowNetwork build(WorldView world, Collection<BlockPos> pumps) {
        return build(world, pumps, DEFAULT_MAX_STRENGTH);
    }

    /**
     * Baut das Netz aus allen bekannten Pumpen auf.
     *
     * @param pumps       Positionen aller Pumpen, die beruecksichtigt werden sollen
     * @param maxStrength Startdruck einer Pumpe und damit ihre Reichweite
     */
    public static FlowNetwork build(WorldView world, Collection<BlockPos> pumps, int maxStrength) {
        if (maxStrength < 1) {
            throw new IllegalArgumentException("maxStrength muss mindestens 1 sein, war " + maxStrength);
        }

        // Pro Kanalblock sammeln wir alle Angebote der einzelnen Pumpen und
        // entscheiden erst danach, welches gewinnt. Getrennte Phasen halten das
        // Ergebnis unabhaengig von der Reihenfolge der Pumpen.
        Map<BlockPos, List<FlowNode>> offers = new HashMap<>();
        List<BlockPos> sortedPumps = new ArrayList<>(pumps);
        Collections.sort(sortedPumps);

        for (BlockPos pump : sortedPumps) {
            if (world.typeAt(pump) != NodeType.PUMP) {
                continue;
            }
            traceFromPump(world, pump, maxStrength, offers);
        }

        Map<BlockPos, FlowNode> resolved = new HashMap<>();
        Set<BlockPos> contested = new HashSet<>();
        for (Map.Entry<BlockPos, List<FlowNode>> entry : offers.entrySet()) {
            FlowNode winner = resolve(entry.getValue());
            if (winner == null) {
                contested.add(entry.getKey());
            } else {
                resolved.put(entry.getKey(), winner);
            }
        }

        return new FlowNetwork(Map.copyOf(resolved), Set.copyOf(contested), maxStrength);
    }

    /**
     * Breitensuche ab einer Pumpe. Der erste Kanal vor der Pumpe bekommt den
     * vollen Druck, jeder weitere eine Stufe weniger.
     */
    private static void traceFromPump(WorldView world, BlockPos pump, int maxStrength,
                                      Map<BlockPos, List<FlowNode>> offers) {
        Direction facing = world.pumpFacing(pump);
        if (facing == null) {
            return;
        }

        BlockPos first = pump.offset(facing);
        if (!world.conducts(first)) {
            return;
        }

        // Innerhalb einer Pumpe darf jeder Kanal nur einmal besucht werden,
        // sonst laufen Ringe endlos.
        Set<BlockPos> visited = new HashSet<>();
        Deque<Step> queue = new ArrayDeque<>();
        visited.add(first);
        queue.add(new Step(first, facing, maxStrength));

        while (!queue.isEmpty()) {
            Step step = queue.poll();
            List<Direction> exits = exitsFrom(world, step.pos, step.direction);

            // Ein Gegenstand auf diesem Block muss dorthin geschoben werden, wo
            // es weitergeht - an einer Kurve also schon in die neue Richtung.
            // Endet der Kanal hier, behaelt er die Einlaufrichtung und der
            // Gegenstand faellt vorne heraus.
            Direction outgoing = exits.isEmpty() ? step.direction : exits.getFirst();
            offers.computeIfAbsent(step.pos, key -> new ArrayList<>())
                    .add(new FlowNode(outgoing, step.strength, pump));

            if (step.strength <= 1) {
                continue;
            }

            for (Direction next : exits) {
                BlockPos target = step.pos.offset(next);
                if (visited.add(target)) {
                    queue.add(new Step(target, next, step.strength - 1));
                }
            }
        }
    }

    /**
     * Moegliche Weiterleitungen aus einem Kanal heraus: geradeaus zuerst, dann
     * Abzweige. Zurueck geht es nie - das Wasser kommt ja von dort. Eine
     * geschlossene Schleuse zaehlt nicht als Ausgang und sperrt damit.
     */
    private static List<Direction> exitsFrom(WorldView world, BlockPos pos, Direction incoming) {
        List<Direction> exits = new ArrayList<>(4);
        if (world.conducts(pos.offset(incoming))) {
            exits.add(incoming);
        }
        for (Direction candidate : Direction.values()) {
            if (candidate == incoming || candidate == incoming.opposite()) {
                continue;
            }
            if (world.conducts(pos.offset(candidate))) {
                exits.add(candidate);
            }
        }
        return exits;
    }

    /**
     * Waehlt aus konkurrierenden Angeboten das staerkste aus. Sind die
     * staerksten uneinig, hebt sich die Stroemung auf ({@code null}).
     */
    private static FlowNode resolve(List<FlowNode> candidates) {
        FlowNode best = null;
        boolean tied = false;
        for (FlowNode candidate : candidates) {
            if (best == null || candidate.strength() > best.strength()) {
                best = candidate;
                tied = false;
            } else if (candidate.strength() == best.strength() && candidate.direction() != best.direction()) {
                tied = true;
            }
        }
        return tied ? null : best;
    }

    /**
     * Stroemung an dieser Position oder {@code null}, wenn dort nichts fliesst.
     */
    public FlowNode at(BlockPos pos) {
        return nodes.get(pos);
    }

    /**
     * Ob sich an dieser Position zwei gleich starke Stroemungen blockieren.
     */
    public boolean isContested(BlockPos pos) {
        return contested.contains(pos);
    }

    public Map<BlockPos, FlowNode> nodes() {
        return nodes;
    }

    public Set<BlockPos> contestedPositions() {
        return contested;
    }

    public int maxStrength() {
        return maxStrength;
    }

    public boolean isEmpty() {
        return nodes.isEmpty();
    }

    private record Step(BlockPos pos, Direction direction, int strength) {
    }
}
