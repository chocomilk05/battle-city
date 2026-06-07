package entities;

import map.Map;
import java.awt.Color;
import java.awt.Graphics;

public class Bullet {

    public static final int SIZE = 6;

    private static final int BASE_SPEED   = 5;

    private static final int STAR_1_SPEED = 6;

    private int x;
    private int y;

    private final int direction;

    private final int speed;

    private final int starLevel;

    private final boolean fromPlayer;

    private boolean active;

    private final Map map;

    public Bullet(int x, int y, int direction, int starLevel,
                  boolean fromPlayer, Map map) {
        this.x          = x;
        this.y          = y;
        this.direction  = direction;
        this.starLevel  = starLevel;
        this.fromPlayer = fromPlayer;
        this.map        = map;
        this.active     = true;

        this.speed = (starLevel >= 1) ? STAR_1_SPEED : BASE_SPEED;
    }

    public void update() {
        if (!active) return;

        switch (direction) {
            case Tank.UP:    y -= speed; break;
            case Tank.DOWN:  y += speed; break;
            case Tank.LEFT:  x -= speed; break;
            case Tank.RIGHT: x += speed; break;
        }

        if (x < 0 || y < 0 ||
            x + SIZE > map.getPixelWidth() ||
            y + SIZE > map.getPixelHeight()) {
            active = false;
            return;
        }

        boolean hitTile = map.bulletHit(x, y, SIZE, SIZE, starLevel, fromPlayer);
        if (hitTile) {
            active = false;
        }
    }

    public void draw(Graphics g) {
        if (!active) return;

        if (fromPlayer) {
            g.setColor(new Color(255, 230, 0)); 
        } else {
            g.setColor(new Color(255, 60, 60)); 
        }
        g.fillRect(x, y, SIZE, SIZE);

        g.setColor(Color.WHITE);
        g.fillRect(x + SIZE / 2 - 1, y + SIZE / 2 - 1, 2, 2);
    }

    public int  getX()          { return x; }
    public int  getY()          { return y; }
    public boolean isActive()   { return active; }
    public boolean isFromPlayer(){ return fromPlayer; }
    public int  getStarLevel()  { return starLevel; }

    public void deactivate()    { active = false; }

    public boolean intersects(int tx, int ty, int tw, int th) {
        return x < tx + tw && x + SIZE > tx &&
               y < ty + th && y + SIZE > ty;
    }
}