package de.tmjh.stroemwerk.flow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Wasserraeder treiben eine angrenzende Pumpe an und verlaengern ihre
 * Reichweite. Sie sind kein Teil der Strecke, sondern haengen aussen an.
 */
class WaterWheelTest {

    private static final FlowSettings SETTINGS =
            new FlowSettings(10, 4.0, 1.0, 4.0, 1.5, 5, 2, 2, 0, 4096);

    private static TestWorld line() {
        return new TestWorld()
                .pump(0, 0, 0, Direction.EAST)
                .channelLine(1, 0, 0, Direction.EAST, 25);
    }

    private static FlowNetwork build(TestWorld world) {
        return FlowNetwork.build(world, world.pumps(), SETTINGS);
    }

    @Test
    @DisplayName("Ohne Wasserrad gilt die Grundreichweite")
    void withoutWheelBaseRangeApplies() {
        FlowNetwork net = build(line());

        assertEquals(10, net.at(new BlockPos(1, 0, 0)).strength());
        assertNotNull(net.at(new BlockPos(10, 0, 0)));
        assertNull(net.at(new BlockPos(11, 0, 0)), "nach 10 Bloecken ist Schluss");
    }

    @Test
    @DisplayName("Ein Wasserrad verlaengert die Strecke")
    void oneWheelExtendsRange() {
        TestWorld world = line().wheel(0, 1, 0);
        FlowNetwork net = build(world);

        assertEquals(15, net.at(new BlockPos(1, 0, 0)).strength());
        assertNotNull(net.at(new BlockPos(15, 0, 0)));
        assertNull(net.at(new BlockPos(16, 0, 0)));
    }

    @Test
    @DisplayName("Zwei Wasserraeder wirken doppelt")
    void twoWheelsStack() {
        TestWorld world = line().wheel(0, 1, 0).wheel(0, -1, 0);
        FlowNetwork net = build(world);

        assertEquals(20, net.at(new BlockPos(1, 0, 0)).strength());
        assertNotNull(net.at(new BlockPos(20, 0, 0)));
    }

    @Test
    @DisplayName("Ab dem dritten Rad bringt es nichts mehr")
    void wheelsAreCappedAtMaximum() {
        TestWorld world = line()
                .wheel(0, 1, 0)
                .wheel(0, -1, 0)
                .wheel(0, 0, 1)
                .wheel(0, 0, -1);
        FlowNetwork net = build(world);

        assertEquals(20, net.at(new BlockPos(1, 0, 0)).strength(), "gedeckelt auf zwei Raeder");
    }

    @Test
    @DisplayName("Ein Rad an der falschen Stelle zaehlt nicht")
    void distantWheelIsIgnored() {
        TestWorld world = line().wheel(0, 3, 0);
        FlowNetwork net = build(world);

        assertEquals(10, net.at(new BlockPos(1, 0, 0)).strength());
    }

    @Test
    @DisplayName("Ein Wasserrad leitet selbst keine Stroemung")
    void wheelDoesNotConduct() {
        TestWorld world = new TestWorld()
                .pump(0, 0, 0, Direction.EAST)
                .wheel(1, 0, 0)
                .channel(2, 0, 0);

        FlowNetwork net = FlowNetwork.build(world, List.of(new BlockPos(0, 0, 0)), SETTINGS);

        // Nach Osten steht ein Rad, kein Kanal - dort geht nichts durch.
        assertNull(net.at(new BlockPos(1, 0, 0)));
        assertNull(net.at(new BlockPos(2, 0, 0)));
    }

    @Test
    @DisplayName("Mehr Druck macht nicht schneller, nur weiter")
    void extraPressureDoesNotSpeedUpBeyondMaximum() {
        TestWorld world = line().wheel(0, 1, 0);
        FlowNetwork net = build(world);

        FlowNode boosted = net.at(new BlockPos(1, 0, 0));
        assertEquals(15, boosted.strength(), "Druck liegt ueber dem Grundwert");
        assertEquals(SETTINGS.maxSpeed(),
                boosted.speed(SETTINGS.maxStrength(), SETTINGS.maxSpeed(), SETTINGS.minSpeed()), 1e-9,
                "Tempo bleibt bei der Hoechstgeschwindigkeit");
    }
}
