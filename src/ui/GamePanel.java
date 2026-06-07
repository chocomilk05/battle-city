package ui;
import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.util.Set;
import java.util.HashSet;

/**
 * GamePanel
 * ---------
 * The main rendering surface and game-loop driver for Battle City.
 *
 * Responsibilities:
 *  - Owns the Swing Timer that ticks the game at ~60 FPS.
 *  - Delegates all logic updates to GameEngine.
 *  - Paints the current Level map, all tanks, bullets, power-ups, HUD, etc.
 *  - Forwards keyboard input to the engine.
 *  - Exposes pause/resume/stop controls used by the parent JFrame menu.
 */
public class GamePanel extends JPanel implements ActionListener, KeyListener {

    // ── Constants ────────────────────────────────────────────────────────────
    public static final int TILE_SIZE   = 32;   // px per map tile
    public static final int COLS        = 26;   // map columns
    public static final int ROWS        = 26;   // map rows
    public static final int MAP_WIDTH   = COLS * TILE_SIZE;   // 832
    public static final int MAP_HEIGHT  = ROWS * TILE_SIZE;   // 832
    public static final int HUD_WIDTH   = 120;  // right-side HUD strip
    public static final int FPS         = 60;
    public static final int TICK_MS     = 1000 / FPS;

    // ── Core references ──────────────────────────────────────────────────────
    private final GameEngine engine;
    private final Timer      gameTimer;

    // ── Keyboard state ───────────────────────────────────────────────────────
    /** Keys currently held down (by keyCode). */
    private final Set<Integer> keysHeld = new HashSet<>();

    // ── Images / sprites  (loaded lazily or via SpriteSheet) ─────────────────
    // Replace with real sprite loading once you have the image pack.
    // private SpriteSheet sprites;

    // ── State flags ──────────────────────────────────────────────────────────
    private boolean paused = false;

    // ─────────────────────────────────────────────────────────────────────────
    //  Constructor
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * @param engine The fully-constructed game engine to drive.
     */
    public GamePanel(GameEngine engine) {
        this.engine = engine;

        setPreferredSize(new Dimension(MAP_WIDTH + HUD_WIDTH, MAP_HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);

        // Timer drives the game loop.
        gameTimer = new Timer(TICK_MS, this);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Game-loop control
    // ─────────────────────────────────────────────────────────────────────────

    /** Start (or restart) the game loop. */
    public void startGame() {
        paused = false;
        gameTimer.start();
        requestFocusInWindow();
    }

    /** Pause / resume toggle. */
    public void togglePause() {
        paused = !paused;
        if (paused) gameTimer.stop();
        else        { gameTimer.start(); requestFocusInWindow(); }
        repaint();
    }

    public boolean isPaused() { return paused; }

    /** Permanently stop the game (called on game-over or exit). */
    public void stopGame() {
        gameTimer.stop();
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  ActionListener – Swing Timer tick
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public void actionPerformed(ActionEvent e) {
        // 1. Feed held keys into the engine so movement is smooth.
        engine.handleInput(keysHeld);

        // 2. Advance all game logic by one frame.
        engine.update();

        // 3. Schedule a repaint.
        repaint();

        // 4. Check terminal conditions.
        if (engine.isGameOver()) {
            stopGame();
            firePropertyChange("GAME_OVER", false, true);
        } else if (engine.isLevelComplete()) {
            stopGame();
            firePropertyChange("LEVEL_COMPLETE", false, true);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Painting
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        // — Map / terrain —
        drawMap(g2);

        // — Game entities (tanks, bullets, power-ups) —
        engine.render(g2);

        // — HUD panel on the right —
        drawHUD(g2);

        // — Pause overlay —
        if (paused) drawPauseOverlay(g2);
    }

    /**
     * Draws the tile-based map from the current Level.
     */
    private void drawMap(Graphics2D g) {
        Level level = engine.getCurrentLevel();
        if (level == null) return;

        int[][] map = level.getMap();
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                int tileType = map[row][col];
                int x = col * TILE_SIZE;
                int y = row * TILE_SIZE;
                drawTile(g, tileType, x, y);
            }
        }
    }

    /**
     * Renders a single map tile.
     * Replace the colour fill with sprite draws once you have the image pack.
     *
     * Tile type constants are defined in Level:
     *   Level.EMPTY, BRICK, STEEL, WATER, BUSH, BASE
     */
    private void drawTile(Graphics2D g, int type, int x, int y) {
        switch (type) {
            case Level.EMPTY -> { /* black background already drawn */ }
            case Level.BRICK -> {
                g.setColor(new Color(180, 80, 0));
                g.fillRect(x, y, TILE_SIZE, TILE_SIZE);
                g.setColor(new Color(100, 40, 0));
                g.drawRect(x, y, TILE_SIZE - 1, TILE_SIZE - 1);
            }
            case Level.STEEL -> {
                g.setColor(new Color(160, 160, 160));
                g.fillRect(x, y, TILE_SIZE, TILE_SIZE);
                g.setColor(Color.WHITE);
                g.drawRect(x, y, TILE_SIZE - 1, TILE_SIZE - 1);
            }
            case Level.WATER -> {
                g.setColor(new Color(0, 100, 200));
                g.fillRect(x, y, TILE_SIZE, TILE_SIZE);
            }
            case Level.BUSH -> {
                // Drawn last so it covers tanks (engine.render draws bushes second pass).
                g.setColor(new Color(0, 160, 0));
                g.fillRect(x, y, TILE_SIZE, TILE_SIZE);
            }
            case Level.BASE -> {
                g.setColor(Color.YELLOW);
                // Simple star/eagle placeholder.
                int cx = x + TILE_SIZE / 2, cy = y + TILE_SIZE / 2;
                int r = TILE_SIZE / 2 - 2;
                g.fillOval(cx - r, cy - r, 2 * r, 2 * r);
                g.setColor(Color.BLACK);
                g.drawString("⬛", x + 4, y + TILE_SIZE - 4);
            }
            default -> {
                g.setColor(Color.DARK_GRAY);
                g.fillRect(x, y, TILE_SIZE, TILE_SIZE);
            }
        }
    }

    /**
     * Draws the right-side HUD: lives, enemy count, level number, score.
     */
    private void drawHUD(Graphics2D g) {
        int hx = MAP_WIDTH + 4;
        g.setColor(new Color(40, 40, 40));
        g.fillRect(MAP_WIDTH, 0, HUD_WIDTH, MAP_HEIGHT);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Monospaced", Font.BOLD, 13));

        int y = 24;
        g.drawString("SCORE", hx, y);
        y += 18;
        g.setColor(Color.ORANGE);
        g.drawString(String.valueOf(engine.getScore()), hx, y);

        y += 30;
        g.setColor(Color.WHITE);
        g.drawString("LEVEL", hx, y);
        y += 18;
        g.setColor(Color.CYAN);
        g.drawString(String.valueOf(engine.getLevelNumber()), hx, y);

        y += 30;
        g.setColor(Color.WHITE);
        g.drawString("LIVES", hx, y);
        y += 18;
        for (int i = 0; i < engine.getLives(); i++) {
            g.setColor(Color.GREEN);
            g.fillRect(hx + (i % 2) * 22, y + (i / 2) * 22, 16, 16);
        }

        y += ((engine.getLives() + 1) / 2) * 22 + 12;
        g.setColor(Color.WHITE);
        g.drawString("ENEMY", hx, y);
        y += 18;
        g.setColor(Color.RED);
        g.drawString("x " + engine.getRemainingEnemies(), hx, y);

        // Stars (power-up level)
        y += 30;
        g.setColor(Color.WHITE);
        g.drawString("STARS", hx, y);
        y += 18;
        int stars = engine.getPlayerStars();
        for (int i = 0; i < 3; i++) {
            g.setColor(i < stars ? Color.YELLOW : Color.GRAY);
            g.drawString("★", hx + i * 22, y);
        }
    }

    /** Semi-transparent "PAUSED" overlay. */
    private void drawPauseOverlay(Graphics2D g) {
        g.setColor(new Color(0, 0, 0, 140));
        g.fillRect(0, 0, MAP_WIDTH, MAP_HEIGHT);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Monospaced", Font.BOLD, 36));
        FontMetrics fm = g.getFontMetrics();
        String msg = "PAUSED";
        int tx = (MAP_WIDTH - fm.stringWidth(msg)) / 2;
        int ty = MAP_HEIGHT / 2;
        g.drawString(msg, tx, ty);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  KeyListener
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public void keyPressed(KeyEvent e) {
        keysHeld.add(e.getKeyCode());

        // Pause shortcut: Escape or P
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE || e.getKeyCode() == KeyEvent.VK_P) {
            togglePause();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        keysHeld.remove(e.getKeyCode());
    }

    @Override
    public void keyTyped(KeyEvent e) { /* unused */ }
}