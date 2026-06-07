package entities;

import map.Map;
import util.SpriteRegistry;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

// Abstract base class for all tanks

public abstract class Tank {

    //Direction constants
    public static final int UP    = 0;
    public static final int RIGHT = 1;
    public static final int DOWN  = 2;
    public static final int LEFT  = 3;

    //Shared state
    protected int x;
    protected int y;

    public static final int SIZE = 28;

    protected int direction;
    protected int speed;
    protected int lives;
    protected int starLevel;
    protected boolean shielded;
    protected boolean destroyed;
    protected Map map;

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

    public abstract void update();

    //Movement

    public void move() {
        int nx = x;
        int ny = y;

        switch (direction) {
            case UP:    ny -= speed; break;
            case DOWN:  ny += speed; break;
            case LEFT:  nx -= speed; break;
            case RIGHT: nx += speed; break;
        }

        int maxW = (map != null) ? map.getPixelWidth()  : ui.GamePanel.MAP_WIDTH;
        int maxH = (map != null) ? map.getPixelHeight() : ui.GamePanel.MAP_HEIGHT;

        nx = Math.max(0, Math.min(nx, maxW - SIZE));
        ny = Math.max(0, Math.min(ny, maxH - SIZE));

        if (map == null || !map.isBlocked(nx, ny, SIZE, SIZE)) {
            x = nx;
            y = ny;
        }
    }

    //Shooting

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
            case RIGHT:
            default:
                bx = x + SIZE;
                by = y + SIZE / 2 - Bullet.SIZE / 2;
                break;
        }

        return new Bullet(bx, by, direction, starLevel, isPlayer, map);
    }

    //Damage

    public void hit() {
        if (shielded) return;
        lives--;
        if (lives <= 0) {
            destroyed = true;
        }
    }
    //Drawing

    public void draw(Graphics g) {
        if (destroyed) return;

        BufferedImage sprite = getSpriteForDirection(direction);

        if (sprite != null) {
            //Sprite path
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                                RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            g2.drawImage(sprite, x, y, SIZE, SIZE, null);
        } else {
            //Fallback
            Color bodyColor   = getTankColor();
            Color barrelColor = bodyColor.darker();

            g.setColor(bodyColor);
            g.fillRect(x + 4, y + 4, SIZE - 8, SIZE - 8);

            g.setColor(bodyColor.darker());
            g.fillRect(x,            y + 4, 6,     SIZE - 8);  // left track
            g.fillRect(x + SIZE - 6, y + 4, 6,     SIZE - 8);  // right track

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
        }

        // Shield aura
        if (shielded) {
            g.setColor(new Color(0, 220, 255, 160));
            g.drawOval(x - 4, y - 4, SIZE + 8, SIZE + 8);
            g.drawOval(x - 3, y - 3, SIZE + 6, SIZE + 6);
        }
    }

    protected abstract BufferedImage getSpriteForDirection(int direction);

    // Fallback colour
    protected abstract Color getTankColor();

    public int     getX()          { return x; }
    public int     getY()          { return y; }
    public int     getDirection()  { return direction; }
    public int     getLives()      { return lives; }
    public int     getStarLevel()  { return starLevel; }
    public boolean isShielded()    { return shielded; }
    public boolean isDestroyed()   { return destroyed; }

    public void setDirection(int direction) { this.direction = direction; }
    public void setShielded(boolean s)      { this.shielded  = s; }
    public void setStarLevel(int level)     { this.starLevel = Math.min(level, 3); }
    public void addLife()                   { this.lives++; }
    public void setMap(Map map)             { this.map = map; }

    public boolean intersects(int ox, int oy, int ow, int oh) {
        return x < ox + ow && x + SIZE > ox &&
               y < oy + oh && y + SIZE > oy;
    }
}