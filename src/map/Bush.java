package map;
 
import java.awt.Color;
import java.awt.Graphics;

public class Bush extends Tile {
 
    private static final Color BUSH_DARK   = new Color( 30, 100,  30);
    private static final Color BUSH_MID    = new Color( 50, 150,  50);
    private static final Color BUSH_LIGHT  = new Color( 80, 190,  60);
    private static final Color BUSH_SHINE  = new Color(120, 210,  80);
 
    public Bush(int x, int y) {
        super(x, y, false, true);
    }

    @Override
    public void draw(Graphics g) {
        g.setColor(BUSH_DARK);
        g.fillRect(x, y, SIZE, SIZE);
 
        g.setColor(BUSH_MID);
        g.fillOval(x + 2,  y + 4,  14, 14);
        g.fillOval(x + 14, y + 2,  14, 14);
        g.fillOval(x + 8,  y + 12, 14, 12);
        g.fillOval(x + 2,  y + 14, 12, 12);
 
        g.setColor(BUSH_LIGHT);
        g.fillOval(x + 4,  y + 6,  10, 10);
        g.fillOval(x + 16, y + 4,  10, 10);
        g.fillOval(x + 10, y + 14, 10, 10);
 
        g.setColor(BUSH_SHINE);
        g.fillOval(x + 6,  y + 7,  5, 5);
        g.fillOval(x + 18, y + 5,  5, 5);
    }
 
    @Override
    public void onHit(int starLevel, boolean fromPlayer) {
    }
}