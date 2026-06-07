package entities;

import map.Map;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Random;

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

    private static final Color COLOR_EASY   = new Color(180, 180, 180);
    private static final Color COLOR_MEDIUM = new Color(110, 110, 110);
    private static final Color COLOR_HARD   = new Color( 60,  60,  60);

    private final Color bodyColor;

   
    public EnemyTank(int x, int y, int speed, Map map,
                     PlayerTank player, int baseX, int baseY) {
        super(x, y, Tank.DOWN, speed, 1, map);
        this.player  = player;
        this.baseX   = baseX;
        this.baseY   = baseY;

        if      (speed >= SPEED_HARD)   bodyColor = COLOR_HARD;
        else if (speed >= SPEED_MEDIUM) bodyColor = COLOR_MEDIUM;
        else                             bodyColor = COLOR_EASY;

        directionTimer = random.nextInt(DIRECTION_CHANGE_TICKS);
        fireTimer      = random.nextInt(FIRE_INTERVAL);
    }

    @Override
    public void update() {
        if (destroyed) return;

        if (frozenTimer > 0) {
            frozenTimer--;
            return;
        }

        int prevX = x;
        int prevY = y;
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
                Bullet b = shoot();
                bullets.add(b);
            }
            fireTimer = FIRE_INTERVAL + random.nextInt(30);
        }

        for (Bullet b : bullets) {
            b.update();
        }
        bullets.removeIf(b -> !b.isActive());
    }

    private void chooseDirection() {
        if (random.nextBoolean()) {
            aimTowardBase();
        } else {
            pickNewDirection();
        }
    }

    private void aimTowardBase() {
        int dx = baseX - x;
        int dy = baseY - y;

        if (Math.abs(dx) > Math.abs(dy)) {
            direction = (dx > 0) ? Tank.RIGHT : Tank.LEFT;
        } else {
            direction = (dy > 0) ? Tank.DOWN  : Tank.UP;
        }
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

    public void freeze(int ticks) {
        frozenTimer = ticks;
    }

    public boolean isFrozen() { return frozenTimer > 0; }

    @Override
    public void draw(java.awt.Graphics g) {
        if (destroyed) return;

        super.draw(g); 

        if (frozenTimer > 0) {
            g.setColor(new Color(100, 180, 255, 80));
            g.fillRect(x, y, SIZE, SIZE);
        }
    }

    @Override
    protected Color getTankColor() { return bodyColor; }

    public ArrayList<Bullet> getBullets() { return bullets; }

    public void setBaseTarget(int bx, int by) { baseX = bx; baseY = by; }

    public void setPlayer(PlayerTank p) { player = p; }
}
