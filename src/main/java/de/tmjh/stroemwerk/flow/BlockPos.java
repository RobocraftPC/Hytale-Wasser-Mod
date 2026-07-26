package de.tmjh.stroemwerk.flow;

/**
 * Unveraenderliche Blockkoordinate. Bewusst ohne Hytale-Typen, damit die
 * Stroemungslogik unabhaengig vom Server getestet werden kann.
 */
public record BlockPos(int x, int y, int z) implements Comparable<BlockPos> {

    public BlockPos offset(Direction dir) {
        return new BlockPos(x + dir.dx(), y + dir.dy(), z + dir.dz());
    }

    public BlockPos offset(Direction dir, int distance) {
        return new BlockPos(x + dir.dx() * distance, y + dir.dy() * distance, z + dir.dz() * distance);
    }

    @Override
    public int compareTo(BlockPos other) {
        int cmp = Integer.compare(x, other.x);
        if (cmp != 0) {
            return cmp;
        }
        cmp = Integer.compare(y, other.y);
        if (cmp != 0) {
            return cmp;
        }
        return Integer.compare(z, other.z);
    }

    @Override
    public String toString() {
        return x + "," + y + "," + z;
    }
}
