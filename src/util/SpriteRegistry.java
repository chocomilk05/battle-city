package util;

import java.awt.image.BufferedImage;

public class SpriteRegistry {

    // Terrain tiles
    public static BufferedImage TILE_BRICK;
    public static BufferedImage TILE_STEEL;
    public static BufferedImage TILE_WATER_1;
    public static BufferedImage TILE_WATER_2;
    public static BufferedImage TILE_BUSH;
    public static BufferedImage TILE_BASE_ALIVE;
    public static BufferedImage TILE_BASE_DEAD;

    //Player tank
    public static BufferedImage PLAYER_UP;
    public static BufferedImage PLAYER_RIGHT;
    public static BufferedImage PLAYER_DOWN;
    public static BufferedImage PLAYER_LEFT;

    //Enemy tank
    public static BufferedImage ENEMY_EASY_UP,   ENEMY_EASY_RIGHT,
                                ENEMY_EASY_DOWN,  ENEMY_EASY_LEFT;
    public static BufferedImage ENEMY_MED_UP,    ENEMY_MED_RIGHT,
                                ENEMY_MED_DOWN,   ENEMY_MED_LEFT;
    public static BufferedImage ENEMY_HARD_UP,   ENEMY_HARD_RIGHT,
                                ENEMY_HARD_DOWN,  ENEMY_HARD_LEFT;

    //Bullets
    public static BufferedImage BULLET_PLAYER_UP,    BULLET_PLAYER_RIGHT,
                                BULLET_PLAYER_DOWN,   BULLET_PLAYER_LEFT;
    public static BufferedImage BULLET_ENEMY_UP,     BULLET_ENEMY_RIGHT,
                                BULLET_ENEMY_DOWN,    BULLET_ENEMY_LEFT;

    //Power ups
    public static BufferedImage PU_TANK;
    public static BufferedImage PU_STAR;
    public static BufferedImage PU_BOMB;
    public static BufferedImage PU_CLOCK;
    public static BufferedImage PU_SHOVEL;
    public static BufferedImage PU_SHIELD;

    //Explosion frames
    public static BufferedImage[] EXPLOSION;

    //Loaded flag
    private static boolean loaded = false;

    public static void load() {
        if (loaded) return;
        loaded = true;

        // TILESET
        SpriteSheet tiles = new SpriteSheet("sprites/GeneralSprites.png", 16, 16);

        TILE_BRICK       = tiles.get(0, 17); 
        TILE_STEEL       = tiles.get(1, 17);  
        TILE_WATER_1     = tiles.get(2, 17);  
        TILE_WATER_2     = tiles.get(3, 17);  
        TILE_BUSH        = tiles.get(3, 18);  
        TILE_BASE_ALIVE  = tiles.get(5, 0);  
        TILE_BASE_DEAD   = tiles.get(6, 0);   

        // TANKS
        SpriteSheet tanks = new SpriteSheet("sprites/GeneralSprites.png", 16, 16);

        PLAYER_UP    = tanks.get(0, 0);   
        PLAYER_RIGHT = tanks.get(6, 0);   
        PLAYER_DOWN  = tanks.get(4, 0); 
        PLAYER_LEFT  = tanks.get(2, 0); 

        ENEMY_EASY_UP    = tanks.get(0, 8);
        ENEMY_EASY_RIGHT = tanks.get(1, 1);
        ENEMY_EASY_DOWN  = tanks.get(2, 1);
        ENEMY_EASY_LEFT  = tanks.get(3, 1);

        ENEMY_MED_UP    = tanks.get(0, 2);
        ENEMY_MED_RIGHT = tanks.get(1, 2);
        ENEMY_MED_DOWN  = tanks.get(2, 2);
        ENEMY_MED_LEFT  = tanks.get(3, 2);

        ENEMY_HARD_UP    = tanks.get(0, 3);
        ENEMY_HARD_RIGHT = tanks.get(1, 3);
        ENEMY_HARD_DOWN  = tanks.get(2, 3);
        ENEMY_HARD_LEFT  = tanks.get(3, 3);

        // BULLETS
        SpriteSheet bullets = new SpriteSheet("sprites/GeneralSprites.png", 4, 4);

        BULLET_PLAYER_UP    = bullets.get(0, 0);
        BULLET_PLAYER_RIGHT = bullets.get(1, 0);
        BULLET_PLAYER_DOWN  = bullets.get(2, 0);
        BULLET_PLAYER_LEFT  = bullets.get(3, 0);

        BULLET_ENEMY_UP    = bullets.get(0, 1);
        BULLET_ENEMY_RIGHT = bullets.get(1, 1);
        BULLET_ENEMY_DOWN  = bullets.get(2, 1);   
        BULLET_ENEMY_LEFT  = bullets.get(3, 1);  

        // POWER-UPS
        SpriteSheet powerups = new SpriteSheet("sprites/Misc.png", 16, 16);

        PU_TANK   = powerups.get(0, 0);
        PU_STAR   = powerups.get(1, 0);
        PU_BOMB   = powerups.get(2, 0);
        PU_CLOCK  = powerups.get(3, 0);
        PU_SHOVEL = powerups.get(4, 0);
        PU_SHIELD = powerups.get(5, 0);

        // EXPLOSION frames
        SpriteSheet expl = new SpriteSheet("sprites/GeneralSprites.png", 16, 16);
        if (expl.isLoaded()) {
            EXPLOSION = new BufferedImage[5];
            for (int i = 0; i < 5; i++) {
                EXPLOSION[i] = expl.get(i, 8);
            }
        }

        System.out.println("SpriteRegistry: all sprites loaded.");
    }

    public static boolean isFullyLoaded() { return loaded; }
}
