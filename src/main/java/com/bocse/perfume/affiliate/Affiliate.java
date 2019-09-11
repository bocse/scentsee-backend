package com.bocse.perfume.affiliate;

import com.bocse.perfume.data.AffiliatePerfume;
import com.bocse.perfume.data.Gender;
import com.bocse.perfume.data.Perfume;
import com.bocse.perfume.utils.TextUtils;
import org.apache.commons.lang3.StringUtils;
import org.supercsv.io.ICsvListReader;

import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

/**
 * Created by bocse on 14.02.2016.
 */
public abstract class Affiliate implements AffiliateInterface {
    private final static Logger logger = Logger.getLogger(Affiliate.class.toString());
    public int errorCount = 0;
    protected boolean enableLevenshtein = false;
    protected int levenshteinTolerance = 4;
    protected boolean disableGenderFilter = false;
    protected List<AffiliatePerfume> products;
    protected Map<Perfume, List<AffiliatePerfume>> perfumeProductMap = null;
    protected Map<String, List<AffiliatePerfume>> brandMap = null;
    protected Map<AffiliatePerfume, String> brandFlattened;
    protected Map<AffiliatePerfume, String> brandFlattenedNoSpaces;
    protected Map<AffiliatePerfume, String> brandFlattenedUV;
    protected Map<AffiliatePerfume, String> nameFlattened;
    protected Map<AffiliatePerfume, String> nameFlattenedNoSpaces;
    protected Map<AffiliatePerfume, String> nameFlattenedUV;

    public List<AffiliatePerfume> getAllAffiliatePerfumes() {
        return products;
    }

    public void computeInvariants() {
        brandFlattenedNoSpaces = new HashMap<>();
        brandFlattened = new HashMap<>();
        brandFlattenedUV = new HashMap<>();
        nameFlattened = new HashMap<>();
        nameFlattenedNoSpaces = new HashMap<>();
        nameFlattenedUV = new HashMap<>();
        for (AffiliatePerfume affiliatePerfume : products) {
            brandFlattened.put(affiliatePerfume, TextUtils.cleanupAndFlatten(affiliatePerfume.getBrand()).toLowerCase());
            brandFlattenedNoSpaces.put(affiliatePerfume, TextUtils.cleanupAndFlatten(affiliatePerfume.getBrand()).replaceAll(" ", "").toLowerCase());
            brandFlattenedUV.put(affiliatePerfume, TextUtils.cleanupAndFlatten(affiliatePerfume.getBrand()).toLowerCase().replaceAll("u", "v").replaceAll("aqva", "acqva").replaceAll("blvd", "boulevard").replaceAll("schoen", "schon").replaceAll("colonie", "cologne"));
            nameFlattened.put(affiliatePerfume, TextUtils.cleanupAndFlatten(affiliatePerfume.getName()).toLowerCase());
            nameFlattenedNoSpaces.put(affiliatePerfume, TextUtils.cleanupAndFlatten(affiliatePerfume.getName()).replaceAll(" ", "").toLowerCase());
            nameFlattenedUV.put(affiliatePerfume, TextUtils.cleanupAndFlatten(affiliatePerfume.getName()).toLowerCase().replaceAll("u", "v").replaceAll("aqva", "acqva").replaceAll("blvd", "boulevard").replaceAll("schoen", "schon").replaceAll("colonie", "cologne"));
        }
    }

    private List<AffiliatePerfume> culling(List<AffiliatePerfume> fullResults, Perfume perfume) {
        String brand = TextUtils.cleanupAndFlatten(perfume.getBrand().toLowerCase()).trim();
        String name = TextUtils.cleanupAndFlatten(perfume.getName().replaceAll("\\(.*\\)", "").toLowerCase()).trim();
        Map<AffiliatePerfume, Integer> affiliateDistance = new HashMap<>();
        List<AffiliatePerfume> resultingList = new ArrayList<>();

        int distance = Integer.MAX_VALUE;
        for (AffiliatePerfume affiliatePerfume : fullResults) {
            int localDistance = StringUtils.getLevenshteinDistance(brand, brandFlattened.get(affiliatePerfume)) +
                    StringUtils.getLevenshteinDistance(name, nameFlattened.get(affiliatePerfume));
            affiliateDistance.put(affiliatePerfume, localDistance);
            if (localDistance < distance) {
                distance = localDistance;
            }
        }

        for (Map.Entry<AffiliatePerfume, Integer> affiliateDistanceEntry : affiliateDistance.entrySet()) {
            if (affiliateDistanceEntry.getValue().equals(distance)) {
                resultingList.add(affiliateDistanceEntry.getKey());

            }
        }
        return resultingList;
    }
    public List<AffiliatePerfume> lookup(Perfume perfume) {
        List<AffiliatePerfume> result = new ArrayList<>();
        String brand = TextUtils.cleanupAndFlatten(perfume.getBrand().toLowerCase()).trim();
        String name = TextUtils.cleanupAndFlatten(perfume.getName().replaceAll("\\(.*\\)", "").toLowerCase()).trim();
        String brandUV = brand.replaceAll("u", "v").replaceAll("aqva", "acqva").replaceAll("blvd", "boulevard").replaceAll("schoen", "schon").replaceAll("colonie", "cologne");
        String nameUV = name.replaceAll("u", "v").replaceAll("aqva", "acqva").replaceAll("blvd", "boulevard").replaceAll("schoen", "schon").replaceAll("colonie", "cologne");
        Gender perfumeGender = perfume.getGender();
        for (AffiliatePerfume affiliatePerfume : products) {
            if (!disableGenderFilter)
            if (!affiliatePerfume.getGender().equals(perfume.getGender()) && !affiliatePerfume.getGender().equals(Gender.UNI) && !perfumeGender.equals(Gender.UNI))
                continue;
            String affiliateSearchableName = affiliatePerfume.getSearchableName();
            String affiliateBrand = brandFlattened.get(affiliatePerfume);
            String affiliateName = nameFlattened.get(affiliatePerfume);
            if ((affiliateBrand.contains(brand) || brand.contains(affiliateBrand))
                    && (affiliateName.contains(name) || name.contains(affiliateName))) {

                //TODO: add additional validation for whole words
                result.add(affiliatePerfume);
            }
        }
        if (result.size() == 0) {
            String searchableName = perfume.getSearchableName();
            for (AffiliatePerfume affiliatePerfume : products) {
                if (!disableGenderFilter)
                if (!affiliatePerfume.getGender().equals(perfume.getGender()) && !affiliatePerfume.getGender().equals(Gender.UNI) && !perfumeGender.equals(Gender.UNI))
                    continue;

                if (searchableName.contains(brandFlattened.get(affiliatePerfume)) && searchableName.contains(nameFlattened.get(affiliatePerfume))) {
                    result.add(affiliatePerfume);
                }
            }

        }

        if (result.size() == 0) {
            String searchableName = TextUtils.cleanupAndFlatten(perfume.getBrand()).toLowerCase().replaceAll(" ", "") + " " +
                    TextUtils.cleanupAndFlatten(perfume.getName()).toLowerCase().replaceAll(" ", "");
            for (AffiliatePerfume affiliatePerfume : products) {
                if (!disableGenderFilter)
                if (!affiliatePerfume.getGender().equals(perfume.getGender()) && !affiliatePerfume.getGender().equals(Gender.UNI) && !perfumeGender.equals(Gender.UNI))
                    continue;

                if (searchableName.contains(brandFlattenedNoSpaces.get(affiliatePerfume)) && searchableName.contains(nameFlattenedNoSpaces.get(affiliatePerfume))) {
                    result.add(affiliatePerfume);
                }
            }

        }
        if (result.size() == 0) {
            for (AffiliatePerfume affiliatePerfume : products) {
                if (!disableGenderFilter)
                if (!affiliatePerfume.getGender().equals(perfume.getGender()) && !affiliatePerfume.getGender().equals(Gender.UNI) && !perfumeGender.equals(Gender.UNI))
                    continue;
                String affiliateBrandUV = brandFlattenedUV.get(affiliatePerfume);
                String affiliateNameUV = nameFlattenedUV.get(affiliatePerfume);
                if ((affiliateBrandUV.contains(brandUV) || brandUV.contains(affiliateBrandUV))
                        && (affiliateNameUV.contains(nameUV) || nameUV.contains(affiliateNameUV))) {

                    //TODO: add additional validation for whole words
                    result.add(affiliatePerfume);
                }
            }
        }
        if (result.size() == 0) {
            for (AffiliatePerfume affiliatePerfume : products) {
                if (!affiliatePerfume.getGender().equals(perfume.getGender()) && !affiliatePerfume.getGender().equals(Gender.UNI) && !perfumeGender.equals(Gender.UNI))
                    continue;
                String affiliateBrandUV = brandFlattenedUV.get(affiliatePerfume);
                String affiliateNameUV = nameFlattenedUV.get(affiliatePerfume);
                if ((StringUtils.getLevenshteinDistance(affiliateBrandUV, brandUV) <= Math.min(levenshteinTolerance, brandUV.length() / 2 + 1))
                        && (StringUtils.getLevenshteinDistance(affiliateNameUV, nameUV) <= Math.min(levenshteinTolerance, nameUV.length() / 2 + 1))) {

                    //TODO: add additional validation for whole words
                    result.add(affiliatePerfume);
                }
            }
        }

        result = culling(result, perfume);

        result.sort(new Comparator<AffiliatePerfume>() {
            @Override
            public int compare(AffiliatePerfume o1, AffiliatePerfume o2) {
                return -o1.getPrice().compareTo(o2.getPrice());
            }
        });

        return result;
    }

    public void readProductsFromFile(String filename) throws IOException {
        readProductsFromCSV(Common.getListReaderFromFile(filename));
    }

    public void readProductsFromByteArray(byte[] contentBytes) throws IOException {
        readProductsFromCSV(Common.getListReaderByteArray(contentBytes));
    }


    public abstract void readProductsFromCSV(ICsvListReader listReader) throws IOException;
}
