package de.tmjh.stroemwerk.flow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FlowNetworkTest {

    @Test
    @DisplayName("Pumpe schiebt geradeaus und verliert pro Block eine Druckstufe")
    void straightLineLosesOneStrengthPerBlock() {
        FlowNetwork net = new TestWorld()
                .pump(0, 0, 0, Direction.EAST)
                .channelLine(1, 0, 0, Direction.EAST, 4)
                .build(10);

        assertEquals(new FlowNode(Direction.EAST, 10, new BlockPos(0, 0, 0)), net.at(new BlockPos(1, 0, 0)));
        assertEquals(9, net.at(new BlockPos(2, 0, 0)).strength());
        assertEquals(8, net.at(new BlockPos(3, 0, 0)).strength());
        assertEquals(7, net.at(new BlockPos(4, 0, 0)).strength());
        assertEquals(4, net.nodes().size());
    }

    @Test
    @DisplayName("Ohne Kanal vor der Pumpe entsteht kein Netz")
    void pumpWithoutChannelProducesNothing() {
        FlowNetwork net = new TestWorld()
                .pump(0, 0, 0, Direction.EAST)
                .channel(5, 0, 0)
                .build();

        assertTrue(net.isEmpty());
    }

    @Test
    @DisplayName("Reichweite endet beim Restdruck 1")
    void flowStopsAtRangeLimit() {
        FlowNetwork net = new TestWorld()
                .pump(0, 0, 0, Direction.EAST)
                .channelLine(1, 0, 0, Direction.EAST, 8)
                .build(3);

        assertEquals(3, net.at(new BlockPos(1, 0, 0)).strength());
        assertEquals(1, net.at(new BlockPos(3, 0, 0)).strength());
        assertNull(net.at(new BlockPos(4, 0, 0)), "hinter der Reichweite fliesst nichts mehr");
        assertEquals(3, net.nodes().size());
    }

    @Test
    @DisplayName("Kanal folgt der Kurve")
    void flowFollowsCorner() {
        FlowNetwork net = new TestWorld()
                .pump(0, 0, 0, Direction.EAST)
                .channelLine(1, 0, 0, Direction.EAST, 3)
                .channelLine(3, 0, 1, Direction.SOUTH, 3)
                .build(20);

        assertEquals(Direction.EAST, net.at(new BlockPos(2, 0, 0)).direction());
        // Am Knick zeigt der letzte gerade Block schon in die neue Richtung.
        assertEquals(Direction.SOUTH, net.at(new BlockPos(3, 0, 0)).direction());
        assertEquals(Direction.SOUTH, net.at(new BlockPos(3, 0, 2)).direction());
        assertEquals(15, net.at(new BlockPos(3, 0, 3)).strength(), "sechster Block ab Druck 20");
    }

    @Test
    @DisplayName("Geradeaus hat Vorrang vor dem Abzweig")
    void straightAheadWinsOverBranch() {
        FlowNetwork net = new TestWorld()
                .pump(0, 0, 0, Direction.EAST)
                .channelLine(1, 0, 0, Direction.EAST, 4)
                .channelLine(2, 0, 1, Direction.SOUTH, 2)
                .build(20);

        assertEquals(Direction.EAST, net.at(new BlockPos(2, 0, 0)).direction());
        // Der Abzweig wird trotzdem versorgt, nur eine Stufe schwaecher.
        assertEquals(Direction.SOUTH, net.at(new BlockPos(2, 0, 1)).direction());
        assertEquals(18, net.at(new BlockPos(2, 0, 1)).strength());
    }

    @Test
    @DisplayName("Kanal darf senkrecht nach oben laufen")
    void flowCanClimb() {
        FlowNetwork net = new TestWorld()
                .pump(0, 0, 0, Direction.EAST)
                .channel(1, 0, 0)
                .channelLine(1, 1, 0, Direction.UP, 3)
                .build(20);

        assertEquals(Direction.UP, net.at(new BlockPos(1, 0, 0)).direction());
        assertEquals(Direction.UP, net.at(new BlockPos(1, 2, 0)).direction());
        assertEquals(17, net.at(new BlockPos(1, 3, 0)).strength());
    }

    @Test
    @DisplayName("Ringkanal laeuft nicht endlos")
    void loopTerminates() {
        FlowNetwork net = new TestWorld()
                .pump(-1, 0, 0, Direction.EAST)
                .channelLine(0, 0, 0, Direction.EAST, 4)
                .channelLine(3, 0, 1, Direction.SOUTH, 3)
                .channelLine(2, 0, 3, Direction.WEST, 3)
                .channelLine(0, 0, 2, Direction.NORTH, 2)
                .build(30);

        assertEquals(12, net.nodes().size());
        assertTrue(net.contestedPositions().isEmpty());
    }

    @Test
    @DisplayName("Die staerkere Pumpe setzt sich durch")
    void strongerPumpWins() {
        // Beide Pumpen speisen dieselbe Strecke, aber von unterschiedlich weit weg.
        FlowNetwork net = new TestWorld()
                .pump(0, 0, 0, Direction.EAST)
                .channelLine(1, 0, 0, Direction.EAST, 6)
                .pump(7, 0, 0, Direction.WEST)
                .build(10);

        // Nahe an der linken Pumpe gewinnt diese.
        assertEquals(Direction.EAST, net.at(new BlockPos(1, 0, 0)).direction());
        assertEquals(new BlockPos(0, 0, 0), net.at(new BlockPos(1, 0, 0)).source());
        // Nahe an der rechten Pumpe gewinnt jene.
        assertEquals(Direction.WEST, net.at(new BlockPos(6, 0, 0)).direction());
        assertEquals(new BlockPos(7, 0, 0), net.at(new BlockPos(6, 0, 0)).source());
    }

    @Test
    @DisplayName("Gleich starke Gegenstroemung blockiert den Kanal")
    void equalOppositeFlowsStall() {
        FlowNetwork net = new TestWorld()
                .pump(0, 0, 0, Direction.EAST)
                .channelLine(1, 0, 0, Direction.EAST, 5)
                .pump(6, 0, 0, Direction.WEST)
                .build(10);

        BlockPos middle = new BlockPos(3, 0, 0);
        assertNull(net.at(middle), "in der Mitte heben sich beide auf");
        assertTrue(net.isContested(middle));
        assertFalse(net.isContested(new BlockPos(1, 0, 0)));
    }

    @Test
    @DisplayName("Zwei Pumpen in dieselbe Richtung addieren sich nicht, blockieren aber auch nicht")
    void alignedPumpsDoNotStall() {
        FlowNetwork net = new TestWorld()
                .pump(0, 0, 0, Direction.EAST)
                .channelLine(1, 0, 0, Direction.EAST, 8)
                .pump(0, 0, 1, Direction.EAST)
                .channelLine(1, 0, 1, Direction.EAST, 1)
                .build(10);

        assertTrue(net.contestedPositions().isEmpty());
        assertEquals(Direction.EAST, net.at(new BlockPos(1, 0, 0)).direction());
    }

    @Test
    @DisplayName("Geschwindigkeit sinkt mit dem Restdruck")
    void speedScalesWithStrength() {
        FlowNode full = new FlowNode(Direction.EAST, 10, new BlockPos(0, 0, 0));
        FlowNode weak = new FlowNode(Direction.EAST, 1, new BlockPos(0, 0, 0));

        assertEquals(4.0, full.speed(10, 4.0, 1.0), 1e-9);
        assertEquals(1.0, weak.speed(10, 4.0, 1.0), 1e-9);
    }

    @Test
    @DisplayName("Entfernte Pumpe verschwindet aus dem Netz")
    void removingPumpClearsFlow() {
        TestWorld world = new TestWorld()
                .pump(0, 0, 0, Direction.EAST)
                .channelLine(1, 0, 0, Direction.EAST, 3);

        assertNotNull(world.build().at(new BlockPos(1, 0, 0)));
        assertTrue(FlowNetwork.build(world, java.util.List.of()).isEmpty());
    }
}
