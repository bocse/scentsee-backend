package com.bocse.perfume.crawlers;

import com.bocse.perfume.affiliate.AffiliateAoroRo;
import com.bocse.perfume.affiliate.AffiliateInterface;
import com.bocse.perfume.affiliate.AffiliateParfumExpress;
import com.bocse.perfume.affiliate.AffiliateStrawberryNet;
import com.bocse.perfume.data.AffiliatePerfume;
import com.bocse.perfume.data.Perfume;
import com.bocse.perfume.iterator.PerfumeIterator;
import com.bocse.perfume.parser.IPerfumeParser;
import com.bocse.perfume.parser.PerfumeParser;
import com.bocse.perfume.serializer.JsonSerializer;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.FileConfiguration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.time.StopWatch;
import org.apache.commons.lang3.NotImplementedException;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

/**
 * Created by bocse on 01.12.2015.
 */
public class DefaultParfumeCrawler {
    private final static Logger logger = Logger.getLogger(DefaultParfumeCrawler.class.toString());
    public final FileConfiguration configuration = new PropertiesConfiguration();
    private final String configFile;
    private final boolean allowPerfumesWithNoNotes = false;
    private ExecutorService executorService;
    //private Map<String, Note> knownNotes=null;
    private List<AffiliateInterface> affiliateInterfaces = new ArrayList<>();
    private Map<String, AtomicLong> affiliateMatchCount = new HashMap<>();
    private boolean doAffiliates = true;
    private List<Perfume> perfumeList = Collections.synchronizedList(new ArrayList<>());

    public DefaultParfumeCrawler(String configFile) {
        this.configFile = configFile;
    }

    public void init() throws ConfigurationException {
        configuration.load(configFile);
        executorService = Executors.newFixedThreadPool(configuration.getInt("working.mode.threadsNumber"));

    }

    public void initAffiliate() throws IOException, InterruptedException {
        if (configuration.getString("affiliate.perform").equals("false")) {
            logger.info("NOT loading affiliates");
            doAffiliates = false;
            return;
        }
        AffiliateParfumExpress affiliateParfumExpress = new AffiliateParfumExpress();

        if (configuration.getString("affiliate.parfumexpress.method").equals("file")) {
            affiliateParfumExpress.readProductsFromFile(configuration.getString("affiliate.parfumexpress.path"));
            affiliateInterfaces.add(affiliateParfumExpress);
            affiliateMatchCount.put(affiliateParfumExpress.getAffiliateName(), new AtomicLong(0));
        } else if (configuration.getString("affiliate.parfumexpress.method").equals("http")) {
            affiliateParfumExpress.readProductsFromURL(configuration.getString("affiliate.parfumexpress.url"));
            affiliateInterfaces.add(affiliateParfumExpress);
            affiliateMatchCount.put(affiliateParfumExpress.getAffiliateName(), new AtomicLong(0));
        } else if (configuration.getString("affiliate.parfumexpress.method").equals("skip")) {
            logger.info("Skipping affiliate ParfumExpress");
        } else
            throw new NotImplementedException("Unknown data source for affiliate products: " + configuration.getString("affiliate.parfumexpress.method"));


        AffiliateStrawberryNet affiliateStrawberryNet = new AffiliateStrawberryNet();

        if (configuration.getString("affiliate.strawberry.method").equals("file")) {
            affiliateStrawberryNet.readProductsFromFile(configuration.getString("affiliate.strawberry.path"));
            affiliateInterfaces.add(affiliateStrawberryNet);
            affiliateMatchCount.put(affiliateStrawberryNet.getAffiliateName(), new AtomicLong(0));
        } else if (configuration.getString("affiliate.strawberry.method").equals("http")) {
            affiliateStrawberryNet.readProductsFromURL(configuration.getString("affiliate.strawberry.url"));
            affiliateInterfaces.add(affiliateStrawberryNet);
            affiliateMatchCount.put(affiliateStrawberryNet.getAffiliateName(), new AtomicLong(0));
        } else if (configuration.getString("affiliate.strawberry.method").equals("skip")) {
            logger.info("Skipping affiliate Strawberry");
        } else
            throw new NotImplementedException("Unknown data source for affiliate products: " + configuration.getString("affiliate.strawberry.method"));

        AffiliateAoroRo affiliateAoroRo = new AffiliateAoroRo();
        if (configuration.getString("affiliate.aoro.method").equals("file")) {
            affiliateAoroRo.readProductsFromFile(configuration.getString("affiliate.aoro.path"));
            affiliateInterfaces.add(affiliateAoroRo);
            affiliateMatchCount.put(affiliateAoroRo.getAffiliateName(), new AtomicLong(0));
        } else if (configuration.getString("affiliate.aoro.method").equals("http-auth-gzip")) {
            affiliateAoroRo.readProductsFromURL(configuration.getString("affiliate.aoro.url"), configuration.getString("affiliate.aoro.username"), configuration.getString("affiliate.aoro.password"));
            affiliateInterfaces.add(affiliateAoroRo);
            affiliateMatchCount.put(affiliateAoroRo.getAffiliateName(), new AtomicLong(0));
        } else if (configuration.getString("affiliate.aoro.method").equals("skip")) {
            logger.info("Skipping affiliate ParfumExpress");
        } else
            throw new NotImplementedException("Unknown data source for affiliate products: " + configuration.getString("affiliate.aoro.method"));

//        affiliateAoro =affiliateAoroRo;
//        affiliateStrawberry=affiliateStrawberryNet;
    }

    public void executeAffiliateOnly() throws IOException {
        final StopWatch stopWatch = new StopWatch();

        final AtomicLong matchWithAffiliate = new AtomicLong(0);

        PerfumeIterator iterator = new PerfumeIterator();
        File compactStoreFile = new File(configuration.getString("output.perfumesCompact.path"));
        iterator.iterateAndKeep(compactStoreFile);
        iterator.swap();
        int index = 0;
        stopWatch.start();
        for (Perfume perfume : iterator.getPerfumeList()) {
            Boolean hasAffiliateMatch = false;

            for (AffiliateInterface affiliateInterface : affiliateInterfaces) {
                List<AffiliatePerfume> affiliatePerfumes = affiliateInterface.lookup(perfume);

                if (affiliatePerfumes.size() > 0) {
                    perfume.getAffiliateProducts().put(affiliateInterface.getAffiliateName(), affiliatePerfumes);
                    affiliateMatchCount.get(affiliateInterface.getAffiliateName()).addAndGet(1);
                    hasAffiliateMatch = true;
                }
            }
            if (hasAffiliateMatch) {
                matchWithAffiliate.addAndGet(1);
            }

            index++;
            if (index % 1000 == 0) {
                logger.info("Done " + (100.0 * index / iterator.getPerfumeList().size()) + " %");
                logger.info("Affiliate   matches: " + matchWithAffiliate.get() + "(" + (100.0 * matchWithAffiliate.get() / iterator.getPerfumeList().size()) + " %)");
                logger.info("Affiliate match breakdown: " + affiliateMatchCount.toString());
            }
        }
        stopWatch.stop();
        logger.info("Elapsed seconds: " + stopWatch.getTime() / 1000);
        logger.info("Done " + (100.0 * index / iterator.getPerfumeList().size()) + " %");
        logger.info("Affiliate   matches: " + matchWithAffiliate.get() + "(" + (100.0 * matchWithAffiliate.get() / iterator.getPerfumeList().size()) + " %)");
        logger.info("Affiliate match: " + affiliateMatchCount.toString());
        compactStoreFile.renameTo(new File(configuration.getString("output.perfumesCompact.path") + ".backup"));
        JsonSerializer.serialize(configuration.getString("output.perfumesCompact.path"), iterator.getPerfumeList(), false);
        logger.info("DONE");
    }

    public void execute() throws IOException, InterruptedException {
        final StopWatch stopWatch = new StopWatch();
        IPerfumeParser parfumeMainParser = new PerfumeParser();
        final AtomicLong matchWithAffiliate = new AtomicLong(0);
        final AtomicLong perfumeGlobalIndex = new AtomicLong(0);
        final AtomicLong brandIndex = new AtomicLong(0);
        final AtomicLong errorCount = new AtomicLong(0);
        final AtomicLong pictureErrorCount = new AtomicLong(0);
        final AtomicLong picturesRetrieved = new AtomicLong(0);
        final List<Future<Object>> futures = new ArrayList<>();

        Map<String, String> brands = parfumeMainParser.parseBrands();
        JsonSerializer.serialize(configuration.getString("output.brandList.path"), brands);
        stopWatch.start();
        for (Map.Entry<String, String> brandIterator : brands.entrySet()) {
            final Map.Entry<String, String> brand = brandIterator;

            Future<Object> future = executorService.submit(new Callable<Object>() {

                @Override
                public Object call() throws Exception {
                    AtomicLong perfumeIndex = new AtomicLong(0);
                    brandIndex.addAndGet(1);
                    IPerfumeParser IPerfumeParser = new PerfumeParser();
                    logger.info("Getting perfumes from " + brand.getKey());
                    Map<String, String> perfumes = IPerfumeParser.parserBrand(brand.getValue());
                    for (Map.Entry<String, String> perfumeEntry : perfumes.entrySet()) {
                        logger.info("Getting perfume " + perfumeEntry.getKey());
                        try {

                            Perfume perfume = IPerfumeParser.parsePerfumeDocument(perfumeEntry.getValue());
                            try {
                                boolean isPictureRetrieved = IPerfumeParser.getPerfumePictures(perfume, configuration.getString("output.pictures.path"));
                                if (isPictureRetrieved) {
                                    picturesRetrieved.incrementAndGet();
                                }
                            } catch (Exception ex) {
                                logger.warning("Error retrieving picture " + perfume.getPictureURL());
                                pictureErrorCount.incrementAndGet();
                            }
                            if (doAffiliates) {
                                Boolean hasAffiliateMatch = false;
                                for (AffiliateInterface affiliateInterface : affiliateInterfaces) {
                                    List<AffiliatePerfume> affiliatePerfumes = affiliateInterface.lookup(perfume);

                                    if (affiliatePerfumes.size() > 0) {
                                        perfume.getAffiliateProducts().put(affiliateInterface.getAffiliateName(), affiliatePerfumes);
                                        affiliateMatchCount.get(affiliateInterface.getAffiliateName()).addAndGet(1);
                                        hasAffiliateMatch = true;
                                    }
                                }
                                if (hasAffiliateMatch) {
                                    matchWithAffiliate.addAndGet(1);
                                }
                            }
                            long stopWatchValue = 0;
                            synchronized (stopWatch) {
                                perfumeGlobalIndex.addAndGet(1);
                                if (perfume.getMixedNotes().size() > 0 || perfume.getTopNotes().size() > 0 || allowPerfumesWithNoNotes) {
                                    if (configuration.getBoolean("output.saveIndividualFiles")) {
                                        JsonSerializer.serialize(configuration.getString("output.perfumes.path") + "_" + perfumeGlobalIndex.get() + ".txt", perfume);
                                    }
                                    perfumeList.add(perfume);
                                }
                                perfumeIndex.addAndGet(1);
                                stopWatchValue = stopWatch.getTime();
                            }
                            logger.info("Total perfumes: \t" + perfumeGlobalIndex.get() + "Perfumes / second: \t" + ((double) perfumeGlobalIndex.get() / (stopWatchValue / 1000.0)));
                            logger.info("Affiliate   matches: " + matchWithAffiliate.get() + "(" + (100.0 * matchWithAffiliate.get() / perfumeGlobalIndex.get()) + " %)");
                            logger.info("Affiliate match breakdown: " + affiliateMatchCount.toString());
                            logger.info("Errors: " + errorCount.get() + "\tPicture Error: " + pictureErrorCount.get() + " Pictures retrieved: " + picturesRetrieved.get());
                        } catch (Exception ex) {
                            logger.warning("Error encountered while reading " + ex.toString() + "\t " + perfumeEntry.getValue());
                            errorCount.incrementAndGet();
                            //throw ex;
                        } finally {
                            //perfumeIndex.addAndGet(1);
                            //perfumeGlobalIndex.addAndGet(1);

                        }
                    }

                    synchronized (stopWatch) {
                        logger.info("Elapsed seconds: " + stopWatch.getTime() / 1000);
                    }
                    return new Object();
                }
            });
            futures.add(future);

        }

        executorService.shutdown();
        executorService.awaitTermination(10, TimeUnit.DAYS);

        int exceptionCount = 0;
        for (Future<Object> future : futures) {
            try {
                future.get();
            } catch (ExecutionException e) {
                logger.info(e.getMessage());
                exceptionCount++;
            }
        }

        logger.warning("Encountered " + exceptionCount + " errors from " + futures.size() + " brands.");
        logger.warning("Encountered " + errorCount.get() + " errors from " + perfumeList.size() + " perfumes.");
        logger.warning("Encountered " + pictureErrorCount.get() + " errors from " + perfumeList.size() + " pictures.");
        logger.info("Elapsed seconds: " + stopWatch.getTime() / 1000);
        logger.info("Creating compacted collection.");
        JsonSerializer.serialize(configuration.getString("output.perfumesCompact.path"), perfumeList, false);
        logger.info("Done.");
    }
}
