package com.bocse.perfume;

import com.bocse.perfume.calibration.PerfumeRecommenderCalibration;
import org.apache.commons.configuration.ConfigurationException;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * Created by bocse on 30.11.2015.
 */
public class CalibrationMain {
    private final static Logger logger = Logger.getLogger(CalibrationMain.class.toString());

    public static void main(String[] args) throws IOException, InterruptedException, ConfigurationException {
        new CalibrationMain().execute(args);
    }

    public void execute(String args[]) {
        try {
            PerfumeRecommenderCalibration perfumeRecommenderCalibration = new PerfumeRecommenderCalibration();
            perfumeRecommenderCalibration.initPerfumeCollection(args[0], args[1]);
            ClassLoader classLoader = getClass().getClassLoader();
            perfumeRecommenderCalibration.evalutePrices();
            perfumeRecommenderCalibration.initData(new File(args[2]));
            perfumeRecommenderCalibration.simulatedAnnealing(System.nanoTime() ^ System.currentTimeMillis());


        } catch (Exception badException) {
            logger.severe("Unexpected fatal error: " + badException.toString());
            badException.printStackTrace();
        }
    }
}
