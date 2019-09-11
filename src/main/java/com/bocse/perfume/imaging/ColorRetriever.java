package com.bocse.perfume.imaging;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * Created by bogdan.bocse on 04/05/16.
 */
public class ColorRetriever {
    private final static Logger logger = Logger.getLogger(ColorRetriever.class.toString());

    private void printPixelARGB(int pixel) {
        int alpha = (pixel >> 24) & 0xff;
        int red = (pixel >> 16) & 0xff;
        int green = (pixel >> 8) & 0xff;
        int blue = (pixel) & 0xff;
        float[] hsv = new float[3];
        Color.RGBtoHSB(red, green, blue, hsv);
        //System.out.println("argb: " + alpha + ", " + red + ", " + green + ", " + blue);
        logger.info("H: " + hsv[0] + "S: " + hsv[1] + "V: " + hsv[2]);
    }

    private void marchThroughImage(BufferedImage image) {
        int w = image.getWidth();
        int h = image.getHeight();
        System.out.println("width, height: " + w + ", " + h);

        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                //System.out.println("x,y: " + j + ", " + i);
                int pixel = image.getRGB(j, i);
                printPixelARGB(pixel);
                //System.out.println("");
            }
        }
    }

    public void getImage(File imageFile) {
        try {
            // get the BufferedImage, using the ImageIO class
            BufferedImage image =
                    ImageIO.read(imageFile);
            marchThroughImage(image);
        } catch (IOException e) {
            logger.warning(e.getMessage());
        }
    }
}
