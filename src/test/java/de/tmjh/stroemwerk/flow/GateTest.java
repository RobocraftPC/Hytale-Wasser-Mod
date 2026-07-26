package de.tmjh.stroemwerk.flow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Die Schleuse ist ein Kanal, der sich schliessen laesst. Damit lassen sich
 * Strecken abstellen und an Gabelungen umleiten.
 */
class GateTest {

    @Test
    @DisplayName("Offene Schleuse verhaelt sich wie ein Kanal")
    void openGateConductsLikeChannel() {
        FlowNetwork net = new TestWorld()
                .pump(0, 0, 0, Direction.EAST)
                .channel(1, 0, 0)
                .gate(2, 0, 0, true)
                .channel(3, 0, 0)
                .build(10);

        assertEquals(9, net.at(new BlockPos(2, 0, 0)).strength());
        assertEquals(8, net.at(new BlockPos(3, 0, 0)).strength());
        assertEquals(Direction.EAST, net.at(new BlockPos(3, 0, 0)).direction());
    }

    @Test
    @DisplayName("Geschlossene Schleuse sperrt die Strecke dahinter")
    void closedGateBlocksFlow() {
        FlowNetwork net = new TestWorld()
                .pump(0, 0, 0, Direction.EAST)
                .channel(1, 0, 0)
                .gate(2, 0, 0, false)
                .channel(3, 0, 0)
                .build(10);

        assertNotNull(net.at(new BlockPos(1, 0, 0)), "davor fliesst es weiter");
        assertNull(net.at(new BlockPos(2, 0, 0)), "die Schleuse selbst fuehrt kein Wasser");
        assertNull(net.at(new BlockPos(3, 0, 0)), "dahinter steht alles still");
    }

    @Test
    @DisplayName("Geschlossene Schleuse direkt vor der Pumpe legt das Netz still")
    void closedGateAtPumpStopsEverything() {
        FlowNetwork net = new TestWorld()
                .pump(0, 0, 0, Direction.EAST)
                .gate(1, 0, 0, false)
                .channelLine(2, 0, 0, Direction.EAST, 3)
                .build(10);

        assertTrue(net.isEmpty());
    }

    @Test
    @DisplayName("Schleuse an der Gabelung leitet die Stroemung um")
    void closedGateRedirectsAtBranch() {
        // Geradeaus hat normalerweise Vorrang. Mit geschlossener Schleuse
        // bleibt nur der Abzweig.
        TestWorld world = new TestWorld()
                .pump(0, 0, 0, Direction.EAST)
                .channel(1, 0, 0)
                .gate(2, 0, 0, true)
                .channel(3, 0, 0)
                .channelLine(1, 0, 1, Direction.SOUTH, 2);

        FlowNetwork open = world.build(10);
        assertEquals(Direction.EAST, open.at(new BlockPos(1, 0, 0)).direction(),
                "offen: geradeaus hat Vorrang");
        assertNotNull(open.at(new BlockPos(3, 0, 0)));

        FlowNetwork closed = world.setGate(2, 0, 0, false).build(10);
        assertEquals(Direction.SOUTH, closed.at(new BlockPos(1, 0, 0)).direction(),
                "geschlossen: die Stroemung nimmt den Abzweig");
        assertNull(closed.at(new BlockPos(3, 0, 0)));
        assertNotNull(closed.at(new BlockPos(1, 0, 2)));
    }

    @Test
    @DisplayName("Schleuse kann eine Gegenstroemung abstellen")
    void gateResolvesStandoff() {
        // Ohne Schleuse blockieren sich beide Pumpen in der Mitte.
        TestWorld world = new TestWorld()
                .pump(0, 0, 0, Direction.EAST)
                .channelLine(1, 0, 0, Direction.EAST, 2)
                .gate(3, 0, 0, true)
                .channelLine(4, 0, 0, Direction.EAST, 2)
                .pump(6, 0, 0, Direction.WEST);

        assertTrue(world.build(10).contestedPositions().contains(new BlockPos(3, 0, 0)),
                "offen heben sich beide in der Mitte auf");

        FlowNetwork closed = world.setGate(3, 0, 0, false).build(10);
        assertTrue(closed.contestedPositions().isEmpty(), "geschlossen ist der Streit vorbei");
        assertEquals(Direction.EAST, closed.at(new BlockPos(2, 0, 0)).direction());
        assertEquals(Direction.WEST, closed.at(new BlockPos(4, 0, 0)).direction());
    }

    @Test
    @DisplayName("Gegenstaende bleiben vor einer geschlossenen Schleuse liegen")
    void itemsStopAtClosedGate() {
        TestWorld world = new TestWorld()
                .pump(0, 0, 0, Direction.EAST)
                .channelLine(1, 0, 0, Direction.EAST, 4)
                .gate(5, 0, 0, false);
        FlowNetwork net = world.build();

        double x = 1.5;
        double y = 0.5;
        double z = 0.5;
        double dt = 1.0 / 20.0;

        for (int tick = 0; tick < 400; tick++) {
            BlockPos block = ItemPush.blockAt(x, y, z);
            Vec3d v = ItemPush.velocity(net.at(block), block, x, y, z, FlowSettings.DEFAULT);
            if (v.length() == 0) {
                break;
            }
            x += v.x() * dt;
            y += v.y() * dt;
            z += v.z() * dt;
        }

        assertTrue(x >= 4.0 && x < 6.0,
                "treibt bis zur Schleuse und nicht weiter, war x=" + x);
    }
}
