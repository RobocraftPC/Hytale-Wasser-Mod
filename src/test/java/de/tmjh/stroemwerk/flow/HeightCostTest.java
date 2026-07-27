package de.tmjh.stroemwerk.flow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Hoehe kostet Druck: bergauf zwei Stufen je Block, waagerecht eine, bergab
 * keine. Wasser faellt schliesslich von selbst.
 */
class HeightCostTest {

    @Test
    @DisplayName("Bergauf kostet doppelt")
    void climbingCostsTwice() {
        FlowNetwork net = new TestWorld()
                .pump(0, 0, 0, Direction.UP)
                .channelLine(0, 1, 0, Direction.UP, 4)
                .build(20);

        assertEquals(20, net.at(new BlockPos(0, 1, 0)).strength());
        assertEquals(18, net.at(new BlockPos(0, 2, 0)).strength());
        assertEquals(16, net.at(new BlockPos(0, 3, 0)).strength());
        assertEquals(14, net.at(new BlockPos(0, 4, 0)).strength());
    }

    @Test
    @DisplayName("Bergab kostet nichts")
    void fallingIsFree() {
        FlowNetwork net = new TestWorld()
                .pump(0, 10, 0, Direction.DOWN)
                .channelLine(0, 9, 0, Direction.DOWN, 6)
                .build(20);

        // Der Druck bleibt im Fallschacht erhalten.
        assertEquals(20, net.at(new BlockPos(0, 9, 0)).strength());
        assertEquals(20, net.at(new BlockPos(0, 4, 0)).strength());
    }

    @Test
    @DisplayName("Ein Fallschacht speist die Strecke unten mit vollem Druck")
    void dropThenRunKeepsFullPressure() {
        FlowNetwork net = new TestWorld()
                .pump(0, 10, 0, Direction.DOWN)
                .channelLine(0, 9, 0, Direction.DOWN, 9)
                .channelLine(1, 1, 0, Direction.EAST, 5)
                .build(10);

        // Nach neun Bloecken Fall ist unten noch alles da...
        assertEquals(10, net.at(new BlockPos(0, 1, 0)).strength());
        // ...und die waagerechte Strecke zehrt normal davon.
        assertEquals(9, net.at(new BlockPos(1, 1, 0)).strength());
        assertEquals(6, net.at(new BlockPos(4, 1, 0)).strength());
    }

    @Test
    @DisplayName("Bergauf reicht eine Pumpe nur halb so weit")
    void climbHalvesTheRange() {
        FlowNetwork net = new TestWorld()
                .pump(0, 0, 0, Direction.UP)
                .channelLine(0, 1, 0, Direction.UP, 20)
                .build(10);

        // Startdruck 10, je Block zwei Stufen: nach fuenf Bloecken ist Schluss.
        assertNotNull(net.at(new BlockPos(0, 5, 0)));
        assertNull(net.at(new BlockPos(0, 6, 0)));
    }

    @Test
    @DisplayName("Ein zu teurer Schritt wird gar nicht erst gegangen")
    void tooExpensiveStepIsNotTaken() {
        FlowNetwork net = new TestWorld()
                .pump(0, 0, 0, Direction.EAST)
                .channelLine(1, 0, 0, Direction.EAST, 2)
                .channel(2, 1, 0)
                .build(3);

        // Bei (2,0,0) ist der Druck 2, ein Schritt nach oben kostet aber 2 -
        // es bliebe 0, also bleibt der Block darueber trocken.
        assertEquals(2, net.at(new BlockPos(2, 0, 0)).strength());
        assertNull(net.at(new BlockPos(2, 1, 0)));
    }

    @Test
    @DisplayName("Ein ausuferndes Netz wird gedeckelt")
    void hugeNetworkIsCapped() {
        // Ein kostenloser Fallschacht koennte beliebig lang werden.
        TestWorld world = new TestWorld().pump(0, 5000, 0, Direction.DOWN);
        for (int y = 4999; y > 0; y--) {
            world.channel(0, y, 0);
        }

        FlowSettings capped = FlowSettings.DEFAULT.withMaxStrength(10);
        FlowNetwork net = FlowNetwork.build(world, world.pumps(), capped);

        assertTrue(net.nodes().size() <= capped.maxNodes(),
                "hoechstens " + capped.maxNodes() + " Knoten, waren " + net.nodes().size());
        assertTrue(net.nodes().size() > 100, "aber ein gutes Stueck weit reicht es");
    }
}
