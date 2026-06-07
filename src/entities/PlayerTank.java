package entities;

import map.Map;
import java.awt.Color;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * PlayerTank
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

    private static final int DEFAULT_SPEED      = 2;
    private static final int START_LIVES        = 3;

    private static final int COOLDOWN_STAR_1    = 30;
    private static final int COOLDOWN_STAR_2    = 25;
    private static final int COOLDOWN_STAR_3    = 20;

    private static final int MAX_BULLETS_STAR_1 = 1;
    private static final int MAX_BULLETS_STAR_2 = 2;

    private static final int SHIELD_DURATION    = 300;  // ~5 s at 60 fps

    private final Set<Integer> keysHeld = new HashSet<>();

    private final ArrayList<Bullet> bullets = new ArrayList<>();

    private int fireCooldown = 0;

    private int shieldTimer  = 0;

    public PlayerTank(int x, int y, Map map) {
        super(x, y, Tank.UP, DEFAULT_SPEED, START_LIVES, map);
    }

    public void keyPressed(int keyCode)  { keysHeld.add(keyCode); }
    public void keyReleased(int keyCode) { keysHeld.remove(keyCode); }

    @Override
    public void update() {
        if (destroyed) return;

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

        if (fireCooldown > 0) fireCooldown--;

        if (keysHeld.contains(KeyEvent.VK_SPACE) && canFire()) {
            Bullet b = shoot();
            bullets.add(b);
            fireCooldown = getFireCooldown();
        }

        for (Bullet b : bullets) {
            b.update();
        }
        bullets.removeIf(b -> !b.isActive());

        tickShield();
    }

    private boolean canFire() {
        if (fireCooldown > 0) return false;
        int maxBullets = (starLevel >= 2) ? MAX_BULLETS_STAR_2 : MAX_BULLETS_STAR_1;
        return bullets.size() < maxBullets;
    }

    private int getFireCooldown() {
        switch (starLevel) {
            case 3:  return COOLDOWN_STAR_3;
            case 2:  return COOLDOWN_STAR_2;
            default: return COOLDOWN_STAR_1;
        }
    }

    private void tickShield() {
        if (shieldTimer > 0) {
            shieldTimer--;
            shielded = (shieldTimer > 0);
        }
    }

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

    public void respawn(int spawnX, int spawnY) {
        x         = spawnX;
        y         = spawnY;
        direction = Tank.UP;
        destroyed = false;
        shielded  = true;
        shieldTimer = 120; 
        bullets.clear();
        fireCooldown = 0;
    }

    @Override
    public void draw(java.awt.Graphics g) {
        if (destroyed) return;
        super.draw(g);
    }

    @Override
    protected Color getTankColor() {
        return new Color(200, 200, 50);
    }

    public ArrayList<Bullet> getBullets() { return bullets; }
    public int getScore()                 { return 0; /* tracked in GameEngine */ }
}