package com.bocse.perfume;


import com.bocse.perfume.data.Gender;
import com.bocse.perfume.data.Perfume;
import com.bocse.perfume.parser.IPerfumeParser;
import com.bocse.perfume.parser.PerfumeParser;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Unit test for simple MultumescDeputyMain.
 */
public class PerfumeParserTest
        extends TestCase {
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public PerfumeParserTest(String testName) {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(PerfumeParserTest.class);
    }

    /**
     * Rigourous Test :-)
     */

}