package de.tmjh.stroemwerk.flow;

/**
 * Rechnet aus, mit welcher Geschwindigkeit ein Gegenstand im Kanal treibt.
 *
 * <p>Entlang der Stroemung wird geschoben, quer dazu zur Kanalmitte gezogen -
 * sonst schrammen Gegenstaende an der Kanalwand entlang und bleiben an Kurven
 * haengen. Reine Rechnung, kein Weltzugriff.
 */
public final class ItemPush {

    private ItemPush() {
    }

    /**
     * Zielgeschwindigkeit fuer einen Gegenstand, der auf {@code block} treibt.
     *
     * @param node   Stroemung an dieser Position, {@code null} wenn dort nichts fliesst
     * @param block  Kanalblock, auf dem der Gegenstand liegt
     * @param itemX  aktuelle Weltkoordinaten des Gegenstands
     */
    public static Vec3d velocity(FlowNode node, BlockPos block, double itemX, double itemY, double itemZ,
                                 FlowSettings settings) {
        if (node == null) {
            return Vec3d.ZERO;
        }

        Direction dir = node.direction();
        double speed = node.speed(settings.maxStrength(), settings.maxSpeed(), settings.minSpeed());

        double vx = dir.dx() * speed;
        double vy = dir.dy() * speed;
        double vz = dir.dz() * speed;

        // Quer zur Stroemung zur Blockmitte ziehen. Die Stroemungsachse bleibt
        // unangetastet, damit die Zentrierung nicht gegen den Transport arbeitet.
        if (dir.dx() == 0) {
            vx += centering(block.x(), itemX, settings);
        }
        if (dir.dy() == 0) {
            vy += centering(block.y(), itemY, settings);
        }
        if (dir.dz() == 0) {
            vz += centering(block.z(), itemZ, settings);
        }

        return new Vec3d(vx, vy, vz);
    }

    private static double centering(int blockCoord, double itemCoord, FlowSettings settings) {
        double delta = (blockCoord + 0.5) - itemCoord;
        double pull = delta * settings.centering();
        return Math.max(-settings.maxCentering(), Math.min(settings.maxCentering(), pull));
    }

    /**
     * Blockposition, auf der sich eine Weltkoordinate befindet.
     */
    public static BlockPos blockAt(double x, double y, double z) {
        return new BlockPos(
                (int) Math.floor(x),
                (int) Math.floor(y),
                (int) Math.floor(z));
    }
}
