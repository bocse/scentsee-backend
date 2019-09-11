package com.bocse.perfume.affiliate;

import com.bocse.perfume.data.AffiliatePerfume;
import com.bocse.perfume.data.Gender;
import com.bocse.perfume.utils.TextUtils;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.ICsvListReader;
import org.supercsv.prefs.CsvPreference;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by bocse on 05.12.2015.
 */
public class AffiliateAoroRo extends Affiliate implements AffiliateInterface {

    private final static Logger logger = Logger.getLogger(AffiliateAoroRo.class.toString());
    private final static int trimSize = 3;
    private final static List<String> blackList = new ArrayList<>();
    private final String affiliateName = "aoro";

    public AffiliateAoroRo() {

        initBlackList();
    }

    private void initBlackList() {
        blackList.add("odorizant camera");
        blackList.add("odorizant de camera");
        blackList.add("spray pentru camera");
        blackList.add("ulei parfumat");
        blackList.add("sapun parfumat");
        blackList.add("fard ochi");
        blackList.add("spray pentru corp");
        blackList.add("gel de dus");
        blackList.add("gel de dus");
        blackList.add("crema de corp");
        blackList.add("lapte de corp");
        blackList.add("lumanari parfumate");
        blackList.add("deostick");
        blackList.add("perie de dinti");
        blackList.add("after shave");
        blackList.add("deospray");
        blackList.add("deodorant");
        blackList.add("roll-on");
        blackList.add("aroma difuzor");
        blackList.add("creion pentru sprancene");
        blackList.add("fard de obraz");
        blackList.add("balsam de buze");
        blackList.add("balsam intens pentru buze");
        blackList.add("set cosmetice");
        blackList.add("sclipici cosmetic");
        blackList.add("particule minerale");
        blackList.add("creion dermatograf");
        blackList.add("baza pentru machiaj");
        blackList.add("gel pentru sprancene");
        blackList.add("farduri de ochi");
    }

    private ICsvListReader getListReaderFromFile(String filename) throws FileNotFoundException {
        ICsvListReader listReader = null;
        listReader = new CsvListReader(new FileReader(filename), CsvPreference.TAB_PREFERENCE);
        return listReader;
    }




    public void readProductsFromURL(String url, String username, String password) throws IOException, InterruptedException {
        readProductsFromCSV(Common.getListReaderFromHTTP(url, username, password));
    }

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
                    String[] brandName = fields.get(5).trim().split(",");
                    affiliatePerfume.setName(brandName[1].trim());
                    affiliatePerfume.setBrand(brandName[0].trim());
                    affiliatePerfume.setVendor(affiliateName);
                    affiliatePerfume.setCurrency(fields.get(12));
                    affiliatePerfume.setPrice(Float.valueOf(fields.get(14)));
                    affiliatePerfume.setAffiliateURL(fields.get(17));
                    affiliatePerfume.setPhotoURL(fields.get(19));

                    String extendedName = fields.get(6).toLowerCase();

                    Boolean blacklisted = false;
                    for (String blacklistItem : blackList) {
                        if (extendedName.contains(blacklistItem)) {
                            //logger.info("Blacklisted: "+extendedName);
                            blacklistCount++;
                            blacklisted = true;
                            break;
                        }
                    }
                    if (blacklisted)
                        continue;
                    Double quantity = extraQuantity(fields.get(4));
                    if (quantity == null)
                        continue;

                    if (extendedName.contains("pentru barbati")) {
                        affiliatePerfume.setGender(Gender.MALE);
                    } else if (
                            extendedName.contains("pentru femei")) {
                        affiliatePerfume.setGender(Gender.FEMALE);
                    } else if (extendedName.contains("unisex")) {
                        affiliatePerfume.setGender(Gender.UNI);
                    } else if (extendedName.contains("femme") || extendedName.contains(" women ") || extendedName.contains(" woman ")) {
                        affiliatePerfume.setGender(Gender.FEMALE);
                    } else if (extendedName.contains("homme") || extendedName.contains(" men ") || extendedName.contains(" man ")) {
                        affiliatePerfume.setGender(Gender.MALE);
                    } else {

                        continue;
                    }
                    affiliatePerfume.setQuantity(quantity);

                    String searchableBrand = this.cutString(TextUtils.cleanupAndFlatten(affiliatePerfume.getBrand()));
                    if (!brandMap.containsKey(searchableBrand)) {
                        brandMap.put(searchableBrand, new ArrayList<>());
                    }
                    brandMap.get(searchableBrand).add(affiliatePerfume);

                    products.add(affiliatePerfume);

                    /*
                    if (perfumeList!=null)
                        matchedPerfume = this.lookupSlow(affiliatePerfume);
                    if (matchedPerfume!=null)
                    {
                        if (!perfumeProductMap.containsKey(matchedPerfume))
                        {
                            perfumeProductMap.put(matchedPerfume, new ArrayList<AffiliatePerfume>());
                        }
                        perfumeProductMap.get(matchedPerfume).add(affiliatePerfume);

                    }
                    */
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
        }
    }

    private String cutString(String s) {
        return s.substring(0, Math.min(s.length(), trimSize));
    }
    /*
    @Override
    public List<AffiliatePerfume> lookup(Perfume perfume) {
        List<AffiliatePerfume> result = new ArrayList<>();
        String brand=TextUtils.cleanupAndFlatten(perfume.getBrand().toLowerCase());
        String name=TextUtils.cleanupAndFlatten(perfume.getName().toLowerCase());
        List<AffiliatePerfume> candidates=brandMap.get(cutString(brand));
        if (candidates!=null) {
            for (AffiliatePerfume affiliatePerfume : candidates) {
                String affiliateName = affiliatePerfume.getSearchableName();
                if (affiliateName.contains(brand) && affiliateName.contains(name)) {
                    result.add(affiliatePerfume);
                }
            }
        }
        return result;
    }
    */

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

    public String getAffiliateName() {
        return affiliateName;
    }

    @Override
    public void readProductsFromFile(String filename) throws IOException {

    }

    @Override
    public void readProductsFromByteArray(byte[] contentBytes) throws IOException {

    }

    /*
    private Perfume lookupSlow(AffiliatePerfume affiliatePerfume) {
        String name = TextUtils.flattenToAscii(TextUtils.removeNonLiterals(affiliatePerfume.getName().toLowerCase()));
        Set<Perfume> shortList=new HashSet<>();

        for (Perfume perfume : perfumeList) {
            if (affiliatePerfume.getGender().equals(perfume.getGender()) && name.contains(TextUtils.flattenToAscii(TextUtils.removeNonLiterals(perfume.getBrand().toLowerCase()))) &&
                    name.contains(TextUtils.flattenToAscii(TextUtils.removeNonLiterals(perfume.getName().toLowerCase())))
                    )
                return perfume;
        }
        return null;
    }
    */
}
