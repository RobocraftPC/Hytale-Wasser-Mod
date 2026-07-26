package de.tmjh.stroemwerk.platform;

import de.tmjh.stroemwerk.flow.BlockPos;
import de.tmjh.stroemwerk.flow.Direction;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Merkt sich pro Welt, in welche Richtung ein Bauteil platziert wurde.
 *
 * <p>Zwischenloesung, solange die Blockrotation nicht direkt aus der Welt
 * gelesen wird. Bewusst nur im Speicher: geht beim Serverneustart verloren,
 * dann muessen Pumpen einmal neu gesetzt werden.
 */
public final class PlacedFacingStore {

    private final Map<String, Map<BlockPos, Direction>> byWorld = new ConcurrentHashMap<>();

    public void put(String worldName, BlockPos pos, Direction facing) {
        byWorld.computeIfAbsent(worldName, key -> new ConcurrentHashMap<>()).put(pos, facing);
    }

    public Direction get(String worldName, BlockPos pos) {
        Map<BlockPos, Direction> world = byWorld.get(worldName);
        return world == null ? null : world.get(pos);
    }

    public void remove(String worldName, BlockPos pos) {
        Map<BlockPos, Direction> world = byWorld.get(worldName);
        if (world != null) {
            world.remove(pos);
        }
    }

    public void clear(String worldName) {
        byWorld.remove(worldName);
    }

    public int size() {
        return byWorld.values().stream().mapToInt(Map::size).sum();
    }

    /**
     * Waagerechte Richtung, in die ein Spieler mit diesem Blickwinkel schaut.
     *
     * @param yaw Blickwinkel in Grad, 0 = Sued, im Uhrzeigersinn
     */
    public static Direction facingFromYaw(float yaw) {
        int quadrant = Math.floorMod(Math.round(yaw / 90.0f), 4);
        return switch (quadrant) {
            case 0 -> Direction.SOUTH;
            case 1 -> Direction.WEST;
            case 2 -> Direction.NORTH;
            default -> Direction.EAST;
        };
    }
}
