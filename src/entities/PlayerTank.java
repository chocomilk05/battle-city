package entities;

import map.Map;
import java.awt.Color;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * PlayerTank — the human-controlled tank.
 *
 * Controls  : W = up, S = down, A = left, D = right, SPACE = fire
 *
 * Star upgrade effects (from the spec):
 *   1 star  → faster bullets (default)
 *   2 stars → two simultaneous bullets allowed
 *   3 stars → bullets can destroy SteelWall
 *
 * Key input is tracked via a Set<Integer> of currently-held key codes.
 * GamePanel calls keyPressed() / keyReleased() on this object.
 * update() is then called each tick by GameEngine; it reads the set and
 * acts — giving smooth, responsive movement without missing key events.
 *
 * Fire-rate limiting: a cooldown counter is decremented each tick.
 * The player can only shoot when the counter reaches zero AND the number
 * of active bullets is below the star-level cap.
 */
public class PlayerTank extends Tank {

    // -------------------------------------------------------------------------
    // Constants
    // -------------------------------------------------------------------------

    private static final int DEFAULT_SPEED      = 2;
    private static final int START_LIVES        = 3;

    /** Ticks between shots at star level 1. Fewer ticks = faster fire. */
    private static final int COOLDOWN_STAR_1    = 30;
    private static final int COOLDOWN_STAR_2    = 25;
    private static final int COOLDOWN_STAR_3    = 20;

    /** Max simultaneous bullets: 1 for stars 1 & 3, 2 for star 2+. */
    private static final int MAX_BULLETS_STAR_1 = 1;
    private static final int MAX_BULLETS_STAR_2 = 2;

    /** Ticks the shield lasts after a Shield power-up is collected. */
    private static final int SHIELD_DURATION    = 300;  // ~5 s at 60 fps

    // -------------------------------------------------------------------------
    // Input state
    // -------------------------------------------------------------------------

    /** Keys currently held down — populated by keyPressed / keyReleased. */
    private final Set<Integer> keysHeld = new HashSet<>();

    // -------------------------------------------------------------------------
    // Bullet management
    // -------------------------------------------------------------------------

    /**
     * Bullets fired by the player that are still active.
     * GameEngine reads this list to draw bullets and check collisions.
     * PlayerTank manages adding new bullets; GameEngine removes inactive ones.
     */
    private final ArrayList<Bullet> bullets = new ArrayList<>();

    /** Ticks remaining before the player can fire again. */
    private int fireCooldown = 0;

    // -------------------------------------------------------------------------
    // Power-up timers (countdown in ticks; 0 = inactive)
    // -------------------------------------------------------------------------

    private int shieldTimer  = 0;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    /**
     * Spawn the player tank at pixel position (x, y) facing UP.
     *
     * @param x   pixel x (top-left)
     * @param y   pixel y (top-left)
     * @param map the current level's map
     */
    public PlayerTank(int x, int y, Map map) {
        super(x, y, Tank.UP, DEFAULT_SPEED, START_LIVES, map);
    }

    // -------------------------------------------------------------------------
    // Key event handling — called by GamePanel's KeyListener
    // -------------------------------------------------------------------------

    public void keyPressed(int keyCode)  { keysHeld.add(keyCode); }
    public void keyReleased(int keyCode) { keysHeld.remove(keyCode); }

    // -------------------------------------------------------------------------
    // update — main per-tick logic (overrides abstract Tank.update)
    // -------------------------------------------------------------------------

    /**
     * Called every game-loop tick by GameEngine.
     * Reads held keys, moves the tank, fires if SPACE is held,
     * and ticks all active power-up timers.
     */
    @Override
    public void update() {
        if (destroyed) return;

        // --- Movement: only one direction at a time; priority: W > S > A > D
        if (keysHeld.contains(KeyEvent.VK_W)) {
            direction = Tank.UP;
            move();
        } else if (keysHeld.contains(KeyEvent.VK_S)) {
            direction = Tank.DOWN;
            move();
        } else if (keysHeld.contains(KeyEvent.VK_A)) {
            direction = Tank.LEFT;
            move();
        } else if (keysHeld.contains(KeyEvent.VK_D)) {
            direction = Tank.RIGHT;
            move();
        }

        // --- Shooting
        if (fireCooldown > 0) fireCooldown--;

        if (keysHeld.contains(KeyEvent.VK_SPACE) && canFire()) {
            Bullet b = shoot();
            bullets.add(b);
            fireCooldown = getFireCooldown();
        }

        // --- Tick active bullets
        for (Bullet b : bullets) {
            b.update();
        }
        // Remove bullets that have become inactive (hit something / left bounds)
        bullets.removeIf(b -> !b.isActive());

        // --- Power-up timers
        tickShield();
    }

    // -------------------------------------------------------------------------
    // Fire-rate helpers
    // -------------------------------------------------------------------------

    /**
     * True if the fire cooldown has expired AND we haven't hit the
     * simultaneous-bullet cap for our current star level.
     */
    private boolean canFire() {
        if (fireCooldown > 0) return false;
        int maxBullets = (starLevel >= 2) ? MAX_BULLETS_STAR_2 : MAX_BULLETS_STAR_1;
        return bullets.size() < maxBullets;
    }

    /** Return the correct cooldown length for the current star level. */
    private int getFireCooldown() {
        switch (starLevel) {
            case 3:  return COOLDOWN_STAR_3;
            case 2:  return COOLDOWN_STAR_2;
            default: return COOLDOWN_STAR_1;
        }
    }

    // -------------------------------------------------------------------------
    // Shield timer
    // -------------------------------------------------------------------------

    private void tickShield() {
        if (shieldTimer > 0) {
            shieldTimer--;
            shielded = (shieldTimer > 0);
        }
    }

    // -------------------------------------------------------------------------
    // Power-up collection
    // -------------------------------------------------------------------------

    /**
     * Called by GameEngine when the player tank overlaps a power-up tile.
     * Each power-up calls the relevant method on the player rather than
     * modifying state directly — keeping the logic here, not scattered.
     *
     * Alternatively you can use a PowerUp.apply(PlayerTank) pattern; both
     * are equally valid for the course.
     */
    public void collectStar() {
        if (starLevel < 3) starLevel++;
    }

    public void collectExtraLife() {
        lives++;
    }

    public void activateShield() {
        shielded = true;
        shieldTimer = SHIELD_DURATION;
    }

    public void deactivateShield() {
         shielded = false; shieldTimer = 0; 
    }

    // -------------------------------------------------------------------------
    // Respawn
    // -------------------------------------------------------------------------

    /**
     * Respawn the player at a given position after losing a life.
     * Resets direction, clears bullets, and grants brief spawn shield.
     *
     * @param spawnX spawn pixel x
     * @param spawnY spawn pixel y
     */
    public void respawn(int spawnX, int spawnY) {
        x         = spawnX;
        y         = spawnY;
        direction = Tank.UP;
        destroyed = false;
        shielded  = true;
        shieldTimer = 120;   // ~2 s of spawn protection
        bullets.clear();
        fireCooldown = 0;
        // Note: lives was already decremented by hit() before this is called
    }

    // -------------------------------------------------------------------------
    // Drawing
    // -------------------------------------------------------------------------

    @Override
    public void draw(java.awt.Graphics g) {
        if (destroyed) return;
        super.draw(g);   // draws body, tracks, barrel, shield ring
    }

    /** Yellow-green player colour — classic Battle City player tank. */
    @Override
    protected Color getTankColor() {
        return new Color(200, 200, 50);
    }

    // -------------------------------------------------------------------------
    // Getters used by GameEngine / HUD
    // -------------------------------------------------------------------------

    public ArrayList<Bullet> getBullets() { return bullets; }
    public int getScore()                 { return 0; /* tracked in GameEngine */ }
}