package com.bocse.perfume.utils;

/**
 * Created by bocse on 05.12.2015.
 */
public class MathUtils {
    public static double pow(double base, double exponent) {

        if (base < 0.0)
            base = 0.0;
        if (exponent == 1)
            return base;
        if (base == 0.0)
            return 0.0;
        else
            return Math.pow(base, exponent);
    }
}
