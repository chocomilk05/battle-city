package powerups;

import java.awt.*;

/**
 * StarPowerUp
 * -----------
 * Upgrades the player's star level (1 → 2 → 3).
 *
 * Star-level effects (applied in GameEngine / PlayerTank):
 *   1 star  → faster bullets  (default)
 *   2 stars → two simultaneous bullets
 *   3 stars → bullets can destroy SteelWall
 */
public class StarPowerUp extends PowerUp {

    public StarPowerUp(int x, int y) {
        super(x, y, Type.STAR);
    }

    /** Custom draw: golden star with a bright shimmer. */
    @Override
    public void draw(Graphics g) {
        // Let the base class handle the standard blink + label.
        super.draw(g);

        // Extra shimmer: small white dot in the upper-left corner.
        g.setColor(new Color(255, 255, 200, 200));
        g.fillOval(getX() + 4, getY() + 4, 5, 5);
    }
}