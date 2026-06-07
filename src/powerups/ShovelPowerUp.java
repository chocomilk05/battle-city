package powerups;

import java.awt.*;

public class ShovelPowerUp extends PowerUp {

    public ShovelPowerUp(int x, int y) {
        super(x, y, Type.SHOVEL);
    }

    @Override
    public void draw(Graphics g) {
        super.draw(g);

        int hx = getX() + SIZE / 2;
        int hy = getY() + 6;
        g.setColor(new Color(160, 80, 0, 160));
        g.drawLine(hx, hy, hx + 4, hy + 14);

        g.setColor(new Color(200, 200, 200, 160));
        g.fillOval(hx + 1, hy + 12, 7, 5);
    }
}