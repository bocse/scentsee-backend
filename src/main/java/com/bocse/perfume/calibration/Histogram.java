package com.bocse.perfume.calibration;

import org.apache.commons.lang.mutable.MutableInt;

import java.util.Map;
import java.util.TreeMap;

/**
 * Created by bogdan.bocse on 12/23/2015.
 */
public class Histogram {

    Integer steps;
    private TreeMap<Double, MutableInt> map;
    private MutableInt counter;
    private Double minValue, maxValue, h;

    public Histogram(Double minValue, Double maxValue, Integer steps) {
        map = new TreeMap<>();
        this.steps = steps;
        this.minValue = minValue;
        this.maxValue = maxValue;
        h = (maxValue - minValue) / steps;
        for (Double x = minValue; x <= maxValue; x += h) {
            map.put(x, new MutableInt(0));
        }
        map.put(minValue - h, new MutableInt(0));
        map.put(maxValue + h, new MutableInt(0));
        counter = new MutableInt(0);
    }

    public void put(Double value) {
        try {
            if (value < minValue) {
                map.floorEntry(minValue - h).getValue().add(1);
            } else if (value > maxValue) {
                map.floorEntry(maxValue + h).getValue().add(1);
            } else {
                map.floorEntry(value).getValue().add(1);
            }
            counter.increment();
        } catch (Exception ex) {
            System.err.println("Unexpected value: " + value);
        }
    }

    public int getCounter() {
        return counter.intValue();
    }

    public void print() {
        for (Map.Entry<Double, MutableInt> entry : map.entrySet()) {
            System.out.println(entry.getKey() + ", " + entry.getValue().intValue());
        }
    }
}
