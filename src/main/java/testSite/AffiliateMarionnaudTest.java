package testSite;

import com.bocse.perfume.data.AffiliatePerfume;
import com.bocse.perfume.data.Perfume;
import com.bocse.perfume.iterator.PerfumeIterator;
import org.apache.commons.configuration.ConfigurationException;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

/**
 * Created by bogdan.bocse on 26/07/16.
 */
public class AffiliateMarionnaudTest {
    private final static Logger logger = Logger.getLogger(AffiliateMarionnaudTest.class.toString());


    public AffiliateMarionnaudTest() {

    }

    public static void main(String[] args) throws IOException, InterruptedException, ConfigurationException {
        try {
            new AffiliateMarionnaudTest().execute();
        } catch (Exception ex) {
            logger.warning("Unknown error" + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public void execute() throws IOException {

        AtomicInteger affiliateMatchCount = new AtomicInteger(0);
        AffiliateBaneasaMarionnaud affiliateBaneasaMarionnaud = new AffiliateBaneasaMarionnaud();
        affiliateBaneasaMarionnaud.readProductsFromFile("/Users/bocse/Downloads/baneasa-marionnaud.csv");
        PerfumeIterator perfumeIterator = new PerfumeIterator();
        perfumeIterator.iterateAndKeep(new File("/Users/bocse/data/perfumes/compact.json"));
        perfumeIterator.swap();
        Set<AffiliatePerfume> allPartnerPerfumes = new HashSet<>(affiliateBaneasaMarionnaud.getAllAffiliatePerfumes());

        for (Perfume perfume : perfumeIterator.getBackgroundPerfumeList()) {
            boolean hasMatch = false;

            List<AffiliatePerfume> affiliatePerfumes = affiliateBaneasaMarionnaud.lookup(perfume);

            allPartnerPerfumes.removeAll(affiliatePerfumes);
            if (affiliatePerfumes.size() > 0) {
                hasMatch = true;
                affiliateMatchCount.addAndGet(affiliatePerfumes.size());
                logger.info(perfume.getBrand() + "-" + perfume.getName() + ": " + affiliatePerfumes.size() + " : " + affiliatePerfumes.toString());
            }
        }
        logger.info("Matched perfumes: " + affiliateMatchCount.get());
        logger.info("Unmatch partner perfumes: " + allPartnerPerfumes.size());
        logger.info("------------------");
        logger.info("------------------");
        logger.info("------------------");
        logger.info("------------------");
        for (AffiliatePerfume affiliatePerfume : allPartnerPerfumes) {
            System.out.println(affiliatePerfume.getBrand() + "," + affiliatePerfume.getName() + "," + affiliatePerfume.getGender());
        }
    }
}
