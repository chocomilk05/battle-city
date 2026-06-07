package powerups;

import java.awt.*;

/**
 * ShovelPowerUp
 * -------------
 * Temporarily fortifies the player's base by replacing the surrounding
 * BrickWall tiles with SteelWalls for SHOVEL_STEEL_MS (GameEngine).
 * After the timer expires, GameEngine restores the original tiles.
 */
public class ShovelPowerUp extends PowerUp {

    public ShovelPowerUp(int x, int y) {
        super(x, y, Type.SHOVEL);
    }

    /** Custom draw: orange background with a shovel silhouette. */
    @Override
    public void draw(Graphics g) {
        super.draw(g);  // base blink + "⛏" label

        // Shovel handle
        int hx = getX() + SIZE / 2;
        int hy = getY() + 6;
        g.setColor(new Color(160, 80, 0, 160));
        g.drawLine(hx, hy, hx + 4, hy + 14);

        // Blade
        g.setColor(new Color(200, 200, 200, 160));
        g.fillOval(hx + 1, hy + 12, 7, 5);
    }
}