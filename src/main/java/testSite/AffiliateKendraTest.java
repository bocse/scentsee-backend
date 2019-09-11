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
public class AffiliateKendraTest {
    private final static Logger logger = Logger.getLogger(AffiliateKendraTest.class.toString());


    public AffiliateKendraTest() {

    }

    public static void main(String[] args) throws IOException, InterruptedException, ConfigurationException {
        try {
            new AffiliateKendraTest().execute();
        } catch (Exception ex) {
            logger.warning("Unknown error" + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public void execute() throws IOException {

        AtomicInteger affiliateMatchCount = new AtomicInteger(0);
        AffiliateBaneasaKendra affiliateBaneasaKendra = new AffiliateBaneasaKendra();
        affiliateBaneasaKendra.readProductsFromFile("/Users/bocse/Downloads/baneasa-kendra.csv");
        PerfumeIterator perfumeIterator = new PerfumeIterator();
        perfumeIterator.iterateAndKeep(new File("/Users/bocse/data/perfumes/compact.json"));
        perfumeIterator.swap();
        Set<AffiliatePerfume> allPartnerPerfumes = new HashSet<>(affiliateBaneasaKendra.getAllAffiliatePerfumes());

        for (Perfume perfume : perfumeIterator.getBackgroundPerfumeList()) {
            boolean hasMatch = false;

            List<AffiliatePerfume> affiliatePerfumes = affiliateBaneasaKendra.lookup(perfume);

            allPartnerPerfumes.removeAll(affiliatePerfumes);
            if (affiliatePerfumes.size() > 0) {
                hasMatch = true;
                affiliateMatchCount.addAndGet(1);
                logger.info(perfume.getBrand() + "-" + perfume.getName() + ": " + affiliatePerfumes.size() + " : " + affiliatePerfumes.toString());
            }
        }
        logger.info("Matched perfumes: " + affiliateMatchCount);
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
