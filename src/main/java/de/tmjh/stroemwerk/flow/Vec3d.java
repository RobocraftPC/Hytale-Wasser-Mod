package de.tmjh.stroemwerk.flow;

/**
 * Einfacher Vektor fuer Geschwindigkeiten. Der Adapter uebersetzt ihn in den
 * Vektortyp des Servers.
 */
public record Vec3d(double x, double y, double z) {

    public static final Vec3d ZERO = new Vec3d(0, 0, 0);

    public Vec3d plus(Vec3d other) {
        return new Vec3d(x + other.x, y + other.y, z + other.z);
    }

    public Vec3d scaled(double factor) {
        return new Vec3d(x * factor, y * factor, z * factor);
    }

    public double length() {
        return Math.sqrt(x * x + y * y + z * z);
    }
}
