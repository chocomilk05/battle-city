package ui;
import javax.swing.*;
import java.awt.*;

/*
  AboutDialog
    Name, Surname, School Number, E-mail
 */
public class AboutDialog extends JDialog {

    private static final String DEV_NAME    = "İrem Irmak";
    private static final String DEV_SURNAME = "Ünlüer";
    private static final String DEV_ID      = "20240702030";
    private static final String DEV_EMAIL   = "iremirmak.unluer@std.yeditepe.edu.tr";

    private static final Color BG       = new Color(10, 10, 10);
    private static final Color ACCENT   = new Color(255, 180, 0);
    private static final Color FG       = new Color(220, 220, 220);
    private static final Color DIVIDER  = new Color(80, 40, 0);

    public AboutDialog(Frame owner) {
        super(owner, "About", true);
        setBackground(BG);
        getContentPane().setBackground(BG);
        setLayout(new BorderLayout());

        add(buildBanner(), BorderLayout.NORTH);
        add(buildInfo(),   BorderLayout.CENTER);
        add(buildClose(),  BorderLayout.SOUTH);

        pack();
        setMinimumSize(new Dimension(380, 320));
        setLocationRelativeTo(owner);
        setResizable(false);
    }

    // Top banner

    private JPanel buildBanner() {
        JPanel p = new JPanel();
        p.setBackground(new Color(80, 30, 0));
        p.setBorder(BorderFactory.createEmptyBorder(16, 0, 16, 0));
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

        JLabel title = centredLabel("BATTLE CITY", new Font("Monospaced", Font.BOLD, 26), ACCENT);
        JLabel sub   = centredLabel("CSE212 Term Project  ·  Spring 2026",
                                    new Font("Monospaced", Font.PLAIN, 12), FG);
        JLabel uni   = centredLabel("Yeditepe University",
                                    new Font("Monospaced", Font.ITALIC, 12),
                                    new Color(180, 140, 80));
        p.add(title);
        p.add(Box.createVerticalStrut(4));
        p.add(sub);
        p.add(uni);
        return p;
    }

    // Developer info ( oyunum calissaydi :( ))

    private JPanel buildInfo() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(BG);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(2, 0, 2, 0, DIVIDER),
            BorderFactory.createEmptyBorder(18, 24, 18, 24)));

        GridBagConstraints l = new GridBagConstraints();
        l.anchor = GridBagConstraints.EAST;
        l.insets = new Insets(5, 8, 5, 12);

        GridBagConstraints v = new GridBagConstraints();
        v.anchor = GridBagConstraints.WEST;
        v.insets = new Insets(5, 0, 5, 8);
        v.fill   = GridBagConstraints.HORIZONTAL;
        v.weightx = 1.0;

        Object[][] rows = {
            {"Developer",     DEV_NAME + " " + DEV_SURNAME},
            {"Student ID",    DEV_ID},
            {"E-mail",        DEV_EMAIL},
            {"Course",        "CSE212 Software Development Methodologies"},
            {"Instructor",    "Yeditepe University"},
            {"Version",       "demo"},
        };

        for (int i = 0; i < rows.length; i++) {
            l.gridy = i;
            v.gridy = i;
            l.gridx = 0;
            v.gridx = 1;

            JLabel key = new JLabel((String) rows[i][0] + ":");
            key.setFont(new Font("Monospaced", Font.BOLD, 13));
            key.setForeground(ACCENT);

            JLabel val = new JLabel((String) rows[i][1]);
            val.setFont(new Font("Monospaced", Font.PLAIN, 13));
            val.setForeground(FG);

            p.add(key, l);
            p.add(val, v);
        }
        return p;
    }

    // ── Close button ──────────────────────────────────────────────────────────

    private JPanel buildClose() {
        JPanel p = new JPanel();
        p.setBackground(BG);
        p.setBorder(BorderFactory.createEmptyBorder(10, 0, 12, 0));

        JButton btn = new JButton("Close");
        btn.setFont(new Font("Monospaced", Font.BOLD, 13));
        btn.setForeground(ACCENT);
        btn.setBackground(new Color(60, 20, 0));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ACCENT, 1),
            BorderFactory.createEmptyBorder(6, 24, 6, 24)));
        btn.addActionListener(e -> dispose());
        p.add(btn);
        return p;
    }

    // ── Utility ───────────────────────────────────────────────────────────────

    private JLabel centredLabel(String text, Font font, Color fg) {
        JLabel lbl = new JLabel(text, SwingConstants.CENTER);
        lbl.setFont(font);
        lbl.setForeground(fg);
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        return lbl;
    }
}