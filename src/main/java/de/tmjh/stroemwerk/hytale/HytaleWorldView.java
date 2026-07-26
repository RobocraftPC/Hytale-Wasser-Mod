package de.tmjh.stroemwerk.hytale;

import com.hypixel.hytale.server.core.universe.world.World;
import de.tmjh.stroemwerk.Stroemwerk;
import de.tmjh.stroemwerk.flow.BlockPos;
import de.tmjh.stroemwerk.flow.Direction;
import de.tmjh.stroemwerk.flow.NodeType;
import de.tmjh.stroemwerk.flow.WorldView;

/**
 * Uebersetzt die Hytale-Welt in die schlanke {@link WorldView}, mit der die
 * Stroemungsberechnung arbeitet.
 *
 * <p>Das ist die einzige Stelle, an der die Wasserbahn Bloecke aus der Welt
 * liest. Aendert sich die Server-API, muss nur diese Klasse nachgezogen werden.
 *
 * <p>Alle Aufrufe gehoeren auf den Thread der Welt.
 */
public final class HytaleWorldView implements WorldView {

    private final World world;
    private final int channelBlockId;
    private final int pumpBlockId;

    public HytaleWorldView(World world, int channelBlockId, int pumpBlockId) {
        this.world = world;
        this.channelBlockId = channelBlockId;
        this.pumpBlockId = pumpBlockId;
    }

    @Override
    public NodeType typeAt(BlockPos pos) {
        int id = world.getBlock(pos.x(), pos.y(), pos.z());
        if (id == channelBlockId) {
            return NodeType.CHANNEL;
        }
        if (id == pumpBlockId) {
            return NodeType.PUMP;
        }
        return NodeType.NONE;
    }

    @Override
    public Direction pumpFacing(BlockPos pos) {
        if (typeAt(pos) != NodeType.PUMP) {
            return null;
        }
        return BlockFacing.of(world, pos);
    }

    public World world() {
        return world;
    }

    public int channelBlockId() {
        return channelBlockId;
    }

    public int pumpBlockId() {
        return pumpBlockId;
    }

    /**
     * Bezeichner der Bauteile, wie sie in den Item-Definitionen stehen.
     */
    public static String channelItemId() {
        return Stroemwerk.CHANNEL_ITEM_ID;
    }

    public static String pumpItemId() {
        return Stroemwerk.PUMP_ITEM_ID;
    }
}
