package net.laurus.builder;

import eu.mihosoft.jcsg.CSG;
import eu.mihosoft.jcsg.Cube;
import eu.mihosoft.vvecmath.Vector3d;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import net.laurus.shape.CsgShape;
import net.laurus.shape.ShapeLayer;

/**
 * Takes an existing SuperMicroFanPlateBuilder and splits its plate model into
 * two interlocking halves with matching alignment tabs.
 */
@Getter
@Builder
@FieldDefaults(
        level = AccessLevel.PRIVATE,
        makeFinal = true
)
public class SplitFanPlateBuilder {

    SuperMicroFanPlateBuilder source;

    // --- Tab settings ---
    @Builder.Default
    double tabWidth = 30;

    @Builder.Default
    double tabHeight = 20;

    @Builder.Default
    double tabDepth = 20;

    // --- Half separation ---
    @Builder.Default
    double halfSeparation = 40; // space between left/right halves

    /**
     * Builds the split model.
     */
    public LayeredShapeBuilder build() {
        System.out.println("[SplitFanPlateBuilder] Starting split build...");

        // 1️⃣ Build the original full plate and combine CSG
        LayeredShapeBuilder full = source.build();
        CSG fullCSG = full.build();

        double[] bounds = full.getBounds();
        double minX = bounds[0], maxX = bounds[1];
        double minY = bounds[2], maxY = bounds[3];
        double minZ = bounds[4], maxZ = bounds[5];

        double midX = (minX + maxX) / 2.0;
        double halfWidth = (maxX - minX) / 2.0;
        double height = maxZ - minZ;
        double depth = maxY - minY;

        System.out
                .printf(
                        "[SplitFanPlateBuilder] Splitting plate at X=%.2f (half width=%.2f)%n", midX, halfWidth
                );

        // 2️⃣ Cutting cubes for left/right halves with separation
        double leftHalfWidth = halfWidth - halfSeparation / 2.0;
        double rightHalfWidth = halfWidth - halfSeparation / 2.0;

        CSG leftCut = new Cube(
                Vector3d.xyz(midX - halfSeparation / 2.0 - leftHalfWidth / 2.0, 0, 0),
                Vector3d.xyz(leftHalfWidth, depth * 2, height * 2)
        ).toCSG();

        CSG rightCut = new Cube(
                Vector3d.xyz(midX + halfSeparation / 2.0 + rightHalfWidth / 2.0, 0, 0),
                Vector3d.xyz(rightHalfWidth, depth * 2, height * 2)
        ).toCSG();

        // 3️⃣ Intersect each half with original plate
        System.out.println("[SplitFanPlateBuilder] Performing CSG intersection for halves...");
        CSG leftHalf = fullCSG.intersect(leftCut);
        CSG rightHalf = fullCSG.intersect(rightCut);

        // 4️⃣ Create layered builder and add halves
        LayeredShapeBuilder splitBuilder = new LayeredShapeBuilder();
        ShapeLayer left = splitBuilder.addNewLayer(0);
        ShapeLayer right = splitBuilder.addNewLayer(0);

        left.addShape(new CsgShape("Left Half", leftHalf, false, null));
        right.addShape(new CsgShape("Right Half", rightHalf, false, null));

        // 5️⃣ Add interlock tabs
        addTabs(left, right, minX, maxX, minZ, maxZ, depth);

        System.out.println("[SplitFanPlateBuilder] Split build complete.");
        return splitBuilder;
    }

    /**
     * Adds alternating interlock tabs along the central split line.
     */
    private void addTabs(
            ShapeLayer left,
            ShapeLayer right,
            double minX,
            double maxX,
            double minZ,
            double maxZ,
            double depth
    ) {
        System.out.println("[SplitFanPlateBuilder] Adding interlock tabs...");

        double midX = (minX + maxX) / 2.0;
        double totalHeight = maxZ - minZ;

        // --- percentages for tab positions along Z ---
        double[] tabPositions = {
                0.05, 0.26, 0.75, 0.9
        };

        double tabShift = halfSeparation / 2.0;

        for (int i = 0; i < tabPositions.length; i++) {
            double zCenter = minZ + tabPositions[i] * totalHeight; // position relative to total
                                                                   // height
            boolean leftHasTab = (i % 2 == 0);

            if (leftHasTab) {
                left
                        .addCube(
                                "Left Tab " + (i + 1), Vector3d
                                        .xyz(midX + tabWidth / 2.0 - tabShift, 0, zCenter), Vector3d
                                                .xyz(tabWidth, tabDepth, tabHeight)
                        );
                right
                        .addCube(
                                "Right Recess " + (i + 1), Vector3d
                                        .xyz(midX + tabWidth / 2.0 + tabShift, 0, zCenter), Vector3d
                                                .xyz(tabWidth, tabDepth * 2, tabHeight), true
                        );
            }
            else {
                right
                        .addCube(
                                "Right Tab " + (i + 1), Vector3d
                                        .xyz(midX - tabWidth / 2.0 + tabShift, 0, zCenter), Vector3d
                                                .xyz(tabWidth, tabDepth, tabHeight)
                        );
                left
                        .addCube(
                                "Left Recess " + (i + 1), Vector3d
                                        .xyz(midX - tabWidth / 2.0 - tabShift, 0, zCenter), Vector3d
                                                .xyz(tabWidth, tabDepth * 2, tabHeight), true
                        );
            }

        }

        System.out
                .printf(
                        "[SplitFanPlateBuilder] Added %d tabs at specified relative heights.%n", tabPositions.length
                );
    }

    private void addTabsVerticalInterlocking(
            ShapeLayer left,
            ShapeLayer right,
            double minX,
            double maxX,
            double minZ,
            double maxZ,
            double fullDepth // total plate depth
    ) {
        System.out.println("[SplitFanPlateBuilder] Adding vertical interlocking tabs...");

        double midX = (minX + maxX) / 2.0;
        double totalHeight = maxZ - minZ;

        double[] tabPositions = {
                0.05, 0.26, 0.75, 0.9
        };
        double tabHalfDepth = tabDepth / 2.0; // each half only occupies half the depth
        double tabShift = halfSeparation / 2.0;

        for (int i = 0; i < tabPositions.length; i++) {
            double zCenter = minZ + tabPositions[i] * totalHeight;
            boolean leftStartsTab = (i % 2 == 0);

            if (leftStartsTab) {
                // Left half tab (front half of depth)
                left
                        .addCube(
                                "Left Tab " + (i + 1), Vector3d
                                        .xyz(
                                                midX - tabWidth - tabShift, -tabHalfDepth
                                                        / 2.0, zCenter
                                        ), Vector3d.xyz(tabWidth, tabHalfDepth, tabHeight), true
                        );

                // Right half tab (back half of depth, recessed)
                right
                        .addCube(
                                "Right Recess " + (i + 1), Vector3d
                                        .xyz(midX + tabShift, tabHalfDepth / 2.0, zCenter), Vector3d
                                                .xyz(tabWidth, tabHalfDepth, tabHeight), true
                        );
            }
            else {
                // Right half tab (back half of depth)
                right
                        .addCube(
                                "Right Tab " + (i + 1), Vector3d
                                        .xyz(midX + tabShift, tabHalfDepth / 2.0, zCenter), Vector3d
                                                .xyz(tabWidth, tabHalfDepth, tabHeight), true
                        );

                // Left half tab (front half of depth, recessed)
                left
                        .addCube(
                                "Left Recess " + (i + 1), Vector3d
                                        .xyz(
                                                midX - tabWidth - tabShift, -tabHalfDepth
                                                        / 2.0, zCenter
                                        ), Vector3d.xyz(tabWidth, tabHalfDepth, tabHeight), true
                        );
            }

        }

        System.out
                .printf(
                        "[SplitFanPlateBuilder] Added %d vertical interlocking tabs with half-depth.%n", tabPositions.length
                );
    }

}
