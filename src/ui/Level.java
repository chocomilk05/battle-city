package ui;
/**
 * Level
 * -----
 * Holds the tile-based map for one game level and provides helpers for
 * querying / mutating tiles at runtime (bullet destruction, Shovel steel, etc.).
 *
 * Tile-type constants are public so GamePanel, GameEngine and the Map Editor
 * all share the same vocabulary without magic numbers.
 *
 * Three built-in levels are hard-coded.  The Map Editor saves custom levels
 * as CSV files and uses {@link #loadFromCSV} to read them back.
 *
 * Map coordinates: row 0 = top, col 0 = left.
 * Map size: 26 × 26 tiles  (matches GamePanel.ROWS × GamePanel.COLS).
 */
public class Level {

    // ── Tile-type constants ───────────────────────────────────────────────────
    public static final int EMPTY = 0;
    public static final int BRICK = 1;
    public static final int STEEL = 2;
    public static final int WATER = 3;
    public static final int BUSH  = 4;
    public static final int BASE  = 5;  // Eagle / Phoenix – player's base

    // ── Map data ──────────────────────────────────────────────────────────────
    private final int[][] map;           // [row][col]
    private final int     levelNumber;

    // ── Spawn positions ───────────────────────────────────────────────────────
    /** {row, col} of the player starting tile. */
    private final int[] playerSpawn;

    /** Array of {row, col} pairs for enemy entry points (top of map). */
    private final int[][] enemySpawnPoints;

    /** Row/col of the player's base tile. */
    private final int baseRow;
    private final int baseCol;

    // ─────────────────────────────────────────────────────────────────────────
    //  Constructor (private – use factory methods)
    // ─────────────────────────────────────────────────────────────────────────

    private Level(int levelNumber, int[][] map,
                  int[] playerSpawn, int[][] enemySpawnPoints,
                  int baseRow, int baseCol) {
        this.levelNumber      = levelNumber;
        this.map              = map;
        this.playerSpawn      = playerSpawn;
        this.enemySpawnPoints = enemySpawnPoints;
        this.baseRow          = baseRow;
        this.baseCol          = baseCol;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Factory / loading
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Load a built-in level (1–3) or fall back to level 1.
     *
     * @param n 1-based level number
     */
    public static Level load(int n) {
        return switch (n) {
            case 1  -> buildLevel1();
            case 2  -> buildLevel2();
            case 3  -> buildLevel3();
            default -> buildLevel1();
        };
    }

    /**
     * Load a custom level saved by the Map Editor as a CSV file.
     * CSV format: one row per map row, comma-separated tile integers.
     * First non-map line: playerSpawnRow,playerSpawnCol
     * Second non-map line: baseRow,baseCol
     *
     * @param filePath path to the CSV file
     * @return loaded Level, or null if file cannot be parsed
     */
    public static Level loadFromCSV(String filePath) {
        try (java.io.BufferedReader br = new java.io.BufferedReader(
                new java.io.FileReader(filePath))) {

            int rows = GamePanel.ROWS;
            int cols = GamePanel.COLS;
            int[][] map = new int[rows][cols];

            for (int r = 0; r < rows; r++) {
                String line = br.readLine();
                if (line == null) break;
                String[] tokens = line.split(",");
                for (int c = 0; c < cols && c < tokens.length; c++) {
                    map[r][c] = Integer.parseInt(tokens[c].trim());
                }
            }

            // Player spawn
            String spawnLine = br.readLine();
            String[] sp = spawnLine != null ? spawnLine.split(",") : new String[]{"23","12"};
            int[] playerSpawn = {Integer.parseInt(sp[0].trim()), Integer.parseInt(sp[1].trim())};

            // Base position
            String baseLine = br.readLine();
            String[] bp = baseLine != null ? baseLine.split(",") : new String[]{"24","12"};
            int baseRow = Integer.parseInt(bp[0].trim());
            int baseCol = Integer.parseInt(bp[1].trim());
            map[baseRow][baseCol] = BASE;

            int[][] enemySpawns = defaultEnemySpawns();

            return new Level(0, map, playerSpawn, enemySpawns, baseRow, baseCol);

        } catch (Exception e) {
            System.err.println("Failed to load level from CSV: " + filePath + " – " + e.getMessage());
            return null;
        }
    }

    /**
     * Serialize this level to a CSV file (used by Map Editor to save).
     *
     * @param filePath destination path
     */
    public void saveToCSV(String filePath) {
        try (java.io.PrintWriter pw = new java.io.PrintWriter(filePath)) {
            for (int r = 0; r < map.length; r++) {
                StringBuilder sb = new StringBuilder();
                for (int c = 0; c < map[r].length; c++) {
                    if (c > 0) sb.append(',');
                    sb.append(map[r][c]);
                }
                pw.println(sb);
            }
            pw.println(playerSpawn[0] + "," + playerSpawn[1]);
            pw.println(baseRow + "," + baseCol);
        } catch (Exception e) {
            System.err.println("Failed to save level: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Tile accessors  (used by GameEngine for collision / destruction)
    // ─────────────────────────────────────────────────────────────────────────

    public int getTile(int row, int col) {
        if (row < 0 || row >= map.length || col < 0 || col >= map[0].length)
            return STEEL; // treat out-of-bounds as solid wall
        return map[row][col];
    }

    public void setTile(int row, int col, int tileType) {
        if (row >= 0 && row < map.length && col >= 0 && col < map[0].length)
            map[row][col] = tileType;
    }

    /** @return true if the tile at (row,col) blocks tank movement. */
    public boolean isSolid(int row, int col) {
        int t = getTile(row, col);
        return t == BRICK || t == STEEL || t == WATER || t == BASE;
    }

    /** Deep-copy the map array (used to snapshot before Shovel effect). */
    public int[][] getMap() { return map; }

    // ─────────────────────────────────────────────────────────────────────────
    //  Spawn point accessors
    // ─────────────────────────────────────────────────────────────────────────

    /** @return {row, col} of player spawn tile. */
    public int[] getPlayerSpawnTile()    { return playerSpawn.clone(); }

    /** @return array of {row, col} enemy spawn tiles. */
    public int[][] getEnemySpawnPoints() { return enemySpawnPoints; }

    public int getBaseRow() { return baseRow; }
    public int getBaseCol() { return baseCol; }
    public int getLevelNumber() { return levelNumber; }

    // ─────────────────────────────────────────────────────────────────────────
    //  Built-in level definitions
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Shorthand: E=EMPTY, B=BRICK, S=STEEL, W=WATER, G=BUSH(green), X=BASE
     * All levels are 26×26.
     */

    // Helper constants for brevity inside the level builders
    private static final int E = EMPTY, B = BRICK, S = STEEL,
                              W = WATER, G = BUSH,  X = BASE;

    // ── Level 1 – Classic open layout, mostly bricks ─────────────────────────
    private static Level buildLevel1() {
        int R = GamePanel.ROWS, C = GamePanel.COLS;
        int[][] map = new int[R][C]; // starts all EMPTY

        // Border walls (steel)
        for (int c = 0; c < C; c++) { map[0][c] = S; map[R-1][c] = S; }
        for (int r = 0; r < R; r++) { map[r][0] = S; map[r][C-1] = S; }

        // Scattered brick clusters
        placeBrickBox(map, 2, 2, 4, 4);
        placeBrickBox(map, 2, 11, 4, 13);
        placeBrickBox(map, 2, 20, 4, 22);

        placeBrickBox(map, 8, 5, 10, 7);
        placeBrickBox(map, 8, 12, 10, 14);
        placeBrickBox(map, 8, 18, 10, 20);

        placeBrickBox(map, 14, 3, 16, 5);
        placeBrickBox(map, 14, 11, 16, 13);
        placeBrickBox(map, 14, 19, 16, 21);

        // A couple of steel walls
        placeRow(map, S, 6, 6, 10);
        placeRow(map, S, 6, 14, 18);

        // Water strip
        placeRow(map, W, 18, 2, 8);
        placeRow(map, W, 18, 16, 22);

        // Some bushes
        placeBox(map, G, 10, 10, 12, 14);

        // Base (eagle) – bottom centre, surrounded by bricks
        int baseRow = 23, baseCol = 12;
        map[baseRow][baseCol] = X;
        surroundWithBrick(map, baseRow, baseCol);

        int[]   playerSpawn    = {22, 10}; // just left of the base
        int[][] enemySpawns    = {{1,1},{1,12},{1,23}};

        return new Level(1, map, playerSpawn, enemySpawns, baseRow, baseCol);
    }

    // ── Level 2 – More steel, water channels ─────────────────────────────────
    private static Level buildLevel2() {
        int R = GamePanel.ROWS, C = GamePanel.COLS;
        int[][] map = new int[R][C];

        // Border
        for (int c = 0; c < C; c++) { map[0][c] = S; map[R-1][c] = S; }
        for (int r = 0; r < R; r++) { map[r][0] = S; map[r][C-1] = S; }

        // Vertical water channels
        placeCol(map, W, 1, 8, 7);
        placeCol(map, W, 1, 16, 7);

        // Steel bunkers
        placeSteelBox(map, 3, 3, 5, 5);
        placeSteelBox(map, 3, 20, 5, 22);
        placeSteelBox(map, 11, 11, 13, 14);

        // Brick corridors
        placeRow(map, B, 7, 2, 7);
        placeRow(map, B, 7, 10, 14);
        placeRow(map, B, 7, 17, 22);

        placeRow(map, B, 13, 2, 6);
        placeRow(map, B, 13, 18, 22);

        placeRow(map, B, 19, 3, 11);
        placeRow(map, B, 19, 14, 22);

        // Bushes
        placeBox(map, G, 5, 8, 9, 10);
        placeBox(map, G, 5, 14, 9, 16);
        placeBox(map, G, 16, 5, 18, 9);
        placeBox(map, G, 16, 15, 18, 19);

        // Base
        int baseRow = 23, baseCol = 12;
        map[baseRow][baseCol] = X;
        surroundWithBrick(map, baseRow, baseCol);

        int[]   playerSpawn = {22, 10};
        int[][] enemySpawns = {{1,1},{1,12},{1,23}};

        return new Level(2, map, playerSpawn, enemySpawns, baseRow, baseCol);
    }

    // ── Level 3 – Hardest: tight corridors, many steel walls ─────────────────
    private static Level buildLevel3() {
        int R = GamePanel.ROWS, C = GamePanel.COLS;
        int[][] map = new int[R][C];

        // Border
        for (int c = 0; c < C; c++) { map[0][c] = S; map[R-1][c] = S; }
        for (int r = 0; r < R; r++) { map[r][0] = S; map[r][C-1] = S; }

        // Vertical steel walls creating a maze-like centre
        placeCol(map, S, 2, 8, 6);
        placeCol(map, S, 2, 17, 6);
        placeCol(map, S, 10, 12, 6);

        placeCol(map, S, 14, 8, 6);
        placeCol(map, S, 14, 17, 6);

        // Horizontal steel spans
        placeRow(map, S, 9, 2, 10);
        placeRow(map, S, 9, 14, 22);
        placeRow(map, S, 17, 4, 22);

        // Water maze
        placeBox(map, W, 4, 4, 7, 6);
        placeBox(map, W, 4, 18, 7, 21);
        placeBox(map, W, 11, 4, 14, 7);
        placeBox(map, W, 11, 17, 14, 21);

        // Remaining bricks
        placeBrickBox(map, 5, 8, 8, 11);
        placeBrickBox(map, 5, 13, 8, 16);
        placeBrickBox(map, 17, 8, 20, 11);
        placeBrickBox(map, 17, 13, 20, 16);

        // Bushes for ambush spots
        placeBox(map, G, 9, 9, 12, 11);
        placeBox(map, G, 9, 13, 12, 15);

        // Base
        int baseRow = 23, baseCol = 12;
        map[baseRow][baseCol] = X;
        surroundWithBrick(map, baseRow, baseCol);

        int[]   playerSpawn = {22, 10};
        int[][] enemySpawns = {{1,1},{1,12},{1,23}};

        return new Level(3, map, playerSpawn, enemySpawns, baseRow, baseCol);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Map-building helpers
    // ─────────────────────────────────────────────────────────────────────────

    private static void placeBox(int[][] map, int tile,
                                  int r1, int c1, int r2, int c2) {
        for (int r = r1; r <= r2 && r < map.length; r++)
            for (int c = c1; c <= c2 && c < map[0].length; c++)
                map[r][c] = tile;
    }

    private static void placeBrickBox(int[][] map,
                                       int r1, int c1, int r2, int c2) {
        placeBox(map, BRICK, r1, c1, r2, c2);
    }

    private static void placeSteelBox(int[][] map,
                                       int r1, int c1, int r2, int c2) {
        placeBox(map, STEEL, r1, c1, r2, c2);
    }

    /** Fill a horizontal range in row r with the given tile. */
    private static void placeRow(int[][] map, int tile, int row, int c1, int c2) {
        for (int c = c1; c <= c2 && c < map[0].length; c++)
            map[row][c] = tile;
    }

    /** Fill a vertical range in col c with the given tile. */
    private static void placeCol(int[][] map, int tile, int col, int r1, int r2) {
        for (int r = r1; r <= r2 && r < map.length; r++)
            map[r][col] = tile;
    }

    /** Place brick in the 8 neighbours around (baseRow, baseCol). */
    private static void surroundWithBrick(int[][] map, int br, int bc) {
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) continue;
                int r = br + dr, c = bc + dc;
                if (r >= 0 && r < map.length && c >= 0 && c < map[0].length)
                    map[r][c] = BRICK;
            }
        }
    }

    /** Default three enemy spawn points if not overridden. */
    private static int[][] defaultEnemySpawns() {
        return new int[][]{{1,1},{1,12},{1,23}};
    }
}