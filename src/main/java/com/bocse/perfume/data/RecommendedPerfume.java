package com.bocse.perfume.data;

import com.bocse.perfume.affiliate.AffiliateCollection;
import org.jsondoc.core.annotation.ApiObject;
import org.jsondoc.core.annotation.ApiObjectField;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by bocse on 30.11.2015.
 */
@ApiObject(name = "RecommendedPerfume", description = "Defines a perfume entity which is the result of either a search or recommendation query. May be linked to none, one or several vendors which actually have this perfume in stock.")
public class RecommendedPerfume implements Comparable<RecommendedPerfume> {
    @ApiObjectField(name = "name", description = "Name of the perfume (i.e. not the brand). Eg: J'adore")
    private String name;
    @ApiObjectField(name = "brand", description = "Brand to which the perfume belongs. Eg: Dior, Armani, Amouage")
    private String brand;
    @ApiObjectField(name = "year", description = "Year of release for the perfume")
    private Integer year = null;
    @ApiObjectField(name = "year", description = "URL of a photo of the perfume, as shown on a third-party source on the Internet. This resource is not guaranteed to be present.")
    private String pictureURL;

    @ApiObjectField(name = "gender", description = "Gender for which this perfume is designed (FEMALE, MALE, UNI)")
    private Gender gender;
    @ApiObjectField(name = "popularity", description = "Logarithmic-scale measure of perfumes popularity, determined by publicly available ratings, reviews and references")
    private Double popularity;
    @ApiObjectField(name = "id", description = "Numeric identifier of the perfume")
    private Long id;
    @ApiObjectField(name = "dominantClasses", description = "Dominant (most relevant) olfactive classes of the perfume. Can be displayed to the user.")
    private Map<NoteType, Double> dominantClasses;
    @ApiObjectField(name = "affiliateProducts", description = "Map of vendors offering this particular perfume")
    private Map<String, List<AffiliatePerfume>> affiliateProducts = new LinkedHashMap<>();
    @ApiObjectField(name = "matchRates", description = "Numeric representations of how well the perfume matches the user-query, based on various algorithms.")
    private Map<RecommendationAlgorithm, Double> matchRates;
    @ApiObjectField(name = "matchRate", description = "Numeric representation of how well the perfume matches the user-query, based on the algorithm used for sorting this particular query")
    private Double matchRate;
    @ApiObjectField(name = "metadata", description = "Additional details about the search process.")
    private String metadata;


    public RecommendedPerfume(Perfume perfume, AffiliateCollection affiliateCollection) {
        //TODO: accept affiliate ordered-mask
        setName(perfume.getName());
        setBrand(perfume.getBrand());
        setYear(perfume.getYear());
        setPictureURL(perfume.getPictureURL());
        setGender(perfume.getGender());
        setId(perfume.getId());
        setAffiliateProducts(perfume.getAffiliateProducts(), affiliateCollection);
        setPopularity(Math.log(1.0 + perfume.getBottleR() * perfume.getBottleP() + perfume.getSillageP() * perfume.getSillageR() + perfume.getLongevityP() * perfume.getLongevityR() + perfume.getScentP() + perfume.getScentR()));
        //setDominantClasses(dominantClasses(perfume));
        matchRates = new HashMap<>();
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }


    public String getPictureURL() {
        return pictureURL;
    }

    public void setPictureURL(String pictureURL) {
        this.pictureURL = pictureURL;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getMatchRate() {
        return matchRate;
    }

    public void setMatchRate(Double matchRate) {
        this.matchRate = matchRate;
    }

    public Map<RecommendationAlgorithm, Double> getMatchRates() {
        return matchRates;
    }

    public void setMatchRates(Map<RecommendationAlgorithm, Double> matchRates) {
        this.matchRates = matchRates;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }


    @Override
    public int compareTo(RecommendedPerfume o) {
        return this.matchRate.compareTo(o.getMatchRate());
    }


    public Map<NoteType, Double> getDominantClasses() {
        return dominantClasses;
    }

    public void setDominantClasses(Map<NoteType, Double> dominantClasses) {
        this.dominantClasses = dominantClasses;
    }

    public Map<String, List<AffiliatePerfume>> getAffiliateProducts() {
        return affiliateProducts;
    }

    public void setAffiliateProducts(Map<String, List<AffiliatePerfume>> affiliateProducts, AffiliateCollection affiliateCollection) {
        this.affiliateProducts.clear();
        if (affiliateCollection != null) {
            for (String affiliate : affiliateCollection.getAffiliates()) {
                if (affiliateProducts.containsKey(affiliate)) {
                    this.affiliateProducts.put(affiliate, affiliateProducts.get(affiliate));
                }
            }
        } else {
            this.affiliateProducts.putAll(affiliateProducts);
        }
    }

    public Double getPopularity() {
        return popularity;
    }

    public void setPopularity(Double popularity) {
        this.popularity = popularity;
    }
}
