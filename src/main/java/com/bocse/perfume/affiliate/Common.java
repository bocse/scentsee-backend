package com.bocse.perfume.affiliate;

import com.bocse.perfume.requester.HttpRequester;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.ICsvListReader;
import org.supercsv.prefs.CsvPreference;

import java.io.*;

/**
 * Created by bocse on 13/08/16.
 */
public class Common {
    public static ICsvListReader getListReaderByteArray(byte[] contentBytes) throws FileNotFoundException {
        ICsvListReader listReader = null;
        listReader = new CsvListReader(new InputStreamReader(new ByteArrayInputStream(contentBytes)), CsvPreference.STANDARD_PREFERENCE);
        return listReader;
    }

    public static ICsvListReader getListReaderFromFile(String filename) throws FileNotFoundException {
        ICsvListReader listReader = null;
        listReader = new CsvListReader(new FileReader(filename), CsvPreference.STANDARD_PREFERENCE);
        return listReader;
    }

    public static ICsvListReader getListReaderFromHTTP(String url, String username, String password) throws IOException, InterruptedException {
        ICsvListReader listReader = null;
        //logger.info("Downloading affiliate list from " + url);
        final HttpGet httpGet = new HttpGet(url);
        final RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(9000).setConnectTimeout(9000).setSocketTimeout(9000).build();
        httpGet.setConfig(requestConfig);
        Reader reader = new StringReader(HttpRequester.getInputStreamFromGzipURL(httpGet, true, username, password));
        listReader = new CsvListReader(reader, CsvPreference.TAB_PREFERENCE);

        return listReader;
    }
}
