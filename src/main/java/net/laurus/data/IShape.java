package net.laurus.data;

import java.awt.Color;

import eu.mihosoft.jcsg.CSG;

/**
 * Interface for shape elements that can be added to layered models.
 */
public interface IShape {

    /** @return the name for this part */
    String getName();

    /** @return true if this shape should be subtracted from the layer */
    boolean isSubtractive();

    /** @return optional color for visualization (may be null) */
    Color getColor();

    /** Set the optional color for visualization */
    void setColor(Color color);

    /** @return the raw CSG shape */
    CSG getShapeCsg();

    /**
     * Returns the shape transformed by a Z offset.
     *
     * @param zOffset the Z translation
     * @return transformed CSG
     */
    default CSG transformed(double zOffset) {
        return getShapeCsg().transformed(eu.mihosoft.vvecmath.Transform.unity().translateZ(zOffset));
    }

}
