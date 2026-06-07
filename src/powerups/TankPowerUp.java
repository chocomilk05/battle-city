package powerups;

import java.awt.*;

/**
 * TankPowerUp  (1UP)
 * ------------------
 * Grants the player one extra life when collected.
 * GameEngine increments the lives counter on pickup.
 */
public class TankPowerUp extends PowerUp {

    public TankPowerUp(int x, int y) {
        super(x, y, Type.TANK);
    }

    /** Custom draw: green background with a tiny tank silhouette. */
    @Override
    public void draw(Graphics g) {
        super.draw(g);  // base blink + "1UP" label

        // Small tank body decoration below the label
        int tx = getX() + SIZE / 2 - 6;
        int ty = getY() + SIZE - 9;
        g.setColor(new Color(0, 120, 0));
        g.fillRect(tx, ty, 12, 6);
        g.fillRect(tx + 4, ty - 3, 4, 4);
    }
}