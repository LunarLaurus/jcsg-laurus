package net.laurus.builder;

import eu.mihosoft.vvecmath.Vector3d;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import net.laurus.shape.ShapeLayer;
import net.laurus.util.Graphics3DUtils;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class PsuPcbEnclosureBuilder {

    @Builder.Default
    final double boxWidth = 120; // X

    @Builder.Default
    final double boxDepth = 210; // Y

    @Builder.Default
    final double boxHeight = 60; // Z

    @Builder.Default
    final double psuWidth = 86; // X

    @Builder.Default
    final double psuDepth = 197; // Y

    @Builder.Default
    final double psuHeight = 32; // Z

    @Builder.Default
    final double pcbWidth = 90; // X

    @Builder.Default
    final double pcbDepth = 32; // Y

    @Builder.Default
    final double pcbHeight = 47; // Z

    @Builder.Default
    final double gapBetween = 5;

    @Builder.Default
    final double cavityRatio = 0.9;

    final boolean debugReferenceSizes = false;

    public LayeredShapeBuilder build() {
        System.out.println("[PsuPcbEnclosureBuilder] Starting build...");

        LayeredShapeBuilder builder = new LayeredShapeBuilder();

        // Outer box
        ShapeLayer boxLayer = builder.addNewLayer(0);
        addOuterBox(boxLayer);

        // Subtractive cavity
        addInnerCavity(boxLayer);

        ShapeLayer supportLayer = builder.addNewLayer(0);
        addPanels(supportLayer);
        double cavityHeight = boxHeight * cavityRatio;

        addRailSupport(supportLayer, -cavityHeight / 2 + 0.05 * cavityHeight);
        // addRailSupport(supportLayer, -cavityHeight / 2 + 0.4 * cavityHeight);

        if (debugReferenceSizes) {
            // PSU layer
            ShapeLayer psuLayer = builder.addNewLayer(0);
            addPsuBox(psuLayer);
            // PCB layer
            ShapeLayer pcbLayer = builder.addNewLayer(0);
            addPcbBox(pcbLayer);
        }

        // Final bounding box
        double[] bounds = boxLayer.getBounds();
        System.out
                .printf(
                        "[PsuPcbEnclosureBuilder] Final bounding box: X[%.2f, %.2f], Y[%.2f, %.2f], Z[%.2f, %.2f]%n", bounds[0], bounds[1], bounds[2], bounds[3], bounds[4], bounds[5]
                );

        System.out.println("[PsuPcbEnclosureBuilder] Build complete.");
        return builder;
    }

    private void addOuterBox(ShapeLayer layer) {
        System.out
                .printf(
                        "[PsuPcbEnclosureBuilder] Adding outer box size %.2f x %.2f x %.2f%n", boxWidth, boxDepth, boxHeight
                );
        layer
                .addCube(
                        "Outer Box", Vector3d.xyz(0, 0, 0), Vector3d
                                .xyz(
                                        boxWidth, boxDepth, boxHeight
                                ), false, Graphics3DUtils.randomColor()
                );
    }

    private void addInnerCavity(ShapeLayer layer) {
        double w = boxWidth * cavityRatio;
        double d = boxDepth * 1.1; // keep cutting through full length
        double h = boxHeight * cavityRatio;

        System.out
                .printf(
                        "[PsuPcbEnclosureBuilder] Adding inner cavity size %.2f x %.2f x %.2f (subtractive)%n", w, d, h
                );

        layer.addCube("Inner Cavity", Vector3d.xyz(0, 0, 0), Vector3d.xyz(w, d, h), true, null);
    }

    private void addPanels(ShapeLayer layer) {
        double w = boxWidth * cavityRatio;
        double wallHeight = boxHeight - pcbHeight;
        double wallDepth = 10;

        double wallX = 0;
        double wallY = boxDepth / 2 - 5;
        double wallZ = -boxHeight / 2 + (wallHeight / 2) + boxHeight * 0.05; // 5% offset from
                                                                             // bottom

        layer
                .addCube(
                        "Front Wall", Vector3d.xyz(wallX, wallY, wallZ), Vector3d
                                .xyz(w, wallDepth, wallHeight)
                );
    }

    private void addRailSupport(ShapeLayer layer, double railZ) {
        double w = boxWidth * cavityRatio;
        double railDepth = boxDepth * 0.65;
        double depthOffset = (boxDepth - railDepth) / 2;
        double sizeW = (w - psuWidth) / 2;
        double sizeH = (w - psuWidth) / 2;

        double leftX = -boxWidth / 2 + (boxWidth - w);
        layer
                .addCube(
                        "Left Corner Fill", Vector3d.xyz(leftX - 3, -depthOffset, railZ), Vector3d
                                .xyz(sizeW, railDepth, sizeH)
                );

        double rightX = boxWidth / 2 - (boxWidth - w);
        layer
                .addCube(
                        "Right Corner Fill", Vector3d.xyz(rightX + 3, -depthOffset, railZ), Vector3d
                                .xyz(sizeW, railDepth, sizeH)
                );
    }

    private void addPsuBox(ShapeLayer layer) {
        double x = 0;
        double y = -45;
        double z = (-boxHeight / 2) + (psuHeight / 2) + 5;

        System.out
                .printf(
                        "[PsuPcbEnclosureBuilder] Adding PSU box at (%.2f, %.2f, %.2f) size %.2f x %.2f x %.2f%n", x, y, z, psuWidth, psuDepth, psuHeight
                );

        layer
                .addCube(
                        "PSU Box", Vector3d.xyz(x, y, z), Vector3d
                                .xyz(
                                        psuWidth, psuDepth, psuHeight
                                ), false, Graphics3DUtils.randomColor()
                );
    }

    private void addPcbBox(ShapeLayer layer) {
        double x = 0;
        double y = boxDepth / 2 - 30;
        double z = (-boxHeight / 2) + (psuHeight / 2) + 15;

        System.out
                .printf(
                        "[PsuPcbEnclosureBuilder] Adding PCB box at (%.2f, %.2f, %.2f) size %.2f x %.2f x %.2f%n", x, y, z, pcbWidth, pcbDepth, pcbHeight
                );

        layer
                .addCube(
                        "PCB Box", Vector3d.xyz(x, y, z), Vector3d
                                .xyz(
                                        pcbWidth, pcbDepth, pcbHeight
                                ), false, Graphics3DUtils.randomColor()
                );
    }

}
