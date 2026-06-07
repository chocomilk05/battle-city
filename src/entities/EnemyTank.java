package entities;

import map.Map;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Random;

/**
 * EnemyTank — AI-controlled enemy.
 *
 * Spec requirements implemented here:
 *   - Enemies enter from the top of the screen (spawn at top).
 *   - They try to destroy the player's base (bottom centre) AND the player tank.
 *   - Difficulty increases by raising speed (set via constructor).
 *   - The Clock power-up freezes them (frozenTimer > 0 → skip update logic).
 *
 * AI behaviour (simple, course-appropriate — no pathfinding):
 *   1. Every DIRECTION_CHANGE_TICKS ticks, re-evaluate direction:
 *      - 50 % chance: aim toward the base (move down / horizontally).
 *      - 50 % chance: pick a random direction.
 *   2. If blocked (wall in the way), immediately pick a new random direction.
 *   3. Fire every FIRE_INTERVAL ticks when the player or base is roughly
 *      in the tank's line of sight (same row or column, ±SIGHT_TOLERANCE px).
 *
 * Bullet management: same ArrayList pattern as PlayerTank.
 * GameEngine reads EnemyTank.getBullets() to draw and check collisions.
 */
public class EnemyTank extends Tank {

    // -------------------------------------------------------------------------
    // Difficulty tiers — pass one of these as the speed constructor argument
    // -------------------------------------------------------------------------

    /** Level 1 enemy speed (slow). */
    public static final int SPEED_EASY   = 1;
    /** Level 2 enemy speed (normal). */
    public static final int SPEED_MEDIUM = 2;
    /** Level 3 enemy speed (fast). */
    public static final int SPEED_HARD   = 3;

    // -------------------------------------------------------------------------
    // AI timing constants
    // -------------------------------------------------------------------------

    /** Ticks between direction re-evaluations. */
    private static final int DIRECTION_CHANGE_TICKS = 60;

    /** Ticks between shots. */
    private static final int FIRE_INTERVAL          = 90;

    /**
     * How many pixels of horizontal/vertical offset is still considered
     * "aligned" for a line-of-sight shot.
     */
    private static final int SIGHT_TOLERANCE        = 12;

    // -------------------------------------------------------------------------
    // State
    // -------------------------------------------------------------------------

    /** Countdown to next direction change. */
    private int directionTimer;

    /** Countdown to next shot. */
    private int fireTimer;

    /**
     * Ticks remaining while this tank is frozen by the Clock power-up.
     * When > 0, update() skips all movement and shooting.
     * Decremented each tick by GameEngine (or here — both work).
     */
    private int frozenTimer;

    /** Active bullets fired by this enemy. */
    private final ArrayList<Bullet> bullets = new ArrayList<>();

    /** Target pixel x/y of the base — set by GameEngine after level load. */
    private int baseX;
    private int baseY;

    /** Reference to the player tank for line-of-sight shooting. */
    private PlayerTank player;

    private final Random random = new Random();

    // -------------------------------------------------------------------------
    // Body colours per difficulty (silver → darker → nearly black)
    // -------------------------------------------------------------------------
    private static final Color COLOR_EASY   = new Color(180, 180, 180);
    private static final Color COLOR_MEDIUM = new Color(110, 110, 110);
    private static final Color COLOR_HARD   = new Color( 60,  60,  60);

    private final Color bodyColor;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    /**
     * @param x      spawn pixel x (top of screen)
     * @param y      spawn pixel y (top of screen)
     * @param speed  movement speed — use SPEED_EASY / MEDIUM / HARD constants
     * @param map    current level's map
     * @param player reference to the player tank (for AI targeting)
     * @param baseX  pixel x of the eagle base (AI target)
     * @param baseY  pixel y of the eagle base (AI target)
     */
    public EnemyTank(int x, int y, int speed, Map map,
                     PlayerTank player, int baseX, int baseY) {
        super(x, y, Tank.DOWN, speed, /*lives*/ 1, map);
        this.player  = player;
        this.baseX   = baseX;
        this.baseY   = baseY;

        // Assign colour based on speed tier
        if      (speed >= SPEED_HARD)   bodyColor = COLOR_HARD;
        else if (speed >= SPEED_MEDIUM) bodyColor = COLOR_MEDIUM;
        else                             bodyColor = COLOR_EASY;

        // Stagger timers so not every enemy fires simultaneously at spawn
        directionTimer = random.nextInt(DIRECTION_CHANGE_TICKS);
        fireTimer      = random.nextInt(FIRE_INTERVAL);
    }

    // -------------------------------------------------------------------------
    // update — AI logic (overrides abstract Tank.update)
    // -------------------------------------------------------------------------

    /**
     * Called every game-loop tick by GameEngine.
     *
     * When frozenTimer > 0 (Clock power-up active), skip all logic.
     * Otherwise: move, change direction when needed / blocked, and fire.
     */
    @Override
    public void update() {
        if (destroyed) return;

        // Clock power-up: tank is frozen
        if (frozenTimer > 0) {
            frozenTimer--;
            return;
        }

        // --- Attempt to move; if blocked, change direction immediately
        int prevX = x;
        int prevY = y;
        move();

        boolean blocked = (x == prevX && y == prevY);
        if (blocked) {
            pickNewDirection();
            directionTimer = DIRECTION_CHANGE_TICKS;
        }

        // --- Periodic direction re-evaluation
        directionTimer--;
        if (directionTimer <= 0) {
            chooseDirection();
            directionTimer = DIRECTION_CHANGE_TICKS + random.nextInt(30);
        }

        // --- Shooting
        fireTimer--;
        if (fireTimer <= 0) {
            if (hasLineOfSight()) {
                Bullet b = shoot();
                bullets.add(b);
            }
            fireTimer = FIRE_INTERVAL + random.nextInt(30);
        }

        // --- Tick own bullets
        for (Bullet b : bullets) {
            b.update();
        }
        bullets.removeIf(b -> !b.isActive());
    }

    // -------------------------------------------------------------------------
    // AI helpers
    // -------------------------------------------------------------------------

    /**
     * Decide a new direction: 50 % chance aim for the base,
     * 50 % chance pick at random.
     */
    private void chooseDirection() {
        if (random.nextBoolean()) {
            aimTowardBase();
        } else {
            pickNewDirection();
        }
    }

    /**
     * Turn toward the base: prefer vertical movement (down toward base)
     * but correct horizontal offset first if large enough.
     */
    private void aimTowardBase() {
        int dx = baseX - x;
        int dy = baseY - y;

        if (Math.abs(dx) > Math.abs(dy)) {
            direction = (dx > 0) ? Tank.RIGHT : Tank.LEFT;
        } else {
            direction = (dy > 0) ? Tank.DOWN  : Tank.UP;
        }
    }

    /** Pick any of the four directions at random. */
    private void pickNewDirection() {
        direction = random.nextInt(4);
    }

    /**
     * Very simple line-of-sight check: is the player or the base
     * roughly aligned with this tank horizontally or vertically?
     *
     * This keeps the AI predictable and appropriate for a course project.
     */
    private boolean hasLineOfSight() {
        int cx = x + SIZE / 2;
        int cy = y + SIZE / 2;

        // Check alignment with the player
        if (!player.isDestroyed()) {
            int px = player.getX() + SIZE / 2;
            int py = player.getY() + SIZE / 2;

            if (direction == Tank.UP    && Math.abs(cx - px) < SIGHT_TOLERANCE && py < cy) return true;
            if (direction == Tank.DOWN  && Math.abs(cx - px) < SIGHT_TOLERANCE && py > cy) return true;
            if (direction == Tank.LEFT  && Math.abs(cy - py) < SIGHT_TOLERANCE && px < cx) return true;
            if (direction == Tank.RIGHT && Math.abs(cy - py) < SIGHT_TOLERANCE && px > cx) return true;
        }

        // Check alignment with the base
        int bCx = baseX + SIZE / 2;
        int bCy = baseY + SIZE / 2;

        if (direction == Tank.DOWN  && Math.abs(cx - bCx) < SIGHT_TOLERANCE && bCy > cy) return true;
        if (direction == Tank.LEFT  && Math.abs(cy - bCy) < SIGHT_TOLERANCE && bCx < cx) return true;
        if (direction == Tank.RIGHT && Math.abs(cy - bCy) < SIGHT_TOLERANCE && bCx > cx) return true;

        return false;
    }

    // -------------------------------------------------------------------------
    // Power-up effects applied by GameEngine
    // -------------------------------------------------------------------------

    /**
     * Freeze this tank for the given number of ticks (Clock power-up).
     * Calling freeze() while already frozen simply resets the timer.
     */
    public void freeze(int ticks) {
        frozenTimer = ticks;
    }

    public boolean isFrozen() { return frozenTimer > 0; }

    // -------------------------------------------------------------------------
    // Drawing
    // -------------------------------------------------------------------------

    @Override
    public void draw(java.awt.Graphics g) {
        if (destroyed) return;

        super.draw(g);  // body, tracks, barrel, shield

        // Visual indicator when frozen: draw a light-blue tint overlay
        if (frozenTimer > 0) {
            g.setColor(new Color(100, 180, 255, 80));
            g.fillRect(x, y, SIZE, SIZE);
        }
    }

    @Override
    protected Color getTankColor() { return bodyColor; }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    public ArrayList<Bullet> getBullets() { return bullets; }

    /** Update base target position (needed when Shovel rearranges tiles). */
    public void setBaseTarget(int bx, int by) { baseX = bx; baseY = by; }

    /** Update player reference if needed mid-level. */
    public void setPlayer(PlayerTank p) { player = p; }
}
