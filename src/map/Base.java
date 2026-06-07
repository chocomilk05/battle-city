package map;
 
import java.awt.Color;
import java.awt.Graphics;
 
/**
 * Base tile — the eagle / bird / phoenix at the bottom centre of the map.
 *
 * Rules from the spec:
 *   - Positioned at the bottom centre of the screen, surrounded by BrickWalls.
 *   - Enemy tanks try to destroy this as their primary goal.
 *   - If an enemy bullet hits the base, the game ends immediately.
 *   - Player bullets also destroy it (so the player must protect it).
 *   - The Shovel power-up temporarily replaces surrounding BrickWalls
 *     with SteelWalls — that logic lives in Level/GameEngine, not here.
 *
 * The base has two visual states:
 *   ALIVE  — eagle icon (gold/yellow)
 *   DEAD   — destroyed icon (black/grey rubble)
 *
 * GameEngine checks isBaseDestroyed() each frame to trigger GAME_OVER.
 */
public class Base extends Tile {
 
    // Eagle colours
    private static final Color EAGLE_BODY   = new Color(200, 160,   0);
    private static final Color EAGLE_DARK   = new Color(100,  70,   0);
    private static final Color EAGLE_WING   = new Color(230, 200,  20);
    private static final Color EAGLE_EYE    = new Color(  0,   0,   0);
 
    // Destroyed state colours
    private static final Color RUBBLE_DARK  = new Color( 40,  40,  40);
    private static final Color RUBBLE_MID   = new Color( 80,  80,  80);
    private static final Color RUBBLE_LIGHT = new Color(130, 130, 130);
 
    private boolean baseDestroyed = false;
 
    public Base(int x, int y) {
        // The base tile itself is not passable and can be destroyed
        super(x, y, /*destructible*/ true, /*passable*/ false);
    }
 
    // -------------------------------------------------------------------------
    // draw — eagle when alive, rubble when destroyed
    // -------------------------------------------------------------------------
    @Override
    public void draw(Graphics g) {
        if (baseDestroyed) {
            drawDestroyed(g);
        } else {
            drawAlive(g);
        }
    }
 
    private void drawAlive(Graphics g) {
        // Black background
        g.setColor(Color.BLACK);
        g.fillRect(x, y, SIZE, SIZE);
 
        // Body — central square
        g.setColor(EAGLE_BODY);
        g.fillRect(x + 8, y + 6, 16, 20);
 
        // Wings — triangles on each side
        g.setColor(EAGLE_WING);
        // Left wing
        int[] lx = { x + 2,  x + 8,  x + 8  };
        int[] ly = { y + 14, y + 8,  y + 20 };
        g.fillPolygon(lx, ly, 3);
        // Right wing
        int[] rx = { x + 30, x + 24, x + 24 };
        int[] ry = { y + 14, y + 8,  y + 20 };
        g.fillPolygon(rx, ry, 3);
 
        // Head
        g.setColor(EAGLE_BODY);
        g.fillOval(x + 10, y + 2, 12, 10);
 
        // Eye
        g.setColor(EAGLE_EYE);
        g.fillOval(x + 14, y + 4, 4, 4);
 
        // Beak
        g.setColor(EAGLE_DARK);
        g.fillRect(x + 14, y + 9, 4, 3);
 
        // Feet
        g.setColor(EAGLE_DARK);
        g.fillRect(x + 10, y + 25, 4, 4);
        g.fillRect(x + 18, y + 25, 4, 4);
    }
 
    private void drawDestroyed(Graphics g) {
        // Dark background
        g.setColor(Color.BLACK);
        g.fillRect(x, y, SIZE, SIZE);
 
        // Rubble chunks scattered across the tile
        g.setColor(RUBBLE_DARK);
        g.fillRect(x + 2,  y + 18, 10, 8);
        g.fillRect(x + 16, y + 20, 12, 8);
        g.fillRect(x + 8,  y + 22, 8,  6);
 
        g.setColor(RUBBLE_MID);
        g.fillRect(x + 4,  y + 20, 6, 4);
        g.fillRect(x + 18, y + 22, 6, 4);
 
        g.setColor(RUBBLE_LIGHT);
        g.fillRect(x + 6,  y + 21, 3, 2);
        g.fillRect(x + 20, y + 23, 3, 2);
 
        // Scattered smaller bits (top half)
        g.setColor(RUBBLE_MID);
        g.fillRect(x + 2,  y + 4,  6, 6);
        g.fillRect(x + 22, y + 6,  6, 6);
        g.fillRect(x + 12, y + 8,  5, 5);
    }
 
    // -------------------------------------------------------------------------
    // onHit — any bullet (player or enemy) destroys the base and ends the game
    // -------------------------------------------------------------------------
    @Override
    public void onHit(int starLevel, boolean fromPlayer) {
        baseDestroyed = true;
        destroyed = true;   // also set the parent flag so Map can detect it
    }
 
    /**
     * Used by GameEngine each frame to check for the game-over condition.
     * Returns true if the base has been hit by any bullet.
     */
    public boolean isBaseDestroyed() {
        return baseDestroyed;
    }
}