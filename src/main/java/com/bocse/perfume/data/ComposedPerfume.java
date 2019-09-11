package com.bocse.perfume.data;

import org.jsondoc.core.annotation.ApiObject;

import java.util.List;
import java.util.Map;

/**
 * Created by bocse on 30.11.2015.
 */
@ApiObject(name = "RecommendedPerfume", description = "Defines a perfume entity as composed by the user, with the essences chosen by the user and recommended by ScentSee.")
public class ComposedPerfume {
    private String name;
    private List<String> suggestedNames;
    private Gender gender;
    private String description;
    private String rawMaterialsVendorName;
    private Long id;

    private Map<String, Double> topChosenNotes;
    private Map<String, Double> heartChosenNotes;
    private Map<String, Double> baseChosenNotes;

    private Map<String, Double> topSuggestedNotes;
    private Map<String, Double> heartSuggestedNotes;
    private Map<String, Double> baseSuggestedNotes;

    private Map<String, String> originatingAnswers;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Map<String, Double> getTopChosenNotes() {
        return topChosenNotes;
    }

    public void setTopChosenNotes(Map<String, Double> topChosenNotes) {
        this.topChosenNotes = topChosenNotes;
    }

    public Map<String, Double> getHeartChosenNotes() {
        return heartChosenNotes;
    }

    public void setHeartChosenNotes(Map<String, Double> heartChosenNotes) {
        this.heartChosenNotes = heartChosenNotes;
    }

    public Map<String, Double> getBaseChosenNotes() {
        return baseChosenNotes;
    }

    public void setBaseChosenNotes(Map<String, Double> baseChosenNotes) {
        this.baseChosenNotes = baseChosenNotes;
    }

    public Map<String, Double> getTopSuggestedNotes() {
        return topSuggestedNotes;
    }

    public void setTopSuggestedNotes(Map<String, Double> topSuggestedNotes) {
        this.topSuggestedNotes = topSuggestedNotes;
    }

    public Map<String, Double> getHeartSuggestedNotes() {
        return heartSuggestedNotes;
    }

    public void setHeartSuggestedNotes(Map<String, Double> heartSuggestedNotes) {
        this.heartSuggestedNotes = heartSuggestedNotes;
    }

    public Map<String, Double> getBaseSuggestedNotes() {
        return baseSuggestedNotes;
    }

    public void setBaseSuggestedNotes(Map<String, Double> baseSuggestedNotes) {
        this.baseSuggestedNotes = baseSuggestedNotes;
    }

    public Map<String, String> getOriginatingAnswers() {
        return originatingAnswers;
    }

    public void setOriginatingAnswers(Map<String, String> originatingAnswers) {
        this.originatingAnswers = originatingAnswers;
    }

    public String getRawMaterialsVendorName() {
        return rawMaterialsVendorName;
    }

    public void setRawMaterialsVendorName(String rawMaterialsVendorName) {
        this.rawMaterialsVendorName = rawMaterialsVendorName;
    }

    public List<String> getSuggestedNames() {
        return suggestedNames;
    }

    public void setSuggestedNames(List<String> suggestedNames) {
        this.suggestedNames = suggestedNames;
    }
}
