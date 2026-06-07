package map;
 
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
 
/**
 * MapEditor — a JPanel that lets the player design their own levels.
 *
 * Accessible from the main menu bar: Map Editor → opens this panel.
 *
 * How it works:
 *   1. A palette on the left shows each tile type as a coloured button.
 *   2. The player clicks a palette button to select a tile type.
 *   3. The player clicks (or drags) on the grid to place tiles.
 *   4. "Save" writes the grid to resources/maps/custom.txt in the same
 *      character format used by Map.loadFromFile().
 *   5. "Clear" resets the whole grid to empty.
 *   6. "Back" tells GameEngine / CardLayout to go back to the main menu.
 *
 * The Map Editor must only use concepts from the course:
 *   - Inheritance / Polymorphism: uses Tile subclasses to draw the palette
 *   - Collections: none needed here (2-D array suffices)
 *   - Threads: none (Swing EDT is sufficient for editor interaction)
 */
public class MapEditor extends JPanel {
 
    // -----------------------------------------------------------------------
    // Grid dimensions  (must match the level files used in gameplay)
    // -----------------------------------------------------------------------
    private static final int GRID_COLS    = 13;
    private static final int GRID_ROWS    = 13;
    private static final int CELL         = Tile.SIZE;   // 32 px
 
    // -----------------------------------------------------------------------
    // Palette — the available tile types (char code + display name + colour)
    // -----------------------------------------------------------------------
    private static final char[]   TILE_CHARS  = { '.', 'B', 'S', 'G', 'W', 'E' };
    private static final String[] TILE_NAMES  = { "Empty", "Brick", "Steel", "Bush", "Water", "Base" };
    private static final Color[]  TILE_COLORS = {
        Color.DARK_GRAY,
        new Color(180, 80,  20),    // brick orange
        new Color(130, 130, 130),   // steel grey
        new Color( 50, 150, 50),    // bush green
        new Color(  0, 120, 200),   // water blue
        new Color(200, 160,  0),    // base gold
    };
 
    // -----------------------------------------------------------------------
    // State
    // -----------------------------------------------------------------------
 
    /** The grid of char codes — each char matches Map.loadFromFile() format. */
    private char[][] gridData = new char[GRID_ROWS][GRID_COLS];
 
    /** Currently selected tile type character. */
    private char selectedTile = 'B';
 
    /** The palette buttons — stored so we can update their border highlight. */
    private JButton[] paletteButtons = new JButton[TILE_CHARS.length];
 
    /** The drawing canvas. */
    private JPanel canvas;
 
    // -----------------------------------------------------------------------
    // Constructor
    // -----------------------------------------------------------------------
    public MapEditor() {
        setLayout(new BorderLayout(8, 8));
        setBackground(new Color(40, 40, 40));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
 
        clearGrid();
 
        // --- Top: title
        JLabel title = new JLabel("Map Editor", SwingConstants.CENTER);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Monospaced", Font.BOLD, 18));
        add(title, BorderLayout.NORTH);
 
        // --- Centre: drawing canvas
        canvas = buildCanvas();
        add(canvas, BorderLayout.CENTER);
 
        // --- West: palette
        add(buildPalette(), BorderLayout.WEST);
 
        // --- South: action buttons (Save / Clear / Back)
        add(buildActionBar(), BorderLayout.SOUTH);
    }
 
    // -----------------------------------------------------------------------
    // Grid helpers
    // -----------------------------------------------------------------------
 
    /** Reset every cell to empty ('.'). */
    private void clearGrid() {
        for (int r = 0; r < GRID_ROWS; r++) {
            for (int c = 0; c < GRID_COLS; c++) {
                gridData[r][c] = '.';
            }
        }
    }
 
    // -----------------------------------------------------------------------
    // Canvas
    // -----------------------------------------------------------------------
 
    /**
     * The drawing canvas is a JPanel that paints the grid and responds to
     * mouse clicks / drags to place tiles.
     */
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
 
        // Mouse listener: place tile on click
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
 
    /**
     * Convert pixel click coordinates to grid cell and place the selected tile.
     */
    private void placeTileAt(int px, int py) {
        int col = px / CELL;
        int row = py / CELL;
        if (col < 0 || col >= GRID_COLS || row < 0 || row >= GRID_ROWS) return;
 
        // Only one base tile is allowed — if placing 'E', clear any previous one
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
 
    /**
     * Paint every cell of the grid onto the canvas Graphics.
     * Uses tile colours from the palette instead of creating full Tile objects,
     * keeping the editor lightweight.
     */
    private void drawGrid(Graphics g) {
        for (int r = 0; r < GRID_ROWS; r++) {
            for (int c = 0; c < GRID_COLS; c++) {
                int px = c * CELL;
                int py = r * CELL;
                char ch = gridData[r][c];
 
                // Background
                g.setColor(Color.BLACK);
                g.fillRect(px, py, CELL, CELL);
 
                // Tile fill using palette colours
                Color tileColor = colorForChar(ch);
                if (tileColor != null) {
                    g.setColor(tileColor);
                    g.fillRect(px + 1, py + 1, CELL - 2, CELL - 2);
                }
 
                // Grid line
                g.setColor(new Color(60, 60, 60));
                g.drawRect(px, py, CELL, CELL);
 
                // Small char label in the corner for clarity
                if (ch != '.') {
                    g.setColor(Color.WHITE);
                    g.setFont(new Font("Monospaced", Font.BOLD, 10));
                    g.drawString(String.valueOf(ch), px + 2, py + 11);
                }
            }
        }
    }
 
    /** Look up the display colour for a tile character. */
    private Color colorForChar(char ch) {
        for (int i = 0; i < TILE_CHARS.length; i++) {
            if (TILE_CHARS[i] == ch) return TILE_COLORS[i];
        }
        return null;
    }
 
    // -----------------------------------------------------------------------
    // Palette panel (left sidebar)
    // -----------------------------------------------------------------------
 
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
 
        // Highlight Brick (index 1) as the default selection
        highlightPaletteButton(1);
        return panel;
    }
 
    /** Give the selected palette button a bright white border; reset others. */
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
 
    // -----------------------------------------------------------------------
    // Action bar (bottom buttons)
    // -----------------------------------------------------------------------
 
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
 
        // Back button — the parent container's CardLayout handles the switch
        JButton backBtn = new JButton("Back to Menu");
        backBtn.setFont(new Font("Monospaced", Font.BOLD, 13));
        backBtn.addActionListener(e -> {
            // Fire an action command that MainMenu's CardLayout can listen for
            firePropertyChange("screen", "EDITOR", "MENU");
        });
        panel.add(backBtn);
 
        return panel;
    }
 
    // -----------------------------------------------------------------------
    // Save
    // -----------------------------------------------------------------------
 
    /**
     * Write the current grid to a .txt file in the same format that
     * Map.loadFromFile() expects.
     *
     * Prompts the user for a filename via JOptionPane, then writes to
     * resources/maps/<filename>.txt
     *
     * Uses PrintWriter(FileWriter) — course-scope file I/O.
     */
    private void saveMap() {
        // Basic validation: must have exactly one base
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
 
        // Ask for filename
        String name = JOptionPane.showInputDialog(this,
                "Enter a name for your map (no spaces):", "custom");
        if (name == null || name.trim().isEmpty()) return;
        name = name.trim().replaceAll("\\s+", "_");
 
        // Ensure the output directory exists
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