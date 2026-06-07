package map;
 
import java.awt.Color;
import java.awt.Graphics;
 
/**
 * SteelWall tile.
 *
 * Rules from the spec:
 *   - Can ONLY be destroyed by the player when they have 3 or more stars.
 *   - Enemy bullets and low-star player bullets bounce off (have no effect).
 *   - Tanks cannot pass through it (passable = false).
 *
 * Also used temporarily by the Shovel power-up to fortify the base:
 * those tiles are created the same way as normal SteelWalls but are
 * removed after a timer expires (handled in Level/GameEngine, not here).
 */
public class SteelWall extends Tile {
 
    private static final Color STEEL_LIGHT  = new Color(180, 180, 180);
    private static final Color STEEL_MID    = new Color(130, 130, 130);
    private static final Color STEEL_DARK   = new Color( 70,  70,  70);
    private static final Color STEEL_SHINE  = new Color(220, 220, 220);
 
    // The minimum star level a player needs to destroy this tile
    public static final int REQUIRED_STAR_LEVEL = 3;
 
    public SteelWall(int x, int y) {
        super(x, y, /*destructible*/ true, /*passable*/ false);
        // Note: destructible=true here because the tile CAN be destroyed —
        // but only under the star-level condition checked in onHit().
    }
 
    // -------------------------------------------------------------------------
    // draw — steel plate appearance with highlight and shadow edges
    // -------------------------------------------------------------------------
    @Override
    public void draw(Graphics g) {
        if (destroyed) return;
 
        // Base plate
        g.setColor(STEEL_MID);
        g.fillRect(x, y, SIZE, SIZE);
 
        // Inner raised panel (inset 4 px)
        g.setColor(STEEL_LIGHT);
        g.fillRect(x + 4, y + 4, SIZE - 8, SIZE - 8);
 
        // Top-left shine
        g.setColor(STEEL_SHINE);
        g.fillRect(x + 4, y + 4, SIZE - 8, 3);
        g.fillRect(x + 4, y + 4, 3, SIZE - 8);
 
        // Bottom-right shadow
        g.setColor(STEEL_DARK);
        g.fillRect(x + 4,          y + SIZE - 7, SIZE - 8, 3);
        g.fillRect(x + SIZE - 7,   y + 4,        3, SIZE - 8);
 
        // Centre cross detail (classic Battle City look)
        g.setColor(STEEL_DARK);
        g.fillRect(x + SIZE / 2 - 1, y + 6,        2, SIZE - 12);
        g.fillRect(x + 6,            y + SIZE / 2 - 1, SIZE - 12, 2);
    }
 
    // -------------------------------------------------------------------------
    // onHit — only destroyed when player has exactly 3 stars
    // -------------------------------------------------------------------------
    @Override
    public void onHit(int starLevel, boolean fromPlayer) {
        if (fromPlayer && starLevel >= REQUIRED_STAR_LEVEL) {
            destroyed = true;
        }
        // Enemy bullets and low-star player bullets: do nothing
    }
}