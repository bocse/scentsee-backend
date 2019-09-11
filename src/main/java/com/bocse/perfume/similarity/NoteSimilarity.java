package com.bocse.perfume.similarity;

import com.bocse.perfume.data.Perfume;
import com.bocse.perfume.statistical.InterfaceFrequencyAnalysis;
import com.bocse.perfume.utils.MathUtils;

import java.util.*;

/**
 * Created by bocse on 05.12.2015.
 */
public class NoteSimilarity {
    private InterfaceFrequencyAnalysis frequencyAnalysis;
    private Double partialMatchEnds = 0.5;
    private Double partialMatchContains = 0.1;

    private Double power = 2.0;
    private Double topWeight = 1.0;
    private Double heartWeight = 0.75;
    private Double baseWeight = 2.0 / 3.0;
    private Double mixedWeight = 0.5;
    private Double mixedExponentialPenalty = 2.0;

    public NoteSimilarity(InterfaceFrequencyAnalysis frequencyAnalysis) {
        this.frequencyAnalysis = frequencyAnalysis;
    }

    public Double getMixedExponentialPenalty() {
        return mixedExponentialPenalty;
    }

    public void setMixedExponentialPenalty(Double mixedExponentialPenalty) {
        this.mixedExponentialPenalty = mixedExponentialPenalty;
    }

    public Double getTopWeight() {
        return topWeight;
    }

    public void setTopWeight(Double topWeight) {
        this.topWeight = topWeight;
    }

    public Double getHeartWeight() {
        return heartWeight;
    }

    public void setHeartWeight(Double heartWeight) {
        this.heartWeight = heartWeight;
    }

    public Double getBaseWeight() {
        return baseWeight;
    }

    public void setBaseWeight(Double baseWeight) {
        this.baseWeight = baseWeight;
    }

    public Double getMixedWeight() {
        return mixedWeight;
    }

    public void setMixedWeight(Double mixedWeight) {
        this.mixedWeight = mixedWeight;
    }

    public Double getPartialMatchContains() {
        return partialMatchContains;
    }

    public void setPartialMatchContains(Double partialMatchContains) {
        this.partialMatchContains = partialMatchContains;
    }

    public Double getPower() {
        return power;
    }

    public void setPower(Double power) {
        this.power = power;
    }

    public Double getPartialMatchEnds() {
        return partialMatchEnds;
    }

    public void setPartialMatchEnds(Double partialMatchEnds) {
        this.partialMatchEnds = partialMatchEnds;
    }

    public double stringOverlap(String string1, String string2) {
        if (string1 == null || string2 == null)
            return 0.0;

        String[] parts1Original = string1.toLowerCase().split(" ");
        String[] parts2Original = string2.toLowerCase().split(" ");
        if (parts1Original.length == 0 || parts2Original.length == 0)
            return 0.0;
        Set<String> parts1Set = new HashSet<String>(Arrays.asList(parts1Original));
        Set<String> parts2Set = new HashSet<String>(Arrays.asList(parts2Original));
        List<String> parts1 = new ArrayList<>(parts1Set);
        List<String> parts2 = new ArrayList<>(parts2Set);
        if (parts1.isEmpty() || parts2.isEmpty())
            return 0.0;

        Double matchCounter = 0.0;
        for (int i = 0; i < parts1.size(); i++) {
            String part1Element = parts1.get(i);
            if (part1Element.isEmpty())
                continue;
            for (int j = 0; j < parts2.size(); j++) {
                String part2Element = parts2.get(j);
                if (part2Element.isEmpty())
                    continue;
                if (part1Element.equals(part2Element)) {
                    matchCounter += 1.0;
                } else if (part1Element.startsWith(part2Element)) {
                    matchCounter += partialMatchEnds;
                } else if (part2Element.startsWith(part1Element)) {
                    matchCounter += partialMatchEnds;
                } else if (part2Element.endsWith(part1Element)) {
                    matchCounter += partialMatchEnds;
                } else if (part1Element.endsWith(part2Element)) {
                    matchCounter += partialMatchEnds;
                } else if (part1Element.contains(part2Element)) {
                    matchCounter += partialMatchContains;
                } else if (part2Element.contains(part1Element)) {
                    matchCounter += partialMatchContains;
                }
            }
        }
        if (parts1.size() + parts2.size() > 0)
            return 2.0 * matchCounter / (parts1.size() + parts2.size());
        else
            return 0.0;
    }

    //TODO: Refactor using correlation (Pearson)
    private Double noteSimilarity(List<String> notes1, List<String> notes2) {
        Double similarity = 0.0;
        Double raritySum1 = 0.0;
        Double raritySum2 = 0.0;

        if (notes1.size() == 0 || notes2.size() == 0)
            return 0.0;
        for (int j = 0; j < notes2.size(); j++) {
            String note2 = notes2.get(j);
            Double rarity2 = frequencyAnalysis.getMixedNoteRarity(note2);
            raritySum2 += rarity2;
        }
        for (int i = 0; i < notes1.size(); i++) {
            String note1 = notes1.get(i);
            Double rarity1 = frequencyAnalysis.getMixedNoteRarity(note1);
            raritySum1 += rarity1;
            for (int j = 0; j < notes2.size(); j++) {
                String note2 = notes2.get(j);
                Double rarity2 = frequencyAnalysis.getMixedNoteRarity(note2);
                Double overlapElement = stringOverlap(note1, note2);

                similarity += (rarity1 + rarity2) * overlapElement;
            }
        }
        //if (notes1.size() + notes2.size() > 0.0) {
        if (raritySum1 + raritySum2 > 0.0) {
            //return 2.0 * similarity / (notes1.size() + notes2.size());
            return similarity / (raritySum1 + raritySum2);
        } else {
            return 0.0;
        }
    }


    public Double getTopNoteSimilarity(Perfume perfume1, Perfume perfume2) {
        return this.noteSimilarity(perfume1.getTopNotes(), perfume2.getTopNotes());
    }

    public Double getHeartNoteSimilarity(Perfume perfume1, Perfume perfume2) {
        return this.noteSimilarity(perfume1.getHeartNotes(), perfume2.getHeartNotes());
    }

    public Double getBaseNoteSimilarity(Perfume perfume1, Perfume perfume2) {
        return this.noteSimilarity(perfume1.getBaseNotes(), perfume2.getBaseNotes());
    }

    public Double getMixedNoteSimilarity(Perfume perfume1, Perfume perfume2) {
        return this.noteSimilarity(perfume1.getMixedNotes(), perfume2.getMixedNotes());
    }

    public Double getBlendedNoteSimilarity(Perfume perfume1, Perfume perfume2) {
        //TODO: include penalty for badly classified parfumes
        double result = 0.0;
        if (perfume1.getTopNotes().size() > 0 && perfume1.getHeartNotes().size() > 0 && perfume1.getBaseNotes().size() > 0
                && perfume2.getTopNotes().size() > 0 && perfume2.getHeartNotes().size() > 0 && perfume2.getBaseNotes().size() > 0) {
            result = MathUtils.pow((
                    topWeight * MathUtils.pow(this.getTopNoteSimilarity(perfume1, perfume2), power)
                            + heartWeight * MathUtils.pow(this.getHeartNoteSimilarity(perfume1, perfume2), power)
                            + baseWeight * MathUtils.pow(this.getBaseNoteSimilarity(perfume1, perfume2), power)
                            + mixedWeight * MathUtils.pow(this.getMixedNoteSimilarity(perfume1, perfume2), power)
            ) / (topWeight + heartWeight + baseWeight + mixedWeight), 1.0 / power);
        } else {
            result = Math.pow(this.getMixedNoteSimilarity(perfume1, perfume2), mixedExponentialPenalty);
        }
        return result;
    }


}
