package ui;
import data.ScoreManager;
import data.ScoreEntry;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

/*
  HighScoreScreen
  Modal dialog that reads the top-10 entries from scores.csv via
  ScoreManager and displays them
  Shows: Rank  Name  Score  Date  Time
 */
public class HighScoreScreen extends JDialog {

    private static final Color BG        = new Color(10, 10, 10);
    private static final Color HEADER_BG = new Color(120, 20, 0);
    private static final Color ROW_ODD   = new Color(25, 25, 25);
    private static final Color ROW_EVEN  = new Color(40, 10, 0);
    private static final Color FG        = Color.ORANGE;
    private static final Color GOLD      = new Color(255, 215, 0);

    public HighScoreScreen(Frame owner) {
        super(owner, "★  High Scores", true);
        setBackground(BG);
        getContentPane().setBackground(BG);
        setLayout(new BorderLayout(0, 0));

        add(buildTitleBar(), BorderLayout.NORTH);
        add(buildTable(),    BorderLayout.CENTER);
        add(buildCloseBar(), BorderLayout.SOUTH);

        setSize(560, 440);
        setLocationRelativeTo(owner);
        setResizable(false);
    }

    // Title

    private JPanel buildTitleBar() {
        JPanel p = new JPanel();
        p.setBackground(HEADER_BG);
        p.setBorder(BorderFactory.createEmptyBorder(14, 0, 14, 0));
        JLabel lbl = new JLabel("★  HIGH SCORES  ★");
        lbl.setFont(new Font("Monospaced", Font.BOLD, 22));
        lbl.setForeground(GOLD);
        p.add(lbl);
        return p;
    }

    // Table

    private JScrollPane buildTable() {
        String[] columns = {"#", "Name", "Score", "Date", "Time"};
        List<ScoreEntry> entries = ScoreManager.loadTop(10);

        int ROWS = 10;
        Object[][] data = new Object[ROWS][5];
        for (int i = 0; i < ROWS; i++) {
            if (i < entries.size()) {
                ScoreEntry e = entries.get(i);
                data[i][0] = i + 1;
                data[i][1] = e.name();
                data[i][2] = String.format("%,d", e.score());
                data[i][3] = e.date();
                data[i][4] = e.time();
            } else {
                data[i][0] = i + 1;
                data[i][1] = "---";
                data[i][2] = "---";
                data[i][3] = "---";
                data[i][4] = "---";
            }
        }

        DefaultTableModel model = new DefaultTableModel(data, columns) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable table = new JTable(model) {
            @Override
            public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                if      (row == 0) c.setBackground(new Color(60, 50, 0));
                else if (row == 1) c.setBackground(new Color(40, 40, 40));
                else if (row == 2) c.setBackground(new Color(40, 20, 0));
                else               c.setBackground(row % 2 == 0 ? ROW_ODD : ROW_EVEN);
                c.setForeground(col == 2 ? GOLD : FG);
                return c;
            }
        };

        table.setFont(new Font("Monospaced", Font.PLAIN, 14));
        table.setRowHeight(30);
        table.setBackground(ROW_ODD);
        table.setForeground(FG);
        table.setGridColor(new Color(60, 20, 0));
        table.setSelectionBackground(new Color(80, 30, 0));
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 1));

        JTableHeader header = table.getTableHeader();
        header.setBackground(HEADER_BG);
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Monospaced", Font.BOLD, 13));
        header.setReorderingAllowed(false);

        int[] widths = {40, 180, 100, 110, 100};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
            table.getColumnModel().getColumn(i).setCellRenderer(centeredRenderer());
        }

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBackground(BG);
        scroll.getViewport().setBackground(BG);
        scroll.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        return scroll;
    }

    private DefaultTableCellRenderer centeredRenderer() {
        DefaultTableCellRenderer r = new DefaultTableCellRenderer();
        r.setHorizontalAlignment(SwingConstants.CENTER);
        return r;
    }

    // Bottom buttons

    private JPanel buildCloseBar() {
        JPanel p = new JPanel();
        p.setBackground(BG);
        p.setBorder(BorderFactory.createEmptyBorder(8, 0, 12, 0));

        JButton clearBtn = styledButton("Clear All Scores");
        clearBtn.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(this,
                "Delete ALL saved scores?", "Clear Scores",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (choice == JOptionPane.YES_OPTION) {
                ScoreManager.clearAll();
                dispose();
                Frame owner = (Frame) getOwner();
                new HighScoreScreen(owner).setVisible(true);
            }
        });
        p.add(clearBtn);

        JButton closeBtn = styledButton("Close");
        closeBtn.addActionListener(e -> dispose());
        p.add(closeBtn);

        return p;
    }

    private JButton styledButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Monospaced", Font.BOLD, 13));
        btn.setForeground(FG);
        btn.setBackground(new Color(60, 10, 0));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(140, 40, 0), 1),
            BorderFactory.createEmptyBorder(6, 18, 6, 18)));
        return btn;
    }
}