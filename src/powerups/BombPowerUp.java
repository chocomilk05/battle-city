package powerups;

import java.awt.*;

/*
  Instantly destroys every enemy tank currently visible on the map
  GameEngine iterates the enemy list and calls kill() on each
 */
public class BombPowerUp extends PowerUp {

    public BombPowerUp(int x, int y) {
        super(x, y, Type.BOMB);
    }

    @Override
    public void draw(Graphics g) {
        super.draw(g); 

        int cx = getX() + SIZE / 2;
        int cy = getY() + SIZE / 2;
        g.setColor(new Color(255, 200, 0, 160));
        for (int angle = 0; angle < 360; angle += 45) {
            double rad = Math.toRadians(angle);
            int ex = cx + (int)(10 * Math.cos(rad));
            int ey = cy + (int)(10 * Math.sin(rad));
            g.drawLine(cx, cy, ex, ey);
        }
    }
}