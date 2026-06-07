package entities;

import map.Map;
import util.SpriteRegistry;
import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class PlayerTank extends Tank {

    private static final int DEFAULT_SPEED      = 2;
    private static final int START_LIVES        = 3;

    private static final int COOLDOWN_STAR_1    = 30;
    private static final int COOLDOWN_STAR_2    = 25;
    private static final int COOLDOWN_STAR_3    = 20;

    private static final int MAX_BULLETS_STAR_1 = 1;
    private static final int MAX_BULLETS_STAR_2 = 2;

    private static final int SHIELD_DURATION    = 300;

    private final Set<Integer>      keysHeld = new HashSet<>();
    private final ArrayList<Bullet> bullets  = new ArrayList<>();

    private int fireCooldown = 0;
    private int shieldTimer  = 0;

    public PlayerTank(int x, int y, Map map) {
        super(x, y, Tank.UP, DEFAULT_SPEED, START_LIVES, map);
    }

    //Key input

    public void keyPressed(int keyCode)  { keysHeld.add(keyCode); }
    public void keyReleased(int keyCode) { keysHeld.remove(keyCode); }

    public void syncKeys(Set<Integer> currentKeys) {
        keysHeld.clear();
        keysHeld.addAll(currentKeys);
    }

    //Update

    @Override
    public void update() {
        if (destroyed) return;

        if (keysHeld.contains(KeyEvent.VK_W) || keysHeld.contains(KeyEvent.VK_UP)) {
            direction = Tank.UP;
            move();
        } else if (keysHeld.contains(KeyEvent.VK_S) || keysHeld.contains(KeyEvent.VK_DOWN)) {
            direction = Tank.DOWN;
            move();
        } else if (keysHeld.contains(KeyEvent.VK_A) || keysHeld.contains(KeyEvent.VK_LEFT)) {
            direction = Tank.LEFT;
            move();
        } else if (keysHeld.contains(KeyEvent.VK_D) || keysHeld.contains(KeyEvent.VK_RIGHT)) {
            direction = Tank.RIGHT;
            move();
        }

        if (fireCooldown > 0) fireCooldown--;

        if (keysHeld.contains(KeyEvent.VK_SPACE) && canFire()) {
            Bullet b = shoot();
            bullets.add(b);
            fireCooldown = getFireCooldown();
        }

        for (Bullet b : bullets) b.update();
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

    //Power-up effects

    public void collectStar()     { if (starLevel < 3) starLevel++; }
    public void collectExtraLife(){ lives++; }

    public void activateShield() {
        shielded    = true;
        shieldTimer = SHIELD_DURATION;
    }

    public void deactivateShield() {
        shielded    = false;
        shieldTimer = 0;
    }

    public void respawn(int spawnX, int spawnY) {
        x            = spawnX;
        y            = spawnY;
        direction    = Tank.UP;
        destroyed    = false;
        shielded     = true;
        shieldTimer  = 120;
        bullets.clear();
        fireCooldown = 0;
    }

    //Sprite selection

    /*
     * Returns the player sprite for the current facing direction.
     * Returns null if sprites were not loaded Tank.draw() will use
     * the colour-rectangle fallback automatically
     */
    @Override
    protected BufferedImage getSpriteForDirection(int direction) {
        switch (direction) {
            case UP:    return SpriteRegistry.PLAYER_UP;
            case RIGHT: return SpriteRegistry.PLAYER_RIGHT;
            case DOWN:  return SpriteRegistry.PLAYER_DOWN;
            case LEFT:  return SpriteRegistry.PLAYER_LEFT;
            default:    return SpriteRegistry.PLAYER_UP;
        }
    }

    @Override
    protected Color getTankColor() {
        return new Color(200, 200, 50);
    }

    public ArrayList<Bullet> getBullets() { return bullets; }
    public int getScore()                 { return 0; }
}