package de.tmjh.stroemwerk.hytale;

import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import de.tmjh.stroemwerk.Stroemwerk;
import de.tmjh.stroemwerk.platform.BlockIds;
import java.util.function.Consumer;

/**
 * Sucht die numerischen Block-IDs der Bauteile aus der Asset-Registry.
 *
 * <p>{@code World.getBlock(x, y, z)} liefert einen Index in die
 * BlockType-Registry, keinen Namen. Damit die Wasserbahn einen Kanal
 * wiedererkennt, muss dieser Index einmal zu Beginn nachgeschlagen werden.
 *
 */
public final class BlockIdResolver {

    private BlockIdResolver() {
    }

    /**
     * Loest alle drei Bauteile auf.
     *
     * @param onMissing wird fuer jedes Bauteil aufgerufen, das nicht gefunden
     *                  wurde - in aller Regel, weil das Asset-Pack nicht
     *                  geladen ist
     */
    public static BlockIds resolve(Consumer<String> onMissing) {
        return new BlockIds(
                indexOf(Stroemwerk.CHANNEL_ITEM_ID, onMissing),
                indexOf(Stroemwerk.PUMP_ITEM_ID, onMissing),
                indexOf(Stroemwerk.GATE_ITEM_ID, onMissing),
                indexOf(Stroemwerk.WHEEL_ITEM_ID, onMissing));
    }

    private static int indexOf(String blockTypeId, Consumer<String> onMissing) {
        var assetMap = BlockType.getAssetMap();
        if (assetMap == null) {
            onMissing.accept(blockTypeId + " (keine BlockType-Registry vorhanden)");
            return BlockIds.UNKNOWN;
        }

        BlockType type = assetMap.getAsset(blockTypeId);
        if (type == null || type.isUnknown()) {
            onMissing.accept(blockTypeId);
            return BlockIds.UNKNOWN;
        }

        return assetMap.getIndex(blockTypeId);
    }
}
