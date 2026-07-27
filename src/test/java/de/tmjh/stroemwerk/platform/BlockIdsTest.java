package de.tmjh.stroemwerk.platform;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.tmjh.stroemwerk.flow.NodeType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BlockIdsTest {

    @Test
    @DisplayName("Bekannte IDs werden ihrem Bauteil zugeordnet")
    void mapsKnownIds() {
        BlockIds ids = new BlockIds(10, 20, 30, 40);

        assertEquals(NodeType.CHANNEL, ids.typeOf(10));
        assertEquals(NodeType.PUMP, ids.typeOf(20));
        assertEquals(NodeType.GATE, ids.typeOf(30));
        assertEquals(NodeType.WHEEL, ids.typeOf(40));
        assertEquals(NodeType.NONE, ids.typeOf(999));
    }

    @Test
    @DisplayName("Nicht aufgeloeste Bauteile schnappen sich keine fremden Bloecke")
    void unresolvedNeverMatches() {
        // Luft hat oft die ID 0, ein nicht gefundenes Bauteil die -1. Ohne
        // Sonderbehandlung wuerde jeder unbekannte Block zum Kanal.
        BlockIds ids = new BlockIds(BlockIds.UNKNOWN, 20, BlockIds.UNKNOWN, BlockIds.UNKNOWN);

        assertEquals(NodeType.NONE, ids.typeOf(BlockIds.UNKNOWN));
        assertEquals(NodeType.NONE, ids.typeOf(0));
        assertEquals(NodeType.PUMP, ids.typeOf(20));
    }

    @Test
    @DisplayName("Ohne aufgeloeste IDs meldet sich der Mod als nicht bereit")
    void reportsWhetherAnythingResolved() {
        assertFalse(BlockIds.NONE_RESOLVED.anyResolved());
        assertTrue(new BlockIds(1, BlockIds.UNKNOWN, BlockIds.UNKNOWN, BlockIds.UNKNOWN).anyResolved());
    }
}
