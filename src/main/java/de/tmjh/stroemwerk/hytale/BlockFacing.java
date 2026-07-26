package de.tmjh.stroemwerk.hytale;

import com.hypixel.hytale.server.core.universe.world.World;
import de.tmjh.stroemwerk.flow.BlockPos;
import de.tmjh.stroemwerk.flow.Direction;
import de.tmjh.stroemwerk.platform.PlacedFacingStore;

/**
 * Liest die Ausrichtung eines platzierten Blocks aus.
 *
 * <p>ANPASSEN: Wie Hytale die Rotation eines Blocks ablegt, ist in dieser
 * Fassung noch nicht gegen die echte Server-Jar geprueft. Bis dahin merkt sich
 * das Plugin die Blickrichtung beim Platzieren selbst
 * ({@link PlacedFacingStore}) - das funktioniert, ueberlebt aber keinen
 * Serverneustart.
 *
 * <p>Sobald der Rotationswert aus der API bekannt ist, sollte {@link #of} ihn
 * direkt aus der Welt lesen und der Zwischenspeicher entfallen.
 */
public final class BlockFacing {

    private static final PlacedFacingStore FALLBACK = new PlacedFacingStore();

    private BlockFacing() {
    }

    public static Direction of(World world, BlockPos pos) {
        return FALLBACK.get(world.getName(), pos);
    }

    /**
     * Merkt sich die Blickrichtung, mit der ein Bauteil platziert wurde.
     */
    public static void remember(World world, BlockPos pos, Direction facing) {
        FALLBACK.put(world.getName(), pos, facing);
    }

    public static void forget(World world, BlockPos pos) {
        FALLBACK.remove(world.getName(), pos);
    }

    public static PlacedFacingStore store() {
        return FALLBACK;
    }
}
