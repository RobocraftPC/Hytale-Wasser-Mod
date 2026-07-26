package de.tmjh.stroemwerk.platform;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.tmjh.stroemwerk.flow.BlockPos;
import de.tmjh.stroemwerk.flow.Direction;
import de.tmjh.stroemwerk.flow.FlowSettings;
import de.tmjh.stroemwerk.flow.NodeType;
import de.tmjh.stroemwerk.flow.WorldView;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ChannelNetworkServiceTest {

    /** Veraenderbare Welt, damit Bauen und Abbauen getestet werden kann. */
    private static final class MutableWorld implements WorldView {
        private final Map<BlockPos, NodeType> types = new HashMap<>();
        private final Map<BlockPos, Direction> facings = new HashMap<>();

        void setChannel(BlockPos pos) {
            types.put(pos, NodeType.CHANNEL);
        }

        void setPump(BlockPos pos, Direction facing) {
            types.put(pos, NodeType.PUMP);
            facings.put(pos, facing);
        }

        void clear(BlockPos pos) {
            types.remove(pos);
            facings.remove(pos);
        }

        @Override
        public NodeType typeAt(BlockPos pos) {
            return types.getOrDefault(pos, NodeType.NONE);
        }

        @Override
        public Direction pumpFacing(BlockPos pos) {
            return facings.get(pos);
        }
    }

    private static MutableWorld worldWithLine() {
        MutableWorld world = new MutableWorld();
        world.setPump(new BlockPos(0, 0, 0), Direction.EAST);
        for (int x = 1; x <= 4; x++) {
            world.setChannel(new BlockPos(x, 0, 0));
        }
        return world;
    }

    @Test
    @DisplayName("Erster Zugriff baut das Netz auf")
    void firstAccessBuildsNetwork() {
        MutableWorld world = worldWithLine();
        ChannelNetworkService service = new ChannelNetworkService(world, FlowSettings.DEFAULT);
        service.onBlockChanged(new BlockPos(0, 0, 0));

        assertNotNull(service.flowAt(new BlockPos(1, 0, 0)));
        assertEquals(1, service.rebuildCount());
    }

    @Test
    @DisplayName("Ohne Aenderung wird nicht neu gerechnet")
    void repeatedReadsUseCache() {
        MutableWorld world = worldWithLine();
        ChannelNetworkService service = new ChannelNetworkService(world, FlowSettings.DEFAULT);
        service.onBlockChanged(new BlockPos(0, 0, 0));

        for (int i = 0; i < 50; i++) {
            service.flowAt(new BlockPos(2, 0, 0));
        }
        assertEquals(1, service.rebuildCount(), "der Zwischenspeicher haelt");
    }

    @Test
    @DisplayName("Ein neuer Kanal verlaengert die Strecke")
    void addingChannelExtendsFlow() {
        MutableWorld world = worldWithLine();
        ChannelNetworkService service = new ChannelNetworkService(world, FlowSettings.DEFAULT);
        service.onBlockChanged(new BlockPos(0, 0, 0));
        assertNull(service.flowAt(new BlockPos(5, 0, 0)));

        world.setChannel(new BlockPos(5, 0, 0));
        service.onBlockChanged(new BlockPos(5, 0, 0));

        assertNotNull(service.flowAt(new BlockPos(5, 0, 0)));
        assertEquals(2, service.rebuildCount());
    }

    @Test
    @DisplayName("Abgebauter Kanal unterbricht die Strecke dahinter")
    void removingChannelCutsFlow() {
        MutableWorld world = worldWithLine();
        ChannelNetworkService service = new ChannelNetworkService(world, FlowSettings.DEFAULT);
        service.onBlockChanged(new BlockPos(0, 0, 0));
        assertNotNull(service.flowAt(new BlockPos(4, 0, 0)));

        world.clear(new BlockPos(2, 0, 0));
        service.onBlockChanged(new BlockPos(2, 0, 0));

        assertNotNull(service.flowAt(new BlockPos(1, 0, 0)), "vor der Luecke fliesst es weiter");
        assertNull(service.flowAt(new BlockPos(4, 0, 0)), "dahinter steht alles still");
    }

    @Test
    @DisplayName("Abgebaute Pumpe legt das Netz still")
    void removingPumpStopsEverything() {
        MutableWorld world = worldWithLine();
        ChannelNetworkService service = new ChannelNetworkService(world, FlowSettings.DEFAULT);
        service.onBlockChanged(new BlockPos(0, 0, 0));
        assertNotNull(service.flowAt(new BlockPos(1, 0, 0)));

        world.clear(new BlockPos(0, 0, 0));
        service.onBlockChanged(new BlockPos(0, 0, 0));

        assertTrue(service.network().isEmpty());
        assertTrue(service.knownPumps().isEmpty());
    }

    @Test
    @DisplayName("Heimlich verschwundene Pumpen fliegen bei der Neuberechnung raus")
    void stalePumpsAreDropped() {
        MutableWorld world = worldWithLine();
        ChannelNetworkService service = new ChannelNetworkService(world, FlowSettings.DEFAULT);
        service.onBlockChanged(new BlockPos(0, 0, 0));
        assertEquals(1, service.knownPumps().size());

        // Anderes Plugin raeumt den Block weg, ohne uns zu fragen.
        world.clear(new BlockPos(0, 0, 0));
        service.invalidate();

        assertTrue(service.network().isEmpty());
        assertTrue(service.knownPumps().isEmpty());
    }

    @Test
    @DisplayName("Blickrichtung wird aus dem Gierwinkel bestimmt")
    void facingFromYaw() {
        assertEquals(Direction.SOUTH, PlacedFacingStore.facingFromYaw(0f));
        assertEquals(Direction.WEST, PlacedFacingStore.facingFromYaw(90f));
        assertEquals(Direction.NORTH, PlacedFacingStore.facingFromYaw(180f));
        assertEquals(Direction.EAST, PlacedFacingStore.facingFromYaw(270f));
        assertEquals(Direction.SOUTH, PlacedFacingStore.facingFromYaw(360f));
        assertEquals(Direction.EAST, PlacedFacingStore.facingFromYaw(-90f));
        assertEquals(Direction.SOUTH, PlacedFacingStore.facingFromYaw(20f), "leicht gedreht zaehlt noch als Sued");
    }

    @Test
    @DisplayName("Platzierte Richtungen werden pro Welt getrennt gehalten")
    void facingStoreSeparatesWorlds() {
        PlacedFacingStore store = new PlacedFacingStore();
        BlockPos pos = new BlockPos(1, 2, 3);
        store.put("orbis", pos, Direction.NORTH);
        store.put("zone1", pos, Direction.SOUTH);

        assertEquals(Direction.NORTH, store.get("orbis", pos));
        assertEquals(Direction.SOUTH, store.get("zone1", pos));

        store.remove("orbis", pos);
        assertNull(store.get("orbis", pos));
        assertEquals(1, store.size());
    }
}
