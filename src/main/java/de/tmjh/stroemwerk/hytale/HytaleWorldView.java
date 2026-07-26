package de.tmjh.stroemwerk.hytale;

import com.hypixel.hytale.server.core.universe.world.World;
import de.tmjh.stroemwerk.flow.BlockPos;
import de.tmjh.stroemwerk.flow.Direction;
import de.tmjh.stroemwerk.flow.NodeType;
import de.tmjh.stroemwerk.flow.WorldView;
import de.tmjh.stroemwerk.platform.BlockIds;
import de.tmjh.stroemwerk.platform.GateStateStore;

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
    private final BlockIds blockIds;
    private final GateStateStore gates;

    public HytaleWorldView(World world, BlockIds blockIds, GateStateStore gates) {
        this.world = world;
        this.blockIds = blockIds;
        this.gates = gates;
    }

    @Override
    public NodeType typeAt(BlockPos pos) {
        return blockIds.typeOf(world.getBlock(pos.x(), pos.y(), pos.z()));
    }

    @Override
    public Direction pumpFacing(BlockPos pos) {
        if (typeAt(pos) != NodeType.PUMP) {
            return null;
        }
        return BlockFacing.of(world, pos);
    }

    @Override
    public boolean isGateOpen(BlockPos pos) {
        return gates.isOpen(world.getName(), pos);
    }

    public World world() {
        return world;
    }

    public BlockIds blockIds() {
        return blockIds;
    }
}
