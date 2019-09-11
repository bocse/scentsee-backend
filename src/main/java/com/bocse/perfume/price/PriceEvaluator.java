package com.bocse.perfume.price;

import com.bocse.perfume.calibration.Histogram;
import com.bocse.perfume.data.AffiliatePerfume;
import com.bocse.perfume.data.Perfume;

import java.util.*;

/**
 * Created by bogdan.bocse on 12/23/2015.
 */
public class PriceEvaluator {
    private final Double standardQuantity = 100.0;
    private final Double halvingFactor = 1.4;
    private final Double defaultStandardPrice = 288.0;

    public PriceEvaluator() {

    }

/*
    public Map<Double, List<Double>> getPriceMap(List<Perfume> perfumes)
    {

        TreeMap<Double, List<Double>> map=new TreeMap<>();
        for (Perfume perfume: perfumes)
        {
            for (AffiliatePerfume affiliatePerfume:perfume.getAffiliateProducts())
            {
                if (!map.containsKey(affiliatePerfume.getQuantity()))
                    map.put(affiliatePerfume.getQuantity(), new ArrayList<>());
                map.get(affiliatePerfume.getQuantity()).add(new Double(affiliatePerfume.getPrice()));
            }
        }
        for (Map.Entry<Double, List<Double>> entry: map.entrySet())
        {
            Collections.sort(entry.getValue());
        }
        return map;
    }
*/

    public Double getPriceStandard100ml(Perfume perfume) {
        Double priceAoro = getPriceStandard100ml(perfume, "aoro");

        Double priceStrawberry = getPriceStandard100ml(perfume, "strawberry");

        if (priceAoro != null && priceStrawberry != null) {
            return (priceAoro + priceStrawberry) / 2;
        } else if (priceAoro != null) {
            return priceAoro;
        } else if (priceStrawberry != null) {
            return priceStrawberry;
        } else {
            return defaultStandardPrice;
        }
    }

    private Double getPriceStandard100ml(Perfume perfume, String affiliateName) {
        List<AffiliatePerfume> affiliatePerfumeList;
        if (!perfume.getAffiliateProducts().containsKey(affiliateName)) {
            return null;
        } else {
            affiliatePerfumeList = perfume.getAffiliateProducts().get(affiliateName);
        }
        if (affiliatePerfumeList.size() > 0) {
            for (AffiliatePerfume affiliatePerfume : affiliatePerfumeList) {
                if (affiliatePerfume.getQuantity() == 100.0)
                    return new Double(affiliatePerfume.getPrice());
            }
            for (AffiliatePerfume affiliatePerfume : affiliatePerfumeList) {
                if (affiliatePerfume.getQuantity() == 50.0)
                    return new Double(1.3 * affiliatePerfume.getPrice());
            }
            for (AffiliatePerfume affiliatePerfume : affiliatePerfumeList) {
                if (affiliatePerfume.getQuantity() == 75.0)
                    return new Double(1.3 * affiliatePerfume.getPrice());
            }
            Double closestQuantity = affiliatePerfumeList.get(0).getQuantity();
            Double price = new Double(affiliatePerfumeList.get(0).getPrice());
            for (AffiliatePerfume affiliatePerfume : affiliatePerfumeList) {
                if (Math.abs(affiliatePerfume.getQuantity() - standardQuantity) < Math.abs(closestQuantity - standardQuantity)) {
                    closestQuantity = affiliatePerfume.getQuantity();
                    price = new Double(affiliatePerfume.getPrice());
                }
            }
            Double priceEstimate = Math.pow(halvingFactor, Math.log(standardQuantity / closestQuantity) / Math.log(2)) * price;
            return priceEstimate;
        } else {
            return null;
        }
    }

    public Map<Double, List<Double>> getPriceMap(List<Perfume> perfumes) {
        TreeMap<Double, List<Double>> map = new TreeMap<>();
        for (Perfume perfume : perfumes) {
            for (List<AffiliatePerfume> affiliateList : perfume.getAffiliateProducts().values())
                for (AffiliatePerfume affiliatePerfume : affiliateList) {
                    if (!map.containsKey(affiliatePerfume.getQuantity()))
                        map.put(affiliatePerfume.getQuantity(), new ArrayList<>());
                    map.get(affiliatePerfume.getQuantity()).add(new Double(affiliatePerfume.getPrice()));
                }
        }
        for (Map.Entry<Double, List<Double>> entry : map.entrySet()) {
            Collections.sort(entry.getValue());
        }
        return map;
    }


    public Double getPriceStandard100mlAllAffiliates(Perfume perfume) {
        Double closestQuantity = 150.0;
        Double price = 150.0;

        if (perfume.getAffiliateProducts().size() > 0) {
            for (List<AffiliatePerfume> affiliateList : perfume.getAffiliateProducts().values())
                for (AffiliatePerfume affiliatePerfume : affiliateList) {
                    if (affiliatePerfume.getQuantity() == 100.0)
                        return new Double(affiliatePerfume.getPrice());
                }
            for (List<AffiliatePerfume> affiliateList : perfume.getAffiliateProducts().values())
                for (AffiliatePerfume affiliatePerfume : affiliateList) {
                    if (affiliatePerfume.getQuantity() == 50.0)
                        return new Double(1.3 * affiliatePerfume.getPrice());
                }
            for (List<AffiliatePerfume> affiliateList : perfume.getAffiliateProducts().values())
                for (AffiliatePerfume affiliatePerfume : affiliateList) {
                    if (affiliatePerfume.getQuantity() == 75.0)
                        return new Double(1.3 * affiliatePerfume.getPrice());
                }
            for (List<AffiliatePerfume> affiliateList : perfume.getAffiliateProducts().values())
                for (AffiliatePerfume affiliatePerfume : affiliateList) {
                    closestQuantity = affiliatePerfume.getQuantity();
                    price = new Double(affiliatePerfume.getPrice());
                    break;
                }

            for (List<AffiliatePerfume> affiliateList : perfume.getAffiliateProducts().values())
                for (AffiliatePerfume affiliatePerfume : affiliateList) {
                    if (Math.abs(affiliatePerfume.getQuantity() - standardQuantity) < Math.abs(closestQuantity - standardQuantity)) {
                        closestQuantity = affiliatePerfume.getQuantity();
                        price = new Double(affiliatePerfume.getPrice());
                    }
                }
            Double priceEstimate = Math.pow(halvingFactor, Math.log(standardQuantity / closestQuantity) / Math.log(2)) * price;
            return priceEstimate;
        } else {
            return defaultStandardPrice;
        }
    }

    public Histogram getHistogramStandardPrice(List<Perfume> perfumes) {
        Histogram histogram = new Histogram(50.0, 550.0, 25);
        for (Perfume perfume : perfumes) {
            if (perfume.getAffiliateProducts().size() > 0)
                histogram.put(this.getPriceStandard100ml(perfume));
        }
        return histogram;
    }

    public Double getAverageStandardPrice(List<Perfume> perfumes) {
        List<Double> prices = new ArrayList<>();
        for (Perfume perfume : perfumes) {
            if (perfume.getAffiliateProducts().size() > 0)
                prices.add(this.getPriceStandard100ml(perfume));
        }
        return prices.stream().mapToDouble(val -> val).average().getAsDouble();
    }

    public Double getPercentileStandardPrice(List<Perfume> perfumes, Double percentile) {
        if (percentile > 1.0)
            throw new IllegalStateException("Percentile should be smaller than 1.0 .");
        if (percentile < 0.0)
            throw new IllegalStateException("Percentile should be positive.");
        List<Double> prices = new ArrayList<>();
        for (Perfume perfume : perfumes) {
            if (perfume.getAffiliateProducts().size() > 0)
                prices.add(this.getPriceStandard100ml(perfume));
        }
        Collections.sort(prices);
        int index = (int) Math.round(prices.size() * percentile);
        if (index >= prices.size()) {
            index = prices.size() - 1;
        }
        Double percentilePrice = prices.get(index);
        return
                percentilePrice;
    }

    public Map<Double, Double> getAverageFromMap(Map<Double, List<Double>> priceMap) {
        TreeMap<Double, Double> priceAverage = new TreeMap<>();
        for (Map.Entry<Double, List<Double>> entry : priceMap.entrySet()) {
            Double averagePrice = entry.getValue().stream().mapToDouble(val -> val).average().getAsDouble();
            priceAverage.put(entry.getKey(), averagePrice);
        }
        return priceAverage;
    }

    public Map<Double, Double> getPercentileFromMap(Map<Double, List<Double>> priceMap, Double percentile) {
        if (percentile > 1.0)
            throw new IllegalStateException("Percentile should be smaller than 1.0 .");
        if (percentile < 0.0)
            throw new IllegalStateException("Percentile should be positive.");
        TreeMap<Double, Double> priceAverage = new TreeMap<>();
        for (Map.Entry<Double, List<Double>> entry : priceMap.entrySet()) {
            int index = (int) Math.round(entry.getValue().size() * percentile);
            if (index >= entry.getValue().size()) {
                index = entry.getValue().size() - 1;
            }
            Double percentilePrice = entry.getValue().get(index);
            priceAverage.put(entry.getKey(), percentilePrice);
        }
        return priceAverage;
    }
}
