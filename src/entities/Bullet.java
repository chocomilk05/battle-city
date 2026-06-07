package entities;

import map.Map;
import util.SpriteRegistry;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

public class Bullet {

    public static final int SIZE = 6;

    private static final int BASE_SPEED   = 5;
    private static final int STAR_1_SPEED = 6;

    private int x;
    private int y;

    private final int     direction;
    private final int     speed;
    private final int     starLevel;
    private final boolean fromPlayer;
    private boolean       active;
    private final Map     map;

    public Bullet(int x, int y, int direction, int starLevel,
                  boolean fromPlayer, Map map) {
        this.x          = x;
        this.y          = y;
        this.direction  = direction;
        this.starLevel  = starLevel;
        this.fromPlayer = fromPlayer;
        this.map        = map;
        this.active     = true;
        this.speed      = (starLevel >= 1) ? STAR_1_SPEED : BASE_SPEED;
    }

    //Update

    public void update() {
        if (!active) return;

        switch (direction) {
            case Tank.UP:    y -= speed; break;
            case Tank.DOWN:  y += speed; break;
            case Tank.LEFT:  x -= speed; break;
            case Tank.RIGHT: x += speed; break;
        }

        int mapW = (map != null) ? map.getPixelWidth()  : ui.GamePanel.MAP_WIDTH;
        int mapH = (map != null) ? map.getPixelHeight() : ui.GamePanel.MAP_HEIGHT;

        if (x < 0 || y < 0 || x + SIZE > mapW || y + SIZE > mapH) {
            active = false;
            return;
        }

        if (map != null && map.bulletHit(x, y, SIZE, SIZE, starLevel, fromPlayer)) {
            active = false;
        }
    }

    //Drawing

    public void draw(Graphics g) {
        if (!active) return;

        BufferedImage sprite = getSpriteForDirection();

        if (sprite != null) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                                RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            g2.drawImage(sprite, x, y, SIZE, SIZE, null);
        } else {
            g.setColor(fromPlayer ? new Color(255, 230, 0) : new Color(255, 60, 60));
            g.fillRect(x, y, SIZE, SIZE);
            g.setColor(Color.WHITE);
            g.fillRect(x + SIZE / 2 - 1, y + SIZE / 2 - 1, 2, 2);
        }
    }

    /*
     * Pick the correct sprite based on owner (player vs enemy) and direction
     */
    private BufferedImage getSpriteForDirection() {
        if (fromPlayer) {
            switch (direction) {
                case Tank.UP:    return SpriteRegistry.BULLET_PLAYER_UP;
                case Tank.RIGHT: return SpriteRegistry.BULLET_PLAYER_RIGHT;
                case Tank.DOWN:  return SpriteRegistry.BULLET_PLAYER_DOWN;
                case Tank.LEFT:  return SpriteRegistry.BULLET_PLAYER_LEFT;
                default:         return SpriteRegistry.BULLET_PLAYER_UP;
            }
        } else {
            switch (direction) {
                case Tank.UP:    return SpriteRegistry.BULLET_ENEMY_UP;
                case Tank.RIGHT: return SpriteRegistry.BULLET_ENEMY_RIGHT;
                case Tank.DOWN:  return SpriteRegistry.BULLET_ENEMY_DOWN;
                case Tank.LEFT:  return SpriteRegistry.BULLET_ENEMY_LEFT;
                default:         return SpriteRegistry.BULLET_ENEMY_UP;
            }
        }
    }

    public int     getX()          { return x; }
    public int     getY()          { return y; }
    public boolean isActive()      { return active; }
    public boolean isFromPlayer()  { return fromPlayer; }
    public int     getStarLevel()  { return starLevel; }

    public void deactivate() { active = false; }

    public boolean intersects(int tx, int ty, int tw, int th) {
        return x < tx + tw && x + SIZE > tx &&
               y < ty + th && y + SIZE > ty;
    }
}