package com.bocse.perfume.utils;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by bocse on 30/08/16.
 */
public class SynonymManager {

    //private PerfumeIterator perfumeIterator;
    private Map<String, String> dictionary = new LinkedHashMap<>();


    public SynonymManager() {

    }

    public void defaultInit() {
        add("pacco", "paco");
        add("profumo", "profvmo");
        add("u", "v");
        add("d&g", "dolce & gabbana");
        add("d & g", "dolce & gabbana");
        add("aqua", "aqva");
        add("acqua", "aqva");
        add("blvd", "boulevard");
        add("schoen", "schon");
        add("colonie", "cologne");
    }

    public void add(String original, String transformed) {
        original = transform(original);
        dictionary.put(original, transformed);
    }

    public String transform(String original) {
        for (Map.Entry<String, String> previousEntry : dictionary.entrySet()) {
            original = original.replaceAll(previousEntry.getKey(), previousEntry.getValue());
        }
        return original;
    }
}
