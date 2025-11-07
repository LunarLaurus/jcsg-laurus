package net.laurus.builder;

import eu.mihosoft.vvecmath.Vector3d;
import net.laurus.shape.ShapeLayer;

/**
 * Builds sample geometry using the updated layered shape system.
 */
public class ModelBuilder {

    /**
     * Builds a 42×36×2 cm plate with three 12×12 cm holes
     */
    public static LayeredShapeBuilder buildPlateWithThreeSquareHoles() {
        LayeredShapeBuilder builder = new LayeredShapeBuilder();

        // Layer at Z=0
        ShapeLayer baseLayer = builder.addNewLayer(0);

        // Base plate (additive)
        baseLayer.addCube("Base Plate", Vector3d.xyz(0, 0, 0), Vector3d.xyz(42, 36, 2));

        // Three holes (subtractive)
        double holeW = 12, holeH = 12;
        double gap = (42 - 3 * holeW) / 4.0;
        double xStart = -21 + gap + holeW / 2.0;

        for (int i = 0; i < 3; i++) {
            double x = xStart + i * (holeW + gap);
            baseLayer
                    .addCube(
                            "Hole " + (i + 1), Vector3d.xyz(x, 0, 1), Vector3d
                                    .xyz(holeW, holeH, 4), true
                    );
        }

        return builder;
    }

    /** Builds a more complex layered block */
    public static LayeredShapeBuilder buildComplexLayeredBlock() {
        LayeredShapeBuilder builder = new LayeredShapeBuilder();

        // Layer 0: base plate + rectangular holes
        ShapeLayer layer0 = builder.addNewLayer(0);
        layer0.addCube("Base Plate", Vector3d.xyz(0, 0, 2), Vector3d.xyz(60, 40, 4));

        double holeW = 12, holeH = 12;
        double gapX = (60 - 3 * holeW) / 4.0;
        double xStart = -30 + gapX + holeW / 2.0;

        for (int i = 0; i < 3; i++) {
            double x = xStart + i * (holeW + gapX);
            layer0
                    .addCube(
                            "Hole " + (i + 1), Vector3d.xyz(x, 0, 2), Vector3d
                                    .xyz(holeW, holeH, 6), true
                    );
        }

        // Layer 1: raised central block + cylinder hole
        ShapeLayer layer1 = builder.addNewLayer(8);
        layer1.addCube("Raised Block", Vector3d.xyz(0, 0, 6), Vector3d.xyz(20, 20, 8));
        layer1.addCylinder("Central Cylinder Hole", 5, 5, 20, 32, true);

        // Layer 2: small side cube
        ShapeLayer layer2 = builder.addNewLayer(16);
        layer2.addCube("Side Cube", Vector3d.xyz(15, 10, 12), Vector3d.xyz(10, 10, 5));

        return builder;
    }

}
