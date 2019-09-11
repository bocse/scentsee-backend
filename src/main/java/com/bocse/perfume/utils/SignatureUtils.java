package com.bocse.perfume.utils;

import com.bocse.perfume.data.NoteType;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by bogdan.bocse on 3/14/2016.
 */
public class SignatureUtils {


    public static void setSignatureAccumulator(Map<NoteType, Double> accumulator, Map<NoteType, Double> operand) {
        for (Map.Entry<NoteType, Double> entry : operand.entrySet()) {
            //accumulator.putIfAbsent(entry.getKey(), 0.0);
            accumulator.put(entry.getKey(), entry.getValue());
        }
    }

    public static void addSignatureAccumulator(Map<NoteType, Double> accumulator, Map<NoteType, Double> operand, Double scalar) {
        for (Map.Entry<NoteType, Double> entry : operand.entrySet()) {
            accumulator.putIfAbsent(entry.getKey(), 0.0);
            accumulator.put(entry.getKey(), accumulator.get(entry.getKey()) + scalar * entry.getValue());
        }
    }

    public static Map<NoteType, Double> addSignature(Map<NoteType, Double> operand1, Double scalar1, Map<NoteType, Double> operand2, Double scalar2) {
        Map<NoteType, Double> accumulator = new HashMap<>();
        addSignatureAccumulator(accumulator, operand1, scalar1);
        addSignatureAccumulator(accumulator, operand2, scalar2);
        return accumulator;
    }

    public static Map<NoteType, Double> flattenSignature(Map<NoteType, Double> top, Double topWeight, Map<NoteType, Double> heart, Double heartWeight, Map<NoteType, Double> base, Double baseWeight) {
        Map<NoteType, Double> accumulator = new HashMap<>();
        addSignatureAccumulator(accumulator, top, topWeight);
        addSignatureAccumulator(accumulator, heart, heartWeight);
        addSignatureAccumulator(accumulator, base, baseWeight);
        return accumulator;
    }

    public static void cullSignature(Map<NoteType, Double> map) {
        for (Iterator<Map.Entry<NoteType, Double>> it = map.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<NoteType, Double> entry = it.next();
            if (entry.getValue() == 0) {
                it.remove();
            }
        }
    }


    public static void normalizeSignature(Map<NoteType, Double> map) {
        //Map<NoteType, Double> normalized = new HashMap<>();
        Double maxValue = 1.0;
        Double minValue = Double.MAX_VALUE;
        Double min = Double.MAX_VALUE;
        Double max = Double.MIN_VALUE;
        for (Double value : map.values()) {
            if (value < minValue)
                minValue = value;
            if (value > max)
                max = value;
        }
        min = minValue;
        if (minValue < 0.0) {
            minValue = -1.0;
        } else {
            minValue = 0.0;
        }
        Double delta = max - min;
        for (Map.Entry<NoteType, Double> entry : map.entrySet()) {
            Double normalizedValue;
            if (delta != 0.0) {
                normalizedValue = (entry.getValue() - min) / delta *
                        (1.0 - minValue) + minValue;
            } else {
                normalizedValue = Math.signum(entry.getValue());
            }
            entry.setValue(normalizedValue);
            //normalized.put(entry.getKey(), normalizedValue );
        }
        //return normalized;
    }


}
