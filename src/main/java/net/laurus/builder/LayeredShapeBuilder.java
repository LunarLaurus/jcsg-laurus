package net.laurus.builder;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import eu.mihosoft.jcsg.CSG;
import eu.mihosoft.vvecmath.Vector3d;
import lombok.Getter;
import net.laurus.data.StlData;
import net.laurus.shape.ShapeLayer;
import net.laurus.util.ShapeUtils;

/**
 * Builder for stacked {@link ShapeLayer} objects. This class only manages
 * layers and delegates combination to {@link ShapeUtils}.
 */
@Getter
public class LayeredShapeBuilder {

    private static final Color[] DEFAULT_PALETTE = new Color[] {
            new Color(100, 150, 255), new Color(255, 150, 100), new Color(150, 255, 100),
            new Color(255, 255, 100), new Color(200, 100, 255)
    };

    /** Index for automatic color assignment */
    private int colorIndex = 0;

    /** Layers managed by this builder */
    private final List<ShapeLayer> layers = new ArrayList<>();

    /**
     * Adds an existing {@link ShapeLayer} to the builder.
     *
     * @param layer the shape layer to add
     * @return this builder
     */
    public LayeredShapeBuilder addLayer(ShapeLayer layer) {

        if (layer.getColor() == null) {
            Color assigned = DEFAULT_PALETTE[colorIndex++ % DEFAULT_PALETTE.length];
            layer.setColor(assigned);
            System.out
                    .println(
                            "[LayeredShapeBuilder] Assigned color " + assigned + " to layer at Z "
                                    + layer.getZOffset()
                    );
        }

        layers.add(layer);
        System.out
                .println(
                        "[LayeredShapeBuilder] Added layer at Z " + layer.getZOffset()
                                + " (total layers: " + layers.size() + ")"
                );
        return this;
    }

    /**
     * Creates a new blank {@link ShapeLayer} with the specified Z offset, adds it
     * to the builder, and returns it.
     *
     * @param zOffset the Z offset for the new layer
     * @return the newly created {@link ShapeLayer}
     */
    public ShapeLayer addNewLayer(int zOffset) {
        ShapeLayer layer = new ShapeLayer(zOffset); // blank layer at given Z
        addLayer(layer);
        return layer;
    }

    /**
     * Builds the combined {@link CSG} of all layers. Delegates to
     * {@link ShapeUtils#combineLayers(List)}.
     *
     * @return combined CSG
     */
    public CSG build() {

        if (layers.isEmpty()) {
            throw new IllegalStateException("No layers added");
        }

        System.out
                .println(
                        "[LayeredShapeBuilder] Building combined CSG from " + layers.size()
                                + " layers"
                );
        CSG combined = ShapeUtils.combineLayers(layers);
        System.out
                .println(
                        "[LayeredShapeBuilder] Combined CSG complete: "
                                + combined.getPolygons().size() + " polygons"
                );
        return combined;
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

        for (ShapeLayer s : layers) {
            CSG transformed = s.transformed(s.getZOffset());

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

        if (layers.isEmpty()) {
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

    public StlData generateStl() {
        return StlData.from(build());
    }

}
