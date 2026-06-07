package map;
 
import java.awt.Color;
import java.awt.Graphics;
 
/**
 * Water tile.
 *
 * Rules from the spec / classic Battle City:
 *   - Tanks CANNOT pass through water (passable = false).
 *   - Bullets DO pass over water freely (not an obstacle for bullets).
 *   - Not destructible.
 *
 * Drawn as an animated-looking blue surface with wave details.
 * For real animation, track a frame counter here and flip between
 * two slight colour variations each second (simple flicker effect).
 */
public class Water extends Tile {
 
    private static final Color WATER_DEEP   = new Color(  0,  80, 160);
    private static final Color WATER_MID    = new Color(  0, 120, 200);
    private static final Color WATER_LIGHT  = new Color( 60, 170, 230);
    private static final Color WAVE_WHITE   = new Color(200, 230, 255);
 
    /**
     * Simple frame toggle for a two-frame wave animation.
     * GameEngine calls tick() each game loop iteration.
     */
    private boolean waveFrame = false;
 
    public Water(int x, int y) {
        super(x, y, /*destructible*/ false, /*passable*/ false);
    }
 
    /**
     * Toggle the wave animation frame.
     * Call this once per game loop tick from Map.tickAnimations().
     */
    public void tick() {
        waveFrame = !waveFrame;
    }
 
    // -------------------------------------------------------------------------
    // draw — blue base with alternating wave lines
    // -------------------------------------------------------------------------
    @Override
    public void draw(Graphics g) {
        // Deep blue base
        g.setColor(WATER_DEEP);
        g.fillRect(x, y, SIZE, SIZE);
 
        // Mid-blue fill (upper portion lighter = sky reflection)
        g.setColor(WATER_MID);
        g.fillRect(x, y, SIZE, SIZE / 2);
 
        // Wave lines — offset by frame for animation effect
        int offset = waveFrame ? 0 : 4;
 
        g.setColor(WATER_LIGHT);
        for (int row = 0; row < 4; row++) {
            int wy = y + 6 + row * 7 + offset;
            g.fillRect(x + 2,        wy, 8, 2);
            g.fillRect(x + 14,       wy, 8, 2);
            g.fillRect(x + 24,       wy, 6, 2);
        }
 
        // Small white highlight crests
        g.setColor(WAVE_WHITE);
        g.fillRect(x + 4,  y + 6  + offset, 4, 1);
        g.fillRect(x + 16, y + 13 + offset, 4, 1);
    }
 
    // -------------------------------------------------------------------------
    // onHit — bullets fly over water; nothing happens
    // -------------------------------------------------------------------------
    @Override
    public void onHit(int starLevel, boolean fromPlayer) {
        // Water is not destructible by bullets
    }
}