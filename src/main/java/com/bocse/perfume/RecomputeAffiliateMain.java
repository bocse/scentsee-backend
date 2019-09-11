package com.bocse.perfume;

import com.bocse.perfume.crawlers.DefaultParfumeCrawler;
import org.apache.commons.configuration.ConfigurationException;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Created by bocse on 30.11.2015.
 */
public class RecomputeAffiliateMain {
    private final static Logger logger = Logger.getLogger(RecomputeAffiliateMain.class.toString());

    public static void main(String[] args) throws IOException, InterruptedException, ConfigurationException {
        new RecomputeAffiliateMain().mainPrivate(args);
    }

    private void mainPrivate(String[] args) {
        try {


            DefaultParfumeCrawler defaultParfumeCrawler = new DefaultParfumeCrawler(args[0]);
            defaultParfumeCrawler.init();
            defaultParfumeCrawler.initAffiliate();

            defaultParfumeCrawler.executeAffiliateOnly();
            return;
        } catch (Exception badException) {
            logger.severe("Unexpected fatal error: " + badException.getMessage());
            badException.printStackTrace();
        }

    }
}
