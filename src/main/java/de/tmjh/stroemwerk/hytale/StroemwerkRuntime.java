package de.tmjh.stroemwerk.hytale;

import com.hypixel.hytale.server.core.universe.world.World;
import de.tmjh.stroemwerk.flow.BlockPos;
import de.tmjh.stroemwerk.flow.FlowNode;
import de.tmjh.stroemwerk.flow.FlowSettings;
import de.tmjh.stroemwerk.flow.ItemPush;
import de.tmjh.stroemwerk.flow.Vec3d;
import de.tmjh.stroemwerk.platform.ChannelNetworkService;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Haelt pro Welt ein Stroemungsnetz und schiebt die Gegenstaende darin.
 *
 * <p>Bindeglied zwischen der getesteten Rechenlogik im Paket {@code flow} und
 * dem laufenden Server.
 */
public final class StroemwerkRuntime {

    private final Map<String, ChannelNetworkService> networks = new ConcurrentHashMap<>();
    private final FlowSettings settings;
    private final int channelBlockId;
    private final int pumpBlockId;

    public StroemwerkRuntime(FlowSettings settings, int channelBlockId, int pumpBlockId) {
        this.settings = settings;
        this.channelBlockId = channelBlockId;
        this.pumpBlockId = pumpBlockId;
    }

    public ChannelNetworkService networkFor(World world) {
        return networks.computeIfAbsent(world.getName(), name ->
                new ChannelNetworkService(new HytaleWorldView(world, channelBlockId, pumpBlockId), settings));
    }

    public void forgetWorld(World world) {
        networks.remove(world.getName());
        BlockFacing.store().clear(world.getName());
    }

    /**
     * Meldet dem Netz, dass an dieser Stelle gebaut oder abgebaut wurde.
     */
    public void onBlockChanged(World world, BlockPos pos) {
        networkFor(world).onBlockChanged(pos);
    }

    /**
     * Geschwindigkeit, mit der ein Gegenstand an dieser Stelle treiben soll.
     * {@link Vec3d#ZERO}, wenn dort keine Wasserbahn liegt.
     */
    public Vec3d velocityAt(World world, double x, double y, double z) {
        BlockPos block = ItemPush.blockAt(x, y, z);
        FlowNode node = networkFor(world).flowAt(block);
        return ItemPush.velocity(node, block, x, y, z, settings);
    }

    public FlowSettings settings() {
        return settings;
    }
}
