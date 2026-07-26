package de.tmjh.stroemwerk.hytale;

import com.hypixel.hytale.server.core.universe.world.World;
import de.tmjh.stroemwerk.flow.BlockPos;
import de.tmjh.stroemwerk.flow.FlowNode;
import de.tmjh.stroemwerk.flow.FlowSettings;
import de.tmjh.stroemwerk.flow.ItemPush;
import de.tmjh.stroemwerk.flow.NodeType;
import de.tmjh.stroemwerk.flow.Vec3d;
import de.tmjh.stroemwerk.platform.BlockIds;
import de.tmjh.stroemwerk.platform.ChannelNetworkService;
import de.tmjh.stroemwerk.platform.GateStateStore;
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
    private final GateStateStore gates = new GateStateStore();
    private final FlowSettings settings;
    private final BlockIds blockIds;

    public StroemwerkRuntime(FlowSettings settings, BlockIds blockIds) {
        this.settings = settings;
        this.blockIds = blockIds;
    }

    public ChannelNetworkService networkFor(World world) {
        return networks.computeIfAbsent(world.getName(), name ->
                new ChannelNetworkService(new HytaleWorldView(world, blockIds, gates), settings));
    }

    public void forgetWorld(World world) {
        networks.remove(world.getName());
        BlockFacing.store().clear(world.getName());
        gates.clear(world.getName());
    }

    /**
     * Meldet dem Netz, dass an dieser Stelle gebaut oder abgebaut wurde.
     */
    public void onBlockChanged(World world, BlockPos pos) {
        networkFor(world).onBlockChanged(pos);
    }

    /**
     * Meldet, dass ein Bauteil abgebaut wurde. Zusaetzlich zum Netz muessen die
     * gemerkten Zustaende weg, sonst erbt ein spaeter an dieselbe Stelle
     * gesetzter Block die alte Ausrichtung oder eine geschlossene Schleuse.
     */
    public void onBlockRemoved(World world, BlockPos pos) {
        BlockFacing.forget(world, pos);
        gates.remove(world.getName(), pos);
        networkFor(world).onBlockChanged(pos);
    }

    /**
     * Schaltet eine Schleuse um und liefert den neuen Zustand. Steht dort keine
     * Schleuse, passiert nichts und es kommt {@code null} zurueck.
     */
    public Boolean toggleGate(World world, BlockPos pos) {
        ChannelNetworkService service = networkFor(world);
        if (service.typeAt(pos) != NodeType.GATE) {
            return null;
        }
        boolean nowOpen = gates.toggle(world.getName(), pos);
        service.invalidate();
        return nowOpen;
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

    public BlockIds blockIds() {
        return blockIds;
    }

    public GateStateStore gates() {
        return gates;
    }
}
