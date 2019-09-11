package com.bocse.perfume.utils;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by bocse on 17.11.2015.
 */
public class TextUtils {

    public static String cleanupAndFlatten(String string) {
        string = string.replaceAll("[^\\p{L}\\p{Nd} ]+", "");
        StringBuilder sb = new StringBuilder(string.length());
        string = Normalizer.normalize(string, Normalizer.Form.NFD);
        for (char c : string.toCharArray()) {
            if (c <= '\u007F') sb.append(c);
        }
        return sb.toString();
    }

    public static String removeNonLiterals(String s) {
        return s.replaceAll("[^\\p{L}\\p{Nd} ]+", "");
    }

    public static String flattenToAscii(String string) {
        StringBuilder sb = new StringBuilder(string.length());
        string = Normalizer.normalize(string, Normalizer.Form.NFD);
        for (char c : string.toCharArray()) {
            if (c <= '\u007F') sb.append(c);
        }
        return sb.toString();
    }

    public static long hash(String string) {
        long h = 9223372036854775783L; // prime
        int len = string.length();

        for (int i = 0; i < len; i++) {
            h = 31 * h + string.charAt(i);
        }
        if (h < 0)
            h = -h;
        h = h / 1000;
        return h;
    }

    public static List<String> syllableSplit(String originalString, int maxConsonant) {
        List<String> allMatches = new ArrayList<String>();
        Matcher m = Pattern.compile("([bcdfghjklmnpqrstvwx]{1," + maxConsonant + "}[aeiouy]+)").matcher(originalString.toLowerCase());
        while (m.find()) {
            allMatches.add(m.group());
        }
        return allMatches;
    }
}
