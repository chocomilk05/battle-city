package entities;

import map.Map;
import java.awt.Color;
import java.awt.Graphics;

/**
 * Abstract base class for all tanks (player and enemy).
 *
 * Defines the shared state and behaviour every tank has:
 *   - position (x, y), size, direction, speed
 *   - lives, star level, shield
 *   - move() — moves in the current direction if the map allows it
 *   - shoot() — creates and returns a new Bullet
 *   - draw() — draws the tank body; subclasses may override for extras
 *   - abstract update() — subclasses implement their own input / AI logic
 *
 * Polymorphism: a List<Tank> in Level can hold both PlayerTank and
 * EnemyTank objects.  Calling tank.update() dispatches to the right
 * subclass at runtime.
 */
public abstract class Tank {

    // -------------------------------------------------------------------------
    // Direction constants — used for movement and sprite rotation
    // -------------------------------------------------------------------------
    public static final int UP    = 0;
    public static final int RIGHT = 1;
    public static final int DOWN  = 2;
    public static final int LEFT  = 3;

    // -------------------------------------------------------------------------
    // Shared state — protected so subclasses can read/write directly
    // -------------------------------------------------------------------------

    /** Top-left pixel position on the game canvas. */
    protected int x;
    protected int y;

    /** Tank body size in pixels (square). */
    public static final int SIZE = 28;

    /** Current facing direction (UP / RIGHT / DOWN / LEFT). */
    protected int direction;

    /**
     * Movement speed in pixels per game-loop tick.
     * PlayerTank default: 2.  EnemyTank: varies by difficulty level.
     */
    protected int speed;

    /**
     * Remaining lives.
     * Player starts with 3.  Enemies have 1 (basic) or more (armoured).
     */
    protected int lives;

    /**
     * Star upgrade level (1, 2, or 3) — only meaningful for PlayerTank,
     * but stored here so Bullet.onHit() can check it without casting.
     *
     * 1 star → faster bullets
     * 2 stars → two simultaneous bullets allowed
     * 3 stars → bullets can destroy SteelWall
     */
    protected int starLevel;

    /** Whether the tank is currently shielded (invulnerable). */
    protected boolean shielded;

    /**
     * Whether this tank has been destroyed and should be removed from
     * the game's entity list.
     */
    protected boolean destroyed;

    /** Reference to the map — needed by move() for collision checks. */
    protected Map map;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    /**
     * @param x         initial pixel x (top-left)
     * @param y         initial pixel y (top-left)
     * @param direction initial facing direction
     * @param speed     movement speed in px / tick
     * @param lives     starting life count
     * @param map       the current level's map (for collision)
     */
    public Tank(int x, int y, int direction, int speed, int lives, Map map) {
        this.x         = x;
        this.y         = y;
        this.direction = direction;
        this.speed     = speed;
        this.lives     = lives;
        this.map       = map;
        this.starLevel = 1;
        this.shielded  = false;
        this.destroyed = false;
    }

    // -------------------------------------------------------------------------
    // Abstract methods — subclasses MUST implement
    // -------------------------------------------------------------------------

    /**
     * Called every game-loop tick by GameEngine.
     * PlayerTank reads keyboard input; EnemyTank runs its AI.
     */
    public abstract void update();

    // -------------------------------------------------------------------------
    // Movement
    // -------------------------------------------------------------------------

    /**
     * Attempt to move one step in the current direction.
     * Checks map boundaries and tile passability before moving.
     * Aligns the tank to the tile grid to prevent diagonal clipping
     * through narrow gaps (classic Battle City feel).
     */
    public void move() {
        int nx = x;
        int ny = y;

        switch (direction) {
            case UP:    ny -= speed; break;
            case DOWN:  ny += speed; break;
            case LEFT:  nx -= speed; break;
            case RIGHT: nx += speed; break;
        }

        // Map boundary clamp
        nx = Math.max(0, Math.min(nx, map.getPixelWidth()  - SIZE));
        ny = Math.max(0, Math.min(ny, map.getPixelHeight() - SIZE));

        // Tile collision check — only move if the new position is clear
        if (!map.isBlocked(nx, ny, SIZE, SIZE)) {
            x = nx;
            y = ny;
        }
    }

    // -------------------------------------------------------------------------
    // Shooting
    // -------------------------------------------------------------------------

    /**
     * Create and return a new Bullet fired from this tank's barrel tip.
     * The bullet starts just in front of the tank, centred on the barrel.
     *
     * Bullet speed, max-simultaneous-shots, and steel-piercing are all
     * derived from this tank's starLevel.
     *
     * @return a new active Bullet, or null if the tank cannot fire right now
     *         (caller — PlayerTank / EnemyTank — decides the fire-rate logic)
     */
    public Bullet shoot() {
        int bx, by;
        boolean isPlayer = (this instanceof PlayerTank);

        switch (direction) {
            case UP:
                bx = x + SIZE / 2 - Bullet.SIZE / 2;
                by = y - Bullet.SIZE;
                break;
            case DOWN:
                bx = x + SIZE / 2 - Bullet.SIZE / 2;
                by = y + SIZE;
                break;
            case LEFT:
                bx = x - Bullet.SIZE;
                by = y + SIZE / 2 - Bullet.SIZE / 2;
                break;
            case RIGHT: // fall-through
            default:
                bx = x + SIZE;
                by = y + SIZE / 2 - Bullet.SIZE / 2;
                break;
        }

        return new Bullet(bx, by, direction, starLevel, isPlayer, map);
    }

    // -------------------------------------------------------------------------
    // Damage
    // -------------------------------------------------------------------------

    /**
     * Called when a bullet hits this tank.
     * Shielded tanks take no damage.
     * Unshielded tanks lose one life; if lives reach 0, destroyed = true.
     */
    public void hit() {
        if (shielded) return;
        lives--;
        if (lives <= 0) {
            destroyed = true;
        }
    }

    // -------------------------------------------------------------------------
    // Drawing
    // -------------------------------------------------------------------------

    /**
     * Draw the tank body facing its current direction.
     * Subclasses can call super.draw(g) then add extras (e.g. shield ring).
     *
     * Replace the fillRect calls with sprite-sheet sub-images when you
     * have your image assets:
     *   g.drawImage(sprites[direction], x, y, SIZE, SIZE, null);
     */
    public void draw(Graphics g) {
        if (destroyed) return;

        Color bodyColor  = getTankColor();
        Color barrelColor = bodyColor.darker();

        // --- Body
        g.setColor(bodyColor);
        g.fillRect(x + 4, y + 4, SIZE - 8, SIZE - 8);

        // --- Tracks (left and right strips)
        g.setColor(bodyColor.darker());
        g.fillRect(x,          y + 4, 6,       SIZE - 8);  // left track
        g.fillRect(x + SIZE - 6, y + 4, 6,     SIZE - 8);  // right track

        // --- Barrel — direction-dependent rectangle
        g.setColor(barrelColor);
        int bw = 4, blen = 10;
        switch (direction) {
            case UP:
                g.fillRect(x + SIZE / 2 - bw / 2, y - blen, bw, blen + 2);
                break;
            case DOWN:
                g.fillRect(x + SIZE / 2 - bw / 2, y + SIZE - 2, bw, blen);
                break;
            case LEFT:
                g.fillRect(x - blen, y + SIZE / 2 - bw / 2, blen + 2, bw);
                break;
            case RIGHT:
                g.fillRect(x + SIZE - 2, y + SIZE / 2 - bw / 2, blen, bw);
                break;
        }

        // --- Shield visual (bright cyan ring around tank)
        if (shielded) {
            g.setColor(new Color(0, 220, 255, 160));
            g.drawOval(x - 4, y - 4, SIZE + 8, SIZE + 8);
            g.drawOval(x - 3, y - 3, SIZE + 6, SIZE + 6);
        }
    }

    /**
     * Returns the body colour for this tank type.
     * Overridden by subclasses to distinguish player (yellow-green)
     * from enemies (various greys / silvers depending on difficulty).
     */
    protected abstract Color getTankColor();

    // -------------------------------------------------------------------------
    // Getters and setters
    // -------------------------------------------------------------------------

    public int  getX()          { return x; }
    public int  getY()          { return y; }
    public int  getDirection()  { return direction; }
    public int  getLives()      { return lives; }
    public int  getStarLevel()  { return starLevel; }
    public boolean isShielded() { return shielded; }
    public boolean isDestroyed(){ return destroyed; }

    public void setDirection(int direction) { this.direction = direction; }
    public void setShielded(boolean s)      { this.shielded  = s; }
    public void setStarLevel(int level)     { this.starLevel = Math.min(level, 3); }
    public void addLife()                   { this.lives++; }
    public void setMap(Map map)             { this.map = map; }

    /**
     * Simple bounding-box check — used by GameEngine for tank-bullet and
     * tank-tank collision.
     */
    public boolean intersects(int ox, int oy, int ow, int oh) {
        return x < ox + ow && x + SIZE > ox &&
               y < oy + oh && y + SIZE > oy;
    }
}