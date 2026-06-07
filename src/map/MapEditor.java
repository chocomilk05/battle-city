package map;
 
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
 
// MapEditor: a JPanel that lets the player design their own levels.

public class MapEditor extends JPanel {
 
    // Grid dimensions 
    private static final int GRID_COLS    = 13;
    private static final int GRID_ROWS    = 13;
    private static final int CELL         = Tile.SIZE; 
 
    // Palette — the available tile types (char code + display name + colour) 
    private static final char[]   TILE_CHARS  = { '.', 'B', 'S', 'G', 'W', 'E' };
    private static final String[] TILE_NAMES  = { "Empty", "Brick", "Steel", "Bush", "Water", "Base" };
    private static final Color[]  TILE_COLORS = {
        Color.DARK_GRAY,
        new Color(180, 80,  20),   
        new Color(130, 130, 130),
        new Color( 50, 150, 50),    
        new Color(  0, 120, 200),  
        new Color(200, 160,  0), 
    };
 
    // State
 
    private char[][] gridData = new char[GRID_ROWS][GRID_COLS];
 
    private char selectedTile = 'B';
 
    private JButton[] paletteButtons = new JButton[TILE_CHARS.length];
 
    private JPanel canvas;
 
    // Constructor
    public MapEditor() {
        setLayout(new BorderLayout(8, 8));
        setBackground(new Color(40, 40, 40));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
 
        clearGrid();
 
        //Top: title
        JLabel title = new JLabel("Map Editor", SwingConstants.CENTER);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Monospaced", Font.BOLD, 18));
        add(title, BorderLayout.NORTH);
 
        //Center: drawing canvas
        canvas = buildCanvas();
        add(canvas, BorderLayout.CENTER);
 
        //West: palette
        add(buildPalette(), BorderLayout.WEST);
 
        //South: action buttons (Save / Clear / Back)
        add(buildActionBar(), BorderLayout.SOUTH);
    }
 
    // Grid helpers
     private void clearGrid() {
        for (int r = 0; r < GRID_ROWS; r++) {
            for (int c = 0; c < GRID_COLS; c++) {
                gridData[r][c] = '.';
            }
        }
    }
 
    // Canvas

    private JPanel buildCanvas() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawGrid(g);
            }
 
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(GRID_COLS * CELL, GRID_ROWS * CELL);
            }
        };
        panel.setBackground(Color.BLACK);
 
        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                placeTileAt(e.getX(), e.getY());
            }
 
            @Override
            public void mouseDragged(MouseEvent e) {
                placeTileAt(e.getX(), e.getY());
            }
        };
        panel.addMouseListener(mouseAdapter);
        panel.addMouseMotionListener(mouseAdapter);
 
        return panel;
    }

    private void placeTileAt(int px, int py) {
        int col = px / CELL;
        int row = py / CELL;
        if (col < 0 || col >= GRID_COLS || row < 0 || row >= GRID_ROWS) return;
 
        if (selectedTile == 'E') {
            for (int r = 0; r < GRID_ROWS; r++) {
                for (int c = 0; c < GRID_COLS; c++) {
                    if (gridData[r][c] == 'E') gridData[r][c] = '.';
                }
            }
        }
 
        gridData[row][col] = selectedTile;
        canvas.repaint();
    }
 
    private void drawGrid(Graphics g) {
        for (int r = 0; r < GRID_ROWS; r++) {
            for (int c = 0; c < GRID_COLS; c++) {
                int px = c * CELL;
                int py = r * CELL;
                char ch = gridData[r][c];
 
                g.setColor(Color.BLACK);
                g.fillRect(px, py, CELL, CELL);
 
                Color tileColor = colorForChar(ch);
                if (tileColor != null) {
                    g.setColor(tileColor);
                    g.fillRect(px + 1, py + 1, CELL - 2, CELL - 2);
                }
 
                g.setColor(new Color(60, 60, 60));
                g.drawRect(px, py, CELL, CELL);
 
                if (ch != '.') {
                    g.setColor(Color.WHITE);
                    g.setFont(new Font("Monospaced", Font.BOLD, 10));
                    g.drawString(String.valueOf(ch), px + 2, py + 11);
                }
            }
        }
    }
 
    private Color colorForChar(char ch) {
        for (int i = 0; i < TILE_CHARS.length; i++) {
            if (TILE_CHARS[i] == ch) return TILE_COLORS[i];
        }
        return null;
    }
 
    // Palette panel
 
    private JPanel buildPalette() {
        JPanel panel = new JPanel(new GridLayout(TILE_CHARS.length, 1, 4, 4));
        panel.setBackground(new Color(40, 40, 40));
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                "Tiles",
                0, 0,
                new Font("Monospaced", Font.BOLD, 12),
                Color.WHITE));
        panel.setPreferredSize(new Dimension(90, 0));
 
        for (int i = 0; i < TILE_CHARS.length; i++) {
            final char tileChar = TILE_CHARS[i];
            final int  idx      = i;
 
            JButton btn = new JButton(TILE_NAMES[i]);
            btn.setBackground(TILE_COLORS[i]);
            btn.setForeground(Color.WHITE);
            btn.setFont(new Font("Monospaced", Font.BOLD, 11));
            btn.setFocusPainted(false);
            btn.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
            btn.addActionListener(e -> {
                selectedTile = tileChar;
                highlightPaletteButton(idx);
            });
 
            paletteButtons[i] = btn;
            panel.add(btn);
        }
 
        highlightPaletteButton(1);
        return panel;
    }
 
    private void highlightPaletteButton(int selectedIdx) {
        for (int i = 0; i < paletteButtons.length; i++) {
            if (i == selectedIdx) {
                paletteButtons[i].setBorder(
                    BorderFactory.createLineBorder(Color.WHITE, 2));
            } else {
                paletteButtons[i].setBorder(
                    BorderFactory.createLineBorder(Color.GRAY, 1));
            }
        }
    }
 
    // Action bar
 
    private JPanel buildActionBar() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 4));
        panel.setBackground(new Color(40, 40, 40));
 
        // Save button
        JButton saveBtn = new JButton("Save");
        saveBtn.setFont(new Font("Monospaced", Font.BOLD, 13));
        saveBtn.addActionListener(e -> saveMap());
        panel.add(saveBtn);
 
        // Clear button
        JButton clearBtn = new JButton("Clear");
        clearBtn.setFont(new Font("Monospaced", Font.BOLD, 13));
        clearBtn.addActionListener(e -> {
            clearGrid();
            canvas.repaint();
        });
        panel.add(clearBtn);
 
        // Back button
        JButton backBtn = new JButton("Back to Menu");
        backBtn.setFont(new Font("Monospaced", Font.BOLD, 13));
        backBtn.addActionListener(e -> {
            firePropertyChange("screen", "EDITOR", "MENU");
        });
        panel.add(backBtn);
 
        return panel;
    }
 
    // Save

    private void saveMap() {
        // validation
        int baseCount = 0;
        for (int r = 0; r < GRID_ROWS; r++) {
            for (int c = 0; c < GRID_COLS; c++) {
                if (gridData[r][c] == 'E') baseCount++;
            }
        }
        if (baseCount != 1) {
            JOptionPane.showMessageDialog(this,
                "Your map must have exactly one Base (E) tile.",
                "Save Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
 
        // Ask for file name
        String name = JOptionPane.showInputDialog(this,
                "Enter a name for your map (no spaces):", "custom");
        if (name == null || name.trim().isEmpty()) return;
        name = name.trim().replaceAll("\\s+", "_");
 
        // Make sure the output directory exists
        java.io.File dir = new java.io.File("resources/maps");
        dir.mkdirs();
 
        String path = "resources/maps/" + name + ".txt";
 
        try (PrintWriter writer = new PrintWriter(new FileWriter(path))) {
            for (int r = 0; r < GRID_ROWS; r++) {
                for (int c = 0; c < GRID_COLS; c++) {
                    writer.print(gridData[r][c]);
                }
                writer.println();
            }
            JOptionPane.showMessageDialog(this,
                "Map saved to " + path,
                "Saved",
                JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                "Could not save map: " + ex.getMessage(),
                "Save Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
}