package de.tmjh.stroemwerk.flow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Eine Pumpe ohne hinterlegte Ausrichtung drueckt in den Kanal, an dem sie
 * haengt. So funktioniert sie auch ohne auslesbare Blockrotation.
 */
class AutoPumpFacingTest {

    /** Welt, in der Pumpen grundsaetzlich keine Ausrichtung gespeichert haben. */
    private static final class UnorientedWorld extends TestWorld {
        @Override
        public Direction pumpFacing(BlockPos pos) {
            return null;
        }
    }

    @Test
    @DisplayName("Die Pumpe findet den einzigen angrenzenden Kanal")
    void findsTheSingleAdjacentChannel() {
        FlowNetwork net = FlowNetwork.build(
                new UnorientedWorld()
                        .pump(0, 0, 0, Direction.EAST)
                        .channelLine(0, 0, 1, Direction.SOUTH, 3),
                java.util.List.of(new BlockPos(0, 0, 0)),
                10);

        // Trotz gespeicherter Ostrichtung zaehlt hier die Nachbarschaft.
        assertEquals(Direction.SOUTH, net.at(new BlockPos(0, 0, 1)).direction());
        assertEquals(10, net.at(new BlockPos(0, 0, 1)).strength());
        assertEquals(3, net.nodes().size());
    }

    @Test
    @DisplayName("Auch nach oben wird gepumpt")
    void findsChannelAbove() {
        FlowNetwork net = FlowNetwork.build(
                new UnorientedWorld()
                        .pump(5, 10, 5, Direction.NORTH)
                        .channelLine(5, 11, 5, Direction.UP, 2),
                java.util.List.of(new BlockPos(5, 10, 5)),
                10);

        assertNotNull(net.at(new BlockPos(5, 11, 5)));
        assertEquals(Direction.UP, net.at(new BlockPos(5, 11, 5)).direction());
    }

    @Test
    @DisplayName("Ohne angrenzenden Kanal passiert nichts")
    void withoutNeighbourNothingFlows() {
        FlowNetwork net = FlowNetwork.build(
                new UnorientedWorld()
                        .pump(0, 0, 0, Direction.EAST)
                        .channelLine(4, 0, 0, Direction.EAST, 3),
                java.util.List.of(new BlockPos(0, 0, 0)),
                10);

        assertTrue(net.isEmpty());
    }

    @Test
    @DisplayName("Eine geschlossene Schleuse zaehlt nicht als Ziel")
    void closedGateIsNoTarget() {
        FlowNetwork net = FlowNetwork.build(
                new UnorientedWorld()
                        .pump(0, 0, 0, Direction.EAST)
                        .gate(1, 0, 0, false)
                        .channelLine(0, 0, 1, Direction.SOUTH, 2),
                java.util.List.of(new BlockPos(0, 0, 0)),
                10);

        // Nach Osten ist dicht, also nimmt die Pumpe den Kanal im Sueden.
        assertNull(net.at(new BlockPos(1, 0, 0)));
        assertEquals(Direction.SOUTH, net.at(new BlockPos(0, 0, 1)).direction());
    }

    @Test
    @DisplayName("Bei mehreren Kanaelen entscheidet eine feste Reihenfolge")
    void multipleNeighboursAreDeterministic() {
        UnorientedWorld world = new UnorientedWorld();
        world.pump(0, 0, 0, Direction.EAST)
                .channel(0, 0, -1)   // NORTH, kommt in Direction.values() zuerst
                .channel(1, 0, 0);   // EAST

        FlowNetwork first = FlowNetwork.build(world, java.util.List.of(new BlockPos(0, 0, 0)), 10);
        FlowNetwork second = FlowNetwork.build(world, java.util.List.of(new BlockPos(0, 0, 0)), 10);

        assertNotNull(first.at(new BlockPos(0, 0, -1)));
        assertEquals(first.nodes(), second.nodes(), "zweimal dasselbe Ergebnis");
    }

    @Test
    @DisplayName("Eine hinterlegte Ausrichtung hat weiter Vorrang")
    void explicitFacingStillWins() {
        // Normale TestWorld: hier ist die Ausrichtung gespeichert.
        FlowNetwork net = new TestWorld()
                .pump(0, 0, 0, Direction.EAST)
                .channel(1, 0, 0)
                .channel(0, 0, 1)
                .build(10);

        assertNotNull(net.at(new BlockPos(1, 0, 0)), "folgt der gespeicherten Ostrichtung");
        assertNull(net.at(new BlockPos(0, 0, 1)));
    }
}
