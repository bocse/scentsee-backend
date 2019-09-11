package com.bocse.perfume.recommender;

import com.bocse.perfume.data.Gender;
import com.bocse.perfume.data.Perfume;

import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Created by bocse on 05.12.2015.
 */
public class RandomRecommender {
    List<Perfume> perfumeCollection;
    private boolean filterGender = true;
    private boolean filterIdentity = true;
    private Double power = 2.0;
    private Double noteWeight = 1.0;
    private Double signatureWeight = 1.0;

    public RandomRecommender(List<Perfume> perfumeCollection) {
        this.perfumeCollection = perfumeCollection;
    }

    public Double getPower() {
        return power;
    }

    public void setPower(Double power) {
        this.power = power;
    }

    public Double getNoteWeight() {
        return noteWeight;
    }

    public void setNoteWeight(Double noteWeight) {
        this.noteWeight = noteWeight;
    }

    public Double getSignatureWeight() {
        return signatureWeight;
    }

    public void setSignatureWeight(Double signatureWeight) {
        this.signatureWeight = signatureWeight;
    }

    public SortedMap<Double, Perfume> recommendByPerfume(List<Perfume> apriori, int maxRecommendations) {
        SortedMap<Double, Perfume> recommendations = new TreeMap<>(java.util.Collections.reverseOrder());
        SortedMap<Double, Perfume> recommendationsCropped = new TreeMap<>();
        Gender aprioriGender = Gender.UNI;
        for (Perfume perfume : apriori) {

            if (!perfume.getGender().equals(Gender.UNI)) {
                if (!aprioriGender.equals(Gender.UNI) && filterGender && !aprioriGender.equals(perfume.getGender())) {
                    throw new IllegalStateException("Cannot filter by gender while not all perfumes are for the same gender.");
                }
                aprioriGender = perfume.getGender();

            }

        }

        for (Perfume candidate : perfumeCollection) {
            if (!candidate.getGender().equals(Gender.UNI) && !aprioriGender.equals(Gender.UNI) && !aprioriGender.equals(candidate.getGender()))
                continue;
            if (apriori.contains(candidate))
                continue;
            if (candidate.getAffiliateProducts().size() > 0) {
                recommendations.put(Math.random(), candidate);
            }
        }

        int recommendationIndex = 0;
        for (Map.Entry<Double, Perfume> entry : recommendations.entrySet()) {

            recommendationsCropped.put(entry.getKey(), entry.getValue());
            recommendationIndex++;

            if (recommendationIndex > maxRecommendations)
                break;

        }
        return recommendationsCropped;
    }
}
