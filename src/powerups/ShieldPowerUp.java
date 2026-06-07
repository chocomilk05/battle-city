package powerups;

import java.awt.*;

/**
 * ShieldPowerUp
 * -------------
 * Makes the player tank invulnerable for SHIELD_DURATION_MS (GameEngine).
 * While shielded, the tank displays a cyan aura and ignores all bullet hits.
 */
public class ShieldPowerUp extends PowerUp {

    public ShieldPowerUp(int x, int y) {
        super(x, y, Type.SHIELD);
    }

    /** Custom draw: cyan background with a shield outline. */
    @Override
    public void draw(Graphics g) {
        super.draw(g);  // base blink + "⛨" label

        // Shield arc decoration
        int sx = getX() + 6;
        int sy = getY() + 4;
        int sw = SIZE - 12;
        int sh = SIZE - 8;
        g.setColor(new Color(180, 255, 255, 140));
        g.drawArc(sx, sy, sw, sh, 0, 180);
        g.drawLine(sx, sy + sh / 2, sx + sw / 2, sy + sh);
        g.drawLine(sx + sw, sy + sh / 2, sx + sw / 2, sy + sh);
    }
}