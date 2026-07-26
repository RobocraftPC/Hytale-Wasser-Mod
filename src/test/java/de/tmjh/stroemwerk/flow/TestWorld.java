package de.tmjh.stroemwerk.flow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handgebaute Welt fuer die Tests: eine Map statt eines Servers.
 */
final class TestWorld implements WorldView {

    private final Map<BlockPos, NodeType> types = new HashMap<>();
    private final Map<BlockPos, Direction> facings = new HashMap<>();
    private final List<BlockPos> pumps = new ArrayList<>();

    TestWorld channel(int x, int y, int z) {
        types.put(new BlockPos(x, y, z), NodeType.CHANNEL);
        return this;
    }

    /** Legt eine Kanalstrecke von {@code count} Bloecken ab Startpunkt. */
    TestWorld channelLine(int x, int y, int z, Direction dir, int count) {
        BlockPos pos = new BlockPos(x, y, z);
        for (int i = 0; i < count; i++) {
            types.put(pos, NodeType.CHANNEL);
            pos = pos.offset(dir);
        }
        return this;
    }

    TestWorld pump(int x, int y, int z, Direction facing) {
        BlockPos pos = new BlockPos(x, y, z);
        types.put(pos, NodeType.PUMP);
        facings.put(pos, facing);
        pumps.add(pos);
        return this;
    }

    List<BlockPos> pumps() {
        return pumps;
    }

    FlowNetwork build() {
        return FlowNetwork.build(this, pumps);
    }

    FlowNetwork build(int maxStrength) {
        return FlowNetwork.build(this, pumps, maxStrength);
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
