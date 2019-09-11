package com.bocse.perfume.similarity;

import com.bocse.perfume.data.NoteType;
import com.bocse.perfume.data.Perfume;
import com.bocse.perfume.data.PerfumeSegmentedSignature;
import com.bocse.perfume.statistical.InterfaceFrequencyAnalysis;
import com.bocse.perfume.utils.MathUtils;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by bocse on 05.12.2015.
 */
public class SignatureSimilarity {
    private static final Double epsilon = 1E-4;
    private InterfaceFrequencyAnalysis frequencyAnalysis;
    private Double power = 2.0;
    private Double topWeight = 1.0;
    private Double heartWeight = 0.75;
    private Double baseWeight = 2.0 / 3.0;
    private Double mixedWeight = 0.5;
    private Double mixedExponentialPenalty = 2.0;

    public SignatureSimilarity(InterfaceFrequencyAnalysis frequencyAnalysis) {
        this.frequencyAnalysis = frequencyAnalysis;
    }

    public Double getMixedExponentialPenalty() {
        return mixedExponentialPenalty;
    }

    public void setMixedExponentialPenalty(Double mixedExponentialPenalty) {
        this.mixedExponentialPenalty = mixedExponentialPenalty;
    }

    public Double getPower() {
        return power;
    }

    public void setPower(Double power) {
        this.power = power;
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

    public Double getTopSignatureSimilarity(Perfume perfume1, Perfume perfume2) {
        return this.signatureSimilarity(perfume1.getTopSignature(), perfume2.getTopSignature());
    }

    public Double getHeartSignatureSimilarity(Perfume perfume1, Perfume perfume2) {
        return this.signatureSimilarity(perfume1.getHeartSignature(), perfume2.getHeartSignature());
    }

    public Double getBaseSignatureSimilarity(Perfume perfume1, Perfume perfume2) {
        return this.signatureSimilarity(perfume1.getBaseSignature(), perfume2.getBaseSignature());
    }

    public Double getMixedSignatureSimilarity(Perfume perfume1, Perfume perfume2) {
        return this.signatureSimilarity(perfume1.getMixedSignature(), perfume2.getMixedSignature());
    }


    public Double getBlendedSignatureSimilarity(PerfumeSegmentedSignature signature, Perfume perfume) {
        double result = 0.0;
        if (perfume.getTopNotes().size() > 0 && perfume.getHeartNotes().size() > 0 && perfume.getBaseNotes().size() > 0) {

            result = MathUtils.pow((
                    topWeight * MathUtils.pow(this.signatureSimilarity(perfume.getTopSignature(), signature.getTop()), power)
                            + heartWeight * MathUtils.pow(this.signatureSimilarity(perfume.getHeartSignature(), signature.getHeart()), power)
                            + baseWeight * MathUtils.pow(this.signatureSimilarity(perfume.getBaseSignature(), signature.getBase()), power)
                            + mixedWeight * MathUtils.pow(this.signatureSimilarity(perfume.getMixedSignature(), signature.getMixed()), power)) / (topWeight + heartWeight + baseWeight + mixedWeight), 1.0 / power);
        } else {
            result = Math.pow(this.signatureSimilarity(perfume.getMixedSignature(), signature.getMixed()), mixedExponentialPenalty);
        }
        return result;
    }

    public Double getBlendedSignatureSimilarity(Perfume perfume1, Perfume perfume2) {
        double result = 0.0;
        if (perfume1.getTopNotes().size() > 0 && perfume1.getHeartNotes().size() > 0 && perfume1.getBaseNotes().size() > 0
                && perfume2.getTopNotes().size() > 0 && perfume2.getHeartNotes().size() > 0 && perfume2.getBaseNotes().size() > 0) {

            result = MathUtils.pow((
                    topWeight * MathUtils.pow(this.getTopSignatureSimilarity(perfume1, perfume2), power)
                            + heartWeight * MathUtils.pow(this.getHeartSignatureSimilarity(perfume1, perfume2), power)
                            + baseWeight * MathUtils.pow(this.getBaseSignatureSimilarity(perfume1, perfume2), power)
                            + mixedWeight * MathUtils.pow(this.getMixedSignatureSimilarity(perfume1, perfume2), power)) / (topWeight + heartWeight + baseWeight + mixedWeight), 1.0 / power);
        } else {
            result = Math.pow(this.getMixedSignatureSimilarity(perfume1, perfume2), mixedExponentialPenalty);
        }
        return result;
    }

    public Double signatureSimilarity(Map<NoteType, Double> signature1, Map<NoteType, Double> signature2) {
        //return signatureSimilarityCovariance(signature1, signature2);
        return signatureSimilarityCosine(signature1, signature2);
    }


    public Double signatureSimilarityCosine(Map<NoteType, Double> signature1, Map<NoteType, Double> signature2) {
//        Double raritySum1=0.0;
//        Double raritySum2=0.0;
        Double signatureSquareSum1 = 0.0;
        Double signatureSquareSum2 = 0.0;
        Double matchRate = 0.0;
        Set<NoteType> set = new HashSet<>();
        set.addAll(signature1.keySet());
        set.addAll(signature2.keySet());

        //Weighted mean 1
        for (NoteType noteType : set) {
            if (!noteType.equals(NoteType.UNKNOWN) && !noteType.equals(NoteType.NON_CLASSIFIED)) {
                Double rarity = frequencyAnalysis.getMixedNoteTypeRarity(noteType);
                Double value = 0.0;
                if (signature1.containsKey(noteType))
                    value = signature1.get(noteType);
                signatureSquareSum1 += rarity * value * value;
                //raritySum1+=rarity;
            }
        }
        //Weighted mean 2
        for (NoteType noteType : set) {
            if (!noteType.equals(NoteType.UNKNOWN) && !noteType.equals(NoteType.NON_CLASSIFIED)) {
                //signatureSum2 += frequencyAnalysis.getMixedNoteTypeRarity(entry2.getKey())*entry2.getValue();
                //signatureSum2 += frequencyAnalysis.getMixedNoteTypeRarity(entry2.getKey())*Math.abs(entry2.getValue());
                Double rarity = frequencyAnalysis.getMixedNoteTypeRarity(noteType);
                Double value = 0.0;
                if (signature2.containsKey(noteType))
                    value = signature2.get(noteType);
                signatureSquareSum2 += rarity * value * value;
                //raritySum2+=rarity;
            }
        }
        //Double signatureAmplitude1=Math.sqrt(signatureSquareSum1);
        //Double signatureAmplitude2=Math.sqrt(signatureSquareSum2);

        for (NoteType noteType : set) {
            if (!noteType.equals(NoteType.UNKNOWN) && !noteType.equals(NoteType.NON_CLASSIFIED)) {
                Double value1 = 0.0;
                if (signature1.containsKey(noteType)) {
                    value1 = signature1.get(noteType);
                }
                Double value2 = 0.0;
                if (signature2.containsKey(noteType))
                    value2 = signature2.get(noteType);
                Double rarity = frequencyAnalysis.getMixedNoteTypeRarity(noteType);
                matchRate += rarity * (value1) * (value2);
            }
        }

        //matchRate/=raritySum1;

        if (signatureSquareSum1 > 0.0 && signatureSquareSum2 > 0.0)
            return 2.0 * (matchRate) / Math.sqrt(signatureSquareSum1 * signatureSquareSum2);
        else
            return 0.0;
    }

    @Deprecated
    public Double signatureSimilarityCovariance(Map<NoteType, Double> signature1, Map<NoteType, Double> signature2) {
        Double raritySum1 = 0.0;
        Double raritySum2 = 0.0;
        Double signatureSum1 = 0.0;
        Double signatureSum2 = 0.0;
        Double matchRate = 0.0;
        Set<NoteType> set = new HashSet<>();
        set.addAll(signature1.keySet());
        set.addAll(signature2.keySet());

        //Weighted mean 1
        for (NoteType noteType : set) {
            if (!noteType.equals(NoteType.UNKNOWN) && !noteType.equals(NoteType.NON_CLASSIFIED)) {
                Double rarity = frequencyAnalysis.getMixedNoteTypeRarity(noteType);
                Double value = 0.0;
                if (signature1.containsKey(noteType))
                    value = signature1.get(noteType);
                signatureSum1 += rarity * value;
                raritySum1 += rarity;
            }
        }
        //Weighted mean 2
        for (NoteType noteType : set) {
            if (!noteType.equals(NoteType.UNKNOWN) && !noteType.equals(NoteType.NON_CLASSIFIED)) {
                //signatureSum2 += frequencyAnalysis.getMixedNoteTypeRarity(entry2.getKey())*entry2.getValue();
                //signatureSum2 += frequencyAnalysis.getMixedNoteTypeRarity(entry2.getKey())*Math.abs(entry2.getValue());
                Double rarity = frequencyAnalysis.getMixedNoteTypeRarity(noteType);
                Double value = 0.0;
                if (signature2.containsKey(noteType))
                    value = signature2.get(noteType);
                signatureSum2 += rarity * value;
                raritySum2 += rarity;
            }
        }
        //Computing weighted means
        Double signatureMean1 = signatureSum1 / raritySum1;
        Double signatureMean2 = signatureSum2 / raritySum2;


        Double signatureCovariance1 = 0.0;
        Double signatureCovariance2 = 0.0;
        //Weighted auto-covariance 1
        for (NoteType noteType : set) {
            if (!noteType.equals(NoteType.UNKNOWN) && !noteType.equals(NoteType.NON_CLASSIFIED)) {
                Double rarity = frequencyAnalysis.getMixedNoteTypeRarity(noteType);
                Double value = 0.0;
                if (signature1.containsKey(noteType))
                    value = signature1.get(noteType);
                signatureCovariance1 += rarity * Math.pow(Math.abs(value - signatureMean1), 2.0);
            }
        }

        //Weighted auto-covariance 2
        for (NoteType noteType : set) {
            if (!noteType.equals(NoteType.UNKNOWN) && !noteType.equals(NoteType.NON_CLASSIFIED)) {
                Double rarity = frequencyAnalysis.getMixedNoteTypeRarity(noteType);
                Double value = 0.0;
                if (signature2.containsKey(noteType))
                    value = signature2.get(noteType);
                signatureCovariance2 += rarity * Math.pow(Math.abs(value - signatureMean2), 2.0);
            }
        }

        //Computing weighted auto-correlation
        signatureCovariance1 /= raritySum1;
        signatureCovariance2 /= raritySum2;


        for (NoteType noteType : set) {
            if (!noteType.equals(NoteType.UNKNOWN) && !noteType.equals(NoteType.NON_CLASSIFIED)) {
                Double value1 = 0.0;
                if (signature1.containsKey(noteType)) {
                    value1 = signature1.get(noteType);
                }
                Double value2 = 0.0;
                if (signature2.containsKey(noteType))
                    value2 = signature2.get(noteType);
                Double rarity = frequencyAnalysis.getMixedNoteTypeRarity(noteType);
                matchRate += rarity * (value1 - signatureMean1) * (value2 - signatureMean2);
            }
        }

        matchRate /= raritySum1;
        //WARNING: covariances can still be legitimately zero!
        if (signatureCovariance1 > 0.0 && signatureCovariance2 > 0.0)
            return 2.0 * (matchRate) / Math.sqrt(signatureCovariance1 * signatureCovariance2);
        else
            return 0.0;
    }

}
