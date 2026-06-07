package map;
 
import java.awt.Graphics;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
 
/**
 * Map holds the full tile grid for one level.
 *
 * Responsibilities:
 *   1. Load a level from a text file (loadFromFile).
 *   2. Draw all tiles in the correct order (solid tiles first, bushes last).
 *   3. Handle bullet-tile collision (bulletHit).
 *   4. Tick water animations each frame (tickAnimations).
 *   5. Expose the base reference so GameEngine can check isBaseDestroyed().
 *
 * -----------------------------------------------------------------------
 * Level file format  (each character = one 32×32 tile):
 *
 *   B  →  BrickWall
 *   S  →  SteelWall
 *   G  →  Bush (Green)
 *   W  →  Water
 *   E  →  Base (Eagle)
 *   .  →  Empty (no tile)
 *
 * Example (13 cols × 13 rows):
 *   BBBBBBBBBBBBB
 *   B...........B
 *   B.SS..GG..S.B
 *   ...
 *   ......E......
 *
 * The file must use consistent row widths.  Extra whitespace is ignored.
 * -----------------------------------------------------------------------
 */
public class Map {
 
    /** The 2-D grid of tiles.  null means an empty cell. */
    private Tile[][] grid;
 
    /** Number of columns and rows derived from the file. */
    private int cols;
    private int rows;
 
    /** Direct reference to the base tile for quick game-over checks. */
    private Base base;
 
    /** Collected Water tiles so we can tick their animation each frame. */
    private ArrayList<Water> waterTiles = new ArrayList<>();
 
    /** Collected Bush tiles so we can draw them in a second pass. */
    private ArrayList<Bush> bushTiles = new ArrayList<>();
 
    // -------------------------------------------------------------------------
    // Loading
    // -------------------------------------------------------------------------
 
    /**
     * Load a level from a text file.
     *
     * @param filePath e.g. "resources/maps/level1.txt"
     * @throws IOException if the file cannot be read
     */
    public void loadFromFile(String filePath) throws IOException {
        // First pass: read all lines to determine grid dimensions
        ArrayList<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.isEmpty()) {
                    lines.add(line);
                }
            }
        }
 
        rows = lines.size();
        cols = lines.get(0).length();
        grid = new Tile[rows][cols];
 
        waterTiles.clear();
        bushTiles.clear();
        base = null;
 
        // Second pass: build tile objects from each character
        for (int row = 0; row < rows; row++) {
            String line = lines.get(row);
            for (int col = 0; col < cols; col++) {
                if (col >= line.length()) break;
 
                int px = col * Tile.SIZE;   // pixel x
                int py = row * Tile.SIZE;   // pixel y
                char ch = line.charAt(col);
 
                Tile tile = createTile(ch, px, py);
                grid[row][col] = tile;
 
                // Keep convenience collections up to date
                if (tile instanceof Water) {
                    waterTiles.add((Water) tile);
                } else if (tile instanceof Bush) {
                    bushTiles.add((Bush) tile);
                } else if (tile instanceof Base) {
                    base = (Base) tile;
                }
            }
        }
    }
 
    /**
     * Factory: convert a single map character into the correct Tile subclass.
     * Returns null for empty cells ('.').
     *
     * Polymorphism in action: the caller (loadFromFile) stores the result as
     * a Tile reference without caring about the concrete type.
     */
    private Tile createTile(char ch, int x, int y) {
        switch (ch) {
            case 'B': return new BrickWall(x, y);
            case 'S': return new SteelWall(x, y);
            case 'G': return new Bush(x, y);
            case 'W': return new Water(x, y);
            case 'E': return new Base(x, y);
            default:  return null;          // '.' or any unknown char = empty
        }
    }
 
    // -------------------------------------------------------------------------
    // Drawing
    // -------------------------------------------------------------------------
 
    /**
     * Draw pass 1: all solid / non-bush tiles.
     * Call this BEFORE drawing tanks so solid walls appear behind tanks.
     *
     * @param g the Graphics context from GamePanel.paintComponent()
     */
    public void drawSolidTiles(Graphics g) {
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                Tile tile = grid[row][col];
                if (tile != null && !tile.isDestroyed() && !(tile instanceof Bush)) {
                    tile.draw(g);
                }
            }
        }
    }
 
    /**
     * Draw pass 2: bushes only.
     * Call this AFTER drawing tanks so tanks appear under bushes.
     *
     * This is how the spec requirement "bushes hide tanks" is implemented —
     * simply drawing order.
     *
     * @param g the Graphics context from GamePanel.paintComponent()
     */
    public void drawBushes(Graphics g) {
        for (Bush bush : bushTiles) {
            bush.draw(g);
        }
    }
 
    // -------------------------------------------------------------------------
    // Animation tick
    // -------------------------------------------------------------------------
 
    /**
     * Advance all tile animations by one game-loop tick.
     * Currently only Water tiles animate (two-frame wave flicker).
     * Call this once per game loop from GameEngine.update().
     *
     * A simple frame-rate divider is applied so water changes every
     * ~30 ticks (~0.5 s at 60 fps) rather than every frame.
     */
    private int animTick = 0;
 
    public void tickAnimations() {
        animTick++;
        if (animTick >= 30) {
            animTick = 0;
            for (Water w : waterTiles) {
                w.tick();
            }
        }
    }
 
    // -------------------------------------------------------------------------
    // Bullet collision
    // -------------------------------------------------------------------------
 
    /**
     * Check whether the bullet at pixel position (bx, by) with the given
     * dimensions collides with any solid tile, and if so call onHit().
     *
     * Converts pixel coordinates to grid indices to avoid iterating
     * the entire grid every frame.
     *
     * @param bx         bullet pixel x (top-left)
     * @param by         bullet pixel y (top-left)
     * @param bw         bullet width  in pixels
     * @param bh         bullet height in pixels
     * @param starLevel  shooter's star level (passed to onHit)
     * @param fromPlayer true if the bullet belongs to the player
     * @return true if the bullet hit something (and should be deactivated)
     */
    public boolean bulletHit(int bx, int by, int bw, int bh,
                              int starLevel, boolean fromPlayer) {
        // Determine which grid cells the bullet overlaps
        int colMin = Math.max(0,        bx / Tile.SIZE);
        int colMax = Math.min(cols - 1, (bx + bw - 1) / Tile.SIZE);
        int rowMin = Math.max(0,        by / Tile.SIZE);
        int rowMax = Math.min(rows - 1, (by + bh - 1) / Tile.SIZE);
 
        boolean hit = false;
        for (int row = rowMin; row <= rowMax; row++) {
            for (int col = colMin; col <= colMax; col++) {
                Tile tile = grid[row][col];
                if (tile == null || tile.isDestroyed()) continue;
 
                // Bushes and Water do not stop bullets
                if (tile instanceof Bush || tile instanceof Water) continue;
 
                tile.onHit(starLevel, fromPlayer);
                hit = true;
 
                // Clean up grid cell if the tile was destroyed
                if (tile.isDestroyed()) {
                    grid[row][col] = null;
                }
            }
        }
        return hit;
    }
 
    // -------------------------------------------------------------------------
    // Tank movement collision
    // -------------------------------------------------------------------------
 
    /**
     * Check whether a tank rectangle (tx, ty, tw, th) would overlap any
     * non-passable tile.  Used by PlayerTank and EnemyTank before moving.
     *
     * @return true if the position is blocked (tank should not move there)
     */
    public boolean isBlocked(int tx, int ty, int tw, int th) {
        int colMin = Math.max(0,        tx / Tile.SIZE);
        int colMax = Math.min(cols - 1, (tx + tw - 1) / Tile.SIZE);
        int rowMin = Math.max(0,        ty / Tile.SIZE);
        int rowMax = Math.min(rows - 1, (ty + th - 1) / Tile.SIZE);
 
        for (int row = rowMin; row <= rowMax; row++) {
            for (int col = colMin; col <= colMax; col++) {
                Tile tile = grid[row][col];
                if (tile != null && !tile.isDestroyed() && !tile.isPassable()) {
                    return true;
                }
            }
        }
        return false;
    }
 
    // -------------------------------------------------------------------------
    // Shovel power-up support
    // -------------------------------------------------------------------------
 
    /**
     * Replace tiles immediately surrounding the base with SteelWalls.
     * Called by the Shovel power-up.
     *
     * Stores the original tile types so restoreBaseWalls() can put them back.
     */
    private Tile[] savedBaseNeighbours = null;
    private int[][] baseNeighbourCoords = null;
 
    public void fortifyBase() {
        if (base == null) return;
 
        int bc = base.getCol();
        int br = base.getRow();
 
        // The 8 cells immediately surrounding the base (skips out-of-bounds)
        int[][] offsets = {
            {-1,-1},{0,-1},{1,-1},
            {-1, 0},       {1, 0},
            {-1, 1},{0, 1},{1, 1}
        };
 
        savedBaseNeighbours = new Tile[offsets.length];
        baseNeighbourCoords = new int[offsets.length][2];
 
        for (int i = 0; i < offsets.length; i++) {
            int r = br + offsets[i][1];
            int c = bc + offsets[i][0];
            baseNeighbourCoords[i][0] = r;
            baseNeighbourCoords[i][1] = c;
 
            if (r < 0 || r >= rows || c < 0 || c >= cols) {
                savedBaseNeighbours[i] = null;
                continue;
            }
 
            savedBaseNeighbours[i] = grid[r][c];   // save original
            grid[r][c] = new SteelWall(c * Tile.SIZE, r * Tile.SIZE);
        }
    }
 
    /**
     * Restore the tiles around the base to their pre-Shovel state.
     * Called by GameEngine when the Shovel timer expires.
     */
    public void restoreBaseWalls() {
        if (savedBaseNeighbours == null) return;
 
        for (int i = 0; i < savedBaseNeighbours.length; i++) {
            int r = baseNeighbourCoords[i][0];
            int c = baseNeighbourCoords[i][1];
            if (r < 0 || r >= rows || c < 0 || c >= cols) continue;
            grid[r][c] = savedBaseNeighbours[i];   // restore original
        }
        savedBaseNeighbours = null;
    }
 
    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------
 
    public int getCols()  { return cols; }
    public int getRows()  { return rows; }
 
    /** Pixel width of the entire map. */
    public int getPixelWidth()  { return cols * Tile.SIZE; }
 
    /** Pixel height of the entire map. */
    public int getPixelHeight() { return rows * Tile.SIZE; }
 
    /** Direct tile access — used by the Map Editor. */
    public Tile getTile(int row, int col) {
        if (row < 0 || row >= rows || col < 0 || col >= cols) return null;
        return grid[row][col];
    }
 
    /** Direct tile setter — used by the Map Editor and Shovel power-up. */
    public void setTile(int row, int col, Tile tile) {
        if (row < 0 || row >= rows || col < 0 || col >= cols) return;
        grid[row][col] = tile;
    }
 
    /**
     * Quick check used by GameEngine every frame.
     * Returns true if the base eagle has been hit by any bullet.
     */
    public boolean isBaseDestroyed() {
        return base != null && base.isBaseDestroyed();
    }
}