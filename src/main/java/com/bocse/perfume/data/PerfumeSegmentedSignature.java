package com.bocse.perfume.data;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by bogdan.bocse on 3/14/2016.
 */
public class PerfumeSegmentedSignature {
    private Map<NoteType, Double> top = new HashMap<>();
    private Map<NoteType, Double> heart = new HashMap<>();
    private Map<NoteType, Double> base = new HashMap<>();
    private Map<NoteType, Double> mixed = new HashMap<>();

    public Map<NoteType, Double> getBase() {
        return base;
    }

    public Map<NoteType, Double> getTop() {
        return top;
    }

    public Map<NoteType, Double> getHeart() {
        return heart;
    }


    public Map<NoteType, Double> getMixed() {
        return mixed;
    }
}
