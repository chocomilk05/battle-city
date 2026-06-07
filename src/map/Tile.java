package map;
 
import java.awt.Graphics;
 
/*
  Abstract base class for all map tiles.
 
  Every tile on the grid extends this class and overrides:
    - draw(): how to paint itself
    - onHit(): what happens when a bullet hits it
 */
public abstract class Tile {
 
    protected int x;
    protected int y;
 
    public static final int SIZE = 32;

    protected boolean destructible;

    protected boolean passable;

    protected boolean destroyed;
 
    public Tile(int x, int y, boolean destructible, boolean passable) {
        this.x = x;
        this.y = y;
        this.destructible = destructible;
        this.passable = passable;
        this.destroyed = false;
    }
 
    // Abstract methods

    public abstract void draw(Graphics g);

    public abstract void onHit(int starLevel, boolean fromPlayer);

    public int getX()               { return x; }
    public int getY()               { return y; }
    public boolean isDestructible() { return destructible; }
    public boolean isPassable()     { return passable; }
    public boolean isDestroyed()    { return destroyed; }

    public int getCol() { return x / SIZE; }

    public int getRow() { return y / SIZE; }
}
