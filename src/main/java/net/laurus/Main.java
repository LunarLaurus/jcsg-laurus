package net.laurus;

import javax.swing.SwingUtilities;

import net.laurus.ui.ModelFrame;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ModelFrame().setVisible(true));
    }

}
