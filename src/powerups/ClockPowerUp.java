package powerups;

import java.awt.*;

// Freezes all enemy tanks for a fixed duration (CLOCK_FREEZE_MS in GameEngine)
 
public class ClockPowerUp extends PowerUp {

    public ClockPowerUp(int x, int y) {
        super(x, y, Type.CLOCK);
    }

    @Override
    public void draw(Graphics g) {
        super.draw(g);

        int cx = getX() + SIZE / 2;
        int cy = getY() + SIZE / 2 + 2;
        g.setColor(new Color(200, 230, 255, 120));
        g.drawOval(cx - 7, cy - 7, 14, 14);

        g.setColor(Color.WHITE);
        g.drawLine(cx, cy, cx,     cy - 5);  // minute hand
        g.drawLine(cx, cy, cx + 4, cy);      // hour hand
    }
}