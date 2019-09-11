package com.bocse.perfume.calibration;

import com.bocse.perfume.data.Perfume;
import com.bocse.perfume.data.RecommendationAlgorithm;
import com.bocse.perfume.distance.NoteDistance;
import com.bocse.perfume.distance.SignatureDistance;
import com.bocse.perfume.iterator.PerfumeIterator;
import com.bocse.perfume.price.PriceEvaluator;
import com.bocse.perfume.recommender.PerfumeRecommender;
import com.bocse.perfume.signature.SignatureEvaluator;
import com.bocse.perfume.similarity.NoteSimilarity;
import com.bocse.perfume.similarity.SignatureSimilarity;
import com.bocse.perfume.statistical.FrequencyAnalysis;
import com.bocse.perfume.statistical.InterfaceFrequencyAnalysis;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.lang3.NotImplementedException;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.ICsvListReader;
import org.supercsv.prefs.CsvPreference;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;


/**
 * Created by bocse on 22.12.2015.
 */
public class PerfumeRecommenderCalibration {

    private final static Logger logger = Logger.getLogger(PerfumeRecommenderCalibration.class.toString());

    private PerfumeIterator iterator;
    private Map<String, List<Perfume>> preferenceMap;
    private InterfaceFrequencyAnalysis frequencyAnalysis;

    private Double maxRankRate = -0.0;
    private Double minAverageRank = 10000.0;
    private Double maxRankPoints = 0.0;
    private AtomicInteger encounteredExceptions = new AtomicInteger(0);
    private Map<String, Double> bestResults = null;
    private Map<String, Double> bestConfig = null;
    private Random parameterRandomizer;


    public PerfumeRecommenderCalibration() {

    }

    public void initPerfumeCollection(String filenamePerfumes, String filenameTypes) throws IOException {
        iterator = new PerfumeIterator();
        iterator.iterateAndKeep(new File(filenamePerfumes));
        iterator.swap();
        SignatureEvaluator signatureEvaluator = new SignatureEvaluator();
        signatureEvaluator.iterateAndKeep(new File(filenameTypes));
        signatureEvaluator.swap();
        for (Perfume perfume : iterator.getPerfumeList()) {
            signatureEvaluator.embedPerfumeSignature(perfume);
        }
        frequencyAnalysis = new FrequencyAnalysis(iterator.getPerfumeList());
        frequencyAnalysis.process();
    }

    public void initData(File file) throws IOException {
        ICsvListReader listReader = null;
        listReader = new CsvListReader(new FileReader(file), CsvPreference.STANDARD_PREFERENCE);
        String[] header = listReader.getHeader(true); // skip the header (can't be used with CsvListReader)
        preferenceMap = new HashMap<>();
        List<String> fields;
        int index = 0;
        while ((fields = listReader.read()) != null) {
            index++;
            //logger.info(fields.toString());
            List userPreferences = new ArrayList<>();
            StringBuffer sb = new StringBuffer();
            Boolean goodDatapoint = Boolean.valueOf(fields.get(0));
            if (!goodDatapoint)
                continue;
            String dateString = fields.get(1);
            String origin = fields.get(8);
            if (origin == null) {
                origin = "nid" + index;
            } else if (preferenceMap.containsKey(origin)) {
                origin = origin + "xid" + index;
            }
            String gender = fields.get(9);
            sb.append(origin);
            sb.append(" : ");
            for (int i = 2; i <= 7; i++) {
                Long id = -1L;
                if (fields.get(i) != null) {
                    id = Long.valueOf(fields.get(i).trim());
                }
                Perfume perfume = iterator.getPerfumeMap().get(id);
                if (perfume != null) {
                    sb.append(perfume.getSearchableName());
                    sb.append(", ");
                    userPreferences.add(perfume);
                }
                if (id > -1 && perfume == null) {
                    logger.severe("data entry issue with id " + id);
                    //throw new IllegalStateException("Failed in parsing " + fields.toString());
                }

            }
            logger.info(sb.toString());
            preferenceMap.put(origin, userPreferences);
        }
        // logger.severe("because fuck you");
    }


    private Double ovenFunction(Integer iteration, Double wavelength) {
        final Double epsilon = 0.01;
        Double x = (Double) Math.PI * iteration / wavelength;
        Double amplitude = Math.sin(x) + epsilon;
        amplitude = amplitude * amplitude;

        return amplitude;

    }

    private Double uniformRandom(Random generator) {
        return generator.nextDouble() * 2 - 1.0;
    }

    private Double wrap(Double x, Double min, Double max) {
        x = x - Math.floor((x - min) / (max - min)) * (max - min);
        if (x < 0) //This corrects the problem caused by using Int instead of Floor
            x = x + max - min;
        return x;
    }

    public Map<String, Double> simulatedAnnealing(final Long seed) throws InterruptedException {
        final Integer maxDepth = 100;
        logger.info("Seed: " + seed);
        final Object lock = new Object();
        final Integer maxThreads = 6;
        final Boolean stop = false;

        bestConfig = new HashMap<>();
        RecommendationAlgorithm algorithm = RecommendationAlgorithm.favoriteSimilarity;
        //Naive signature
//        Double power = 1.0;
//        Double signatureWeight = 1.0;
//        Double noteWeight = 0.15;
//        Double topWeight = 1.0;
//        Double heartWeight = 0.75;
//        Double baseWeight = 0.66;
//        Double mixedWeight=0.5;
//        Double mixedExponentialPenalty = 1.0;
//        Double priceDiscrimination = 1.0;

        //Best match to date
        Double power = 2.5;
        Double signatureWeight = 0.22;
        Double noteWeight = 0.6931;
        Double topWeight = 0.142;
        Double heartWeight = 0.4139;
        Double baseWeight = 0.8701;
        Double mixedWeight = 0.0155;
        Double mixedExponentialPenalty = 1.87;
        Double priceDiscrimination = 2.051;


//        Double power = 1.4;
//        Double signatureWeight = 0.6;
//        Double noteWeight = 0.7;
//        Double topWeight = 0.25;
//        Double heartWeight = 0.73;
//        Double baseWeight = 0.91;
//        Double mixedWeight=0.5;
//        Double mixedExponentialPenalty = 1.4;
//        Double priceDiscrimination = 1.65;


//        Double power = 2.26;
//        Double signatureWeight = 0.11;
//        Double noteWeight = 0.7;
//        Double topWeight = 0.1;
//        Double heartWeight = 0.45;
//        Double baseWeight = 0.99;
//        Double mixedWeight=0.5;
//        Double mixedExponentialPenalty = 1.9;
//        Double priceDiscrimination = 2.05;
        bestConfig.put("power", power);
        bestConfig.put("signatureWeight", signatureWeight);
        bestConfig.put("noteWeight", noteWeight);
        bestConfig.put("topWeight", topWeight);
        bestConfig.put("heartWeight", heartWeight);
        bestConfig.put("baseWeight", baseWeight);
        bestConfig.put("mixedWeight", mixedWeight);
        bestConfig.put("mixedExponentialPenalty", mixedExponentialPenalty);
        bestConfig.put("priceDiscrimination", priceDiscrimination);


        List<Double> rankRateList = new ArrayList<>();
        List<Double> averageRankList = new ArrayList<>();
        List<Double> rankPoints = new ArrayList<>();
        List<Double> minRank = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Map<String, Double> partialRanks = runRecommendationTest(seed + i * 1117192327, maxDepth, 2, 2, algorithm, power, signatureWeight, noteWeight,
                    topWeight, heartWeight, baseWeight, mixedWeight, mixedExponentialPenalty, priceDiscrimination);
            rankRateList.add(partialRanks.get("rankedPercent"));
            averageRankList.add(partialRanks.get("averageRank"));
            rankPoints.add(partialRanks.get("rankPoints"));
            minRank.add(partialRanks.get("minRank"));
        }
        Double initialAverageRankRate = rankRateList.stream().mapToDouble(val -> val).average().getAsDouble();
        Double initialAverageRank = averageRankList.stream().mapToDouble(val -> val).average().getAsDouble();
        Double initialAverageRankPoints = rankPoints.stream().mapToDouble(val -> val).average().getAsDouble();
        Double initialAverageMinRank = minRank.stream().mapToDouble(val -> val).average().getAsDouble();
        maxRankRate = initialAverageRankRate;
        minAverageRank = initialAverageRank;
        maxRankPoints = initialAverageRankPoints;
        System.gc();
        logger.info(String.format("Optimization started at with rank rate %f and average rank %f and rankPoints %f and minRank %f", maxRankRate, minAverageRank, maxRankPoints, initialAverageMinRank));
        ExecutorService executorService = Executors.newFixedThreadPool(maxThreads);
        for (int threadIndex = 0; threadIndex < maxThreads; threadIndex++) {
            executorService.submit(new Callable<Object>() {

                @Override
                public Object call() throws Exception {

                    Thread.sleep((int) (Math.random() * 100));
                    Long localSeed = seed * 11172319 + Thread.currentThread().getId() * 2319261711L + System.nanoTime();
                    logger.info("Thread " + Thread.currentThread().getId() + " \t Seed:" + localSeed);
                    Random parameterRandomizer = new Random(localSeed);
                    Integer iteration = 0;
                    while (!stop) {
                        try {
                            iteration++;
                            Double power, signatureWeight, noteWeight, topWeight, heartWeight, baseWeight, mixedWeight, mixedExponentialPenalty, priceDiscrimination;

                            Double amplitude = ovenFunction(iteration, 40.0);
                            synchronized (lock) {
                                power = wrap(1.5 * amplitude * uniformRandom(parameterRandomizer) + bestConfig.get("power"), 0.1, 2.5);
                                signatureWeight = wrap(amplitude * uniformRandom(parameterRandomizer) + bestConfig.get("signatureWeight"), 0.0, 1.0);

                                noteWeight = wrap(amplitude * uniformRandom(parameterRandomizer) + bestConfig.get("noteWeight"), 0.0, 1.0);   //(0,1)
                                topWeight = wrap(amplitude * uniformRandom(parameterRandomizer) + bestConfig.get("topWeight"), 0.0, 1.0);  //(0,1)
                                heartWeight = wrap(amplitude * uniformRandom(parameterRandomizer) + bestConfig.get("heartWeight"), 0.0, 1.0);    //(0,1)
                                baseWeight = wrap(amplitude * uniformRandom(parameterRandomizer) + bestConfig.get("baseWeight"), 0.0, 1.0);   //(0,1)
                                mixedWeight = wrap(amplitude * uniformRandom(parameterRandomizer) + bestConfig.get("baseWeight"), 0.0, 1.0);   //(0,1)
                                mixedExponentialPenalty = wrap(amplitude * uniformRandom(parameterRandomizer) + bestConfig.get("mixedExponentialPenalty"), 0.8, 2.0);
                                priceDiscrimination = wrap(amplitude * uniformRandom(parameterRandomizer) + bestConfig.get("priceDiscrimination"), 0.0, 2.5);
                            }
                            Map<String, Double> ranks = runRecommendationTest(seed, maxDepth, 2, 2, algorithm, power, signatureWeight, noteWeight,
                                    topWeight, heartWeight, baseWeight, mixedWeight, mixedExponentialPenalty, priceDiscrimination);


                            if (ranks.get("rankPoints") - 1 > maxRankPoints)
                            //(maxRankRate < ranks.get("rankedPercent") - 0.1)
                            //&& minAverageRank>ranks.get("averageRank"))
                            {
                                List<Double> rankRateList = new ArrayList<>();
                                List<Double> averageRankList = new ArrayList<>();
                                List<Double> rankPoints = new ArrayList<>();
                                for (int i = 0; i < 10; i++) {
                                    Map<String, Double> partialRanks = runRecommendationTest(seed + i * 1117192327, maxDepth, 2, 2, algorithm, power, signatureWeight, noteWeight,
                                            topWeight, heartWeight, baseWeight, mixedWeight, mixedExponentialPenalty, priceDiscrimination);
                                    rankRateList.add(partialRanks.get("rankedPercent"));
                                    averageRankList.add(partialRanks.get("averageRank"));
                                    rankPoints.add(partialRanks.get("rankPoints"));
                                }
                                Boolean skip = (rankRateList.size() == 0 || averageRankList.size() == 0);

                                if (!skip) {
                                    synchronized (lock) {
                                        Double averageRankRate = rankRateList.stream().mapToDouble(val -> val).average().getAsDouble();
                                        Double averageRank = averageRankList.stream().mapToDouble(val -> val).average().getAsDouble();
                                        Double averageRankPoints = rankPoints.stream().mapToDouble(val -> val).average().getAsDouble();
                                        if (averageRankPoints - 1.0 > maxRankPoints) {
                                            //averageRankRate - 0.1 > maxRankRate) {
                                            // && averageRank < minAverageRank) {

                                            bestConfig.put("power", power);
                                            bestConfig.put("signatureWeight", signatureWeight);
                                            bestConfig.put("noteWeight", noteWeight);
                                            bestConfig.put("topWeight", topWeight);
                                            bestConfig.put("heartWeight", heartWeight);
                                            bestConfig.put("baseWeight", baseWeight);
                                            bestConfig.put("mixedWeight", mixedWeight);
                                            bestConfig.put("mixedExponentialPenalty", mixedExponentialPenalty);
                                            bestConfig.put("priceDiscrimination", priceDiscrimination);

                                            minAverageRank = averageRank;
                                            maxRankRate = averageRankRate;
                                            maxRankPoints = averageRankPoints;
                                            bestResults = ranks;

                                            logger.info("New best results!\n-----------------\n-----------");

                                            Gson gson = new GsonBuilder().setPrettyPrinting().create();
                                            logger.info("Exception so far: " + encounteredExceptions.get());
                                            logger.info("Best config: " + gson.toJson(bestConfig));
                                            logger.info("Results: " + gson.toJson(bestResults));


                                        }
                                    }
                                }
                            }
                        } catch (Exception ex) {
                            encounteredExceptions.incrementAndGet();
                        }


                    }
                    logger.warning("WHY?!");
                    return new Object();
                }


            });
        }
        //executorService.shutdown();
        //executorService.awaitTermination(10, TimeUnit.DAYS);

        return bestResults;
    }

    public Map<String, Double> runRecommendationTest(Long seed, Integer maxResults,
                                                     Integer inputSize, Integer expectedSize,
                                                     RecommendationAlgorithm algorithm,
                                                     Double power,
                                                     Double signatureWeight, Double noteWeight,
                                                     Double topWeight, Double heartWeight, Double baseWeight, Double mixedWeight,
                                                     Double mixedExponentialPenalty, Double priceDiscrimination) {
        Map<String, List<Integer>> rankMap = new HashMap<>();
        int rankCounter = 0;
        int rankable = 0;
        Double minRank = maxResults + 1.0;
        List<Integer> allRankList = new ArrayList<>();
        NoteSimilarity noteSimilarity = new NoteSimilarity(frequencyAnalysis);
        noteSimilarity.setTopWeight(topWeight);
        noteSimilarity.setHeartWeight(heartWeight);
        noteSimilarity.setBaseWeight(baseWeight);
        noteSimilarity.setMixedWeight(mixedWeight);
        noteSimilarity.setMixedExponentialPenalty(mixedExponentialPenalty);

        NoteDistance noteDistance = new NoteDistance();
        noteDistance.setTopWeight(topWeight);
        noteDistance.setHeartWeight(heartWeight);
        noteDistance.setBaseWeight(baseWeight);
        noteDistance.setMixedWeight(mixedWeight);
        noteDistance.setMixedExponentialPenalty(mixedExponentialPenalty);

        SignatureSimilarity signatureSimilarity = new SignatureSimilarity(frequencyAnalysis);
        signatureSimilarity.setTopWeight(topWeight);
        signatureSimilarity.setHeartWeight(heartWeight);
        signatureSimilarity.setBaseWeight(baseWeight);
        signatureSimilarity.setMixedWeight(mixedWeight);
        signatureSimilarity.setMixedExponentialPenalty(mixedExponentialPenalty);

        SignatureDistance signatureDistance = new SignatureDistance();
        signatureDistance.setTopWeight(topWeight);
        signatureDistance.setHeartWeight(heartWeight);
        signatureDistance.setBaseWeight(baseWeight);
        signatureDistance.setMixedWeight(mixedWeight);
        signatureDistance.setMixedExponentialPenalty(mixedExponentialPenalty);

        PerfumeRecommender perfumeRecommender = new PerfumeRecommender(iterator.getPerfumeList(), noteSimilarity, signatureSimilarity);
        perfumeRecommender.setPower(power);
        perfumeRecommender.setSignatureWeight(signatureWeight);
        perfumeRecommender.setNoteWeight(noteWeight);
        perfumeRecommender.setPriceDiscriminationFactor(priceDiscrimination);
        Random random = new Random(seed);
        Double rankPoints = 0.0;
        for (Map.Entry<String, List<Perfume>> entry : preferenceMap.entrySet()) {


            SortedMap<Double, Perfume> mostSimilar;
            ArrayList<Perfume> allPerfumes = new ArrayList<>();
            allPerfumes.addAll(entry.getValue());
            Collections.shuffle(allPerfumes, random);
            ArrayList<Perfume> chosenPerfumes = new ArrayList<>();
            ArrayList<Perfume> expectedPerfumes = new ArrayList<>();
            int cutIndex;
            //cutIndex=allPerfumes.size()/2+allPerfumes.size()%2;
            cutIndex = 1;
            chosenPerfumes.addAll(allPerfumes.subList(0, cutIndex));
            expectedPerfumes.addAll(allPerfumes.subList(cutIndex, allPerfumes.size()));
            rankable += expectedPerfumes.size();
            if (algorithm.equals(RecommendationAlgorithm.favoriteSimilarity))
                mostSimilar = perfumeRecommender.recommendByPerfumeSimilarity(chosenPerfumes, chosenPerfumes.get(0).getGender(), true, maxResults, null);
            else
                throw new NotImplementedException("We haven't implemented " + algorithm.toString());
            Integer rankIndex = 0;
            List<Integer> rankArray = new ArrayList<>();

            for (Map.Entry<Double, Perfume> rankEntry : mostSimilar.entrySet()) {
                if (expectedPerfumes.contains(rankEntry.getValue())) {
                    rankArray.add(rankIndex);
                    rankPoints += (maxResults - rankIndex - 1.0);
                    minRank = Math.min(minRank, rankIndex);
                    rankCounter++;
                    allRankList.add(rankIndex);
                }
                rankIndex++;
            }
            rankMap.put(entry.getKey(), rankArray);
        }
        Double rankedPercent = (100.0 * (double) rankCounter / rankable);
        Double averageRank = allRankList.stream().mapToDouble(val -> val).average().getAsDouble();
        Double blendedRank = (100.0 * (double) rankCounter / rankable) / averageRank;
//        logger.info("Ranked percent: \t"+rankedPercent+
//                "\tAverage rank: \t"+averageRank+
//                        "\tBlended rank: \t" +blendedRank
//        );
        Map<String, Double> results = new HashMap<>();
        results.put("rankedPercent", rankedPercent);
        results.put("averageRank", averageRank);
        results.put("blendedRank", blendedRank);
        results.put("rankPoints", rankPoints);
        results.put("minRank", minRank);
        return results;
    }


    public void selfCompareTest() {
        this.selfCompareTest(19910626L, 5000, 2, 2, RecommendationAlgorithm.favoriteSimilarity, 1.0, 1.0, 0.5,
                1.0, 0.75, 2.0 / 3.0, 1.0);
    }

    private void selfCompareTest(Long seed, Integer maxResults,
                                 Integer inputSize, Integer expectedSize,
                                 RecommendationAlgorithm algorithm,
                                 Double power,
                                 Double signatureWeight, Double noteWeight,
                                 Double topWeight, Double heartWeight, Double baseWeight,
                                 Double mixedExponentialPenalty) {
        Map<String, List<Integer>> rankMap = new HashMap<>();
        NoteSimilarity noteSimilarity = new NoteSimilarity(frequencyAnalysis);
        noteSimilarity.setTopWeight(topWeight);
        noteSimilarity.setHeartWeight(heartWeight);
        noteSimilarity.setBaseWeight(baseWeight);
        noteSimilarity.setMixedExponentialPenalty(mixedExponentialPenalty);

        NoteDistance noteDistance = new NoteDistance();
        noteDistance.setTopWeight(topWeight);
        noteDistance.setHeartWeight(heartWeight);
        noteDistance.setBaseWeight(baseWeight);
        noteDistance.setMixedExponentialPenalty(mixedExponentialPenalty);

        SignatureSimilarity signatureSimilarity = new SignatureSimilarity(frequencyAnalysis);
        signatureSimilarity.setTopWeight(topWeight);
        signatureSimilarity.setHeartWeight(heartWeight);
        signatureSimilarity.setBaseWeight(baseWeight);
        signatureSimilarity.setMixedExponentialPenalty(mixedExponentialPenalty);

        SignatureDistance signatureDistance = new SignatureDistance();
        signatureDistance.setTopWeight(topWeight);
        signatureDistance.setHeartWeight(heartWeight);
        signatureDistance.setBaseWeight(baseWeight);
        signatureDistance.setMixedExponentialPenalty(mixedExponentialPenalty);

        PerfumeRecommender perfumeRecommender = new PerfumeRecommender(iterator.getPerfumeList(), noteSimilarity, signatureSimilarity);
        perfumeRecommender.setPower(power);
        perfumeRecommender.setSignatureWeight(signatureWeight);
        perfumeRecommender.setNoteWeight(noteWeight);
        perfumeRecommender.setPriceDiscriminationFactor(1.50);
        Histogram hNS = new Histogram(0.0, 1.5, 100);
        Histogram hSS = new Histogram(0.0, 1.5, 100);

        for (Perfume perfume : iterator.getPerfumeList()) {
            Double sSimilarity = signatureSimilarity.getBlendedSignatureSimilarity(perfume, perfume);
            Double nSimilarity = noteSimilarity.getBlendedNoteSimilarity(perfume, perfume);
            Double sDistance = signatureDistance.getBlendedSignatureDistance(perfume, perfume);
            Double nDistance = noteDistance.getBlendedNoteDistance(perfume, perfume);
            System.out.println("ID: " + perfume.getId() + "\tSS: " + sSimilarity + "\tNS: " + nSimilarity + "\tSD: " + sDistance + "\tND:" + nDistance);
            hNS.put(nSimilarity);
            hSS.put(sSimilarity);
        }

        hNS.print();
        System.out.println("-----------------");
        hSS.print();
        System.out.println("-----------------");
    }

    public void distanceHistogram() {

        NoteDistance noteDistance = new NoteDistance();
        SignatureSimilarity signatureSimilarity = new SignatureSimilarity(frequencyAnalysis);
        NoteSimilarity noteSimilarity = new NoteSimilarity(frequencyAnalysis);
        SignatureDistance signatureDistance = new SignatureDistance();

        Histogram hNS = new Histogram(0.1, 0.9, 100);
        Histogram hSS = new Histogram(0.1, 0.9, 100);
        Integer index = 0;
        for (Perfume perfume1 : iterator.getPerfumeList()) {
            if (perfume1.getTopNotes().size() == 0)
                continue;
            for (Perfume perfume2 : iterator.getPerfumeList()) {
                if (perfume1.getAffiliateProducts().size() == 0 || perfume1.getTopNotes().size() == 0)
                    continue;
                if (!perfume1.equals(perfume2)) {
                    Double sSimilarity = signatureSimilarity.getBlendedSignatureSimilarity(perfume1, perfume2);
                    Double nSimilarity = noteSimilarity.getBlendedNoteSimilarity(perfume1, perfume2);
                    hNS.put(nSimilarity);
                    hSS.put(sSimilarity);
                }
            }
            logger.info("Done " + hNS.getCounter());
            if (Math.random() < 0.05) {
                hNS.print();
                System.out.println("-----------------");
                hSS.print();
                System.out.println("-----------------");
            }

        }

        hNS.print();
        System.out.println("-----------------");
        hSS.print();
        System.out.println("-----------------");
    }

    public void showStandardPrices() {
        PriceEvaluator evaluator = new PriceEvaluator();
        for (Perfume perfume : iterator.getPerfumeList()) {
            //if (perfume.getAffiliateProducts().size()>0)
            {
                System.out.println(perfume.getSearchableName() + " : " + evaluator.getPriceStandard100ml(perfume) + " RON");
            }
        }
    }

    public void evalutePrices() {
        PriceEvaluator evaluator = new PriceEvaluator();
        Map<Double, List<Double>> priceMap = evaluator.getPriceMap(iterator.getPerfumeList());
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        logger.info("Average standard: " + evaluator.getAverageStandardPrice(iterator.getPerfumeList()));
        logger.info("Median standard: " + evaluator.getPercentileStandardPrice(iterator.getPerfumeList(), 0.5));
        logger.info("66% standard: " + evaluator.getPercentileStandardPrice(iterator.getPerfumeList(), 2.0 / 3.0));
        logger.info("75% standard: " + evaluator.getPercentileStandardPrice(iterator.getPerfumeList(), 0.75));
        logger.info("90% standard: " + evaluator.getPercentileStandardPrice(iterator.getPerfumeList(), 0.9));
        logger.info("Histogram:");
        evaluator.getHistogramStandardPrice(iterator.getPerfumeList()).print();

        Map<Double, Double> priceAverages = evaluator.getAverageFromMap(priceMap);
        Map<Double, Double> priceMedian = evaluator.getPercentileFromMap(priceMap, 0.5);
        Map<Double, Double> price90P = evaluator.getPercentileFromMap(priceMap, 0.9);
        logger.info("Average: " + gson.toJson(priceAverages));
        logger.info("Median: " + gson.toJson(priceMedian));
        logger.info("90%: " + gson.toJson(price90P));
    }

}
