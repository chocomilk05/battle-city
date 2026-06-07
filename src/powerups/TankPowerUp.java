package powerups;

import java.awt.*;

/*
  TankPowerUp
  Grants the player one extra life
 */
public class TankPowerUp extends PowerUp {

    public TankPowerUp(int x, int y) {
        super(x, y, Type.TANK);
    }

    @Override
    public void draw(Graphics g) {
        super.draw(g);

        int tx = getX() + SIZE / 2 - 6;
        int ty = getY() + SIZE - 9;
        g.setColor(new Color(0, 120, 0));
        g.fillRect(tx, ty, 12, 6);
        g.fillRect(tx + 4, ty - 3, 4, 4);
    }
}