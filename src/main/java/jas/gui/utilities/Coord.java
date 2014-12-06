package jas.gui.utilities;

/**
 * Essentially an immutable version of java.awt.Point
 */
public class Coord {

    public final int x;
    public final int z;

    public Coord(int x, int z) {
        this.x = x;
        this.z = z;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof Coord)) {
            return false;
        }
        Coord grid = (Coord) obj;
        return x == grid.x && z == grid.z;
    }

    @Override
    public int hashCode() {
        return x ^ RotateLeft(z, 16);
    }

    private int RotateLeft(int value, int count) {
        return (value << count) | (value >> (32 - count));
    }

    public float distance(Coord coord) {
        return (float) Math.sqrt(distanceSQ(coord));
    }

    public float distanceSQ(Coord coord) {
        float xDis = x - coord.x;
        float zDis = z - coord.z;
        return xDis * xDis + zDis * zDis;
    }

    public Coord add(Coord coord) {
        return new Coord(x + coord.x, z + coord.z);
    }

    public Coord add(int x, int z) {
        return new Coord(x + this.x, z + this.z);
    }

    public Coord subt(Coord coord) {
        return new Coord(x - coord.x, z - coord.z);
    }

    public Coord subt(int x, int z) {
        return new Coord(this.x - x, this.z - z);
    }

    public Coord mult(int scale) {
        return new Coord(x * scale, z * scale);
    }

    public Coord mult(int scaleX, int scaleZ) {
        return new Coord(x * scaleX, z * scaleZ);
    }

    public Coord mult(Coord scale) {
        return new Coord(x * scale.x, z * scale.z);
    }

    public Coord multf(float scale) {
        return new Coord((int) (x * scale), (int) (z * scale));
    }

    public Coord multf(float scaleX, float scaleZ) {
        return new Coord((int) (x * scaleX), (int) (z * scaleZ));
    }

    public Coord div(int scale) {
        return new Coord(x / scale, z / scale);
    }

    public Coord div(int scaleX, int scaleZ) {
        return new Coord(x / scaleX, z / scaleZ);
    }

    public Coord div(Coord scale) {
        return new Coord(x / scale.x, z / scale.z);
    }

    public Coord divf(float scale) {
        return new Coord((int) (x / scale), (int) (z / scale));
    }

    public Coord divf(float scaleX, float scaleZ) {
        return new Coord((int) (x / scaleX), (int) (z / scaleZ));
    }

    @Override
    public String toString() {
        return "[" + x + "," + z + "]";
    }
}
