package ui;
import javax.swing.*;
import java.awt.*;

/**
 * HelpScreen
 * ----------
 * Modal dialog that explains controls, power-ups, and game rules.
 * Organized into tabbed sections for quick navigation.
 */
public class HelpScreen extends JDialog {

    // ── Colours ───────────────────────────────────────────────────────────────
    private static final Color BG        = new Color(12, 12, 12);
    private static final Color HEADER_BG = new Color(0, 60, 0);
    private static final Color FG        = new Color(200, 255, 180);
    private static final Color ACCENT    = new Color(80, 255, 80);
    private static final Color KEY_BG    = new Color(40, 40, 40);
    private static final Color KEY_FG    = Color.YELLOW;

    // ─────────────────────────────────────────────────────────────────────────

    public HelpScreen(Frame owner) {
        super(owner, "?  How to Play", true);
        setBackground(BG);
        getContentPane().setBackground(BG);
        setLayout(new BorderLayout());

        add(buildHeader(),  BorderLayout.NORTH);
        add(buildTabs(),    BorderLayout.CENTER);
        add(buildClose(),   BorderLayout.SOUTH);

        setSize(580, 520);
        setLocationRelativeTo(owner);
        setResizable(false);
    }

    // ── Header ────────────────────────────────────────────────────────────────

    private JPanel buildHeader() {
        JPanel p = new JPanel();
        p.setBackground(HEADER_BG);
        p.setBorder(BorderFactory.createEmptyBorder(12, 0, 12, 0));
        JLabel lbl = new JLabel("BATTLE CITY  –  HOW TO PLAY");
        lbl.setFont(new Font("Monospaced", Font.BOLD, 18));
        lbl.setForeground(ACCENT);
        p.add(lbl);
        return p;
    }

    // ── Tabbed pane ───────────────────────────────────────────────────────────

    private JTabbedPane buildTabs() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(BG);
        tabs.setForeground(ACCENT);
        tabs.setFont(new Font("Monospaced", Font.BOLD, 13));

        tabs.addTab("Controls",  buildControlsPanel());
        tabs.addTab("Power-Ups", buildPowerUpsPanel());
        tabs.addTab("Rules",     buildRulesPanel());
        return tabs;
    }

    // ── Controls tab ──────────────────────────────────────────────────────────

    private JPanel buildControlsPanel() {
        JPanel p = darkPanel();
        p.setLayout(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 12, 6, 12);
        gc.anchor = GridBagConstraints.WEST;

        Object[][] controls = {
            {"W  /  ↑",    "Move tank Up"},
            {"S  /  ↓",    "Move tank Down"},
            {"A  /  ←",    "Move tank Left"},
            {"D  /  →",    "Move tank Right"},
            {"SPACE",       "Fire"},
            {"P  /  ESC",   "Pause / Resume game"},
            {"ENTER",       "Start new game (from menu)"},
        };

        gc.gridy = 0;
        gc.gridx = 0; addLabel(p, gc, "Key / Button", KEY_FG, Font.BOLD);
        gc.gridx = 1; addLabel(p, gc, "Action",       KEY_FG, Font.BOLD);

        for (Object[] row : controls) {
            gc.gridy++;
            gc.gridx = 0; addKeyLabel(p, gc, (String) row[0]);
            gc.gridx = 1; addLabel(p, gc, (String) row[1], FG, Font.PLAIN);
        }
        return p;
    }

    // ── Power-ups tab ─────────────────────────────────────────────────────────

    private JPanel buildPowerUpsPanel() {
        JPanel p = darkPanel();
        p.setLayout(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets  = new Insets(5, 12, 5, 12);
        gc.anchor  = GridBagConstraints.WEST;

        Object[][] pups = {
            {"⭐ STAR",   "Upgrades your tank.\n"
                        + "  1 star  → faster bullets\n"
                        + "  2 stars → fire two bullets at once\n"
                        + "  3 stars → bullets destroy steel walls"},
            {"🪖 TANK",   "Grants one extra life."},
            {"💣 BOMB",   "Instantly destroys ALL enemy tanks on screen."},
            {"🕐 CLOCK",  "Freezes all enemy tanks for 8 seconds."},
            {"⛏ SHOVEL",  "Surrounds your base with steel walls for 15 seconds."},
            {"🛡 SHIELD",  "Makes your tank invulnerable for 8 seconds."},
        };

        gc.gridy = 0;
        gc.gridx = 0; addLabel(p, gc, "Power-Up",  KEY_FG, Font.BOLD);
        gc.gridx = 1; addLabel(p, gc, "Effect",    KEY_FG, Font.BOLD);

        for (Object[] row : pups) {
            gc.gridy++;
            gc.gridx = 0; addLabel(p, gc, (String) row[0], ACCENT, Font.BOLD);
            gc.gridx = 1;
            JTextArea ta = new JTextArea((String) row[1]);
            ta.setFont(new Font("Monospaced", Font.PLAIN, 12));
            ta.setForeground(FG);
            ta.setBackground(BG);
            ta.setEditable(false);
            ta.setOpaque(false);
            p.add(ta, gc);
        }
        return p;
    }

    // ── Rules tab ─────────────────────────────────────────────────────────────

    private JPanel buildRulesPanel() {
        JPanel p = darkPanel();
        p.setLayout(new BorderLayout());

        String rules =
            "OBJECTIVE\n" +
            "  Destroy all 20 enemy tanks per level to advance.\n\n" +
            "GAME OVER conditions:\n" +
            "  • Your base (eagle/phoenix) is destroyed by an enemy.\n" +
            "  • You lose all lives (shown on the right-side panel).\n\n" +
            "TERRAIN\n" +
            "  Brick  – destroyed by any bullet.\n" +
            "  Steel  – only destroyed by a 3-star player bullet.\n" +
            "  Water  – impassable by tanks; bullets pass over.\n" +
            "  Bush   – tanks pass through but are hidden underneath.\n\n" +
            "SCORING\n" +
            "  Basic enemy  : 100 pts\n" +
            "  Fast enemy   : 200 pts\n" +
            "  Power enemy  : 300 pts\n" +
            "  Armoured     : 400 pts\n" +
            "  Power-up bonus: +500 pts each\n\n" +
            "TIPS\n" +
            "  • Collect Stars quickly – 3 stars let you pierce steel.\n" +
            "  • Use the Shovel power-up when enemies near your base.\n" +
            "  • Bushes hide enemies – stay alert!\n";

        JTextArea ta = new JTextArea(rules);
        ta.setFont(new Font("Monospaced", Font.PLAIN, 12));
        ta.setForeground(FG);
        ta.setBackground(BG);
        ta.setEditable(false);
        ta.setMargin(new Insets(12, 16, 12, 16));

        JScrollPane scroll = new JScrollPane(ta);
        scroll.setBackground(BG);
        scroll.getViewport().setBackground(BG);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        p.add(scroll, BorderLayout.CENTER);
        return p;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private JPanel darkPanel() {
        JPanel p = new JPanel();
        p.setBackground(BG);
        return p;
    }

    private void addLabel(JPanel p, GridBagConstraints gc,
                          String text, Color fg, int style) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Monospaced", style, 13));
        lbl.setForeground(fg);
        p.add(lbl, gc);
    }

    private void addKeyLabel(JPanel p, GridBagConstraints gc, String text) {
        JLabel lbl = new JLabel("  " + text + "  ");
        lbl.setFont(new Font("Monospaced", Font.BOLD, 12));
        lbl.setForeground(KEY_FG);
        lbl.setBackground(KEY_BG);
        lbl.setOpaque(true);
        lbl.setBorder(BorderFactory.createLineBorder(new Color(80,80,0), 1));
        p.add(lbl, gc);
    }

    // ── Close button ──────────────────────────────────────────────────────────

    private JPanel buildClose() {
        JPanel p = new JPanel();
        p.setBackground(BG);
        p.setBorder(BorderFactory.createEmptyBorder(8, 0, 10, 0));
        JButton btn = new JButton("Close");
        btn.setFont(new Font("Monospaced", Font.BOLD, 13));
        btn.setForeground(ACCENT);
        btn.setBackground(new Color(0, 50, 0));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ACCENT, 1),
            BorderFactory.createEmptyBorder(6, 24, 6, 24)));
        btn.addActionListener(e -> dispose());
        p.add(btn);
        return p;
    }
}