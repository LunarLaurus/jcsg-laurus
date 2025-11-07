package net.laurus.util;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import eu.mihosoft.jcsg.CSG;
import net.laurus.data.IShape;
import net.laurus.shape.ColoredTriangle;
import net.laurus.shape.ShapeLayer;
import net.laurus.shape.Triangle;

/**
 * Utility methods for working with {@link IShape} and {@link ShapeLayer}.
 */
public final class ShapeUtils {

    private ShapeUtils() {
        // prevent instantiation
    }

    /** Combine all shapes in a layer list into a single CSG */
    public static CSG combineLayers(List<ShapeLayer> layers) {

        System.out
                .println(
                        "combineLayers called with " + (layers == null ? 0 : layers.size())
                                + " layers."
                );

        if (layers == null || layers.isEmpty()) {
            System.out.println("No layers provided, returning empty CSG.");
            return CSG.fromPolygons(new ArrayList<>());
        }

        CSG result = null;

        for (int i = 0; i < layers.size(); i++) {
            ShapeLayer layer = layers.get(i);
            System.out
                    .println(
                            "Combining layer " + i + " with " + layer.getShapes().size()
                                    + " shapes."
                    );
            CSG layerCSG = layer.combineShapes();
            result = result == null ? layerCSG : result.union(layerCSG);
            System.out
                    .println(
                            "Layer " + i + " combined. Current result has polygons: "
                                    + result.getPolygons().size()
                    );
        }

        System.out.println("All layers combined successfully.");
        return result;
    }

    /** Extract additive shapes from a layer */
    public static List<IShape> getAdditiveShapes(ShapeLayer layer) {
        System.out
                .println(
                        "Extracting additive shapes from layer with " + layer.getShapes().size()
                                + " shapes."
                );
        List<IShape> result = new ArrayList<>();

        for (IShape s : layer.getShapes()) {

            if (!s.isSubtractive()) {
                System.out.println("Adding shape: " + s.getName());
                result.add(s);
            }

        }

        System.out.println("Found " + result.size() + " additive shapes.");
        return result;
    }

    /** Extract subtractive shapes from a layer */
    public static List<IShape> getSubtractiveShapes(ShapeLayer layer) {
        System.out
                .println(
                        "Extracting subtractive shapes from layer with " + layer.getShapes().size()
                                + " shapes."
                );
        List<IShape> result = new ArrayList<>();

        for (IShape s : layer.getShapes()) {

            if (s.isSubtractive()) {
                System.out.println("Adding shape: " + s.getName());
                result.add(s);
            }

        }

        System.out.println("Found " + result.size() + " subtractive shapes.");
        return result;
    }

    /** Collect triangles from a CSG object */
    public static List<Triangle> collectTriangles(CSG model) {
        List<Triangle> tris = new ArrayList<>();

        if (model == null) {
            return tris;
        }

        for (var p : model.getPolygons()) {
            var verts = p.vertices;

            if (verts == null || verts.size() < 3) {
                continue;
            }

            var v0 = verts.get(0).pos;

            for (int i = 1; i < verts.size() - 1; i++) {
                tris.add(new Triangle(v0, verts.get(i).pos, verts.get(i + 1).pos));
            }

        }

        return tris;
    }

    /** Build colored triangles for solid rendering */
    public static List<ColoredTriangle> buildSolidColoredTriangles(List<ShapeLayer> layers) {
        List<ColoredTriangle> coloredTris = new ArrayList<>();

        for (ShapeLayer layer : layers) {
            double zOffset = layer.getZOffset();
            Color layerColor = layer.getColor() != null ? layer.getColor() : Color.BLUE;

            // Start with empty CSG
            CSG combinedCSG = CSG.fromPolygons(new ArrayList<>());

            // Union additive shapes
            for (IShape s : getAdditiveShapes(layer)) {
                combinedCSG = combinedCSG.union(transformZ(s, zOffset));
            }

            // Subtract subtractive shapes
            for (IShape s : getSubtractiveShapes(layer)) {
                combinedCSG = combinedCSG.difference(transformZ(s, zOffset));
            }

            for (Triangle t : collectTriangles(combinedCSG)) {
                coloredTris.add(new ColoredTriangle(t, layerColor, false));
            }

        }

        return coloredTris;
    }

    /** Build colored triangles for wireframe visualization */
    public static List<ColoredTriangle> buildWireframeTriangles(List<ShapeLayer> layers) {
        List<ColoredTriangle> coloredTris = new ArrayList<>();

        for (ShapeLayer layer : layers) {
            double zOffset = layer.getZOffset();
            Color layerColor = layer.getColor() != null ? layer.getColor() : Color.BLUE;

            // Additive shapes
            for (IShape s : getAdditiveShapes(layer)) {
                CSG addCSG = transformZ(s, zOffset);

                for (Triangle t : collectTriangles(addCSG)) {
                    coloredTris.add(new ColoredTriangle(t, layerColor, false));
                }

            }

            // Subtractive shapes (always red)
            for (IShape s : getSubtractiveShapes(layer)) {
                CSG subCSG = transformZ(s, zOffset);

                for (Triangle t : collectTriangles(subCSG)) {
                    coloredTris.add(new ColoredTriangle(t, Color.RED, true));
                }

            }

        }

        return coloredTris;
    }

    /** Apply Z offset to a shape */
    public static CSG transformZ(IShape shape, double zOffset) {
        return shape.transformed(zOffset);
    }

}
