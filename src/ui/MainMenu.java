package ui;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;

/**
 * MainMenu
 * --------
 * Splash / title screen shown at launch and after each game ends.
 *
 * Features:
 *  - Animated "BATTLE CITY" title that flickers like the original arcade.
 *  - Blinking "PRESS ENTER TO START" prompt.
 *  - Row of navigation buttons: New Game | High Scores | Options | Help | Exit.
 *  - Keyboard shortcut: Enter → New Game.
 */
public class MainMenu extends JPanel implements ActionListener {

    // ── Parent reference ──────────────────────────────────────────────────────
    private final GameFrame owner;

    // ── Animation ─────────────────────────────────────────────────────────────
    private final Timer animTimer;
    private boolean     blinkOn     = true;
    private int         flickerStep = 0;
    private float       titleHue    = 0f;   // colour cycle for title

    // ── Colours ───────────────────────────────────────────────────────────────
    private static final Color BG        = Color.BLACK;
    private static final Color TITLE_A   = new Color(220, 40,  40);
    private static final Color TITLE_B   = new Color(255, 180, 0);
    private static final Color BLINK_COL = Color.WHITE;
    private static final Color PANEL_BG  = new Color(20, 20, 20);
    private static final Color BTN_FG    = Color.ORANGE;
    private static final Color BTN_BG    = new Color(40, 0, 0);
    private static final Color BTN_HOVER = new Color(80, 20, 0);

    // ─────────────────────────────────────────────────────────────────────────
    //  Constructor
    // ─────────────────────────────────────────────────────────────────────────

    public MainMenu(GameFrame owner) {
        this.owner = owner;
        setPreferredSize(new Dimension(
            GamePanel.MAP_WIDTH + GamePanel.HUD_WIDTH,
            GamePanel.MAP_HEIGHT));
        setBackground(BG);
        setLayout(new BorderLayout());

        // ── Centre: title art + blink prompt ──────────────────────────────────
        JPanel centre = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                paintTitle((Graphics2D) g);
            }
        };
        centre.setOpaque(false);
        add(centre, BorderLayout.CENTER);

        // ── Bottom: button row ────────────────────────────────────────────────
        JPanel btnRow = buildButtonRow();
        add(btnRow, BorderLayout.SOUTH);

        // ── Enter key shortcut ────────────────────────────────────────────────
        setFocusable(true);
        addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) owner.startNewGame();
            }
        });

        // ── Animation timer (10 FPS is enough for title effects) ─────────────
        animTimer = new Timer(100, this);
        animTimer.start();
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Button row
    // ─────────────────────────────────────────────────────────────────────────

    private JPanel buildButtonRow() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER, 18, 14));
        row.setBackground(PANEL_BG);
        row.setBorder(BorderFactory.createMatteBorder(2, 0, 0, 0, new Color(80,0,0)));

        row.add(menuButton("▶  NEW GAME",    () -> owner.startNewGame()));
        row.add(menuButton("★  HIGH SCORES", () -> new HighScoreScreen(owner).setVisible(true)));
        row.add(menuButton("⚙  OPTIONS",     () -> new OptionsScreen(owner).setVisible(true)));
        row.add(menuButton("?  HELP",        () -> new HelpScreen(owner).setVisible(true)));
        row.add(menuButton("✕  EXIT",        () -> {
            int r = JOptionPane.showConfirmDialog(owner,
                "Exit Battle City?", "Exit", JOptionPane.YES_NO_OPTION);
            if (r == JOptionPane.YES_OPTION) System.exit(0);
        }));
        return row;
    }

    private JButton menuButton(String label, Runnable action) {
        JButton btn = new JButton(label) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = getModel().isRollover() ? BTN_HOVER : BTN_BG;
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(new Color(160, 40, 0));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 8, 8);
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Monospaced", Font.BOLD, 13));
        btn.setForeground(BTN_FG);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setPreferredSize(new Dimension(160, 38));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> action.run());
        return btn;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Title painting
    // ─────────────────────────────────────────────────────────────────────────

    private void paintTitle(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                           RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth(), h = getHeight();

        // ── Background gradient ───────────────────────────────────────────────
        GradientPaint grad = new GradientPaint(0, 0, new Color(20,0,0),
                                               0, h, BG);
        g.setPaint(grad);
        g.fillRect(0, 0, w, h);

        // ── Decorative pixel grid ─────────────────────────────────────────────
        g.setColor(new Color(30, 5, 5));
        for (int x = 0; x < w; x += 32) g.drawLine(x, 0, x, h);
        for (int y = 0; y < h; y += 32) g.drawLine(0, y, w, y);

        // ── "BATTLE CITY" big pixel-style title ───────────────────────────────
        Color titleCol = (flickerStep % 4 == 0) ? TITLE_B : TITLE_A;
        Font titleFont = new Font("Monospaced", Font.BOLD, 64);
        g.setFont(titleFont);
        g.setColor(titleCol);

        String line1 = "BATTLE";
        String line2 = "CITY";
        FontMetrics fm = g.getFontMetrics();
        int x1 = (w - fm.stringWidth(line1)) / 2;
        int x2 = (w - fm.stringWidth(line2)) / 2;
        int y1 = h / 3;
        int y2 = y1 + fm.getHeight() + 8;

        // Drop shadow
        g.setColor(new Color(80, 0, 0));
        g.drawString(line1, x1 + 4, y1 + 4);
        g.drawString(line2, x2 + 4, y2 + 4);

        g.setColor(titleCol);
        g.drawString(line1, x1, y1);
        g.drawString(line2, x2, y2);

        // ── "PRESS ENTER TO START" blink ─────────────────────────────────────
        if (blinkOn) {
            g.setFont(new Font("Monospaced", Font.BOLD, 16));
            g.setColor(BLINK_COL);
            String prompt = "PRESS  ENTER  TO  START";
            FontMetrics fm2 = g.getFontMetrics();
            int px = (w - fm2.stringWidth(prompt)) / 2;
            g.drawString(prompt, px, y2 + 80);
        }

        // ── Sub-title / credits ───────────────────────────────────────────────
        g.setFont(new Font("Monospaced", Font.PLAIN, 11));
        g.setColor(new Color(120, 120, 120));
        String sub = "CSE212  ·  Yeditepe University  ·  Spring 2025";
        FontMetrics fm3 = g.getFontMetrics();
        g.drawString(sub, (w - fm3.stringWidth(sub)) / 2, h - 30);

        // ── Small tank icons (decorative) ─────────────────────────────────────
        drawDecorativeTank(g, 60,  h / 2 + 30, Color.GREEN);
        drawDecorativeTank(g, w - 90, h / 2 + 30, Color.RED);
    }

    /** Draws a tiny symbolic tank rectangle as decoration. */
    private void drawDecorativeTank(Graphics2D g, int x, int y, Color c) {
        g.setColor(c);
        g.fillRect(x, y, 28, 22);          // body
        g.fillRect(x + 10, y - 6, 8, 10); // turret base
        g.fillRect(x + 13, y - 14, 4, 12); // barrel
        g.setColor(c.darker());
        g.drawRect(x, y, 28, 22);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Animation timer tick
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public void actionPerformed(ActionEvent e) {
        blinkOn     = !blinkOn;
        flickerStep = (flickerStep + 1) % 8;
        repaint();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        requestFocusInWindow();
        animTimer.start();
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        animTimer.stop();
    }
}
