package de.tmjh.stroemwerk.platform;

import de.tmjh.stroemwerk.flow.NodeType;

/**
 * Die Block-IDs der Bauteile, wie der Server sie vergeben hat.
 *
 * <p>Sie stehen erst beim Start fest und werden von dort durchgereicht. Ist ein
 * Bauteil nicht auffindbar, bleibt seine ID {@link #UNKNOWN} - dann verhaelt
 * sich der Mod so, als gaebe es das Bauteil nicht, statt zufaellig irgendeinen
 * Block als Kanal zu behandeln.
 */
public record BlockIds(int channel, int pump, int gate) {

    /** Nicht aufgeloest. Wird nie einem echten Block zugeordnet. */
    public static final int UNKNOWN = -1;

    public static final BlockIds NONE_RESOLVED = new BlockIds(UNKNOWN, UNKNOWN, UNKNOWN);

    /**
     * Ordnet eine Block-ID einem Bauteil zu.
     */
    public NodeType typeOf(int blockId) {
        if (blockId == UNKNOWN) {
            return NodeType.NONE;
        }
        if (blockId == channel) {
            return NodeType.CHANNEL;
        }
        if (blockId == pump) {
            return NodeType.PUMP;
        }
        if (blockId == gate) {
            return NodeType.GATE;
        }
        return NodeType.NONE;
    }

    /** Ob ueberhaupt ein Bauteil aufgeloest werden konnte. */
    public boolean anyResolved() {
        return channel != UNKNOWN || pump != UNKNOWN || gate != UNKNOWN;
    }
}
