package net.laurus.util;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Path2D;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import eu.mihosoft.vvecmath.Vector3d;
import lombok.experimental.UtilityClass;
import net.laurus.shape.Edge;
import net.laurus.shape.Triangle;

@UtilityClass
public final class Graphics3DUtils {

    /** 
     * Generates a random opaque Color.
     */
    public static Color randomColor() {
        ThreadLocalRandom rand = ThreadLocalRandom.current();
        int r = rand.nextInt(0, 256);
        int g = rand.nextInt(0, 256);
        int b = rand.nextInt(0, 256);
        return new Color(r, g, b);
    }

    // --- Math Utilities ---

    public static double[] multiply(double[][] m, double[] v) {
        double[] r = new double[3];

        for (int i = 0; i < 3; i++) {
            r[i] = m[i][0] * v[0] + m[i][1] * v[1] + m[i][2] * v[2];
        }

        return r;
    }

    public static double[][] rotationMatrix(double rx, double ry) {
        double cx = Math.cos(Math.toRadians(rx));
        double sx = Math.sin(Math.toRadians(rx));
        double cy = Math.cos(Math.toRadians(ry));
        double sy = Math.sin(Math.toRadians(ry));

        return new double[][] {
                {
                        cy, sx * sy, cx * sy
                }, {
                        0, cx, -sx
                }, {
                        -sy, sx * cy, cx * cy
                }
        };
    }

    /**
     * Maps Vector3d vertices to 2D screen coordinates. Uses the exact vertex
     * instances from triangles for reliable lookups.
     */
    public static Map<Vector3d, Point>
            computeVertexMap(List<Triangle> tris, double[][] rotMat, int cx, int cy, double scale) {
        return tris
                .stream()
                .flatMap(t -> Stream.of(t.a(), t.b(), t.c()))
                .distinct() // ensures exact vertex instances
                .collect(Collectors.toMap(v -> v, v ->
                {
                    double[] r = multiply(rotMat, new double[] {
                            v.x(), v.y(), v.z()
                    });
                    return new Point((int) (cx + r[0] * scale), (int) (cy - r[1] * scale));
                }));
    }

    // --- Boundary Edge Utilities ---

    /**
     * Computes boundary edges: edges appearing exactly once.
     */
    public static Set<Edge> computeBoundaryEdges(List<Triangle> triangles) {
        Set<Edge> once = new HashSet<>();
        Set<Edge> multiple = new HashSet<>();

        for (Triangle t : triangles) {

            for (Edge e : List
                    .of(new Edge(t.a(), t.b()), new Edge(t.b(), t.c()), new Edge(t.c(), t.a()))) {

                if (!once.add(e)) {
                    multiple.add(e);
                }

            }

        }

        once.removeAll(multiple);
        return once;
    }

    // --- Drawing Utilities ---

    public static void drawTriangle(Graphics2D g2, Map<Vector3d, Point> vertexMap, Triangle t) {
        Point p0 = vertexMap.get(t.a());
        Point p1 = vertexMap.get(t.b());
        Point p2 = vertexMap.get(t.c());

        if (p0 == null || p1 == null || p2 == null)
            return;

        Path2D path = new Path2D.Double();
        path.moveTo(p0.x, p0.y);
        path.lineTo(p1.x, p1.y);
        path.lineTo(p2.x, p2.y);
        path.closePath();

        g2.fill(path);
    }

    public static void
            drawTriangleEdges(Graphics2D g2, Map<Vector3d, Point> vertexMap, Triangle t) {
        Point p0 = vertexMap.get(t.a());
        Point p1 = vertexMap.get(t.b());
        Point p2 = vertexMap.get(t.c());

        if (p0 == null || p1 == null || p2 == null)
            return;

        g2.drawLine(p0.x, p0.y, p1.x, p1.y);
        g2.drawLine(p1.x, p1.y, p2.x, p2.y);
        g2.drawLine(p2.x, p2.y, p0.x, p0.y);
    }

    public static void drawEdges(Graphics2D g2, Map<Vector3d, Point> vertexMap, Set<Edge> edges) {

        for (Edge e : edges) {
            Point p1 = vertexMap.get(e.p1());
            Point p2 = vertexMap.get(e.p2());

            if (p1 != null && p2 != null) {
                g2.drawLine(p1.x, p1.y, p2.x, p2.y);
            }

        }

    }

}
