package com.bocse.perfume.affiliate;

import com.bocse.perfume.data.AffiliatePerfume;
import com.bocse.perfume.data.Gender;
import com.bocse.perfume.requester.HttpRequester;
import com.bocse.perfume.utils.TextUtils;
import org.apache.http.client.methods.HttpGet;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.ICsvListReader;
import org.supercsv.prefs.CsvPreference;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by bocse on 05.12.2015.
 */
public class AffiliateParfumExpress extends Affiliate implements AffiliateInterface {

    private final static Logger logger = Logger.getLogger(AffiliateParfumExpress.class.toString());
    private final static int trimSize = 3;
    private static final CsvPreference COMMA = new CsvPreference.Builder('"', ',', "\n").build();
    private final static List<String> blackList = new ArrayList<>();
    //private final static Set<String> categorySet=new HashSet<>();
    private final String affiliateName = "parfumexpress";

    public AffiliateParfumExpress() {
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
//        final RequestConfig requestConfig = RequestConfig.custom()
//                .setConnectionRequestTimeout(9000).setConnectTimeout(9000).setSocketTimeout(9000).build();
//        httpGet.setConfig(requestConfig);
        Reader reader = new StringReader(HttpRequester.getInputStreamTLSURL(httpGet, true));
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
                    affiliatePerfume.setBrand(fields.get(1));
                    if (fields.get(2).length() > fields.get(1).length() + 1) {
                        affiliatePerfume.setName(fields.get(2).substring(fields.get(1).length() + 1, fields.get(2).length()));
                    } else {
                        affiliatePerfume.setName(fields.get(2));
                    }
                    affiliatePerfume.setCurrency("RON");
                    Float price = extractPrice(fields.get(5));
                    if (price == null) {
                        blacklistCount++;
                        continue;
                    }
                    affiliatePerfume.setPrice(price);

                    affiliatePerfume.setAffiliateURL(fields.get(6));
                    affiliatePerfume.setPhotoURL(fields.get(8));
                    affiliatePerfume.setVendor(affiliateName);
                    Double quantity = extraQuantity(fields.get(2));
                    if (quantity == null) {
                        blacklistCount++;
                        continue;
                    }
                    affiliatePerfume.setQuantity(quantity);
                    if (fields.get(2).contains("barbati")) {
                        affiliatePerfume.setGender(Gender.MALE);
                    } else if (fields.get(2).contains("femei")) {
                        affiliatePerfume.setGender(Gender.FEMALE);
                    } else if (fields.get(2).contains("unisex")) {
                        affiliatePerfume.setGender(Gender.UNI);
                    } else {
                        logger.info("Unknown gender" + fields.get(2));
                        blacklistCount++;
                        continue;
                    }

                    if (!fields.get(2).contains("EDP") && !fields.get(2).contains("EDT") && !fields.get(2).contains("EDC") && !fields.get(2).contains("Eau Fraiche") && !fields.get(2).contains("Eau De Cologne")) {
                        logger.info("Not EDT or EDP " + fields.get(2));
                        continue;
                    }
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
            //logger.info("Categories: \t"+categorySet.toString());
        }

    }

    private String cutString(String s) {
        return s.substring(0, Math.min(s.length(), trimSize));
    }

    private Double extraQuantity(String description) {
        String[] descriptionParts = description.split(" ");
        int partIndex = -1;
        int index = 0;
        for (index = descriptionParts.length - 1; index >= 0; index--) {

            if (descriptionParts[index].equals("ml")) {
                partIndex = index;
                break;
            }
        }
        if (partIndex > 0) {
            try {
                Double quantity = Double.valueOf(descriptionParts[partIndex - 1].replace(',', '.'));
                return quantity;
            } catch (NumberFormatException ex) {
                //logger.warning("Failed to parse quantity: "+description);
                return null;
            }
        } else {
            //logger.warning("Failed to parse quantity: "+description);
            return null;
        }
    }

    public Float extractPrice(String priceString) {
        int index = priceString.indexOf(' ');
        if (index > 0) {
            try {
                return Float.valueOf(priceString.substring(0, index));
            } catch (NumberFormatException nfex) {
                return null;
            }
        } else {
            try {
                return Float.valueOf(priceString.trim());
            } catch (NumberFormatException nfex) {
                return null;
            }
            //return null;
        }
    }

    public String getAffiliateName() {
        return affiliateName;
    }

}
