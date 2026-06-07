package entities;

import map.Map;
import util.SpriteRegistry;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Random;

/*
  EnemyTank
  Sprites are chosen based on speed tier (easy / medium / hard) and the
  current facing direction pulled from SpriteRegistry.ENEMY_*_* fields
  If those fields are null Tank.draw() falls back automatically to the
  original colour-rectangle rendering
 */
public class EnemyTank extends Tank {

    public static final int SPEED_EASY   = 1;
    public static final int SPEED_MEDIUM = 2;
    public static final int SPEED_HARD   = 3;

    private static final int DIRECTION_CHANGE_TICKS = 60;
    private static final int FIRE_INTERVAL          = 90;
    private static final int SIGHT_TOLERANCE        = 12;

    private int directionTimer;
    private int fireTimer;
    private int frozenTimer;

    private final ArrayList<Bullet> bullets = new ArrayList<>();

    private int baseX;
    private int baseY;

    private PlayerTank player;

    private final Random random = new Random();

    private final int speedTier;

    // Fallback colours
    private static final Color COLOR_EASY   = new Color(180, 180, 180);
    private static final Color COLOR_MEDIUM = new Color(110, 110, 110);
    private static final Color COLOR_HARD   = new Color( 60,  60,  60);

    private final Color bodyColor;

    public EnemyTank(int x, int y, int speed, Map map,
                     PlayerTank player, int baseX, int baseY) {
        super(x, y, Tank.DOWN, speed, 1, map);
        this.player    = player;
        this.baseX     = baseX;
        this.baseY     = baseY;
        this.speedTier = speed;

        if      (speed >= SPEED_HARD)   bodyColor = COLOR_HARD;
        else if (speed >= SPEED_MEDIUM) bodyColor = COLOR_MEDIUM;
        else                             bodyColor = COLOR_EASY;

        directionTimer = random.nextInt(DIRECTION_CHANGE_TICKS);
        fireTimer      = random.nextInt(FIRE_INTERVAL);
    }

    //Update

    @Override
    public void update() {
        if (destroyed) return;

        if (frozenTimer > 0) {
            frozenTimer--;
            return;
        }

        int prevX = x, prevY = y;
        move();

        boolean blocked = (x == prevX && y == prevY);
        if (blocked) {
            pickNewDirection();
            directionTimer = DIRECTION_CHANGE_TICKS;
        }

        directionTimer--;
        if (directionTimer <= 0) {
            chooseDirection();
            directionTimer = DIRECTION_CHANGE_TICKS + random.nextInt(30);
        }

        fireTimer--;
        if (fireTimer <= 0) {
            if (hasLineOfSight()) {
                bullets.add(shoot());
            }
            fireTimer = FIRE_INTERVAL + random.nextInt(30);
        }

        for (Bullet b : bullets) b.update();
        bullets.removeIf(b -> !b.isActive());
    }

    //AI helpers

    private void chooseDirection() {
        if (random.nextBoolean()) aimTowardBase();
        else                      pickNewDirection();
    }

    private void aimTowardBase() {
        int dx = baseX - x;
        int dy = baseY - y;
        if (Math.abs(dx) > Math.abs(dy))
            direction = (dx > 0) ? Tank.RIGHT : Tank.LEFT;
        else
            direction = (dy > 0) ? Tank.DOWN  : Tank.UP;
    }

    private void pickNewDirection() {
        direction = random.nextInt(4);
    }

    private boolean hasLineOfSight() {
        int cx = x + SIZE / 2;
        int cy = y + SIZE / 2;

        if (!player.isDestroyed()) {
            int px = player.getX() + SIZE / 2;
            int py = player.getY() + SIZE / 2;
            if (direction == Tank.UP    && Math.abs(cx - px) < SIGHT_TOLERANCE && py < cy) return true;
            if (direction == Tank.DOWN  && Math.abs(cx - px) < SIGHT_TOLERANCE && py > cy) return true;
            if (direction == Tank.LEFT  && Math.abs(cy - py) < SIGHT_TOLERANCE && px < cx) return true;
            if (direction == Tank.RIGHT && Math.abs(cy - py) < SIGHT_TOLERANCE && px > cx) return true;
        }

        int bCx = baseX + SIZE / 2;
        int bCy = baseY + SIZE / 2;
        if (direction == Tank.DOWN  && Math.abs(cx - bCx) < SIGHT_TOLERANCE && bCy > cy) return true;
        if (direction == Tank.LEFT  && Math.abs(cy - bCy) < SIGHT_TOLERANCE && bCx < cx) return true;
        if (direction == Tank.RIGHT && Math.abs(cy - bCy) < SIGHT_TOLERANCE && bCx > cx) return true;

        return false;
    }

    //Frozen effect

    public void freeze(int ticks) { frozenTimer = ticks; }
    public boolean isFrozen()     { return frozenTimer > 0; }

    //Drawing

    @Override
    public void draw(Graphics g) {
        if (destroyed) return;

        super.draw(g);  // handles sprite or fallback, plus shield aura

        // Blue frozen tint overlay on top of the sprite
        if (frozenTimer > 0) {
            g.setColor(new Color(100, 180, 255, 80));
            g.fillRect(x, y, SIZE, SIZE);
        }
    }

    /*
     * Picks the sprite row that matches this tank's speed tier
     * then returns the frame for the given direction
     */
    @Override
    protected BufferedImage getSpriteForDirection(int direction) {
        BufferedImage up, right, down, left;

        if (speedTier >= SPEED_HARD) {
            up    = SpriteRegistry.ENEMY_HARD_UP;
            right = SpriteRegistry.ENEMY_HARD_RIGHT;
            down  = SpriteRegistry.ENEMY_HARD_DOWN;
            left  = SpriteRegistry.ENEMY_HARD_LEFT;
        } else if (speedTier >= SPEED_MEDIUM) {
            up    = SpriteRegistry.ENEMY_MED_UP;
            right = SpriteRegistry.ENEMY_MED_RIGHT;
            down  = SpriteRegistry.ENEMY_MED_DOWN;
            left  = SpriteRegistry.ENEMY_MED_LEFT;
        } else {
            up    = SpriteRegistry.ENEMY_EASY_UP;
            right = SpriteRegistry.ENEMY_EASY_RIGHT;
            down  = SpriteRegistry.ENEMY_EASY_DOWN;
            left  = SpriteRegistry.ENEMY_EASY_LEFT;
        }

        switch (direction) {
            case UP:    return up;
            case RIGHT: return right;
            case DOWN:  return down;
            case LEFT:  return left;
            default:    return down;
        }
    }

    @Override
    protected Color getTankColor() { return bodyColor; }

    public ArrayList<Bullet> getBullets()          { return bullets; }
    public void setBaseTarget(int bx, int by)      { baseX = bx; baseY = by; }
    public void setPlayer(PlayerTank p)            { player = p; }
}