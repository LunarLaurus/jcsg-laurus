package net.laurus.shape;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import eu.mihosoft.jcsg.CSG;
import eu.mihosoft.jcsg.Cube;
import eu.mihosoft.jcsg.Cylinder;
import eu.mihosoft.vvecmath.Transform;
import eu.mihosoft.vvecmath.Vector3d;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.laurus.data.IShape;

/**
 * Represents a single Z-layer in a stacked 3D model. Each layer can contain
 * multiple {@link PrimitiveShape} objects. Additive shapes are unioned, and
 * subtractive shapes are applied as differences.
 */
@RequiredArgsConstructor
@Getter
public class ShapeLayer {

    /** Z offset of this layer */
    private final double zOffset;

    /** List of shapes in this layer */
    private final List<IShape> shapes = new ArrayList<>();

    /** Optional base layer color for visualization */
    @Getter
    @Setter
    private Color color;

    /**
     * Adds a shape to this layer.
     *
     * @param shape the layered shape to add
     */
    public void addShape(IShape shape) {

        if (shape.getColor() == null && !shape.isSubtractive()) {
            shape.setColor(color != null ? color : Color.blue);
        }

        shapes.add(shape);
        System.out
                .println(
                        "[ShapeLayer] Added " + shape.getName() + ", it is "
                                + (shape.isSubtractive() ? "a subtractive" : "an additive")
                                + " shape with color " + shape.getColor() + " at Z offset "
                                + zOffset
                );
    }

    // ----------------------
    // Convenience methods
    // ----------------------

    /**
     * Adds a cube to this layer.
     *
     * @param center      center of the cube
     * @param size        size of the cube
     * @param subtractive whether this cube should be subtracted
     */
    public void addCube(String name, Vector3d center, Vector3d size, boolean subtractive) {
        addCube(name, center, size, subtractive, null);
    }

    /**
     * Adds a cube to this layer.
     *
     * @param center      center of the cube
     * @param size        size of the cube
     * @param subtractive whether this cube should be subtracted
     * @param color       optional color (null = use layer color)
     */
    public void
            addCube(String name, Vector3d center, Vector3d size, boolean subtractive, Color color) {
        Cube cube = new Cube(center, size);
        addShape(new PrimitiveShape(name, cube, subtractive, color));
    }

    /**
     * Adds an additive cube with layer color.
     *
     * @param center center of the cube
     * @param size   size of the cube
     */
    public void addCube(String name, Vector3d center, Vector3d size) {
        addCube(name, center, size, false, null);
    }

    /**
     * Adds a cylinder to this layer.
     *
     * @param radiusTop    top radius
     * @param radiusBottom bottom radius
     * @param height       height
     * @param resolution   number of sides
     * @param subtractive  whether this cylinder should be subtracted
     * @param color        optional color (null = use layer color)
     */
    public void addCylinder(
            String name,
            double radiusTop,
            double radiusBottom,
            double height,
            int resolution,
            boolean subtractive,
            Color color
    ) {
        Cylinder cyl = new Cylinder(radiusTop, radiusBottom, height, resolution);
        addShape(new PrimitiveShape(name, cyl, subtractive, color));
    }

    /**
     * Adds a cylinder to this layer (additive, layer color).
     *
     * @param radiusTop    top radius
     * @param radiusBottom bottom radius
     * @param height       height
     * @param resolution   number of sides
     */
    public void addCylinder(
            String name,
            double radiusTop,
            double radiusBottom,
            double height,
            int resolution
    ) {
        addCylinder(name, radiusTop, radiusBottom, height, resolution, false, null);
    }

    /**
     * Adds a cylinder to this layer (subtractive, layer color).
     *
     * @param radiusTop    top radius
     * @param radiusBottom bottom radius
     * @param height       height
     * @param resolution   number of sides
     */
    public void addSubtractiveCylinder(
            String name,
            double radiusTop,
            double radiusBottom,
            double height,
            int resolution
    ) {
        addCylinder(name, radiusTop, radiusBottom, height, resolution, true, null);
    }

    /**
     * Adds a cylinder to this layer, with optional subtraction and default layer
     * color.
     *
     * @param radiusTop    top radius
     * @param radiusBottom bottom radius
     * @param height       height
     * @param resolution   number of sides
     * @param subtractive  whether this cylinder should be subtracted
     */
    public void addCylinder(
            String name,
            double radiusTop,
            double radiusBottom,
            double height,
            int resolution,
            boolean subtractive
    ) {
        addCylinder(name, radiusTop, radiusBottom, height, resolution, subtractive, null);
    }

    // ----------------------
    // Combine shapes
    // ----------------------

    /**
     * Combines all shapes in this layer into a single {@link CSG} object.
     * Subtractive shapes are applied as differences, additive shapes as unions.
     *
     * @return combined CSG of this layer
     */
    public CSG combineShapes() {
        System.out.println("[ShapeLayer] Combining shapes for layer at Z offset " + zOffset);

        List<IShape> additiveShapes = new ArrayList<>();
        List<IShape> subtractiveShapes = new ArrayList<>();

        for (IShape s : shapes) {

            if (s.isSubtractive()) {
                subtractiveShapes.add(s);
                System.out.println("[ShapeLayer] Found subtractive shape: " + s.getName());
            }
            else {
                additiveShapes.add(s);
                System.out.println("[ShapeLayer] Found additive shape: " + s.getName());
            }

        }

        // Union all additive shapes
        CSG result = null;

        for (IShape s : additiveShapes) {
            CSG transformed = s.transformed(zOffset);
            result = (result == null) ? transformed : result.union(transformed);
            System.out.println("[ShapeLayer] Added to union: " + s.getName());
        }

        if (result == null) {
            result = CSG.fromPolygons(new ArrayList<>());
            System.out.println("[ShapeLayer] No additive shapes, created empty CSG");
        }

        // Apply all subtractive shapes
        for (IShape s : subtractiveShapes) {
            CSG transformed = s.transformed(zOffset);
            result = result.difference(transformed);
            System.out.println("[ShapeLayer] Applied subtractive difference: " + s.getName());
        }

        System.out.println("[ShapeLayer] Combined CSG complete for layer at Z offset " + zOffset);
        return result;
    }

    /**
     * Computes the axis-aligned bounding box of all shapes in this layer.
     *
     * @return double[6] = {minX, maxX, minY, maxY, minZ, maxZ}
     */
    public double[] getBounds() {
        double minX = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;
        double minZ = Double.POSITIVE_INFINITY;
        double maxZ = Double.NEGATIVE_INFINITY;

        for (IShape s : shapes) {
            CSG transformed = s.transformed(zOffset);

            for (var p : transformed.getPolygons()) {

                for (var v : p.vertices) {
                    Vector3d pos = v.pos;
                    minX = Math.min(minX, pos.x());
                    maxX = Math.max(maxX, pos.x());
                    minY = Math.min(minY, pos.y());
                    maxY = Math.max(maxY, pos.y());
                    minZ = Math.min(minZ, pos.z());
                    maxZ = Math.max(maxZ, pos.z());
                }

            }

        }

        if (shapes.isEmpty()) {
            minX = minY = minZ = 0;
            maxX = maxY = maxZ = 0;
        }

        return new double[] {
                minX, maxX, minY, maxY, minZ, maxZ
        };
    }

    /** Convenience methods */
    public double getMaxX() {
        return getBounds()[1];
    }

    public double getMaxY() {
        return getBounds()[3];
    }

    public double getMaxZ() {
        return getBounds()[5];
    }

    public double getMinX() {
        return getBounds()[0];
    }

    public double getMinY() {
        return getBounds()[2];
    }

    public double getMinZ() {
        return getBounds()[4];
    }

    /**
     * Returns the shape transformed by a Z offset.
     *
     * @param zOffset the Z translation
     * @return transformed CSG
     */
    public CSG transformed(double zOffset) {
        return combineShapes().transformed(Transform.unity().translateZ(zOffset));
    }

}
