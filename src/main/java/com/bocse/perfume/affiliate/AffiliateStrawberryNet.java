package com.bocse.perfume.affiliate;

import com.bocse.perfume.data.AffiliatePerfume;
import com.bocse.perfume.data.Gender;
import com.bocse.perfume.requester.HttpRequester;
import com.bocse.perfume.utils.TextUtils;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.ICsvListReader;
import org.supercsv.prefs.CsvPreference;

import java.io.*;
import java.util.*;
import java.util.logging.Logger;

/**
 * Created by bocse on 05.12.2015.
 */
public class AffiliateStrawberryNet extends Affiliate implements AffiliateInterface {

    private final static Logger logger = Logger.getLogger(AffiliateStrawberryNet.class.toString());
    private final static int trimSize = 3;
    private static final CsvPreference COMMA = new CsvPreference.Builder('\0', ',', "\n").build();
    private final static Set<String> categorySet = new HashSet<>();
    private final static List<String> blackList = new ArrayList<>();
    private final String affiliateName = "strawberry";
    private Double exchangeUSDtoRON = 3.99;

    public AffiliateStrawberryNet() {
        initBlackList();
    }

    private void initBlackList() {
        blackList.add("after shave");
        blackList.add("body lotion");
        blackList.add("shower gel");
        blackList.add("deodorant stick");
//        blackList.add("");
//        blackList.add("");
//        blackList.add("");
//        blackList.add("");
//        27763
    }

    private ICsvListReader getListReaderFromFile(String filename) throws FileNotFoundException {
        ICsvListReader listReader = null;

        listReader = new CsvListReader(new FileReader(filename), COMMA);
        return listReader;
    }

    private ICsvListReader getListReaderFromHTTP(String url) throws IOException, InterruptedException {
        ICsvListReader listReader = null;
        logger.info("Downloading affiliate list from " + url);
        final HttpGet httpGet = new HttpGet(url);
        final RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(9000).setConnectTimeout(9000).setSocketTimeout(9000).build();
        httpGet.setConfig(requestConfig);
        Reader reader = new StringReader(HttpRequester.getInputStreamURL(httpGet, true));
        listReader = new CsvListReader(reader, COMMA);

        return listReader;
    }

    public void readProductsFromFile(String filename) throws IOException {
        readProductsFromCSV(getListReaderFromFile(filename));
    }

    public void readProductsFromURL(String url) throws IOException, InterruptedException {
        readProductsFromCSV(getListReaderFromHTTP(url));
    }

    @Override
    public void readProductsFromCSV(ICsvListReader listReader) throws IOException {
        logger.info("Parsing affiliate products");
        int blacklistCount = 0;
        products = new ArrayList<>();
        brandMap = new HashMap<>();
        perfumeProductMap = new HashMap<>();
        //ICsvListReader listReader = null;
        try {
            //listReader = new CsvListReader(new FileReader(filename), CsvPreference.TAB_PREFERENCE);

            String[] header = listReader.getHeader(true); // skip the header (can't be used with CsvListReader)
            //final CellProcessor[] processors = getProcessors();

            List<String> fields;
            while ((fields = listReader.read()) != null) {
                //if (totalProducts>1000 )
                //    break;
                try {
                    AffiliatePerfume affiliatePerfume = new AffiliatePerfume();
                    //0 - name (brand - name - volEU/volUS)
                    //1 - image
                    //2 - price USD
                    //3 - category
                    //4 - some id
                    //5 - affiliate URL
                    //6 - description
                    String[] brandName = fields.get(0).trim().split("-");
                    affiliatePerfume.setName(brandName[1].trim());
                    affiliatePerfume.setBrand(brandName[0].trim());
                    affiliatePerfume.setVendor(this.affiliateName);
                    affiliatePerfume.setCurrency("RON");
                    affiliatePerfume.setPrice((float) Math.ceil(Float.valueOf(fields.get(2)) * exchangeUSDtoRON));
                    affiliatePerfume.setAffiliateURL(fields.get(5));
                    affiliatePerfume.setPhotoURL(fields.get(1));

                    String category = fields.get(3).toLowerCase();
                    categorySet.add(category);
                    if (category.contains("fragrance")) {
                        if (category.contains("women")) {
                            affiliatePerfume.setGender(Gender.FEMALE);
                        } else if (category.contains("men")) {
                            affiliatePerfume.setGender(Gender.MALE);
                        } else if (category.contains("uni")) {
                            affiliatePerfume.setGender(Gender.UNI);
                        } else {
                            blacklistCount++;
                            continue;
                        }

                    } else {
                        blacklistCount++;
                        continue;
                    }
                    Boolean blacklisted = false;
                    for (String blacklistItem : blackList) {
                        if (affiliatePerfume.getName().toLowerCase().contains(blacklistItem)) {
                            //logger.info("Blacklisted: "+extendedName);
                            blacklistCount++;
                            blacklisted = true;
                            break;
                        }
                    }
                    if (blacklisted)
                        continue;
                    Double quantity = extraQuantity(brandName[2]);
                    if (quantity == null) {
                        blacklistCount++;
                        continue;
                    }
                    affiliatePerfume.setQuantity(quantity);

                    String searchableBrand = this.cutString(TextUtils.cleanupAndFlatten(affiliatePerfume.getBrand()));
                    if (!brandMap.containsKey(searchableBrand)) {
                        brandMap.put(searchableBrand, new ArrayList<>());
                    }
                    brandMap.get(searchableBrand).add(affiliatePerfume);

                    products.add(affiliatePerfume);

                } catch (NumberFormatException nfex) {
                    logger.warning("Failed to load element " + fields.toString());
                }
                //logger.info(fields.toArray().toString());
            }
            computeInvariants();
        } finally {
            if (listReader != null) {
                listReader.close();
            }
            logger.info("Products that are filtered out: " + blacklistCount);
            logger.info("Loaded affiliate products: " + products.size());
            logger.info("Categories: \t" + categorySet.toString());
        }

    }

    private String cutString(String s) {
        return s.substring(0, Math.min(s.length(), trimSize));
    }

    private Double extraQuantity(String description) {
        description = description.trim();
        if (description.contains("ml/")) {
            description = description.substring(0, description.indexOf("ml/"));
        } else {
            return null;
        }
        try {
            return Double.valueOf(description);
        } catch (NumberFormatException nfex) {
            return null;
        }

    }

    public String getAffiliateName() {
        return affiliateName;
    }

}
