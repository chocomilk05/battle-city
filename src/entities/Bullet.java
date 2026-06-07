package entities;

import map.Map;
import java.awt.Color;
import java.awt.Graphics;

/**
 * Bullet — a projectile fired by a tank.
 *
 * Lifecycle:
 *   1. Created by Tank.shoot() and added to the firing tank's bullet list.
 *   2. Each tick, GameEngine calls bullet.update():
 *        a. Move in the firing direction.
 *        b. Check map bounds → deactivate if off-screen.
 *        c. Check map tile collision via Map.bulletHit() → deactivate + destroy tile.
 *   3. GameEngine also checks bullet-vs-tank collisions externally
 *      (so each enemy bullet is tested against the player, and vice versa).
 *   4. When active == false, the owning tank's ArrayList removes it.
 *
 * Star-level effects (from spec):
 *   1 star  → normal speed
 *   2 stars → same speed (extra cap on simultaneous bullets, tracked in PlayerTank)
 *   3 stars → same speed BUT can destroy SteelWall
 *             (starLevel passed to Map.bulletHit → SteelWall.onHit checks it)
 */
public class Bullet {

    // -------------------------------------------------------------------------
    // Constants
    // -------------------------------------------------------------------------

    /** Bullet size in pixels (square hitbox). */
    public static final int SIZE = 6;

    /** Base bullet speed in pixels per tick. */
    private static final int BASE_SPEED   = 5;

    /** Extra speed added at star level 1 (faster shots). */
    private static final int STAR_1_SPEED = 6;

    // -------------------------------------------------------------------------
    // State
    // -------------------------------------------------------------------------

    /** Top-left pixel position. */
    private int x;
    private int y;

    /** Firing direction — one of Tank.UP / DOWN / LEFT / RIGHT. */
    private final int direction;

    /** Speed in pixels per tick, derived from the shooter's star level. */
    private final int speed;

    /**
     * Shooter's star level — passed to Map.bulletHit() so SteelWall.onHit()
     * can decide whether to break.
     */
    private final int starLevel;

    /**
     * True if this bullet was fired by the player.
     * Passed to Map.bulletHit() and used by GameEngine to determine
     * which tanks this bullet can hurt.
     */
    private final boolean fromPlayer;

    /**
     * Active flag.  Set to false when the bullet hits something or
     * leaves the map.  The owning tank removes it next cleanup pass.
     */
    private boolean active;

    /** Reference to the map for boundary and tile collision checks. */
    private final Map map;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    /**
     * @param x          initial pixel x (top-left of bullet)
     * @param y          initial pixel y
     * @param direction  Tank.UP / DOWN / LEFT / RIGHT
     * @param starLevel  shooter's star level (1–3)
     * @param fromPlayer true if fired by the player
     * @param map        current level's map
     */
    public Bullet(int x, int y, int direction, int starLevel,
                  boolean fromPlayer, Map map) {
        this.x          = x;
        this.y          = y;
        this.direction  = direction;
        this.starLevel  = starLevel;
        this.fromPlayer = fromPlayer;
        this.map        = map;
        this.active     = true;

        // Star level 1 → faster bullets (per spec)
        this.speed = (starLevel >= 1) ? STAR_1_SPEED : BASE_SPEED;
    }

    // -------------------------------------------------------------------------
    // update — called every game-loop tick
    // -------------------------------------------------------------------------

    /**
     * Move the bullet one step and check for map collisions.
     *
     * Tank-vs-bullet collisions are handled externally by GameEngine
     * because this class doesn't have access to the tank list.
     */
    public void update() {
        if (!active) return;

        // --- Move
        switch (direction) {
            case Tank.UP:    y -= speed; break;
            case Tank.DOWN:  y += speed; break;
            case Tank.LEFT:  x -= speed; break;
            case Tank.RIGHT: x += speed; break;
        }

        // --- Map boundary check
        if (x < 0 || y < 0 ||
            x + SIZE > map.getPixelWidth() ||
            y + SIZE > map.getPixelHeight()) {
            active = false;
            return;
        }

        // --- Tile collision: Map handles which tiles stop bullets and
        //     calls the appropriate Tile.onHit() via polymorphism
        boolean hitTile = map.bulletHit(x, y, SIZE, SIZE, starLevel, fromPlayer);
        if (hitTile) {
            active = false;
        }
    }

    // -------------------------------------------------------------------------
    // Drawing
    // -------------------------------------------------------------------------

    /**
     * Draw the bullet as a small filled square.
     * Player bullets → bright yellow; enemy bullets → bright red.
     *
     * Swap for a sprite sub-image when you have assets.
     */
    public void draw(Graphics g) {
        if (!active) return;

        if (fromPlayer) {
            g.setColor(new Color(255, 230, 0));   // yellow player bullet
        } else {
            g.setColor(new Color(255, 60, 60));   // red enemy bullet
        }
        g.fillRect(x, y, SIZE, SIZE);

        // Small white centre dot for a "lit" appearance
        g.setColor(Color.WHITE);
        g.fillRect(x + SIZE / 2 - 1, y + SIZE / 2 - 1, 2, 2);
    }

    // -------------------------------------------------------------------------
    // Getters — used by GameEngine for tank collision checks
    // -------------------------------------------------------------------------

    public int  getX()          { return x; }
    public int  getY()          { return y; }
    public boolean isActive()   { return active; }
    public boolean isFromPlayer(){ return fromPlayer; }
    public int  getStarLevel()  { return starLevel; }

    /**
     * Deactivate this bullet from outside (e.g. GameEngine confirmed it
     * hit a tank, or a Bomb power-up cleared all enemy bullets).
     */
    public void deactivate()    { active = false; }

    /**
     * Simple bounding-box overlap test — used by GameEngine to check
     * whether this bullet intersects a tank.
     *
     * @param tx tank pixel x
     * @param ty tank pixel y
     * @param tw tank width
     * @param th tank height
     */
    public boolean intersects(int tx, int ty, int tw, int th) {
        return x < tx + tw && x + SIZE > tx &&
               y < ty + th && y + SIZE > ty;
    }
}