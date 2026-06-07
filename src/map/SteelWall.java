package map;
 
import java.awt.Color;
import java.awt.Graphics;
 
public class SteelWall extends Tile {
 
    private static final Color STEEL_LIGHT  = new Color(180, 180, 180);
    private static final Color STEEL_MID    = new Color(130, 130, 130);
    private static final Color STEEL_DARK   = new Color( 70,  70,  70);
    private static final Color STEEL_SHINE  = new Color(220, 220, 220);
 
    public static final int REQUIRED_STAR_LEVEL = 3;
 
    public SteelWall(int x, int y) {
        super(x, y,  true,  false);

    }
 
    // draw 
    @Override
    public void draw(Graphics g) {
        if (destroyed) return;
 
        g.setColor(STEEL_MID);
        g.fillRect(x, y, SIZE, SIZE);
 
        g.setColor(STEEL_LIGHT);
        g.fillRect(x + 4, y + 4, SIZE - 8, SIZE - 8);
 
        g.setColor(STEEL_SHINE);
        g.fillRect(x + 4, y + 4, SIZE - 8, 3);
        g.fillRect(x + 4, y + 4, 3, SIZE - 8);
 
        g.setColor(STEEL_DARK);
        g.fillRect(x + 4,          y + SIZE - 7, SIZE - 8, 3);
        g.fillRect(x + SIZE - 7,   y + 4,        3, SIZE - 8);
 
        g.setColor(STEEL_DARK);
        g.fillRect(x + SIZE / 2 - 1, y + 6,        2, SIZE - 12);
        g.fillRect(x + 6,            y + SIZE / 2 - 1, SIZE - 12, 2);
    }
 
    @Override
    public void onHit(int starLevel, boolean fromPlayer) {
        if (fromPlayer && starLevel >= REQUIRED_STAR_LEVEL) {
            destroyed = true;
        }
    }
}