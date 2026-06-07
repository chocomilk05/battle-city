package powerups;

import java.awt.*;

/*
  StarPowerUp
  Upgrades the player's star level
 */
public class StarPowerUp extends PowerUp {

    public StarPowerUp(int x, int y) {
        super(x, y, Type.STAR);
    }

    @Override
    public void draw(Graphics g) {
        super.draw(g);

        g.setColor(new Color(255, 255, 200, 200));
        g.fillOval(getX() + 4, getY() + 4, 5, 5);
    }
}