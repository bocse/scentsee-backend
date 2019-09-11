package com.bocse.perfume.affiliate;

import com.bocse.perfume.data.AffiliatePerfume;
import com.bocse.perfume.data.Perfume;
import org.supercsv.io.ICsvListReader;

import java.io.IOException;
import java.util.List;

/**
 * Created by bocse on 06.12.2015.
 */
public interface AffiliateInterface {
    List<AffiliatePerfume> lookup(Perfume perfume);

    String getAffiliateName();

    void readProductsFromFile(String filename) throws IOException;

    void readProductsFromByteArray(byte[] contentBytes) throws IOException;

    public void readProductsFromCSV(ICsvListReader listReader) throws IOException;
}
