package com.bocse.perfume.data;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by bogdan.bocse on 3/14/2016.
 */
public class PerfumeMixedSignature {
    private Map<NoteType, Double> mixed = new HashMap<>();


    public Map<NoteType, Double> getMixed() {
        return mixed;
    }
}
