package com.bocse.perfume.data;

import java.util.List;

/**
 * Created by bogdan.bocse on 4/11/2016.
 */
public class PerfumeSLD {

    private String name;
    private String brand;
    private String searchableName;
    private String holding;
    private String pictureURL;
    private String perfumer;
    private String bottler;
    private String manufacturer;

    private Integer year;
    private Integer country;
    private Boolean inProduction;


    private String classification;
    private String structureType;

    private Gender gender;

    private List<String> topNotes;
    private List<String> heartNotes;
    private List<String> baseNotes;
    private List<String> mixedNotes;

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

    public String getHolding() {
        return holding;
    }

    public void setHolding(String holding) {
        this.holding = holding;
    }

    public String getPictureURL() {
        return pictureURL;
    }

    public void setPictureURL(String pictureURL) {
        this.pictureURL = pictureURL;
    }

    public String getPerfumer() {
        return perfumer;
    }

    public void setPerfumer(String perfumer) {
        this.perfumer = perfumer;
    }

    public String getBottler() {
        return bottler;
    }

    public void setBottler(String bottler) {
        this.bottler = bottler;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Integer getCountry() {
        return country;
    }

    public void setCountry(Integer country) {
        this.country = country;
    }

    public Boolean getInProduction() {
        return inProduction;
    }

    public void setInProduction(Boolean inProduction) {
        this.inProduction = inProduction;
    }

    public String getClassification() {
        return classification;
    }

    public void setClassification(String classification) {
        this.classification = classification;
    }

    public String getStructureType() {
        return structureType;
    }

    public void setStructureType(String structureType) {
        this.structureType = structureType;
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

    public String getSearchableName() {
        return searchableName;
    }

    public void setSearchableName(String searchableName) {
        this.searchableName = searchableName;
    }

    public List<String> getMixedNotes() {
        return mixedNotes;
    }

    public void setMixedNotes(List<String> mixedNotes) {
        this.mixedNotes = mixedNotes;
    }
}
