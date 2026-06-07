package powerups;

import java.awt.*;

/*
  PowerUp
  Base class for all collectible power-up items that appear on the map
  when an enemy tank is destroyed
 */
public class PowerUp {

    // Power-up type enum
    public enum Type {
        TANK, 
        STAR,  
        BOMB,  
        CLOCK, 
        SHOVEL,
        SHIELD
    }

    // Size
    public static final int SIZE = 28;

    // State
    protected int  x;
    protected int  y;
    private final Type type;

    private int blinkTick = 0;

    // Type-specific visuals
    private static final Color[] BG_COLORS = {
        new Color(60,  220, 60),   // TANK
        new Color(255, 200,  0),   // STAR
        new Color(220,  50, 50),   // BOMB
        new Color( 80, 160, 255),  // CLOCK
        new Color(200, 120,  0),   // SHOVEL
        new Color( 60, 200, 200),  // SHIELD
    };
    private static final String[] LABELS = {
        "1UP", "★", "✦", "⏱", "⛏", "⛨"
    };

    public PowerUp(int x, int y, Type type) {
        this.x    = x;
        this.y    = y;
        this.type = type;
    }

    // Getters

    public Type      getType()   { return type; }
    public int       getX()      { return x; }
    public int       getY()      { return y; }

    public Rectangle getBounds() { return new Rectangle(x, y, SIZE, SIZE); }

    // Drawing

    public void draw(Graphics g) {
        blinkTick++;
        boolean showBorder = (blinkTick / 8) % 2 == 0;

        int idx = type.ordinal();
        Color bg = BG_COLORS[idx];

        g.setColor(bg.darker());
        g.fillRect(x, y, SIZE, SIZE);
        g.setColor(bg);
        g.fillRect(x + 2, y + 2, SIZE - 4, SIZE - 4);

        if (showBorder) {
            g.setColor(Color.WHITE);
            g.drawRect(x, y, SIZE - 1, SIZE - 1);
            g.drawRect(x + 1, y + 1, SIZE - 3, SIZE - 3);
        }

        g.setColor(Color.BLACK);
        g.setFont(new Font("Monospaced", Font.BOLD, 11));
        FontMetrics fm = g.getFontMetrics();
        String lbl = LABELS[idx];
        int tx = x + (SIZE - fm.stringWidth(lbl)) / 2;
        int ty = y + (SIZE + fm.getAscent() - fm.getDescent()) / 2;
        g.drawString(lbl, tx, ty);

        g.setColor(Color.WHITE);
        g.drawString(lbl, tx - 1, ty - 1);
    }
}