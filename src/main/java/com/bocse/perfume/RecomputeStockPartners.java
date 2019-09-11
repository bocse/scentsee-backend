package com.bocse.perfume;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;
import com.bocse.perfume.affiliate.*;
import com.bocse.perfume.data.AffiliatePerfume;
import com.bocse.perfume.data.Perfume;
import com.bocse.perfume.iterator.PerfumeIterator;
import com.bocse.perfume.signature.SignatureEvaluator;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.FileConfiguration;
import org.apache.commons.configuration.PropertiesConfiguration;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

/**
 * Created by bocse on 30.11.2015.
 */
public class RecomputeStockPartners {
    private final static Logger logger = Logger.getLogger(RecomputeStockPartners.class.toString());
    public final FileConfiguration configuration = new PropertiesConfiguration();
    private final String configFile;
    private AmazonS3 s3Client;
    private String s3BucketName;
    private SignatureEvaluator signatureEvaluator;
    private PerfumeIterator perfumeIteratorUpdated;

    public RecomputeStockPartners(String configFile) {
        this.configFile = configFile;
    }

    public static void main(String[] args) throws IOException, InterruptedException, ConfigurationException {
        new RecomputeStockPartners(args[0]).execute(args);
    }

    private void initS3() {
        final String s3AccessKey = configuration.getString("s3.accessKey");
        final String s3SecretKey = configuration.getString("s3.secretKey");
        s3BucketName = configuration.getString("s3.bucketName");
        s3Client = new AmazonS3Client(
                new BasicAWSCredentials(s3AccessKey, s3SecretKey));
    }

    private void initConfig() throws ConfigurationException {
        configuration.load(configFile);
    }

    private void loadData() throws IOException {
        signatureEvaluator = new SignatureEvaluator();
        signatureEvaluator.iterateAndKeep(new File(configuration.getString("input.update.notes.path")));
        signatureEvaluator.swap();

        perfumeIteratorUpdated = new PerfumeIterator();
        perfumeIteratorUpdated.iterateAndKeep(new File(configuration.getString("iterator.update.input.path")));
        perfumeIteratorUpdated.swap();

        for (Perfume perfume : perfumeIteratorUpdated.getPerfumeList()) {
            signatureEvaluator.embedPerfumeSignature(perfume);
        }
        logger.info("Loaded data");

    }

    private Map<String, String> listS3() {


        final ListObjectsV2Request req = new ListObjectsV2Request().withBucketName(s3BucketName);
        Map<String, String> keyMap = new HashMap<>();
        Map<String, Long> timestampMap = new HashMap<>();
        ListObjectsV2Result result;
        do {
            result = s3Client.listObjectsV2(req);

            for (S3ObjectSummary objectSummary :
                    result.getObjectSummaries()) {
                String s3Key = objectSummary.getKey();
                Long s3Timestamp = objectSummary.getLastModified().getTime();
                //TODO: fix this total patchwork
                String[] parts = s3Key.split("/");
                String stockIdentifier;
                if (parts.length > 1) {
                    stockIdentifier = parts[0];
                } else {
                    continue;
                }
                if (!keyMap.containsKey(stockIdentifier)) {
                    keyMap.put(stockIdentifier, s3Key);
                    timestampMap.put(stockIdentifier, objectSummary.getLastModified().getTime());
                } else {
                    Long previousTimestamp = objectSummary.getLastModified().getTime();
                    if (previousTimestamp > timestampMap.get(stockIdentifier)) {
                        keyMap.put(stockIdentifier, s3Key);
                        timestampMap.put(stockIdentifier, objectSummary.getLastModified().getTime());
                    }
                }

            }
            //System.out.println("Next Continuation Token : " + result.getNextContinuationToken());
            req.setContinuationToken(result.getNextContinuationToken());
        } while (result.isTruncated() == true);
        logger.info(keyMap.toString());
        return keyMap;
    }

    private byte[] getS3Key(String key) throws IOException {
        S3Object object = s3Client.getObject(
                new GetObjectRequest(s3BucketName, key));

        byte[] bytes = IOUtils.toByteArray(object.getObjectContent());
        return bytes;
    }

    private void loadPartnerData(Map<String, String> mostRecentStocks) throws IOException, ClassNotFoundException {
        for (Map.Entry<String, String> element : mostRecentStocks.entrySet()) {
            ClassLoader classLoader = RecomputeStockPartners.class.getClassLoader();
            NumberFormat formatter = new DecimalFormat("#0.00");

            String stockIdentifier = element.getKey();
            String s3Key = element.getValue();

            //TODO: define  partners matchers for which constructor is defined
            Affiliate affiliate=new AffiliateStrawberryNet();


            AtomicInteger affiliateMatchCount = new AtomicInteger(0);
            int index = 0;
            Set<AffiliatePerfume> matchedAffiliate = new HashSet<>();
            for (Perfume perfume : perfumeIteratorUpdated.getBackgroundPerfumeList()) {
                boolean hasMatch = false;

                List<AffiliatePerfume> affiliatePerfumes = affiliate.lookup(perfume);

                if (affiliatePerfumes.size() > 0) {
                    hasMatch = true;
                    affiliateMatchCount.incrementAndGet();
                    perfume.getAffiliateProducts().put(affiliate.getAffiliateName(), affiliatePerfumes);
                    matchedAffiliate.addAll(affiliatePerfumes);
                } else {
                    perfume.getAffiliateProducts().remove(affiliate.getAffiliateName());
                }


                index++;
                if (index % 1000 == 0) {
                    logger.info("Done " + (100.0 * index / perfumeIteratorUpdated.getPerfumeList().size()) + " %");
                    logger.info("Matches with partner " + stockIdentifier + ": " + affiliateMatchCount.toString() + " / " + affiliate.getAllAffiliatePerfumes().size());
                }
            }
            logger.info("Match rate with partner  " + stockIdentifier + " : " + formatter.format(100.0 * affiliateMatchCount.get() / affiliate.getAllAffiliatePerfumes().size()));
        }

    }

    private void serialize() throws IOException {
        //
        perfumeIteratorUpdated.serialize(new File(configuration.getString("output.merged.perfumesCompact.path")));
    }

    private void execute(String[] args) {
        try {
            initConfig();
            initS3();
            Map<String, String> mostRecentStocks = listS3();
            loadData();
            loadPartnerData(mostRecentStocks);
            serialize();
            return;
        } catch (Exception badException) {
            logger.severe("Unexpected fatal error: " + badException.getMessage());
            badException.printStackTrace();
        }

    }
}
