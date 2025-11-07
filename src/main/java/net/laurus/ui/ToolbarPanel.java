package net.laurus.ui;

import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;

public class ToolbarPanel extends JPanel {

    public ToolbarPanel(ModelPanel modelPanel) {
        // Wireframe toggle
        JButton wireframeBtn = new JButton("Toggle Wireframe");
        wireframeBtn.addActionListener(e -> {
            modelPanel.setWireframe(!modelPanel.isWireframe());
            modelPanel.refresh();
        });
        add(wireframeBtn);

        // Edge highlights
        JButton edgeHighlightBtn = new JButton("Toggle Edge Highlights");
        edgeHighlightBtn.addActionListener(e -> {
            modelPanel.setShowEdges(!modelPanel.isShowEdges());
            modelPanel.refresh();
        });
        add(edgeHighlightBtn);

        // Export STL
        JButton exportBtn = new JButton("Export STL");
        exportBtn.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Export STL File");
            fileChooser.setSelectedFile(new File("model.stl"));

            int userSelection = fileChooser.showSaveDialog(this);

            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();

                try {
                    var stlData = modelPanel.getBuilder().generateStl();
                    boolean success = stlData.writeToFile(file.getAbsolutePath());

                    if (success) {
                        System.out.println("[Export] STL file saved to: " + file.getAbsolutePath());
                    }
                    else {
                        System.err.println("[Export] Failed to write STL file.");
                    }

                }
                catch (Exception ex) {
                    ex.printStackTrace();
                }

            }

        });
        add(exportBtn);
    }

}
