package map;
 
import java.awt.Color;
import java.awt.Graphics;
 
/**
 * Bush (green bush / foliage) tile.
 *
 * Rules from the spec:
 *   - Tanks CAN move through / under it  (passable = true).
 *   - It hides both the player's tank AND enemy tanks beneath it —
 *     adding an extra level of difficulty.
 *   - Bullets pass through it freely (not destructible).
 *   - Drawn LAST (on top of tanks) so tanks underneath are hidden.
 *     The Map.draw() method draws bushes in a second pass after tanks.
 *
 * Drawing order in Map.draw():
 *   1. All non-bush tiles (solid ground)
 *   2. All tanks and bullets
 *   3. All Bush tiles  ← drawn on top so they occlude tanks beneath
 */
public class Bush extends Tile {
 
    // Green shades for the foliage
    private static final Color BUSH_DARK   = new Color( 30, 100,  30);
    private static final Color BUSH_MID    = new Color( 50, 150,  50);
    private static final Color BUSH_LIGHT  = new Color( 80, 190,  60);
    private static final Color BUSH_SHINE  = new Color(120, 210,  80);
 
    public Bush(int x, int y) {
        super(x, y, /*destructible*/ false, /*passable*/ true);
    }
 
    // -------------------------------------------------------------------------
    // draw — overlapping blob circles for a foliage effect
    // Drawn AFTER tanks — see class JavaDoc above
    // -------------------------------------------------------------------------
    @Override
    public void draw(Graphics g) {
        // Background base
        g.setColor(BUSH_DARK);
        g.fillRect(x, y, SIZE, SIZE);
 
        // Large dark blobs
        g.setColor(BUSH_MID);
        g.fillOval(x + 2,  y + 4,  14, 14);
        g.fillOval(x + 14, y + 2,  14, 14);
        g.fillOval(x + 8,  y + 12, 14, 12);
        g.fillOval(x + 2,  y + 14, 12, 12);
 
        // Lighter mid-layer
        g.setColor(BUSH_LIGHT);
        g.fillOval(x + 4,  y + 6,  10, 10);
        g.fillOval(x + 16, y + 4,  10, 10);
        g.fillOval(x + 10, y + 14, 10, 10);
 
        // Small highlight dots
        g.setColor(BUSH_SHINE);
        g.fillOval(x + 6,  y + 7,  5, 5);
        g.fillOval(x + 18, y + 5,  5, 5);
    }
 
    // -------------------------------------------------------------------------
    // onHit — bullets pass straight through bushes; nothing happens
    // -------------------------------------------------------------------------
    @Override
    public void onHit(int starLevel, boolean fromPlayer) {
        // Bush is indestructible — do nothing
    }
}