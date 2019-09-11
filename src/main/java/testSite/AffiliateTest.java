package testSite;


import com.bocse.perfume.affiliate.AffiliateParfumExpress;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Created by bocse on 22.11.2015.
 */
public class AffiliateTest {
    private final static Logger logger = Logger.getLogger(AffiliateTest.class.toString());


    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println(System.getProperty("https.protocols"));
        System.out.println(System.getProperty("com.sun.net.ssl.rsaPreMasterSecretFix"));

        //System.setProperty("https.protocols", "TLSv1.2");
        //System.setProperty("com.sun.net.ssl.rsaPreMasterSecretFix", "true");
        AffiliateParfumExpress affiliateParfumExpress = new AffiliateParfumExpress();
        //AffiliateStrawberryNet affiliateStrawberryNet = new AffiliateStrawberryNet();
        //affiliateStawberryNet.readProductsFromURL("https://feeds.performancehorizon.com/bogdanbocse/1100l32/09ce8dc841cfbac8852cbf7b41247095.csv");
        affiliateParfumExpress.readProductsFromURL("http://feeds.2parale.ro/feed/9b2ae00fd867b.csv");
        //affiliateParfumExpress.readProductsFromFile("C:\\Users\\bogdan.bocse\\Downloads\\feed_9b2ae00fd867b.csv");
        //PerfumeIterator perfumeIterator=new PerfumeIterator(configuration.getString("iterator.input.path"));
        //List<Perfume> perfumeList=perfumeIterator.iterate();
//        AffiliateAoroRo affiliateAoroRo =new AffiliateAoroRo(configuration.getString("affiliate.input.path"),perfumeList);
//    affiliateAoroRo.readProducts();
    }
}
