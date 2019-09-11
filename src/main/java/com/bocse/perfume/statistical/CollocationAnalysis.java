package com.bocse.perfume.statistical;

import com.bocse.perfume.data.Perfume;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by bogdan.bocse on 19/07/16.
 */
public class CollocationAnalysis {
    private final static Logger logger = Logger.getLogger(FrequencyAnalysis.class.toString());

    private final Map<String, Map<String, Double>> collocationMap = new HashMap<>();
    private final List<Perfume> perfumeList;

    public CollocationAnalysis(List<Perfume> perfumeList) {
        this.perfumeList = perfumeList;
    }

    public void process() {
        Long startTime = System.currentTimeMillis();
        performAnalysis();
        Long endTime = System.currentTimeMillis();
        logger.info("Collocation analysis done in " + (endTime - startTime) + "ms.");
    }

    private void performAnalysis() {
        for (Perfume perfume : perfumeList) {
            for (String note1 : perfume.getMixedNotes()) {
                if (!collocationMap.containsKey(note1))
                    collocationMap.put(note1, new HashMap<>());
                Map<String, Double> noteMap = collocationMap.get(note1);

                for (String note2 : perfume.getMixedNotes()) {
                    if (note1 != note2) {
                        noteMap.putIfAbsent(note2, 0.0);
                        noteMap.put(note2, noteMap.get(note2) + 1.0);
                    }
                }
            }
        }
    }

    public Double collocation(String note1, String note2) {
        if (collocationMap.containsKey(note1)) {
            Map<String, Double> submap = collocationMap.get(note1);
            if (submap.containsKey(note2)) {
                return submap.get(note2);
            } else {
                return 0.0;
            }
        } else {
            return 0.0;
        }
    }
}
