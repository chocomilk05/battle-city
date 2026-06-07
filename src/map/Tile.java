package map;
 
import java.awt.Graphics;
 
/**
 * Abstract base class for all map tiles.
 *
 * Every tile on the grid extends this class and overrides:
 *   - draw()   : how to paint itself
 *   - onHit()  : what happens when a bullet strikes it
 *
 * The two boolean flags drive movement and collision logic in
 * the game engine without needing to know the concrete type.
 */
public abstract class Tile {
 
    // Pixel position of this tile's top-left corner on screen
    protected int x;
    protected int y;
 
    // Tile size in pixels — all tiles share the same constant size
    public static final int SIZE = 32;
 
    /**
     * Whether a bullet can destroy this tile.
     * BrickWall → true, SteelWall → depends on star level,
     * Water / Bush / Base → false (base has its own game-over logic)
     */
    protected boolean destructible;
 
    /**
     * Whether a tank can drive onto / through this tile.
     * Bush → true (tanks move under bushes), everything else → false.
     */
    protected boolean passable;
 
    /**
     * Whether this tile has been destroyed and should be removed from the grid.
     * The map checks this flag each frame and replaces destroyed tiles with null.
     */
    protected boolean destroyed;
 
    public Tile(int x, int y, boolean destructible, boolean passable) {
        this.x = x;
        this.y = y;
        this.destructible = destructible;
        this.passable = passable;
        this.destroyed = false;
    }
 
    // -------------------------------------------------------------------------
    // Abstract methods — every subclass must implement these
    // -------------------------------------------------------------------------
 
    /**
     * Draw this tile using the provided Graphics context.
     * Called every frame by Map.draw().
     */
    public abstract void draw(Graphics g);
 
    /**
     * Called when a bullet hits this tile.
     * Subclasses decide whether to destroy themselves, apply special logic, etc.
     *
     * @param starLevel the shooter's star level (1-3); needed by SteelWall
     * @param fromPlayer true if the bullet belongs to the player
     */
    public abstract void onHit(int starLevel, boolean fromPlayer);
 
    // -------------------------------------------------------------------------
    // Getters used by the game engine
    // -------------------------------------------------------------------------
 
    public int getX()               { return x; }
    public int getY()               { return y; }
    public boolean isDestructible() { return destructible; }
    public boolean isPassable()     { return passable; }
    public boolean isDestroyed()    { return destroyed; }
 
    /**
     * Convenience: returns the column index of this tile in the grid.
     * e.g. tile at pixel x=96 with SIZE=32 → column 3
     */
    public int getCol() { return x / SIZE; }
 
    /**
     * Convenience: returns the row index of this tile in the grid.
     */
    public int getRow() { return y / SIZE; }
}
