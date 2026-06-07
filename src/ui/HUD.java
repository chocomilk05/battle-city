package ui;

import data.ScoreManager;
import data.ScoreEntry;
import javax.swing.*;
import java.awt.*;

public class HUD {

    // Layout
    private static final int X   = GamePanel.MAP_WIDTH;
    private static final int W   = GamePanel.HUD_WIDTH;
    private static final int PAD = 8;
    private static final int IX  = X + PAD;
    private static final int IW  = W - PAD * 2;

    // Colours
    private static final Color BG            = new Color(28, 14, 0);
    private static final Color BORDER_COL    = new Color(100, 40, 0);
    private static final Color LABEL_COL     = new Color(160, 120, 60);
    private static final Color SCORE_COL     = new Color(255, 220, 60);
    private static final Color ENEMY_COL     = new Color(220, 60, 60);
    private static final Color LIFE_COL      = new Color(60, 220, 60);
    private static final Color STAR_ON       = new Color(255, 215, 0);
    private static final Color STAR_OFF      = new Color(60, 50, 20);
    private static final Color SECTION_LINE  = new Color(80, 30, 0);
    private static final Color PAUSE_BG      = new Color(0, 0, 0, 180);
    private static final Color POWERUP_ACTIVE = new Color(100, 255, 100);

    // Fonts
    private static final Font F_LABEL = new Font("Monospaced", Font.BOLD,  11);
    private static final Font F_VALUE = new Font("Monospaced", Font.BOLD,  16);
    private static final Font F_SMALL = new Font("Monospaced", Font.PLAIN, 11);
    private static final Font F_SCORE = new Font("Monospaced", Font.BOLD,  14);
    private static final Font F_PAUSE = new Font("Monospaced", Font.BOLD,  15);
    private static final Font F_STAR  = new Font("Dialog",     Font.PLAIN, 18);

    private int cachedHighScore = -1;

    //  Main draw entry point

    public void draw(Graphics2D g, GameEngine engine, boolean paused) {
        enableAA(g);

        g.setColor(BG);
        g.fillRect(X, 0, W, GamePanel.MAP_HEIGHT);
        g.setColor(BORDER_COL);
        g.drawLine(X, 0, X, GamePanel.MAP_HEIGHT);

        int y = 18;

        // SCORE
        y = drawSection(g, y, "SCORE");
        g.setFont(F_SCORE);
        g.setColor(SCORE_COL);
        g.drawString(formatScore(engine.getScore()), IX, y);
        y += 6;

        // HI-SCORE
        y = separator(g, y);
        g.setFont(F_LABEL);
        g.setColor(LABEL_COL);
        g.drawString("HI-SCORE", IX, y);
        y += 14;
        g.setFont(F_SMALL);
        g.setColor(new Color(200, 160, 80));
        g.drawString(formatScore(getHighScore(engine.getScore())), IX, y);
        y += 6;

        // LEVEL
        y = separator(g, y);
        y = drawSection(g, y, "LEVEL");
        g.setFont(F_VALUE);
        g.setColor(new Color(80, 200, 255));
        g.drawString(String.valueOf(engine.getLevelNumber()), IX, y);
        y += 6;

        // ENEMY
        y = separator(g, y);
        y = drawSection(g, y, "ENEMY");
        int remaining = Math.max(0, engine.getRemainingEnemies());
        y = drawTankIcons(g, y, remaining, ENEMY_COL, 20);
        y += 4;

        // LIVES
        y = separator(g, y);
        y = drawSection(g, y, "LIVES");
        y = drawTankIcons(g, y, engine.getLives(), LIFE_COL, 3);
        y += 4;

        // STARS
        y = separator(g, y);
        y = drawSection(g, y, "STARS");
        y = drawStars(g, y, engine.getPlayerStars());
        y += 4;

        // ACTIVE POWER-UP
        y = separator(g, y);
        drawActivePowerUp(g, y, engine);

        // PAUSED overlay
        if (paused) drawPauseBanner(g);
    }

    // Section helpers

    private int drawSection(Graphics2D g, int y, String label) {
        g.setFont(F_LABEL);
        g.setColor(LABEL_COL);
        g.drawString(label, IX, y);
        return y + 16;
    }

    private int separator(Graphics2D g, int y) {
        y += 6;
        g.setColor(SECTION_LINE);
        g.drawLine(IX, y, IX + IW, y);
        return y + 8;
    }

    // Tank icon grid

    private int drawTankIcons(Graphics2D g, int y, int count, Color colour, int cap) {
        int shown = Math.min(count, cap);
        int cols = 2, tw = 14, th = 11, hgap = 4, vgap = 4;

        for (int i = 0; i < shown; i++) {
            int col = i % cols;
            int row = i / cols;
            drawMiniTank(g, IX + col * (tw + hgap), y + row * (th + vgap), colour);
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

    private void drawMiniTank(Graphics2D g, int x, int y, Color c) {
        g.setColor(c);
        g.fillRect(x,     y + 3, 14, 8);
        g.fillRect(x + 4, y,      6,  5);
        g.fillRect(x + 6, y - 3,  2,  5);
        g.setColor(c.darker());
        g.drawRect(x, y + 3, 14, 8);
    }

    // Stars

    private int drawStars(Graphics2D g, int y, int starCount) {
        g.setFont(F_STAR);
        for (int i = 0; i < 3; i++) {
            g.setColor(i < starCount ? STAR_ON : STAR_OFF);
            g.drawString("★", IX + i * 22, y);
        }
        return y + 6;
    }

    // Active power-up indicator

    private void drawActivePowerUp(Graphics2D g, int y, GameEngine engine) {
        g.setFont(F_LABEL);
        g.setColor(LABEL_COL);
        g.drawString("ACTIVE", IX, y);
        y += 16;

        g.setFont(F_SMALL);
        boolean any = false;

        if (engine.isClockActive()) {
            g.setColor(POWERUP_ACTIVE);
            g.drawString("CLOCK", IX, y);
            y += 14; any = true;
        }
        if (engine.isShieldActive()) {
            g.setColor(new Color(100, 180, 255));
            g.drawString("SHIELD", IX, y);
            y += 14; any = true;
        }
        if (engine.isShovelActive()) {
            g.setColor(new Color(200, 140, 60));
            g.drawString("SHOVEL", IX, y);
            any = true;
        }
        if (!any) {
            g.setColor(new Color(80, 70, 50));
            g.drawString("none", IX, y);
        }
    }

    // Pause banner

    private void drawPauseBanner(Graphics2D g) {
        g.setColor(PAUSE_BG);
        g.fillRect(X, 0, W, GamePanel.MAP_HEIGHT);

        g.setFont(F_PAUSE);
        g.setColor(Color.WHITE);
        FontMetrics fm = g.getFontMetrics();

        String[] lines = {"", "PAUSE", "", "P / ESC", "to resume"};
        int lineH  = fm.getHeight() + 4;
        int startY = (GamePanel.MAP_HEIGHT - lines.length * lineH) / 2;

        for (int i = 0; i < lines.length; i++) {
            int lx = X + (W - fm.stringWidth(lines[i])) / 2;
            g.drawString(lines[i], lx, startY + i * lineH + fm.getAscent());
        }
    }

    // Utilities

    private String formatScore(int score) {
        return String.format("%06d", Math.max(0, score));
    }

    private int getHighScore(int currentScore) {
        if (cachedHighScore < 0) {
            cachedHighScore = ScoreManager.loadTop(1)
                .stream()
                .mapToInt(ScoreEntry::score)
                .max()
                .orElse(0);
        }
        if (currentScore > cachedHighScore) cachedHighScore = currentScore;
        return cachedHighScore;
    }

    private void enableAA(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                           RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                           RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    }
}