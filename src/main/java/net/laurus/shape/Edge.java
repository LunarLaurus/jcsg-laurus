package net.laurus.shape;

import java.util.Objects;

import eu.mihosoft.jcsg.Vertex;
import eu.mihosoft.vvecmath.Vector3d;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * Represents an undirected edge between two vertices (value-based equality).
 * Order of vertices does not matter. Uses a small epsilon to handle
 * floating-point inaccuracies.
 */
@Getter
@Accessors(fluent = true)
@ToString
public class Edge {

    private static final double EPSILON = 1e-6; // tolerance for floating-point comparison

    private static final int SCALE = 1_000_000; // for hashCode quantization

    private final Vector3d p1;

    private final Vector3d p2;

    public Edge(Vertex a, Vertex b) {
        this(a.pos, b.pos);
    }

    public Edge(Vector3d a, Vector3d b) {

        // Order vertices consistently
        if (compareVector(a, b) <= 0) {
            this.p1 = a;
            this.p2 = b;
        }
        else {
            this.p1 = b;
            this.p2 = a;
        }

    }

    /**
     * Lexicographical comparison of vectors.
     */
    private static int compareVector(Vector3d a, Vector3d b) {
        int cmpX = Double.compare(a.x(), b.x());

        if (cmpX != 0) {
            return cmpX;
        }

        int cmpY = Double.compare(a.y(), b.y());

        if (cmpY != 0) {
            return cmpY;
        }

        return Double.compare(a.z(), b.z());
    }

    /**
     * Equality with tolerance.
     */
    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }

        if (!(o instanceof Edge e)) {
            return false;
        }

        return equalsVector(p1, e.p1) && equalsVector(p2, e.p2);
    }

    @Override
    public int hashCode() {
        // Quantize coordinates to maintain consistency with equals
        return Objects
                .hash(
                        quantize(p1.x()), quantize(p1.y()), quantize(p1.z()), quantize(
                                p2.x()
                        ), quantize(p2.y()), quantize(p2.z())
                );
    }

    private static boolean equalsVector(Vector3d a, Vector3d b) {
        return Math.abs(a.x() - b.x()) < EPSILON && Math.abs(a.y() - b.y()) < EPSILON
                && Math.abs(a.z() - b.z()) < EPSILON;
    }

    private static int quantize(double value) {
        return (int) (value * SCALE);
    }

}
