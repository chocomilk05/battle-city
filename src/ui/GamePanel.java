package ui;

import util.SpriteRegistry;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.Set;
import java.util.HashSet;

// GamePanel
 
public class GamePanel extends JPanel implements ActionListener, KeyListener {

    // Constants
    public static final int TILE_SIZE  = 32;
    public static final int COLS       = 26;
    public static final int ROWS       = 26;
    public static final int MAP_WIDTH  = COLS * TILE_SIZE;   // 832
    public static final int MAP_HEIGHT = ROWS * TILE_SIZE;   // 832
    public static final int HUD_WIDTH  = 120;
    public static final int FPS        = 60;
    public static final int TICK_MS    = 1000 / FPS;

    // Core references
    private final GameEngine engine;
    private final Timer      gameTimer;

    // Keyboard state
    private final Set<Integer> keysHeld = new HashSet<>();

    // State flags
    private boolean paused = false;

    // Water animation
    private boolean waterFrame = false;
    private int     waterTick  = 0;

    //  Constructor

    public GamePanel(GameEngine engine) {
        this.engine = engine;

        SpriteRegistry.load();

        setPreferredSize(new Dimension(MAP_WIDTH + HUD_WIDTH, MAP_HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);

        gameTimer = new Timer(TICK_MS, this);
    }

    // Game-loop control

    public void startGame() {
        paused = false;
        gameTimer.start();
        requestFocusInWindow();
    }

    public void togglePause() {
        paused = !paused;
        if (paused) gameTimer.stop();
        else        { gameTimer.start(); requestFocusInWindow(); }
        repaint();
    }

    public boolean isPaused() { return paused; }

    public void stopGame() {
        gameTimer.stop();
    }

    //ActionListener

    @Override
    public void actionPerformed(ActionEvent e) {
        engine.handleInput(keysHeld);
        engine.update();
        repaint();

        if (engine.isGameOver()) {
            stopGame();
            firePropertyChange("GAME_OVER", false, true);
        } else if (engine.isLevelComplete()) {
            stopGame();
            firePropertyChange("LEVEL_COMPLETE", false, true);
        }
    }

    // Painting

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        waterTick++;
        if (waterTick >= 30) { waterTick = 0; waterFrame = !waterFrame; }

        drawMap(g2);
        engine.render(g2);
        drawHUD(g2);
        if (paused) drawPauseOverlay(g2);
    }

    // Map drawing

    private void drawMap(Graphics2D g) {
        Level level = engine.getCurrentLevel();
        if (level == null) return;

        int[][] map = level.getMap();
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                drawTile(g, map[row][col], col * TILE_SIZE, row * TILE_SIZE);
            }
        }
    }

    private void drawTile(Graphics2D g, int type, int x, int y) {
        switch (type) {

            case Level.EMPTY -> {}

            case Level.BRICK -> {
                BufferedImage img = SpriteRegistry.TILE_BRICK;
                if (img != null) {
                    g.drawImage(img, x, y, TILE_SIZE, TILE_SIZE, null);
                } else {
                    g.setColor(new Color(180, 80, 0));
                    g.fillRect(x, y, TILE_SIZE, TILE_SIZE);
                    g.setColor(new Color(100, 40, 0));
                    g.drawRect(x, y, TILE_SIZE - 1, TILE_SIZE - 1);
                }
            }

            case Level.STEEL -> {
                BufferedImage img = SpriteRegistry.TILE_STEEL;
                if (img != null) {
                    g.drawImage(img, x, y, TILE_SIZE, TILE_SIZE, null);
                } else {
                    g.setColor(new Color(160, 160, 160));
                    g.fillRect(x, y, TILE_SIZE, TILE_SIZE);
                    g.setColor(Color.WHITE);
                    g.drawRect(x, y, TILE_SIZE - 1, TILE_SIZE - 1);
                }
            }

            case Level.WATER -> {
                // Alternate between frame 1 and frame 2 for a ripple effect
                BufferedImage img = waterFrame
                        ? SpriteRegistry.TILE_WATER_2
                        : SpriteRegistry.TILE_WATER_1;
                if (img == null) img = SpriteRegistry.TILE_WATER_1; // fallback to frame 1
                if (img != null) {
                    g.drawImage(img, x, y, TILE_SIZE, TILE_SIZE, null);
                } else {
                    g.setColor(new Color(0, 100, 200));
                    g.fillRect(x, y, TILE_SIZE, TILE_SIZE);
                }
            }

            case Level.BUSH -> {
                BufferedImage img = SpriteRegistry.TILE_BUSH;
                if (img != null) {
                    g.drawImage(img, x, y, TILE_SIZE, TILE_SIZE, null);
                } else {
                    g.setColor(new Color(0, 160, 0));
                    g.fillRect(x, y, TILE_SIZE, TILE_SIZE);
                }
            }

            case Level.BASE -> {
                BufferedImage img = SpriteRegistry.TILE_BASE_ALIVE;
                if (img != null) {
                    g.drawImage(img, x, y, TILE_SIZE, TILE_SIZE, null);
                } else {
                    g.setColor(Color.YELLOW);
                    int cx = x + TILE_SIZE / 2, cy = y + TILE_SIZE / 2;
                    int r = TILE_SIZE / 2 - 2;
                    g.fillOval(cx - r, cy - r, 2 * r, 2 * r);
                }
            }

            default -> {
                g.setColor(Color.DARK_GRAY);
                g.fillRect(x, y, TILE_SIZE, TILE_SIZE);
            }
        }
    }

    // HUD

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

    // Pause overlay

    private void drawPauseOverlay(Graphics2D g) {
        g.setColor(new Color(0, 0, 0, 140));
        g.fillRect(0, 0, MAP_WIDTH, MAP_HEIGHT);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Monospaced", Font.BOLD, 36));
        FontMetrics fm = g.getFontMetrics();
        String msg = "PAUSED";
        g.drawString(msg, (MAP_WIDTH - fm.stringWidth(msg)) / 2, MAP_HEIGHT / 2);
    }

    // KeyListener

    @Override
    public void keyPressed(KeyEvent e) {
        keysHeld.add(e.getKeyCode());
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE || e.getKeyCode() == KeyEvent.VK_P) {
            togglePause();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) { keysHeld.remove(e.getKeyCode()); }

    @Override
    public void keyTyped(KeyEvent e) {}
}