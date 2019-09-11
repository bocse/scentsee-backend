package com.bocse.perfume.distance;

import com.bocse.perfume.data.NoteType;
import com.bocse.perfume.data.Perfume;
import com.bocse.perfume.utils.MathUtils;

import java.util.Map;

/**
 * Created by bocse on 05.12.2015.
 */
@Deprecated
public class SignatureDistance {
    private Double power = 2.0;
    private Double topWeight = 1.0;
    private Double heartWeight = 1.0;
    private Double baseWeight = 1.0;
    private Double mixedWeight = 1.0;
    private Double mixedExponentialPenalty = 2.0;

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


    public Double getTopSignatureDistance(Perfume perfume1, Perfume perfume2) {
        return this.signatureDistance(perfume1.getTopSignature(), perfume2.getTopSignature());
    }

    public Double getHeartSignatureDistance(Perfume perfume1, Perfume perfume2) {
        return this.signatureDistance(perfume1.getHeartSignature(), perfume2.getHeartSignature());
    }

    public Double getBaseSignatureDistance(Perfume perfume1, Perfume perfume2) {
        return this.signatureDistance(perfume1.getBaseSignature(), perfume2.getBaseSignature());
    }

    public Double getMixedSignatureDistance(Perfume perfume1, Perfume perfume2) {
        return this.signatureDistance(perfume1.getMixedSignature(), perfume2.getMixedSignature());
    }


    public Double getBlendedSignatureDistance(Perfume perfume1, Perfume perfume2) {
        double result = 0.0;
        if (perfume1.getTopNotes().size() > 0 && perfume1.getHeartNotes().size() > 0 && perfume1.getBaseNotes().size() > 0
                && perfume2.getTopNotes().size() > 0 && perfume2.getHeartNotes().size() > 0 && perfume2.getBaseNotes().size() > 0) {

            result = MathUtils.pow((
                    topWeight * MathUtils.pow(this.getTopSignatureDistance(perfume1, perfume2), power)
                            + heartWeight * MathUtils.pow(this.getHeartSignatureDistance(perfume1, perfume2), power)
                            + baseWeight * MathUtils.pow(this.getBaseSignatureDistance(perfume1, perfume2), power)
                            + mixedWeight * MathUtils.pow(this.getMixedSignatureDistance(perfume1, perfume2), power)), 1.0 / power);
        } else {
            result = Math.pow(this.getMixedSignatureDistance(perfume1, perfume2), mixedExponentialPenalty);
        }
        return result;
    }

    private Double signatureDistance(Map<NoteType, Double> signature1, Map<NoteType, Double> signature2) {
        double distance = 0;
        for (Map.Entry<NoteType, Double> entry1 : signature1.entrySet()) {
            if (!entry1.getKey().equals(NoteType.UNKNOWN)) {
                if (signature2.containsKey(entry1.getKey())) {
                    distance += Math.pow(Math.abs(entry1.getValue() - signature2.get(entry1.getKey())), power);
                } else
                    distance += Math.pow(entry1.getValue(), power);
            }
        }
        for (Map.Entry<NoteType, Double> entry2 : signature2.entrySet()) {
            if (!entry2.getKey().equals(NoteType.UNKNOWN) && signature1.containsKey(entry2.getKey())) {
                distance += Math.pow(entry2.getValue(), power);
            }
        }
        return distance;
    }

}
