package powerups;

import java.awt.*;

/**
 * PowerUp
 * -------
 * Base class for all collectible power-up items that appear on the map
 * when an enemy tank is destroyed.
 *
 * Subclasses (BombPowerUp, ClockPowerUp, etc.) call super(x, y, Type.XXX)
 * and can override draw() for a custom icon if desired.
 *
 * GameEngine creates PowerUp instances via:
 *   new PowerUp(x, y, PowerUp.Type.STAR)
 * and tests collection with getBounds() / getType().
 */
public class PowerUp {

    // ── Power-up type enum ────────────────────────────────────────────────────
    public enum Type {
        TANK,   // extra life
        STAR,   // upgrade star level
        BOMB,   // destroy all on-screen enemies
        CLOCK,  // freeze enemies
        SHOVEL, // steel-wall base fortification
        SHIELD  // player invulnerability
    }

    // ── Size ──────────────────────────────────────────────────────────────────
    public static final int SIZE = 28;

    // ── State ─────────────────────────────────────────────────────────────────
    protected int  x;
    protected int  y;
    private final Type type;

    /** Blink animation — toggled by draw() every call via a simple counter. */
    private int blinkTick = 0;

    // ── Type-specific visuals ─────────────────────────────────────────────────
    private static final Color[] BG_COLORS = {
        new Color(60,  220, 60),   // TANK   – green
        new Color(255, 200,  0),   // STAR   – gold
        new Color(220,  50, 50),   // BOMB   – red
        new Color( 80, 160, 255),  // CLOCK  – blue
        new Color(200, 120,  0),   // SHOVEL – orange
        new Color( 60, 200, 200),  // SHIELD – cyan
    };
    private static final String[] LABELS = {
        "1UP", "★", "✦", "⏱", "⛏", "⛨"
    };

    // ─────────────────────────────────────────────────────────────────────────

    public PowerUp(int x, int y, Type type) {
        this.x    = x;
        this.y    = y;
        this.type = type;
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public Type      getType()   { return type; }
    public int       getX()      { return x; }
    public int       getY()      { return y; }

    /** Axis-aligned bounding rectangle used for collision detection. */
    public Rectangle getBounds() { return new Rectangle(x, y, SIZE, SIZE); }

    // ── Drawing ───────────────────────────────────────────────────────────────

    /**
     * Draw the power-up icon with a blinking border so the player notices it.
     * Subclasses may override for a custom sprite look.
     */
    public void draw(Graphics g) {
        blinkTick++;
        boolean showBorder = (blinkTick / 8) % 2 == 0;

        int idx = type.ordinal();
        Color bg = BG_COLORS[idx];

        // Background square
        g.setColor(bg.darker());
        g.fillRect(x, y, SIZE, SIZE);
        g.setColor(bg);
        g.fillRect(x + 2, y + 2, SIZE - 4, SIZE - 4);

        // Blinking white border
        if (showBorder) {
            g.setColor(Color.WHITE);
            g.drawRect(x, y, SIZE - 1, SIZE - 1);
            g.drawRect(x + 1, y + 1, SIZE - 3, SIZE - 3);
        }

        // Label
        g.setColor(Color.BLACK);
        g.setFont(new Font("Monospaced", Font.BOLD, 11));
        FontMetrics fm = g.getFontMetrics();
        String lbl = LABELS[idx];
        int tx = x + (SIZE - fm.stringWidth(lbl)) / 2;
        int ty = y + (SIZE + fm.getAscent() - fm.getDescent()) / 2;
        g.drawString(lbl, tx, ty);

        // White highlight copy (1 px up-left) for readability
        g.setColor(Color.WHITE);
        g.drawString(lbl, tx - 1, ty - 1);
    }
}