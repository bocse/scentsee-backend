package com.bocse.perfume.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by bocse on 30.11.2015.
 */
public class Perfume {
    private String name;
    private String brand;
    private Integer year = null;
    private String url;
    private String pictureURL;
    private Gender gender;
    private String description;
    private Long id;
    private Map<String, List<AffiliatePerfume>> affiliateProducts = new ConcurrentHashMap<>(8, 0.5F, 4);
    private Boolean inProduction = true;
    private String searchableName;

    private List<String> topNotes;
    private List<String> heartNotes;
    private List<String> baseNotes;
    private List<String> mixedNotes;

    private Map<NoteType, Double> topSignature;
    private Map<NoteType, Double> heartSignature;
    private Map<NoteType, Double> baseSignature;
    private Map<NoteType, Double> mixedSignature;

    private boolean substandard = false;
    private Double scentR = 0.0;
    private Integer scentP = 0;
    private Double sillageR = 0.0;
    private Integer sillageP = 0;
    private Double longevityR = 0.0;
    private Integer longevityP = 0;
    private Double bottleR = 0.0;
    private Integer bottleP = 0;

    public Double getScentR() {
        return scentR;
    }

    public void setScentR(Double scentR) {
        this.scentR = scentR;
    }

    public Double getSillageR() {
        return sillageR;
    }

    public void setSillageR(Double sillageR) {
        this.sillageR = sillageR;
    }

    public Double getLongevityR() {
        return longevityR;
    }

    public void setLongevityR(Double longevityR) {
        this.longevityR = longevityR;
    }

    public Double getBottleR() {
        return bottleR;
    }

    public void setBottleR(Double bottleR) {
        this.bottleR = bottleR;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public Boolean getInProduction() {
        return inProduction;
    }

    public void setInProduction(Boolean inProduction) {
        this.inProduction = inProduction;
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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        //this.id = TextUtils.hash(url);
        this.url = url;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public List<String> getTopNotes() {
        return topNotes;
    }

    public void setTopNotes(List<String> topNotes) {
        this.topNotes = topNotes;
    }

    public List<String> getHeartNotes() {
        return heartNotes;
    }

    public void setHeartNotes(List<String> heartNotes) {
        this.heartNotes = heartNotes;
    }

    public List<String> getBaseNotes() {
        return baseNotes;
    }

    public void setBaseNotes(List<String> baseNotes) {
        this.baseNotes = baseNotes;
    }

    public List<String> getMixedNotes() {

        if (baseNotes == null || baseNotes.size() == 0)
            return mixedNotes;
        else {
            List<String> allNotes = new ArrayList<>(baseNotes.size() + heartNotes.size() + topNotes.size());
            allNotes.addAll(topNotes);
            allNotes.addAll(heartNotes);
            allNotes.addAll(baseNotes);
            return allNotes;
        }
    }

    public void setMixedNotes(List<String> mixedNotes) {
        this.mixedNotes = mixedNotes;
    }

    public boolean isSubstandard() {
        return substandard;
    }

    public void setSubstandard(boolean substandard) {
        this.substandard = substandard;
    }

    public Map<NoteType, Double> getTopSignature() {
        return topSignature;
    }

    public void setTopSignature(Map<NoteType, Double> topSignature) {
        this.topSignature = topSignature;
    }

    public Map<NoteType, Double> getHeartSignature() {
        return heartSignature;
    }

    public void setHeartSignature(Map<NoteType, Double> heartSignature) {
        this.heartSignature = heartSignature;
    }

    public Map<NoteType, Double> getBaseSignature() {
        return baseSignature;
    }

    public void setBaseSignature(Map<NoteType, Double> baseSignature) {
        this.baseSignature = baseSignature;
    }

    public Map<NoteType, Double> getMixedSignature() {
        return mixedSignature;
    }

    public void setMixedSignature(Map<NoteType, Double> mixedSignature) {
        this.mixedSignature = mixedSignature;
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

    public String getSearchableName() {
        return searchableName;
    }

    public void setSearchableName(String searchableName) {
        this.searchableName = searchableName;
    }


    public Integer getScentP() {
        return scentP;
    }

    public void setScentP(Integer scentP) {
        this.scentP = scentP;
    }


    public Integer getSillageP() {
        return sillageP;
    }

    public void setSillageP(Integer sillageP) {
        this.sillageP = sillageP;
    }


    public Integer getLongevityP() {
        return longevityP;
    }

    public void setLongevityP(Integer longevityP) {
        this.longevityP = longevityP;
    }


    public Integer getBottleP() {
        return bottleP;
    }

    public void setBottleP(Integer bottleP) {
        this.bottleP = bottleP;
    }

    public Map<String, List<AffiliatePerfume>> getAffiliateProducts() {
        return affiliateProducts;
    }

    public void setAffiliateProducts(Map<String, List<AffiliatePerfume>> affiliateProducts) {
        this.affiliateProducts = affiliateProducts;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Perfume perfume = (Perfume) o;

        if (!id.equals(perfume.id)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
