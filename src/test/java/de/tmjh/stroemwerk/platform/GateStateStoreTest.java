package de.tmjh.stroemwerk.platform;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.tmjh.stroemwerk.flow.BlockPos;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GateStateStoreTest {

    private static final BlockPos POS = new BlockPos(4, 8, 15);

    @Test
    @DisplayName("Eine frisch gesetzte Schleuse steht offen")
    void defaultsToOpen() {
        GateStateStore store = new GateStateStore();

        assertTrue(store.isOpen("orbis", POS));
        assertEquals(0, store.closedCount(), "offene Schleusen brauchen keinen Eintrag");
    }

    @Test
    @DisplayName("Umschalten wechselt den Zustand und meldet ihn zurueck")
    void toggleReturnsNewState() {
        GateStateStore store = new GateStateStore();

        assertFalse(store.toggle("orbis", POS), "erst zu");
        assertFalse(store.isOpen("orbis", POS));
        assertEquals(1, store.closedCount());

        assertTrue(store.toggle("orbis", POS), "dann wieder auf");
        assertTrue(store.isOpen("orbis", POS));
        assertEquals(0, store.closedCount());
    }

    @Test
    @DisplayName("Ein abgebauter Block hinterlaesst keinen Zustand")
    void removeClearsClosedState() {
        GateStateStore store = new GateStateStore();
        store.setOpen("orbis", POS, false);

        store.remove("orbis", POS);

        // Sonst waere eine spaeter hier gesetzte Schleuse ohne Zutun geschlossen.
        assertTrue(store.isOpen("orbis", POS));
        assertEquals(0, store.closedCount());
    }

    @Test
    @DisplayName("Welten teilen sich keine Schleusenzustaende")
    void worldsAreSeparate() {
        GateStateStore store = new GateStateStore();
        store.setOpen("orbis", POS, false);

        assertFalse(store.isOpen("orbis", POS));
        assertTrue(store.isOpen("zone1", POS));

        store.clear("orbis");
        assertTrue(store.isOpen("orbis", POS));
    }
}
