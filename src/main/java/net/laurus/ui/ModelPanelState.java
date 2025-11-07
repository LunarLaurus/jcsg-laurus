package net.laurus.ui;

import java.awt.Color;

public class ModelPanelState {

    private double scale;

    private double rotX;

    private double rotY;

    private boolean wireframe;

    private boolean showEdges;

    private Color solidColor;

    public ModelPanelState(ModelPanel panel) {
        this.scale = panel.getScale();
        this.rotX = panel.getRotX();
        this.rotY = panel.getRotY();
        this.wireframe = panel.isWireframe();
        this.showEdges = panel.isShowEdges();
        // this.solidColor = panel.getSolidColor();
    }

    public boolean hasChanged(ModelPanel panel) {
        return scale != panel.getScale() || rotX != panel.getRotX() || rotY != panel.getRotY()
                || wireframe != panel.isWireframe() || showEdges != panel.isShowEdges();
        /* || !solidColor.equals(panel.getSolidColor() */
    }

    public void update(ModelPanel panel) {
        scale = panel.getScale();
        rotX = panel.getRotX();
        rotY = panel.getRotY();
        wireframe = panel.isWireframe();
        showEdges = panel.isShowEdges();
        // solidColor = panel.getSolidColor();
    }

}
