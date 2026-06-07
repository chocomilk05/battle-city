package map;
 
import java.awt.Color;
import java.awt.Graphics;

public class BrickWall extends Tile {
 
    // Brick colours
    private static final Color BRICK_MAIN   = new Color(180, 80,  20);
    private static final Color BRICK_DARK   = new Color(120, 50,  10);
    private static final Color BRICK_MORTAR = new Color( 80, 40,   5);
 
    public BrickWall(int x, int y) {
        super(x, y, true, false);
    }
 
    @Override
    public void draw(Graphics g) {
        if (destroyed) return;
 
        // Fills the whole tile with the base brick colour
        g.setColor(BRICK_MAIN);
        g.fillRect(x, y, SIZE, SIZE);
 
        // Horizontal mortar lines
        g.setColor(BRICK_MORTAR);
        g.fillRect(x,        y + SIZE / 2 - 1, SIZE, 2);
 
        // Top row
        g.setColor(BRICK_DARK);
        g.fillRect(x + SIZE / 2 - 1, y,             1, SIZE / 2);
 
        // Bottom row
        g.setColor(BRICK_DARK);
        g.fillRect(x - 1,            y + SIZE / 2,  1, SIZE / 2); // left edge seam
        g.fillRect(x + SIZE - 1,     y + SIZE / 2,  1, SIZE / 2); // right edge seam
        g.fillRect(x + SIZE / 4 * 3, y + SIZE / 2,  1, SIZE / 2);
    }
 
    @Override
    public void onHit(int starLevel, boolean fromPlayer) {
        destroyed = true;
    }
}
