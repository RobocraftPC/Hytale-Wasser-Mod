package de.tmjh.stroemwerk.flow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ItemPushTest {

    private static final FlowSettings SETTINGS = FlowSettings.DEFAULT;
    private static final BlockPos BLOCK = new BlockPos(10, 64, 20);

    @Test
    @DisplayName("Ohne Stroemung wird nicht geschoben")
    void noFlowNoPush() {
        Vec3d v = ItemPush.velocity(null, BLOCK, 10.5, 64.5, 20.5, SETTINGS);
        assertEquals(Vec3d.ZERO, v);
    }

    @Test
    @DisplayName("Mittig im Kanal wird nur laengs geschoben")
    void centeredItemMovesAlongFlowOnly() {
        FlowNode node = new FlowNode(Direction.EAST, SETTINGS.maxStrength(), BLOCK);
        Vec3d v = ItemPush.velocity(node, BLOCK, 10.5, 64.5, 20.5, SETTINGS);

        assertEquals(SETTINGS.maxSpeed(), v.x(), 1e-9);
        assertEquals(0.0, v.y(), 1e-9);
        assertEquals(0.0, v.z(), 1e-9);
    }

    @Test
    @DisplayName("Aus der Mitte verrutschte Gegenstaende werden zurueckgezogen")
    void offCenterItemIsPulledBack() {
        FlowNode node = new FlowNode(Direction.EAST, SETTINGS.maxStrength(), BLOCK);
        Vec3d v = ItemPush.velocity(node, BLOCK, 10.5, 64.5, 20.9, SETTINGS);

        assertEquals(SETTINGS.maxSpeed(), v.x(), 1e-9, "Transport bleibt unveraendert");
        assertTrue(v.z() < 0, "wird nach -Z zur Mitte gezogen");
    }

    @Test
    @DisplayName("Die Zentrierung ist gedeckelt")
    void centeringIsClamped() {
        FlowNode node = new FlowNode(Direction.EAST, SETTINGS.maxStrength(), BLOCK);
        Vec3d v = ItemPush.velocity(node, BLOCK, 10.5, 64.5, 25.0, SETTINGS);

        assertEquals(-SETTINGS.maxCentering(), v.z(), 1e-9);
    }

    @Test
    @DisplayName("Senkrechte Stroemung hebt und zentriert waagerecht")
    void verticalFlowCentersHorizontally() {
        FlowNode node = new FlowNode(Direction.UP, SETTINGS.maxStrength(), BLOCK);
        Vec3d v = ItemPush.velocity(node, BLOCK, 10.2, 64.5, 20.5, SETTINGS);

        assertEquals(SETTINGS.maxSpeed(), v.y(), 1e-9);
        assertTrue(v.x() > 0, "wird nach +X zur Mitte gezogen");
        assertEquals(0.0, v.z(), 1e-9);
    }

    @Test
    @DisplayName("Schwacher Druck schiebt langsamer")
    void weakFlowIsSlower() {
        FlowNode strong = new FlowNode(Direction.EAST, SETTINGS.maxStrength(), BLOCK);
        FlowNode weak = new FlowNode(Direction.EAST, 1, BLOCK);

        double fast = ItemPush.velocity(strong, BLOCK, 10.5, 64.5, 20.5, SETTINGS).x();
        double slow = ItemPush.velocity(weak, BLOCK, 10.5, 64.5, 20.5, SETTINGS).x();

        assertEquals(SETTINGS.maxSpeed(), fast, 1e-9);
        assertEquals(SETTINGS.minSpeed(), slow, 1e-9);
        assertTrue(slow < fast);
    }

    @Test
    @DisplayName("Blockposition wird auch bei negativen Koordinaten abgerundet")
    void blockAtFloorsNegatives() {
        assertEquals(new BlockPos(-1, -1, -1), ItemPush.blockAt(-0.3, -0.001, -0.9));
        assertEquals(new BlockPos(10, 64, 20), ItemPush.blockAt(10.5, 64.9, 20.0));
    }

    @Test
    @DisplayName("Ein Gegenstand folgt der Kurve durch das Netz")
    void itemTravelsAroundCorner() {
        TestWorld world = new TestWorld()
                .pump(0, 0, 0, Direction.EAST)
                .channelLine(1, 0, 0, Direction.EAST, 3)
                .channelLine(3, 0, 1, Direction.SOUTH, 3);
        FlowNetwork net = world.build();

        // Simulation in kleinen Schritten: Position -> Block -> Stroemung -> Bewegung.
        double x = 1.5;
        double y = 0.5;
        double z = 0.5;
        double dt = 1.0 / 20.0;

        for (int tick = 0; tick < 200; tick++) {
            BlockPos block = ItemPush.blockAt(x, y, z);
            Vec3d v = ItemPush.velocity(net.at(block), block, x, y, z, SETTINGS);
            if (v.length() == 0) {
                break;
            }
            x += v.x() * dt;
            y += v.y() * dt;
            z += v.z() * dt;
        }

        // Der Gegenstand muss um die Ecke und am Ende der Suedstrecke sein.
        assertEquals(3, ItemPush.blockAt(x, y, z).x());
        assertTrue(z > 3.0, "hat die Kurve genommen und ist ans Ende getrieben, war z=" + z);
    }
}
