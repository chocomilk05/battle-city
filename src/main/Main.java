import javax.swing.*;

/**
 * Main
 * ----
 * Application entry point for Battle City.
 *
 * Responsibilities:
 *   1. Apply the system look-and-feel for native window decorations.
 *   2. Launch the top-level GameFrame on the Swing Event Dispatch Thread.
 *
 * All game logic, rendering, and UI live in GameFrame and its children.
 * This class intentionally contains nothing else — keeping the entry
 * point as thin and readable as possible.
 *
 * Run:
 *   javac *.java && java Main
 */
public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Use the OS native look-and-feel (Windows / macOS / GTK)
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // Falls back to Java's default cross-platform L&F — acceptable.
            }

            GameFrame frame = new GameFrame();
            frame.setVisible(true);
        });
    }
}