package ui;
import entities.*;
import powerups.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class GameEngine {

    public static final int  TOTAL_ENEMIES_PER_LEVEL = 20;
    public static final int  MAX_ENEMIES_ON_SCREEN   = 4;
    public static final int  PLAYER_START_LIVES      = 3;
    public static final int  SPAWN_INTERVAL_MS       = 2000;
    public static final long CLOCK_FREEZE_MS         = 8_000;
    public static final long SHOVEL_STEEL_MS         = 15_000;
    public static final long SHIELD_DURATION_MS      = 8_000;

    private PlayerTank player;
    private final List<EnemyTank> enemies  = new CopyOnWriteArrayList<>();
    private final List<PowerUp>   powerUps = new CopyOnWriteArrayList<>();

    private Level   currentLevel;
    private int     levelNumber         = 1;
    private int     score               = 0;
    private int     lives               = PLAYER_START_LIVES;

    private int     totalEnemiesSpawned = 0;

    private volatile boolean gameOver      = false;
    private volatile boolean levelComplete = false;
    private volatile boolean clockFrozen   = false;

    private Thread  spawnerThread;
    private volatile boolean spawnerRunning = false;

    private Thread clockThread;
    private Thread shovelThread;
    private Thread shieldThread;

    private int[][] savedBaseTiles;

    // Level loading

    public void loadLevel(int levelNumber) {
        this.levelNumber         = levelNumber;
        this.currentLevel        = Level.load(levelNumber);
        this.totalEnemiesSpawned = 0;
        this.levelComplete       = false;
        this.clockFrozen         = false;

        enemies.clear();
        powerUps.clear();

        int[] spawnPos = currentLevel.getPlayerSpawnTile();
        player = new PlayerTank(
            spawnPos[1] * GamePanel.TILE_SIZE,
            spawnPos[0] * GamePanel.TILE_SIZE,
            null
        );

        startSpawnerThread();
    }

    private void startSpawnerThread() {
        spawnerRunning = false;
        if (spawnerThread != null && spawnerThread.isAlive()) {
            spawnerThread.interrupt();
        }
        spawnerRunning = true;
        spawnerThread = new Thread(() -> {
            while (spawnerRunning && totalEnemiesSpawned < TOTAL_ENEMIES_PER_LEVEL) {
                try {
                    Thread.sleep(SPAWN_INTERVAL_MS);
                } catch (InterruptedException ie) {
                    break;
                }
                while (spawnerRunning
                        && totalEnemiesSpawned < TOTAL_ENEMIES_PER_LEVEL
                        && enemies.size() >= MAX_ENEMIES_ON_SCREEN) {
                    try { Thread.sleep(500); } catch (InterruptedException ie) { break; }
                }
                if (spawnerRunning) {
                    spawnEnemy();
                }
            }
        }, "EnemySpawner");
        spawnerThread.setDaemon(true);
        spawnerThread.start();
    }

    private synchronized void spawnEnemy() {
        if (enemies.size() >= MAX_ENEMIES_ON_SCREEN) return;
        if (totalEnemiesSpawned >= TOTAL_ENEMIES_PER_LEVEL) return;

        int[][] spawnPoints = currentLevel.getEnemySpawnPoints();
        int[]   pt          = spawnPoints[totalEnemiesSpawned % spawnPoints.length];

        int speed = (levelNumber == 1) ? EnemyTank.SPEED_EASY
                  : (levelNumber == 2) ? EnemyTank.SPEED_MEDIUM
                  :                      EnemyTank.SPEED_HARD;

        int baseRow = currentLevel.getBaseRow();
        int baseCol = currentLevel.getBaseCol();

        EnemyTank enemy = new EnemyTank(
            pt[1] * GamePanel.TILE_SIZE,
            pt[0] * GamePanel.TILE_SIZE,
            speed,
            null,
            player,
            baseCol * GamePanel.TILE_SIZE,
            baseRow * GamePanel.TILE_SIZE
        );
        enemies.add(enemy);
        totalEnemiesSpawned++;
    }

    // Per-tick update

    public void update() {
        if (gameOver || levelComplete) return;

        updatePlayer();
        updateEnemies();
        checkBulletCollisions();
        checkPowerUpCollections();
        checkLevelComplete();
    }

    private void updatePlayer() {
        if (player == null || player.isDestroyed()) return;
        player.update();
    }

    private void updateEnemies() {
        Iterator<EnemyTank> it = enemies.iterator();
        while (it.hasNext()) {
            EnemyTank e = it.next();
            if (e.isDestroyed()) {
                it.remove();
                continue;
            }
            if (!clockFrozen) {
                e.update();
            }
        }
    }

    // Bullet collision

    private void checkBulletCollisions() {
        List<Bullet> allBullets = new ArrayList<>();
        if (player != null && !player.isDestroyed()) {
            allBullets.addAll(player.getBullets());
        }
        for (EnemyTank e : enemies) {
            allBullets.addAll(e.getBullets());
        }

        Set<Bullet> toDeactivate = new HashSet<>();

        for (Bullet b : allBullets) {
            if (!b.isActive() || toDeactivate.contains(b)) continue;

            //Tile collision
            if (!b.isActive()) continue;

            //Player bullet hits enemy tank
            if (b.isFromPlayer()) {
                for (EnemyTank enemy : enemies) {
                    if (enemy.isDestroyed()) continue;
                    if (b.intersects(enemy.getX(), enemy.getY(), Tank.SIZE, Tank.SIZE)) {
                        enemy.hit();
                        if (enemy.isDestroyed()) {
                            score += 200;
                            maybeDropPowerUp(
                                enemy.getX() + Tank.SIZE / 2,
                                enemy.getY() + Tank.SIZE / 2);
                        }
                        toDeactivate.add(b);
                        break;
                    }
                }
            }

            //Enemy bullet hits player
            if (!b.isFromPlayer() && player != null
                    && !player.isDestroyed() && !player.isShielded()) {
                if (b.intersects(player.getX(), player.getY(), Tank.SIZE, Tank.SIZE)) {
                    handlePlayerHit();
                    toDeactivate.add(b);
                }
            }

            //Enemy bullet hits base
            if (!b.isFromPlayer()) {
                int basePixelX = currentLevel.getBaseCol() * GamePanel.TILE_SIZE;
                int basePixelY = currentLevel.getBaseRow() * GamePanel.TILE_SIZE;
                if (b.intersects(basePixelX, basePixelY,
                                 GamePanel.TILE_SIZE, GamePanel.TILE_SIZE)) {
                    triggerGameOver();
                    toDeactivate.add(b);
                }
            }

            //Opposite bullets cancel each other out
            for (Bullet other : allBullets) {
                if (other == b || toDeactivate.contains(other)) continue;
                if (b.isFromPlayer() != other.isFromPlayer()
                        && b.intersects(other.getX(), other.getY(),
                                        Bullet.SIZE, Bullet.SIZE)) {
                    toDeactivate.add(b);
                    toDeactivate.add(other);
                }
            }
        }

        for (Bullet b : toDeactivate) b.deactivate();
    }

    // Player hit

    private void handlePlayerHit() {
        lives--;
        if (lives <= 0) {
            triggerGameOver();
        } else {
            int[] spawn = currentLevel.getPlayerSpawnTile();
            player.respawn(
                spawn[1] * GamePanel.TILE_SIZE,
                spawn[0] * GamePanel.TILE_SIZE
            );
        }
    }

    private void triggerGameOver() {
        gameOver       = true;
        spawnerRunning = false;
        if (spawnerThread != null) spawnerThread.interrupt();
    }

    // Player–enemy body collision

    private void checkPlayerEnemyCollisions() {
        if (player == null || player.isDestroyed()) return;
        for (EnemyTank enemy : enemies) {
            if (!enemy.isDestroyed()
                    && player.intersects(enemy.getX(), enemy.getY(), Tank.SIZE, Tank.SIZE)) {
            }
        }
    }

    // Power-up collection

    private void checkPowerUpCollections() {
        if (player == null || player.isDestroyed()) return;
        powerUps.removeIf(pu -> {
            if (player.intersects(pu.getX(), pu.getY(), PowerUp.SIZE, PowerUp.SIZE)) {
                applyPowerUp(pu);
                return true;
            }
            return false;
        });
    }

    private void applyPowerUp(PowerUp pu) {
        switch (pu.getType()) {
            case TANK   -> lives++;
            case STAR   -> player.collectStar();
            case BOMB   -> {
                for (EnemyTank e : enemies) {
                    if (!e.isDestroyed()) { e.hit(); score += 200; }
                }
            }
            case CLOCK  -> startClockFreeze();
            case SHOVEL -> startShovelSteel();
            case SHIELD -> startShield();
        }
        score += 500;
    }

    private void maybeDropPowerUp(int x, int y) {
        if (Math.random() < 0.25) {
            PowerUp.Type[] types = PowerUp.Type.values();
            PowerUp.Type t = types[(int)(Math.random() * types.length)];
            PowerUp pu;
            switch (t) {
                case BOMB   -> pu = new BombPowerUp(x, y);
                case CLOCK  -> pu = new ClockPowerUp(x, y);
                case SHIELD -> pu = new ShieldPowerUp(x, y);
                case SHOVEL -> pu = new ShovelPowerUp(x, y);
                case STAR   -> pu = new StarPowerUp(x, y);
                default     -> pu = new TankPowerUp(x, y);
            }
            powerUps.add(pu);
        }
    }

    // Level-complete check

    private void checkLevelComplete() {
        if (totalEnemiesSpawned >= TOTAL_ENEMIES_PER_LEVEL && enemies.isEmpty()) {
            levelComplete  = true;
            spawnerRunning = false;
        }
    }

    // Power-up timers

    private void startClockFreeze() {
        clockFrozen = true;
        if (clockThread != null && clockThread.isAlive()) clockThread.interrupt();
        clockThread = new Thread(() -> {
            try { Thread.sleep(CLOCK_FREEZE_MS); } catch (InterruptedException ignored) {}
            clockFrozen = false;
        }, "ClockTimer");
        clockThread.setDaemon(true);
        clockThread.start();
    }

    private void startShovelSteel() {
        int br = currentLevel.getBaseRow();
        int bc = currentLevel.getBaseCol();
        int[][] ring = {
            {br-1, bc-1}, {br-1, bc}, {br-1, bc+1},
            {br,   bc-1},             {br,   bc+1},
            {br+1, bc-1}, {br+1, bc}, {br+1, bc+1}
        };
        savedBaseTiles = new int[ring.length][3];
        for (int i = 0; i < ring.length; i++) {
            savedBaseTiles[i][0] = ring[i][0];
            savedBaseTiles[i][1] = ring[i][1];
            if (ring[i][0] >= 0 && ring[i][0] < GamePanel.ROWS &&
                ring[i][1] >= 0 && ring[i][1] < GamePanel.COLS) {
                savedBaseTiles[i][2] = currentLevel.getTile(ring[i][0], ring[i][1]);
                currentLevel.setTile(ring[i][0], ring[i][1], Level.STEEL);
            }
        }
        if (shovelThread != null && shovelThread.isAlive()) shovelThread.interrupt();
        shovelThread = new Thread(() -> {
            try { Thread.sleep(SHOVEL_STEEL_MS); } catch (InterruptedException ignored) {}
            if (savedBaseTiles != null) {
                for (int[] entry : savedBaseTiles) {
                    if (entry[0] >= 0 && entry[0] < GamePanel.ROWS &&
                        entry[1] >= 0 && entry[1] < GamePanel.COLS) {
                        currentLevel.setTile(entry[0], entry[1], entry[2]);
                    }
                }
            }
        }, "ShovelTimer");
        shovelThread.setDaemon(true);
        shovelThread.start();
    }

    private void startShield() {
        player.activateShield();
        if (shieldThread != null && shieldThread.isAlive()) shieldThread.interrupt();
        shieldThread = new Thread(() -> {
            try { Thread.sleep(SHIELD_DURATION_MS); } catch (InterruptedException ignored) {}
            player.deactivateShield();
        }, "ShieldTimer");
        shieldThread.setDaemon(true);
        shieldThread.start();
    }

    // Input handling

    public void handleInput(Set<Integer> keysHeld) {
        if (player == null || player.isDestroyed() || gameOver || levelComplete) return;

        for (int key : keysHeld) player.keyPressed(key);
        if      (keysHeld.contains(KeyEvent.VK_W) || keysHeld.contains(KeyEvent.VK_UP))
            player.setDirection(Tank.UP);
        else if (keysHeld.contains(KeyEvent.VK_S) || keysHeld.contains(KeyEvent.VK_DOWN))
            player.setDirection(Tank.DOWN);
        else if (keysHeld.contains(KeyEvent.VK_A) || keysHeld.contains(KeyEvent.VK_LEFT))
            player.setDirection(Tank.LEFT);
        else if (keysHeld.contains(KeyEvent.VK_D) || keysHeld.contains(KeyEvent.VK_RIGHT))
            player.setDirection(Tank.RIGHT);
    }

    // Rendering

    public void render(Graphics2D g) {
        for (PowerUp pu : powerUps)  pu.draw(g);
        for (EnemyTank e : enemies)  e.draw(g);
        if (player != null)           player.draw(g);

        if (player != null) {
            for (Bullet b : player.getBullets()) b.draw(g);
        }
        for (EnemyTank e : enemies) {
            for (Bullet b : e.getBullets()) b.draw(g);
        }

        drawBushOverlay(g);
    }

    private void drawBushOverlay(Graphics2D g) {
        if (currentLevel == null) return;
        int[][] map = currentLevel.getMap();
        for (int row = 0; row < GamePanel.ROWS; row++) {
            for (int col = 0; col < GamePanel.COLS; col++) {
                if (map[row][col] == Level.BUSH) {
                    g.setColor(new Color(0, 160, 0, 200));
                    g.fillRect(col * GamePanel.TILE_SIZE,
                               row * GamePanel.TILE_SIZE,
                               GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);
                }
            }
        }
    }

    // Getters

    public Level   getCurrentLevel()     { return currentLevel; }
    public int     getLevelNumber()      { return levelNumber; }
    public int     getScore()            { return score; }
    public int     getLives()            { return lives; }
    public boolean isGameOver()          { return gameOver; }
    public boolean isLevelComplete()     { return levelComplete; }
    public boolean isClockActive()       { return clockFrozen; }
    public boolean isShieldActive()      { return player != null && player.isShielded(); }
    public boolean isShovelActive()      { return shovelThread != null && shovelThread.isAlive(); }
    public int     getPlayerStars()      { return player != null ? player.getStarLevel() : 0; }

    public int getRemainingEnemies() {
        return (TOTAL_ENEMIES_PER_LEVEL - totalEnemiesSpawned) + enemies.size();
    }

    public void resetGame() {
        score    = 0;
        lives    = PLAYER_START_LIVES;
        gameOver = false;
        loadLevel(1);
    }

    public void nextLevel() {
        levelComplete = false;
        loadLevel(levelNumber + 1);
    }
}