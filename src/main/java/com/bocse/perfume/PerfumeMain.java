package com.bocse.perfume;

import com.bocse.perfume.crawlers.DefaultParfumeCrawler;
import org.apache.commons.configuration.ConfigurationException;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Created by bocse on 30.11.2015.
 */
public class PerfumeMain {
    private final static Logger logger = Logger.getLogger(PerfumeMain.class.toString());

    public static void main(String[] args) throws IOException, InterruptedException, ConfigurationException {
        try {
            DefaultParfumeCrawler defaultParfumeCrawler = new DefaultParfumeCrawler(args[0]);
            defaultParfumeCrawler.init();

            defaultParfumeCrawler.initAffiliate();
            defaultParfumeCrawler.execute();
            return;
        } catch (Exception badException) {
            logger.severe("Unexpected fatal error: " + badException.getMessage());
            badException.printStackTrace();
        }
    }
}
