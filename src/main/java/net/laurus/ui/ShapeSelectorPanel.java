package net.laurus.ui;

import javax.swing.JComboBox;
import javax.swing.JPanel;

import net.laurus.data.ShapeType;

public class ShapeSelectorPanel extends JPanel {

    public ShapeSelectorPanel(ModelPanel modelPanel) {
        JComboBox<ShapeType> shapeCombo = new JComboBox<>(ShapeType.values());
        shapeCombo.addActionListener(e -> {
            ShapeType selected = (ShapeType) shapeCombo.getSelectedItem();

            if (selected != null) {
                modelPanel.setBuilder(selected.createBuilder());
                modelPanel.refresh();
            }

        });
        add(shapeCombo);
    }

}
