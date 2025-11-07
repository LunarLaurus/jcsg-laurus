package net.laurus.builder;

import eu.mihosoft.vvecmath.Vector3d;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.laurus.shape.ShapeLayer;

/**
 * Utility class to build configurable plates with holes and routing slots.
 */
@Getter
@RequiredArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class SuperMicroFanPlateBuilder {

    // Plate dimensions
    @Builder.Default
    final double plateWidth = 420;

    @Builder.Default
    final double plateDepth = 25;

    @Builder.Default
    final double plateHeight = 360;

    // Fan slot configuration
    @Builder.Default
    int fanSlots = 3;

    @Builder.Default
    double fanWidth = 120;

    @Builder.Default
    double fanDepth = 40;

    @Builder.Default
    double fanHeight = 120;

    @Builder.Default
    double fanZOffset = 0;

    // Cable routing configuration
    @Builder.Default
    boolean addCableRouting = false;

    @Builder.Default
    double cableWidth = 300;

    @Builder.Default
    double cableDepth = 40;

    @Builder.Default
    double cableHeight = 30;

    @Builder.Default
    double cableZOffset = 0;

    /**
     * Builds the layered plate shape.
     */
    public LayeredShapeBuilder build() {
        System.out.println("[SuperMicroFanPlateBuilder] Starting build...");

        LayeredShapeBuilder builder = new LayeredShapeBuilder();
        ShapeLayer baseLayer = builder.addNewLayer(0);

        addBasePlate(baseLayer);
        addFanSlots(baseLayer);
        addCableRoutingHole(baseLayer);

        double[] bounds = baseLayer.getBounds();
        System.out
                .printf(
                        "[SuperMicroFanPlateBuilder] Final bounding box: X[%.2f, %.2f], Y[%.2f, %.2f], Z[%.2f, %.2f]%n", bounds[0], bounds[1], bounds[2], bounds[3], bounds[4], bounds[5]
                );

        System.out
                .printf(
                        "[SuperMicroFanPlateBuilder] Build complete: width=%.2f, depth=%.2f, height=%.2f%n", plateWidth, plateDepth, plateHeight
                );
        return builder;
    }

    /** Adds the main base plate to the layer */
    private void addBasePlate(ShapeLayer layer) {
        System.out
                .printf(
                        "[SuperMicroFanPlateBuilder] Adding base plate at (0,0,0) with size %.2f x %.2f x %.2f%n", plateWidth, plateDepth, plateHeight
                );

        layer
                .addCube(
                        "Base Plate", Vector3d.xyz(0, 0, 0), Vector3d
                                .xyz(plateWidth, plateDepth, plateHeight)
                );
    }

    /** Adds the fan slots as subtractive cubes */
    private void addFanSlots(ShapeLayer layer) {

        if (fanSlots <= 0) {
            System.out.println("[SuperMicroFanPlateBuilder] No fan slots to add.");
            return;
        }

        double totalGap = plateWidth - fanSlots * fanWidth;

        if (totalGap < 0) {
            throw new IllegalArgumentException("Fan slots exceed plate width");
        }

        double gap = totalGap / (fanSlots + 1);

        for (int i = 0; i < fanSlots; i++) {
            double x = -plateWidth / 2 + gap * (i + 1) + fanWidth * (i + 0.5);
            System.out
                    .printf(
                            "[SuperMicroFanPlateBuilder] Adding fan slot %d at X=%.2f, size %.2f x %.2f x %.2f (subtractive)%n", i
                                    + 1, x, fanWidth, fanDepth, fanHeight
                    );

            layer
                    .addCube(
                            "Fan Slot " + (i + 1), Vector3d.xyz(x, 0, fanZOffset), Vector3d
                                    .xyz(fanWidth, fanDepth, fanHeight), true // subtractive
                    );
        }

    }

    /** Adds a cable routing hole at the bottom */
    private void addCableRoutingHole(ShapeLayer layer) {

        if (!addCableRouting) {
            System.out.println("[SuperMicroFanPlateBuilder] Cable routing not enabled.");
            return;
        }

        double[] bounds = layer.getBounds();
        double minZ = bounds[4];
        double holeCenterZ = minZ + cableHeight / 2.0 + cableZOffset + 40;
        double holeCenterX = (bounds[0] + bounds[1]) / 2.0;
        double holeCenterY = (bounds[2] + bounds[3]) / 2.0;

        System.out
                .printf(
                        "[SuperMicroFanPlateBuilder] Adding cable routing hole at (%.2f, %.2f, %.2f) with size %.2f x %.2f x %.2f (subtractive)%n", holeCenterX, holeCenterY, holeCenterZ, cableWidth, cableDepth, cableHeight
                );

        layer
                .addCube(
                        "Cable Routing Hole", Vector3d
                                .xyz(holeCenterX, holeCenterY, holeCenterZ), Vector3d
                                        .xyz(cableWidth, cableDepth, cableHeight), true // subtractive
                );
    }

}
