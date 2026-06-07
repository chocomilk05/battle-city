package map;
 
import java.awt.Color;
import java.awt.Graphics;
 
/**
 * BrickWall tile.
 *
 * Rules from the spec:
 *   - Destroyed by any bullet (player or enemy), regardless of star level.
 *   - Tanks cannot pass through it (passable = false).
 *
 * Drawn as an orange-brown brick pattern to match the original game look.
 * In a real build, swap the fillRect calls for:
 *   g.drawImage(spriteSheet.getSubimage(...), x, y, null);
 */
public class BrickWall extends Tile {
 
    // Brick colours — adjust to match your sprite sheet palette
    private static final Color BRICK_MAIN   = new Color(180, 80,  20);
    private static final Color BRICK_DARK   = new Color(120, 50,  10);
    private static final Color BRICK_MORTAR = new Color( 80, 40,   5);
 
    public BrickWall(int x, int y) {
        super(x, y, /*destructible*/ true, /*passable*/ false);
    }
 
    // -------------------------------------------------------------------------
    // draw — simple brick pattern (2×2 sub-blocks with mortar lines)
    // -------------------------------------------------------------------------
    @Override
    public void draw(Graphics g) {
        if (destroyed) return;
 
        // Fill the whole tile with the base brick colour
        g.setColor(BRICK_MAIN);
        g.fillRect(x, y, SIZE, SIZE);
 
        // Horizontal mortar lines
        g.setColor(BRICK_MORTAR);
        g.fillRect(x,        y + SIZE / 2 - 1, SIZE, 2);
 
        // Top row: two bricks side by side
        g.setColor(BRICK_DARK);
        g.fillRect(x + SIZE / 2 - 1, y,             1, SIZE / 2);
 
        // Bottom row: offset by half a brick (classic brick bond pattern)
        g.setColor(BRICK_DARK);
        g.fillRect(x - 1,            y + SIZE / 2,  1, SIZE / 2); // left edge seam
        g.fillRect(x + SIZE - 1,     y + SIZE / 2,  1, SIZE / 2); // right edge seam
        g.fillRect(x + SIZE / 4 * 3, y + SIZE / 2,  1, SIZE / 2);
    }
 
    // -------------------------------------------------------------------------
    // onHit — any bullet destroys this tile
    // -------------------------------------------------------------------------
    @Override
    public void onHit(int starLevel, boolean fromPlayer) {
        // Brick is destroyed by everyone — star level does not matter here
        destroyed = true;
    }
}
