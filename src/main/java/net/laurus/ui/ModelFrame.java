package net.laurus.ui;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;

import net.laurus.data.ShapeType;

public class ModelFrame extends JFrame {

    private final ModelPanel modelPanel;

    public ModelFrame() {
        setTitle("JCSG Swing Viewer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Initial builder
        modelPanel = new ModelPanel(ShapeType.HP_PSU_HOUSING.createBuilder());
        add(modelPanel, BorderLayout.CENTER);

        // Toolbar panel
        JPanel toolbar = new JPanel();
        toolbar.add(new ShapeSelectorPanel(modelPanel));
        toolbar.add(new ToolbarPanel(modelPanel));
        add(toolbar, BorderLayout.NORTH);
    }

}
