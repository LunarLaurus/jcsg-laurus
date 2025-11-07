package net.laurus.shape;

import java.awt.Color;

import eu.mihosoft.jcsg.CSG;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.laurus.data.IShape;

/**
 * Represents a single shape inside a {@link ShapeLayer}. Can be additive or
 * subtractive and optionally colored.
 */
@RequiredArgsConstructor
@AllArgsConstructor
public class CsgShape implements IShape {

    /** The name for this part */
    @Getter
    private final String name;

    /** The underlying CSG shape */
    private final CSG shape;

    /** Whether this shape should be subtracted from the layer */
    @Getter
    private final boolean subtractive;

    /** Optional color for visualization */
    @Getter
    @Setter
    private Color color;

    @Override
    public CSG getShapeCsg() {
        return shape;
    }

}
