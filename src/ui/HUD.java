package ui;

import data.ScoreManager;
import data.ScoreEntry;
import javax.swing.*;
import java.awt.*;

/**
 * HUD
 * ---
 * Paints the right-side heads-up display during gameplay.
 *
 * Displays (top to bottom):
 *  - Current score
 *  - High score (loaded once from ScoreManager)
 *  - Level number
 *  - Remaining enemy count (tank icons)
 *  - Player lives (tank icons)
 *  - Star power-up level (★ filled / empty)
 *  - Active power-up indicator (Clock / Shield / Shovel)
 *  - "PAUSED" banner when the game is paused
 *
 * Usage:
 *   HUD hud = new HUD();
 *   // in GamePanel.paintComponent:
 *   hud.draw(g2, engine, isPaused());
 *
 * HUD is a plain helper object – it does NOT extend JComponent
 * so it can be called directly inside GamePanel.paintComponent
 * without adding another child panel. This avoids double-buffering
 * artefacts and keeps the rendering pipeline simple.
 */
public class HUD {

    // ── Layout constants ──────────────────────────────────────────────────────
    /** X pixel where the HUD strip starts (= MAP_WIDTH). */
    private static final int X       = GamePanel.MAP_WIDTH;
    /** Total width of the HUD strip. */
    private static final int W       = GamePanel.HUD_WIDTH;
    /** Horizontal padding inside the strip. */
    private static final int PAD     = 8;
    /** Usable inner X. */
    private static final int IX      = X + PAD;
    /** Usable inner width. */
    private static final int IW      = W - PAD * 2;

    // ── Colours ───────────────────────────────────────────────────────────────
    private static final Color BG           = new Color(28, 14, 0);
    private static final Color BORDER_COL   = new Color(100, 40, 0);
    private static final Color LABEL_COL    = new Color(160, 120, 60);
    private static final Color VALUE_COL    = Color.ORANGE;
    private static final Color SCORE_COL    = new Color(255, 220, 60);
    private static final Color ENEMY_COL    = new Color(220, 60, 60);
    private static final Color LIFE_COL     = new Color(60, 220, 60);
    private static final Color STAR_ON      = new Color(255, 215, 0);
    private static final Color STAR_OFF     = new Color(60, 50, 20);
    private static final Color SECTION_LINE = new Color(80, 30, 0);
    private static final Color PAUSE_BG     = new Color(0, 0, 0, 180);
    private static final Color PAUSE_FG     = Color.WHITE;
    private static final Color POWERUP_ACTIVE = new Color(100, 255, 100);

    // ── Fonts ─────────────────────────────────────────────────────────────────
    private static final Font F_LABEL  = new Font("Monospaced", Font.BOLD,  11);
    private static final Font F_VALUE  = new Font("Monospaced", Font.BOLD,  16);
    private static final Font F_SMALL  = new Font("Monospaced", Font.PLAIN, 11);
    private static final Font F_SCORE  = new Font("Monospaced", Font.BOLD,  14);
    private static final Font F_PAUSE  = new Font("Monospaced", Font.BOLD,  15);
    private static final Font F_STAR   = new Font("Dialog",     Font.PLAIN, 18);

    // ── Cached high score (read once per session) ─────────────────────────────
    private int cachedHighScore = -1;

    // ─────────────────────────────────────────────────────────────────────────
    //  Main draw entry point
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Draw the full HUD strip onto the provided Graphics2D context.
     * Call this inside {@link GamePanel#paintComponent(Graphics)} after
     * the map and entities have been drawn.
     *
     * @param g       the graphics context (already translated to panel origin)
     * @param engine  the live game engine to read state from
     * @param paused  whether the game is currently paused
     */
    public void draw(Graphics2D g, GameEngine engine, boolean paused) {
        enableAA(g);

        // ── Background strip ──────────────────────────────────────────────────
        g.setColor(BG);
        g.fillRect(X, 0, W, GamePanel.MAP_HEIGHT);
        g.setColor(BORDER_COL);
        g.drawLine(X, 0, X, GamePanel.MAP_HEIGHT);

        int y = 18;   // running vertical cursor

        // ── SCORE ─────────────────────────────────────────────────────────────
        y = drawSection(g, y, "SCORE");
        g.setFont(F_SCORE);
        g.setColor(SCORE_COL);
        g.drawString(formatScore(engine.getScore()), IX, y);
        y += 6;

        // ── HI-SCORE ──────────────────────────────────────────────────────────
        y = separator(g, y);
        g.setFont(F_LABEL);
        g.setColor(LABEL_COL);
        g.drawString("HI-SCORE", IX, y);
        y += 14;
        g.setFont(F_SMALL);
        g.setColor(new Color(200, 160, 80));
        g.drawString(formatScore(getHighScore(engine.getScore())), IX, y);
        y += 6;

        // ── LEVEL ─────────────────────────────────────────────────────────────
        y = separator(g, y);
        y = drawSection(g, y, "LEVEL");
        g.setFont(F_VALUE);
        g.setColor(new Color(80, 200, 255));
        g.drawString(String.valueOf(engine.getLevelNumber()), IX, y);
        y += 6;

        // ── ENEMY TANKS remaining ─────────────────────────────────────────────
        y = separator(g, y);
        y = drawSection(g, y, "ENEMY");
        y = drawTankIcons(g, y, engine.getRemainingEnemies(), ENEMY_COL, 20);
        y += 4;

        // ── PLAYER LIVES ──────────────────────────────────────────────────────
        y = separator(g, y);
        y = drawSection(g, y, "LIVES");
        y = drawTankIcons(g, y, engine.getLives(), LIFE_COL, 3);
        y += 4;

        // ── STAR POWER level ──────────────────────────────────────────────────
        y = separator(g, y);
        y = drawSection(g, y, "STARS");
        y = drawStars(g, y, engine.getPlayerStars());
        y += 4;

        // ── Active power-up indicator ─────────────────────────────────────────
        // (Clock / Shield / Shovel – GameEngine exposes these flags)
        y = separator(g, y);
        drawActivePowerUp(g, y, engine);

        // ── PAUSED overlay ────────────────────────────────────────────────────
        if (paused) drawPauseBanner(g);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Section helpers
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Draw a small section label and return the Y coordinate for the value.
     */
    private int drawSection(Graphics2D g, int y, String label) {
        g.setFont(F_LABEL);
        g.setColor(LABEL_COL);
        g.drawString(label, IX, y);
        return y + 16;   // value sits 16 px below the label
    }

    /** Draw a thin separator line and return new y. */
    private int separator(Graphics2D g, int y) {
        y += 6;
        g.setColor(SECTION_LINE);
        g.drawLine(IX, y, IX + IW, y);
        return y + 8;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Tank icon grid
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Draw small tank silhouettes in a 2-column grid.
     *
     * @param count   number of tanks to show
     * @param colour  fill colour
     * @param cap     maximum to show (excess shown as "+N more")
     * @return new y cursor after all icons
     */
    private int drawTankIcons(Graphics2D g, int y, int count, Color colour, int cap) {
        int shown = Math.min(count, cap);
        int cols  = 2;
        int tw = 14, th = 11, hgap = 4, vgap = 4;

        for (int i = 0; i < shown; i++) {
            int col = i % cols;
            int row = i / cols;
            int tx  = IX + col * (tw + hgap);
            int ty  = y  + row * (th + vgap);
            drawMiniTank(g, tx, ty, colour);
        }

        int rows = (shown + cols - 1) / cols;
        int newY = y + rows * (th + vgap);

        if (count > cap) {
            g.setFont(F_SMALL);
            g.setColor(colour);
            g.drawString("+" + (count - cap), IX, newY + 12);
            newY += 14;
        }

        return newY;
    }

    /** Draw a tiny symbolic tank (body + turret + barrel). */
    private void drawMiniTank(Graphics2D g, int x, int y, Color c) {
        g.setColor(c);
        g.fillRect(x,     y + 3,  14, 8);  // body
        g.fillRect(x + 4, y,      6,  5);  // turret
        g.fillRect(x + 6, y - 3,  2,  5);  // barrel
        g.setColor(c.darker());
        g.drawRect(x, y + 3, 14, 8);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Star display
    // ─────────────────────────────────────────────────────────────────────────

    private int drawStars(Graphics2D g, int y, int starCount) {
        g.setFont(F_STAR);
        for (int i = 0; i < 3; i++) {
            g.setColor(i < starCount ? STAR_ON : STAR_OFF);
            g.drawString("★", IX + i * 22, y);
        }
        return y + 6;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Active power-up indicator
    // ─────────────────────────────────────────────────────────────────────────

    private void drawActivePowerUp(Graphics2D g, int y, GameEngine engine) {
        g.setFont(F_LABEL);
        g.setColor(LABEL_COL);
        g.drawString("ACTIVE", IX, y);
        y += 16;

        g.setFont(F_SMALL);
        boolean any = false;

        if (engine.isClockActive()) {
            g.setColor(POWERUP_ACTIVE);
            g.drawString("🕐 CLOCK", IX, y);
            y += 14; any = true;
        }
        if (engine.isShieldActive()) {
            g.setColor(new Color(100, 180, 255));
            g.drawString("🛡 SHIELD", IX, y);
            y += 14; any = true;
        }
        if (engine.isShovelActive()) {
            g.setColor(new Color(200, 140, 60));
            g.drawString("⛏ SHOVEL", IX, y);
            any = true;
        }
        if (!any) {
            g.setColor(new Color(80, 70, 50));
            g.drawString("none", IX, y);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Pause banner
    // ─────────────────────────────────────────────────────────────────────────

    private void drawPauseBanner(Graphics2D g) {
        // Only covers the HUD strip
        g.setColor(PAUSE_BG);
        g.fillRect(X, 0, W, GamePanel.MAP_HEIGHT);

        g.setFont(F_PAUSE);
        g.setColor(PAUSE_FG);
        FontMetrics fm = g.getFontMetrics();

        String[] lines = {"", "PAUSE", "", "P / ESC", "to resume"};
        int lineH = fm.getHeight() + 4;
        int totalH = lines.length * lineH;
        int startY = (GamePanel.MAP_HEIGHT - totalH) / 2;

        for (int i = 0; i < lines.length; i++) {
            int lx = X + (W - fm.stringWidth(lines[i])) / 2;
            g.drawString(lines[i], lx, startY + i * lineH + fm.getAscent());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Utilities
    // ─────────────────────────────────────────────────────────────────────────

    /** Format score with leading zeros to 6 digits, e.g. 002500. */
    private String formatScore(int score) {
        return String.format("%06d", Math.max(0, score));
    }

    /** Return the all-time high score, caching on first call. */
    private int getHighScore(int currentScore) {
        if (cachedHighScore < 0) {
            cachedHighScore = ScoreManager.loadTop(1)
                .stream()
                .mapToInt(ScoreEntry::score)
                .max()
                .orElse(0);
        }
        return Math.max(cachedHighScore, currentScore);
    }

    private void enableAA(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                           RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                           RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    }
}