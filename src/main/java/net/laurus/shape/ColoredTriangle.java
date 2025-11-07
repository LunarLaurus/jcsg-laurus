package net.laurus.shape;

import java.awt.Color;

/**
 * Triangle associated with a color (from its layer)
 */
public class ColoredTriangle {

    public final Triangle triangle;

    public final Color color;

    public final boolean subtractive;

    public ColoredTriangle(Triangle triangle, Color color, boolean subtractive) {
        this.triangle = triangle;
        this.color = color;
        this.subtractive = subtractive;
    }

}
