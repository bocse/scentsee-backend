package com.bocse.perfume.utils;

import org.apache.commons.lang.StringUtils;

/**
 * Created by bocse on 21.11.2015.
 */
public class NameUtils {

    public static String getFirstNames(String fullName) {
        StringBuffer firstNames = new StringBuffer();
        String[] names = fullName.split(" ");
        for (String name : names) {
            if (!StringUtils.isAllUpperCase(name))
                firstNames.append(name + " ");

        }
        return firstNames.toString().trim();
    }

    public static String getLastName(String fullName) {
        StringBuffer lastNames = new StringBuffer();
        String[] names = fullName.split(" ");
        for (String name : names) {
            if (StringUtils.isAllUpperCase(name)) {
                lastNames.append(Character.toUpperCase(name.charAt(0)) + name.substring(1).toLowerCase() + " ");
            }

        }
        return lastNames.toString().trim();
    }
}
