package map;
 
import java.awt.Graphics;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
 
/*
  Responsibilities:
    1. Load a level from a text file (loadFromFile).
    2. Draw all tiles in the correct order (solid tiles first, bushes last).
    3. Handle bullet-tile collision (bulletHit).
    4. Tick water animations each frame (tickAnimations).
    5. Expose the base reference so GameEngine can check isBaseDestroyed().
*/
public class Map {
 
    private Tile[][] grid;
 
    private int cols;
    private int rows;
 
    private Base base;
 
    private ArrayList<Water> waterTiles = new ArrayList<>();
 
    private ArrayList<Bush> bushTiles = new ArrayList<>();

    public void loadFromFile(String filePath) throws IOException {
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
 
        for (int row = 0; row < rows; row++) {
            String line = lines.get(row);
            for (int col = 0; col < cols; col++) {
                if (col >= line.length()) break;
 
                int px = col * Tile.SIZE;
                int py = row * Tile.SIZE; 
                char ch = line.charAt(col);
 
                Tile tile = createTile(ch, px, py);
                grid[row][col] = tile;
 
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
 
    private Tile createTile(char ch, int x, int y) {
        switch (ch) {
            case 'B': return new BrickWall(x, y);
            case 'S': return new SteelWall(x, y);
            case 'G': return new Bush(x, y);
            case 'W': return new Water(x, y);
            case 'E': return new Base(x, y);
            default:  return null; 
        }
    }

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
 
    public void drawBushes(Graphics g) {
        for (Bush bush : bushTiles) {
            bush.draw(g);
        }
    }
 
    // Animation tick

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

    public boolean bulletHit(int bx, int by, int bw, int bh,
                              int starLevel, boolean fromPlayer) {
        int colMin = Math.max(0,        bx / Tile.SIZE);
        int colMax = Math.min(cols - 1, (bx + bw - 1) / Tile.SIZE);
        int rowMin = Math.max(0,        by / Tile.SIZE);
        int rowMax = Math.min(rows - 1, (by + bh - 1) / Tile.SIZE);
 
        boolean hit = false;
        for (int row = rowMin; row <= rowMax; row++) {
            for (int col = colMin; col <= colMax; col++) {
                Tile tile = grid[row][col];
                if (tile == null || tile.isDestroyed()) continue;
 
                if (tile instanceof Bush || tile instanceof Water) continue;
 
                tile.onHit(starLevel, fromPlayer);
                hit = true;
 
                if (tile.isDestroyed()) {
                    grid[row][col] = null;
                }
            }
        }
        return hit;
    }

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

    private Tile[] savedBaseNeighbours = null;
    private int[][] baseNeighbourCoords = null;
 
    public void fortifyBase() {
        if (base == null) return;
 
        int bc = base.getCol();
        int br = base.getRow();
 
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
 
    public int getCols()  { return cols; }
    public int getRows()  { return rows; }
 
    public int getPixelWidth()  { return cols * Tile.SIZE; }
 
    public int getPixelHeight() { return rows * Tile.SIZE; }
 
    public Tile getTile(int row, int col) {
        if (row < 0 || row >= rows || col < 0 || col >= cols) return null;
        return grid[row][col];
    }
 
    public void setTile(int row, int col, Tile tile) {
        if (row < 0 || row >= rows || col < 0 || col >= cols) return;
        grid[row][col] = tile;
    }
 
    //Returns true if the base eagle has been hit by any bullet.
    
    public boolean isBaseDestroyed() {
        return base != null && base.isBaseDestroyed();
    }
}