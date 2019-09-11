package com.bocse.perfume.affiliate;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bogdan.bocse on 24/07/16.
 */

public class AffiliateCollection {

    private List<String> affiliates = new ArrayList<>();

    public void appendAffiliates(List<String> list) {
        for (String affiliate : list) {
            String cleanup = affiliate.trim().toLowerCase();
            if (!affiliates.contains(cleanup))
                affiliates.add(cleanup);
        }
    }

    public List<String> getAffiliates() {
        return affiliates;
    }

    public void setAffiliates(List<String> list) {
        affiliates.clear();
        appendAffiliates(list);
    }
}
