package map;
 
import java.awt.Color;
import java.awt.Graphics;
 
public class Water extends Tile {
 
    private static final Color WATER_DEEP   = new Color(  0,  80, 160);
    private static final Color WATER_MID    = new Color(  0, 120, 200);
    private static final Color WATER_LIGHT  = new Color( 60, 170, 230);
    private static final Color WAVE_WHITE   = new Color(200, 230, 255);
 
    private boolean waveFrame = false;
 
    public Water(int x, int y) {
        super(x, y,  false,  false);
    }
 
    
     //Toggle the wave animation frame
     
    public void tick() {
        waveFrame = !waveFrame;
    }
 
    // draw
    @Override
    public void draw(Graphics g) {
        // base
        g.setColor(WATER_DEEP);
        g.fillRect(x, y, SIZE, SIZE);
 
        g.setColor(WATER_MID);
        g.fillRect(x, y, SIZE, SIZE / 2);
 
        // Wave lines
        int offset = waveFrame ? 0 : 4;
 
        g.setColor(WATER_LIGHT);
        for (int row = 0; row < 4; row++) {
            int wy = y + 6 + row * 7 + offset;
            g.fillRect(x + 2,        wy, 8, 2);
            g.fillRect(x + 14,       wy, 8, 2);
            g.fillRect(x + 24,       wy, 6, 2);
        }
 
        g.setColor(WAVE_WHITE);
        g.fillRect(x + 4,  y + 6  + offset, 4, 1);
        g.fillRect(x + 16, y + 13 + offset, 4, 1);
    }
 
    @Override
    public void onHit(int starLevel, boolean fromPlayer) {
        // Water is not destructible by bullets
    }
}