import javax.swing.*;

import ui.GameFrame;

/*
  Main
  Application entry point for Battle City
 
  Responsibilities:
    1. Apply the system look-and-feel for native window decorations.
    2. Launch the top-level GameFrame on the Swing Event Dispatch Thread.
    javac *.java && java Main
 */
public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }

            GameFrame frame = new GameFrame();
            frame.setVisible(true);
        });
    }
}