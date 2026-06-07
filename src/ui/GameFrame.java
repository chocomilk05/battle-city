import data.ScoreManager;
import data.ScoreEntry;
import map.MapEditor;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class GameFrame extends JFrame implements PropertyChangeListener {

    // ── Card names ────────────────────────────────────────────────────────────
    public static final String CARD_MENU   = "MENU";
    public static final String CARD_GAME   = "GAME";
    public static final String CARD_EDITOR = "EDITOR";

    // ── Layout ────────────────────────────────────────────────────────────────
    private final CardLayout   cardLayout  = new CardLayout();
    private final JPanel       cardPanel   = new JPanel(cardLayout);

    // ── Core game objects (created fresh on each New Game) ────────────────────
    private GameEngine  engine;
    private GamePanel   gamePanel;

    // ── Persistent screens ────────────────────────────────────────────────────
    private final MainMenu  mainMenu;
    private       MapEditor mapEditor;  // created lazily

    // ── Menu-bar controls ─────────────────────────────────────────────────────
    private JMenuItem pauseItem;   // toggles Pause / Resume
    private JMenuItem newGameItem;

    // ─────────────────────────────────────────────────────────────────────────
    //  Constructor
    // ─────────────────────────────────────────────────────────────────────────

    public GameFrame() {
        super("Battle City");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        // ── Main menu ─────────────────────────────────────────────────────────
        mainMenu = new MainMenu(this);
        cardPanel.add(mainMenu, CARD_MENU);

        // ── Placeholder game panel (real one created on New Game) ─────────────
        cardPanel.add(new JPanel(), CARD_GAME);   // replaced on first new game
        cardPanel.add(new JPanel(), CARD_EDITOR); // replaced on first editor open

        setContentPane(cardPanel);
        setJMenuBar(buildMenuBar());

        showMenu();
        pack();
        setLocationRelativeTo(null);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Menu bar
    // ─────────────────────────────────────────────────────────────────────────

    private JMenuBar buildMenuBar() {
        JMenuBar bar = new JMenuBar();
        bar.setBackground(new Color(30, 30, 30));
        bar.setBorderPainted(false);

        // ── Game menu ─────────────────────────────────────────────────────────
        JMenu gameMenu = darkMenu("Game");

        newGameItem = darkItem("New Game", KeyEvent.VK_N, e -> startNewGame());
        gameMenu.add(newGameItem);

        pauseItem = darkItem("Pause", KeyEvent.VK_P, e -> togglePause());
        pauseItem.setVisible(false);
        gameMenu.add(pauseItem);

        gameMenu.addSeparator();
        gameMenu.add(darkItem("Map Editor", KeyEvent.VK_M, e -> openMapEditor()));
        gameMenu.addSeparator();
        gameMenu.add(darkItem("Exit", KeyEvent.VK_Q, e -> confirmExit()));
        bar.add(gameMenu);

        // ── View menu ─────────────────────────────────────────────────────────
        JMenu viewMenu = darkMenu("View");
        viewMenu.add(darkItem("High Scores", KeyEvent.VK_H, e -> showHighScores()));
        bar.add(viewMenu);

        // ── Help menu ─────────────────────────────────────────────────────────
        JMenu helpMenu = darkMenu("Help");
        helpMenu.add(darkItem("How to Play", KeyEvent.VK_F1, e -> showHelp()));
        helpMenu.addSeparator();
        helpMenu.add(darkItem("Options", 0, e -> showOptions()));
        helpMenu.addSeparator();
        helpMenu.add(darkItem("About", 0, e -> showAbout()));
        bar.add(helpMenu);

        return bar;
    }

    // ── Factory helpers for dark-themed menu components ───────────────────────

    private JMenu darkMenu(String text) {
        JMenu m = new JMenu(text);
        m.setForeground(Color.LIGHT_GRAY);
        m.setFont(new Font("Monospaced", Font.BOLD, 13));
        return m;
    }

    private JMenuItem darkItem(String text, int mnemonic, ActionListener al) {
        JMenuItem item = new JMenuItem(text);
        item.setBackground(new Color(30, 30, 30));
        item.setForeground(Color.LIGHT_GRAY);
        item.setFont(new Font("Monospaced", Font.PLAIN, 13));
        if (mnemonic != 0) item.setMnemonic(mnemonic);
        item.addActionListener(al);
        return item;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Card navigation
    // ─────────────────────────────────────────────────────────────────────────

    /** Show the main menu and stop any running game. */
    public void showMenu() {
        if (gamePanel != null) gamePanel.stopGame();
        pauseItem.setVisible(false);
        cardLayout.show(cardPanel, CARD_MENU);
        pack();
    }

    /** Create a fresh engine + panel, load level 1, and show the game card. */
    public void startNewGame() {
        // Stop previous game if any
        if (gamePanel != null) {
            gamePanel.stopGame();
            gamePanel.removePropertyChangeListener(this);
        }

        engine    = new GameEngine();
        gamePanel = new GamePanel(engine);
        gamePanel.addPropertyChangeListener(this);

        // Replace the GAME card
        cardPanel.remove(getCardComponent(CARD_GAME));
        cardPanel.add(gamePanel, CARD_GAME);
        cardPanel.revalidate();

        engine.resetGame();   // loads level 1
        cardLayout.show(cardPanel, CARD_GAME);
        pauseItem.setVisible(true);
        pauseItem.setText("Pause");
        pack();
        gamePanel.startGame();
    }

    /** Open / re-open the map editor. */
    private void openMapEditor() {
        if (gamePanel != null) gamePanel.stopGame();
        pauseItem.setVisible(false);

        if (mapEditor == null) {
            mapEditor = new MapEditor();
        }
        cardPanel.remove(getCardComponent(CARD_EDITOR));
        cardPanel.add(mapEditor, CARD_EDITOR);
        cardPanel.revalidate();

        cardLayout.show(cardPanel, CARD_EDITOR);
        pack();
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  In-game actions
    // ─────────────────────────────────────────────────────────────────────────

    private void togglePause() {
        if (gamePanel == null) return;
        gamePanel.togglePause();
        pauseItem.setText(gamePanel.isPaused() ? "Resume" : "Pause");
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  PropertyChangeListener – receives GAME_OVER / LEVEL_COMPLETE
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        SwingUtilities.invokeLater(() -> {
            switch (evt.getPropertyName()) {
                case "GAME_OVER"      -> handleGameOver();
                case "LEVEL_COMPLETE" -> handleLevelComplete();
            }
        });
    }

    private void handleGameOver() {
        pauseItem.setVisible(false);
        // Ask for player name and save score
        String name = JOptionPane.showInputDialog(this,
            "GAME OVER!\nYour score: " + engine.getScore() +
            "\n\nEnter your name to save your score:",
            "Game Over", JOptionPane.PLAIN_MESSAGE);
        if (name != null && !name.isBlank()) {
            ScoreManager.save(new ScoreEntry(
                name.trim(),
                engine.getScore(),
                LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE),
                LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
            ));
        }
        showMenu();
    }

    private void handleLevelComplete() {
        int next = engine.getLevelNumber() + 1;
        JOptionPane.showMessageDialog(this,
            "Level " + engine.getLevelNumber() + " complete!\nGet ready for level " + next + "…",
            "Level Complete", JOptionPane.INFORMATION_MESSAGE);
        engine.nextLevel();
        gamePanel.startGame();
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Dialog launchers
    // ─────────────────────────────────────────────────────────────────────────

    private void showHighScores() {
        new HighScoreScreen(this).setVisible(true);
    }

    private void showHelp() {
        new HelpScreen(this).setVisible(true);
    }

    private void showOptions() {
        new OptionsScreen(this).setVisible(true);
    }

    private void showAbout() {
        new AboutDialog(this).setVisible(true);
    }

    private void confirmExit() {
        int choice = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to exit?", "Exit",
            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (choice == JOptionPane.YES_OPTION) System.exit(0);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Utility
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Find the component currently occupying a named card slot.
     * We walk the cardPanel's children to find the one with the matching name.
     */
    private Component getCardComponent(String name) {
        for (Component c : cardPanel.getComponents()) {
            if (name.equals(c.getName())) return c;
        }
        // CardLayout doesn't store names on components by default,
        // so we add them via setName when we build each card component.
        return new JPanel(); // safe fallback
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Entry point
    // ─────────────────────────────────────────────────────────────────────────

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
            new GameFrame().setVisible(true);
        });
    }
}