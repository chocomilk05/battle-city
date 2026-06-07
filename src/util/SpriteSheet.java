package util;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class SpriteSheet {

    private final BufferedImage sheet;
    private final int tileW;
    private final int tileH;

    public SpriteSheet(String resourcePath, int tileW, int tileH) {
        this.tileW = tileW;
        this.tileH = tileH;

        BufferedImage loaded = null;
        try {
            InputStream is = SpriteSheet.class
                    .getClassLoader()
                    .getResourceAsStream(resourcePath);
            if (is == null) {
                System.err.println("SpriteSheet: resource not found – " + resourcePath);
            } else {
                loaded = ImageIO.read(is);
                System.out.println("SpriteSheet: loaded " + resourcePath
                        + " (" + loaded.getWidth() + "x" + loaded.getHeight() + ")");
            }
        } catch (IOException e) {
            System.err.println("SpriteSheet: failed to load " + resourcePath
                    + " – " + e.getMessage());
        }
        sheet = loaded;
    }


    public BufferedImage get(int col, int row) {
        if (sheet == null) return null;
        int x = col * tileW;
        int y = row * tileH;
        if (x + tileW > sheet.getWidth() || y + tileH > sheet.getHeight()) {
            System.err.println("SpriteSheet.get: out of bounds (" + col + "," + row + ")");
            return null;
        }
        return sheet.getSubimage(x, y, tileW, tileH);
    }


    public BufferedImage getRegion(int x, int y, int w, int h) {
        if (sheet == null) return null;
        if (x + w > sheet.getWidth() || y + h > sheet.getHeight()) {
            System.err.println("SpriteSheet.getRegion: out of bounds ("
                    + x + "," + y + "," + w + "," + h + ")");
            return null;
        }
        return sheet.getSubimage(x, y, w, h);
    }

    public boolean isLoaded() { return sheet != null; }
    public int getTileW()     { return tileW; }
    public int getTileH()     { return tileH; }
}
