package testSite;


import com.bocse.perfume.data.Perfume;
import com.bocse.perfume.imaging.ColorThief;
import com.bocse.perfume.imaging.MMCQ;
import com.bocse.perfume.parser.IPerfumeParser;
import com.bocse.perfume.parser.PerfumeParser;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by bocse on 22.11.2015.
 */
public class PictureTest {
    private final static Logger logger = Logger.getLogger(PictureTest.class.toString());


    public static void main(String[] args) throws IOException, InterruptedException {

        Perfume perfume;
        List<Perfume> perfumeList = new ArrayList<>();
        IPerfumeParser pp = new PerfumeParser();
        perfume = pp.parsePerfumeDocument("[ you must fill in an URL of a source which allows scraping]");
        perfumeList.add(perfume);

    }

    private static String createRGBHexString(int[] rgb) {
        String rgbHex = Integer
                .toHexString(rgb[0] << 16 | rgb[1] << 8 | rgb[2]);

        // Left-pad with 0s
        int length = rgbHex.length();
        if (length < 6) {
            rgbHex = "00000".substring(0, 6 - length) + rgbHex;
        }

        return "#" + rgbHex;
    }
}
