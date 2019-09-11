package com.bocse.perfume.recommender;

import com.bocse.perfume.affiliate.AffiliateCollection;
import com.bocse.perfume.data.*;
import com.bocse.perfume.price.PriceEvaluator;
import com.bocse.perfume.signature.SignatureComposer;
import com.bocse.perfume.similarity.NoteSimilarity;
import com.bocse.perfume.similarity.SignatureSimilarity;
import com.bocse.perfume.utils.MathUtils;
import com.bocse.perfume.utils.SignatureUtils;
import com.bocse.perfume.utils.TextUtils;

import java.util.*;

/**
 * Created by bocse on 05.12.2015.
 */
public class PerfumeRecommender {
    private final Double similarityThreshold = 0.1;
    List<Perfume> perfumeCollection;
    private boolean filterGender = true;
    private Double power = 2.0;
    private Double sigma = 25.0;
    private Double priceDiscriminationFactor = 0.75;
    private Boolean includeSubstandard = true;
    private NoteSimilarity noteSimilarity = null;
    private SignatureSimilarity signatureSimilarity = null;
    private Double noteWeight = 1.0;
    private Double signatureWeight = 1.0;

    public PerfumeRecommender(List<Perfume> perfumeCollection, NoteSimilarity noteSimilarity, SignatureSimilarity signatureSimilarity) {
        this.perfumeCollection = perfumeCollection;
        this.noteSimilarity = noteSimilarity;
        this.signatureSimilarity = signatureSimilarity;

    }

    public Double getPower() {
        return power;
    }

    public void setPower(Double power) {
        this.power = power;
    }

    public Double getNoteWeight() {
        return noteWeight;
    }

    public void setNoteWeight(Double noteWeight) {
        this.noteWeight = noteWeight;
    }

    public Double getSignatureWeight() {
        return signatureWeight;
    }

    public void setSignatureWeight(Double signatureWeight) {
        this.signatureWeight = signatureWeight;
    }

    /*
        public PerfumeRecommender(List<Perfume> perfumeCollection)
        {
            this.perfumeCollection=perfumeCollection;
            noteSimilarity=new NoteSimilarity();
            noteDistance=new NoteDistance();
            signatureSimilarity=new SignatureSimilarity();
            signatureDistance=new SignatureDistance();
        }
    */
    private Gender getAprioriGender(List<Perfume> apriori) {
        Gender aprioriGender = Gender.UNI;
        for (Perfume perfume : apriori) {

            if (!perfume.getGender().equals(Gender.UNI)) {
                if (!aprioriGender.equals(Gender.UNI) && filterGender && !aprioriGender.equals(perfume.getGender())) {
                    throw new IllegalStateException("Cannot filter by gender while not all perfumes are for the same gender.");
                }
                aprioriGender = perfume.getGender();

            }

        }
        return aprioriGender;
    }

    private SortedMap<Double, Perfume> cropRecommendation(SortedMap<Double, Perfume> recommendations, int maxRecommendations) {
        SortedMap<Double, Perfume> recommendationsCropped = new TreeMap<>(Collections.reverseOrder());
        int recommendationIndex = 0;
        for (Map.Entry<Double, Perfume> entry : recommendations.entrySet()) {

            recommendationsCropped.put(entry.getKey(), entry.getValue());
            recommendationIndex++;

            if (recommendationIndex >= maxRecommendations)
                break;

        }
        return recommendationsCropped;
    }

    private Double radialBaseFunctionExponential(Double radius) {

        return Math.exp(-radius / sigma);
    }

    private Double radialBaseFunctionQuadratic(Double radius) {
        return 1.0 / (1.0 + radius * radius / sigma);
    }

    private Double getAveragePrice(List<Perfume> apriori) {
        PriceEvaluator priceEvaluator = new PriceEvaluator();
        List<Double> prices = new ArrayList<>();
        for (Perfume perfume : apriori) {
            Double price = priceEvaluator.getPriceStandard100ml(perfume);
            prices.add(price);
        }
        Double averagePrice = prices.stream().mapToDouble(val -> val).average().getAsDouble();
        return averagePrice;
    }

    private Double priceDifferentialQuadratic(Double originalPrice, Double recommendedPrice) {
        if (originalPrice == null)
            return 1.0;
        if (originalPrice == 0) {
            throw new IllegalStateException("Cannot make reference to price zero.");
        }
        Double ratio = Math.abs((originalPrice - recommendedPrice) / originalPrice);
        return 1.0 / (1.0 + ratio * ratio * priceDiscriminationFactor);
    }
    /*
    public SortedMap<Double, Perfume> recommendByPerfumeDistance(List<Perfume> apriori, int maxRecommendations) {
        SortedMap<Double, Perfume> recommendations = new TreeMap<>(java.util.Collections.reverseOrder());
        Gender aprioriGender=getAprioriGender(apriori);
        Double averagePrice=getAveragePrice(apriori);
        PriceEvaluator priceEvaluator=new PriceEvaluator();

        //NoteDistance noteSimilarity = new NoteDistance();
        //SignatureDistance signatureSimilarity = new SignatureDistance();
        for (Perfume candidate : perfumeCollection) {
            if (!candidate.getGender().equals(Gender.UNI) && !aprioriGender.equals(Gender.UNI) && !aprioriGender.equals(candidate.getGender()))
                continue;
            if (apriori.contains(candidate))
                continue;
            if (candidate.getAffiliateProducts().size()>0 || allowNonAffiliate)
            {
                Double objectiveFunction = 0.0;
                for (Perfume reference : apriori) {
                    Double signatureDistance = this.signatureDistance.getBlendedSignatureDistance(reference, candidate);
                    Double noteDistance = this.noteDistance.getBlendedNoteDistance(reference, candidate);
                    Double overallDistance = (signatureWeight * MathUtils.pow(signatureDistance, power) + noteWeight * MathUtils.pow(noteDistance, power)) / (noteWeight + signatureWeight);
                    objectiveFunction += radialBaseFunctionQuadratic(overallDistance);
                }
                objectiveFunction=objectiveFunction/apriori.size();
                objectiveFunction=objectiveFunction*priceDifferentialQuadratic(averagePrice, priceEvaluator.getPriceStandard100ml(candidate));
                recommendations.put(objectiveFunction, candidate);
            }
        }

        //minObjective=recommendations.firstKey();
        //maxObjective=recommendations.lastKey();

        return cropRecommendation(recommendations, maxRecommendations);
    }
*/

    public SortedMap<Double, Perfume> recommendByAdvancesSearch(
            Gender gender,
            Double minPrice, //hard filter
            Double maxPrice,
            Double preferredPrice,
            Map<NoteType, Double> signatureDelta,
            List<Perfume> aprioriAffinity,
            List<Perfume> aprioriAversion,
            List<String> fullNameFragments,
            List<String> brandFragments,
            List<String> nameFragments,
            Boolean inStockAtLeastOneVendor,
            AffiliateCollection acceptedAffiliateList,
            int maxRecommendations) {
        if (gender == null) {
            if (aprioriAffinity != null && !aprioriAffinity.isEmpty())
                gender = getAprioriGender(aprioriAffinity);
            else if (aprioriAversion != null && !aprioriAversion.isEmpty()) {
                gender = getAprioriGender(aprioriAversion);
            }
        }

        SignatureUtils.normalizeSignature(signatureDelta);
        SignatureComposer signatureComposer = new SignatureComposer();
        PerfumeSegmentedSignature segmentedSignature = signatureComposer.composeSignature(
                aprioriAffinity, aprioriAversion, signatureSimilarity.getTopWeight(), signatureSimilarity.getHeartWeight(), signatureSimilarity.getBaseWeight());
        SignatureUtils.normalizeSignature(segmentedSignature.getTop());
        SignatureUtils.normalizeSignature(segmentedSignature.getHeart());
        SignatureUtils.normalizeSignature(segmentedSignature.getBase());
        SignatureUtils.normalizeSignature(segmentedSignature.getMixed());


        SignatureUtils.setSignatureAccumulator(segmentedSignature.getTop(), signatureDelta);
        SignatureUtils.setSignatureAccumulator(segmentedSignature.getHeart(), signatureDelta);
        SignatureUtils.setSignatureAccumulator(segmentedSignature.getBase(), signatureDelta);
        SignatureUtils.setSignatureAccumulator(segmentedSignature.getMixed(), signatureDelta);

        SignatureUtils.cullSignature(segmentedSignature.getTop());
        SignatureUtils.cullSignature(segmentedSignature.getHeart());
        SignatureUtils.cullSignature(segmentedSignature.getBase());
        SignatureUtils.cullSignature(segmentedSignature.getMixed());
        SortedMap<Double, Perfume> result = recommendByAdvancesSearchIterator(
                gender,
                minPrice,
                maxPrice,
                preferredPrice,
                segmentedSignature,
                aprioriAffinity,
                aprioriAversion,
                fullNameFragments,
                brandFragments,
                nameFragments,
                inStockAtLeastOneVendor,
                acceptedAffiliateList,
                maxRecommendations);
        return result;
    }

    public SortedMap<Double, Perfume> recommendByAdvancesSearchIterator(
            Gender gender,
            Double minPrice, //hard filter
            Double maxPrice, //hard filter
            Double preferredPrice, //objective
            PerfumeSegmentedSignature signature,
            List<Perfume> aprioriAffinity, //objective
            List<Perfume> aprioriAversion, //objective
            List<String> fullNameFragments,
            List<String> brandFragments,
            List<String> nameFragments,
            Boolean inStockAtLeastOneVendor,
            AffiliateCollection acceptedAffiliateList,
            int maxRecommendations) {

        if (acceptedAffiliateList != null) {
            if (acceptedAffiliateList.getAffiliates() == null || acceptedAffiliateList.getAffiliates().isEmpty()) {
                acceptedAffiliateList = null;
            }
        }
        SortedMap<Double, Perfume> recommendations = new TreeMap<>(java.util.Collections.reverseOrder());

        if (fullNameFragments != null) {
            for (int i = 0; i < fullNameFragments.size(); i++) {
                String flattened = TextUtils.flattenToAscii(fullNameFragments.get(i)).toLowerCase();
                fullNameFragments.set(i, flattened);
            }
        }

        if (nameFragments != null) {
            for (int i = 0; i < nameFragments.size(); i++) {
                String flattened = TextUtils.flattenToAscii(nameFragments.get(i)).toLowerCase();
                nameFragments.set(i, flattened);
            }
        }

        if (brandFragments != null) {
            for (int i = 0; i < brandFragments.size(); i++) {
                String flattened = TextUtils.flattenToAscii(brandFragments.get(i)).toLowerCase();
                brandFragments.set(i, flattened);
            }
        }
        PriceEvaluator priceEvaluator = new PriceEvaluator();

        for (Perfume candidate : perfumeCollection) {
            //TODO: iterate through vendors/affiliates
            Double candidatePrice = priceEvaluator.getPriceStandard100ml(candidate);
            if (minPrice != null) {
                if (candidatePrice < minPrice) {
                    continue;
                }
            }
            if (maxPrice != null) {
                if (candidatePrice > maxPrice) {
                    continue;
                }

            }

            if (candidate.isSubstandard() && !includeSubstandard) {
                continue;
            }
            if (gender != null) {
                if (!candidate.getGender().equals(Gender.UNI) && !gender.equals(Gender.UNI) && !gender.equals(candidate.getGender()))
                    continue;
            }
            if (aprioriAffinity.contains(candidate))
                continue;
            if (aprioriAversion.contains(candidate))
                continue;
            if (fullNameFragments != null && !fullNameFragments.isEmpty()) {
                Boolean nameMatch = false;
                for (String fullNameFragment : fullNameFragments) {
                    if (candidate.getSearchableName().contains(fullNameFragment)) {
                        nameMatch = true;
                        break;
                    }
                }
                if (!nameMatch)
                    continue;
            }
            if (brandFragments != null && !brandFragments.isEmpty()) {
                Boolean nameMatch = false;
                for (String brandFragment : brandFragments) {
                    if (TextUtils.flattenToAscii(candidate.getBrand()).toLowerCase().contains(brandFragment)) {
                        nameMatch = true;
                        break;
                    }
                }
                if (!nameMatch)
                    continue;
            }
            if (nameFragments != null && !nameFragments.isEmpty()) {
                Boolean nameMatch = false;
                for (String nameFragment : nameFragments) {
                    if (TextUtils.flattenToAscii(candidate.getName()).toLowerCase().contains(nameFragment)) {
                        nameMatch = true;
                        break;
                    }
                }
                if (!nameMatch)
                    continue;
            }
            if (acceptedAffiliateList != null) {
                List<String> matchedAffiliates = new ArrayList<>();
                for (String affiliateName : acceptedAffiliateList.getAffiliates()) {
                    if (candidate.getAffiliateProducts().containsKey(affiliateName)) {
                        matchedAffiliates.add(affiliateName);
                    }
                }

                if (matchedAffiliates.size() == 0) {
                    continue;
                }
            } else {
                if (inStockAtLeastOneVendor && candidate.getAffiliateProducts().size() == 0) {
                    continue;
                }

            }
            //Arithmethic initialization
            Double averageAffinityMatch = 0.0;
            //Geometric initilization
            //Double averageMatch = 1.0;
            for (Perfume reference : aprioriAffinity) {
                Double noteMatch = this.noteSimilarity.getBlendedNoteSimilarity(reference, candidate);
                //Arithmetic mean
                Double overallMatch = noteMatch, power;
                averageAffinityMatch += overallMatch;

            }
            if (aprioriAffinity.size() > 0) {
                averageAffinityMatch = MathUtils.pow(averageAffinityMatch / aprioriAffinity.size(), 1.0 / power);
            } else {
                averageAffinityMatch = 0.0;
            }
            Double averageAversionMatch = 0.0;
            //Geometric initilization
            //Double averageMatch = 1.0;
            for (Perfume reference : aprioriAversion) {
                //Double signatureMatch = this.signatureSimilarity.getBlendedSignatureSimilarity(reference, candidate);
                Double noteMatch = this.noteSimilarity.getBlendedNoteSimilarity(reference, candidate);
                //Arithmetic mean
                Double overallMatch = noteMatch;
                averageAversionMatch += overallMatch;
            }
            if (aprioriAversion.size() > 0) {
                averageAversionMatch = MathUtils.pow(averageAversionMatch / aprioriAversion.size(), 1.0 / power);
            } else {
                averageAversionMatch = 0.0;
            }
            Double signatureMatch = this.signatureSimilarity.getBlendedSignatureSimilarity(signature, candidate);

            Double averageDiffrentialMatch = ((averageAffinityMatch - averageAversionMatch) * noteWeight + signatureMatch * signatureWeight) / (signatureWeight + noteWeight) * priceDifferentialQuadratic(preferredPrice, priceEvaluator.getPriceStandard100ml(candidate));
            if (Double.isNaN(averageDiffrentialMatch) || Double.isInfinite(averageDiffrentialMatch)) {
                continue;
            }
            if (averageDiffrentialMatch > similarityThreshold) {
                recommendations.put(averageDiffrentialMatch, candidate);
            }
        }


        return cropRecommendation(recommendations, maxRecommendations);
    }

    public SortedMap<Double, Perfume> recommendByPerfumeSimilarity(List<Perfume> apriori,
                                                                   Gender gender,
                                                                   Boolean inStockAtLeastOneVendor,
                                                                   int maxRecommendations,
                                                                   AffiliateCollection acceptedAffiliateList) {
        if (acceptedAffiliateList != null) {
            if (acceptedAffiliateList.getAffiliates() == null || acceptedAffiliateList.getAffiliates().isEmpty()) {
                acceptedAffiliateList = null;
            }
        }
        SortedMap<Double, Perfume> recommendations = new TreeMap<>(java.util.Collections.reverseOrder());
        //Gender aprioriGender=getAprioriGender(apriori);
        Double averagePrice = getAveragePrice(apriori);
        PriceEvaluator priceEvaluator = new PriceEvaluator();
        for (Perfume candidate : perfumeCollection) {
            if (gender != null &&
                    !candidate.getGender().equals(Gender.UNI) && !gender.equals(candidate.getGender())) {
                continue;
            }
            //if (!candidate.getGender().equals(Gender.UNI) && !aprioriGender.equals(Gender.UNI) && !aprioriGender.equals(candidate.getGender()))
            //    continue;
            if (apriori.contains(candidate))
                continue;
            if (candidate.isSubstandard() && !includeSubstandard) {
                continue;
            }
            if (candidate.getAffiliateProducts().size() > 0 || (!inStockAtLeastOneVendor)) {

                if (acceptedAffiliateList == null || !inStockAtLeastOneVendor || hasAtLeastOneAffiliate(acceptedAffiliateList, candidate)) {
                    //Arithmethic initialization
                    Double averageMatch = 0.0;
                    //Geometric initilization
                    //Double averageMatch = 1.0;
                    for (Perfume reference : apriori) {
                        Double signatureMatch = this.signatureSimilarity.getBlendedSignatureSimilarity(reference, candidate);
                        Double noteMatch = this.noteSimilarity.getBlendedNoteSimilarity(reference, candidate);
                        //Arithmetic mean
                        Double overallMatch = (this.signatureWeight * MathUtils.pow(signatureMatch, power) + this.noteWeight * MathUtils.pow(noteMatch, power)) / (this.noteWeight + this.signatureWeight);
                        averageMatch += overallMatch;
                        //Geometric mean
                        //Double overallMatch =  MathUtils.pow(MathUtils.pow(signatureMatch, signatureWeight) *  MathUtils.pow(noteMatch, power), 1/(signatureWeight+noteWeight));
                        //averageMatch *= overallMatch;
                    }
                    //Arithmethic
                    averageMatch = MathUtils.pow(averageMatch / apriori.size(), 1.0 / power);
                    //Geometric
                    //averageMatch=MathUtils.pow(averageMatch, 1.0/apriori.size());

                    //Price differential correction
                    averageMatch *= priceDifferentialQuadratic(averagePrice, priceEvaluator.getPriceStandard100ml(candidate));
                    if (Double.isNaN(averageMatch) || Double.isInfinite(averageMatch)) {
                        continue;
                    }
                    if (averageMatch > similarityThreshold) {
                        recommendations.put(averageMatch, candidate);
                    }
                }
            }
        }

        return cropRecommendation(recommendations, maxRecommendations);
    }

    public SortedMap<Double, Perfume> recommendByQuestionnaireSimilarity(Map<Gender, Map<String, Question>> questionnaireMapping, Map<String, String> answers, Double averagePrice, Gender gender,
                                                                         Boolean inStockAtLeastOneVendor,
                                                                         int maxRecommendations,
                                                                         AffiliateCollection collection) {

        if (collection != null) {
            if (collection.getAffiliates() == null || collection.getAffiliates().isEmpty()) {
                collection = null;
            }
        }
        SortedMap<Double, Perfume> recommendations = new TreeMap<>(java.util.Collections.reverseOrder());
        PriceEvaluator priceEvaluator = new PriceEvaluator();
        SignatureComposer signatureComposer = new SignatureComposer(questionnaireMapping.get(gender));
        Map<NoteType, Double> composedSignature = signatureComposer.composeSignature(answers);
        //NoteSimilarity noteSimilarity = new NoteSimilarity();
        //SignatureSimilarity signatureSimilarity = new SignatureSimilarity();
        for (Perfume candidate : perfumeCollection) {
            if (candidate.isSubstandard() && !includeSubstandard) {
                continue;
            }
            if (candidate.getGender().equals(gender) || candidate.getGender().equals(Gender.UNI) || (gender == null)) {
                if (candidate.getAffiliateProducts().size() > 0 || (!inStockAtLeastOneVendor)) {
                    if (collection == null || !inStockAtLeastOneVendor || hasAtLeastOneAffiliate(collection, candidate)) {
                        //Arithmethic initialization
                        Double averageMatch = 0.0;
                        //Geometric initilization
                        //Double averageMatch = 1.0;
                        Double signatureMatch = this.signatureSimilarity.signatureSimilarity(composedSignature, candidate.getMixedSignature());
                        averageMatch = signatureMatch * priceDifferentialQuadratic(averagePrice, priceEvaluator.getPriceStandard100ml(candidate));
                        if (Double.isNaN(averageMatch) || Double.isInfinite(averageMatch)) {
                            continue;
                        }
                        if (averageMatch > similarityThreshold) {
                            recommendations.put(averageMatch, candidate);
                        }
                    }
                }
            }
        }
        return cropRecommendation(recommendations, maxRecommendations);
    }


    public NoteSimilarity getNoteSimilarity() {
        return noteSimilarity;
    }

    public void setNoteSimilarity(NoteSimilarity noteSimilarity) {
        this.noteSimilarity = noteSimilarity;
    }

    public SignatureSimilarity getSignatureSimilarity() {
        return signatureSimilarity;
    }

    public void setSignatureSimilarity(SignatureSimilarity signatureSimilarity) {
        this.signatureSimilarity = signatureSimilarity;
    }

    public Double getPriceDiscriminationFactor() {
        return priceDiscriminationFactor;
    }

    public void setPriceDiscriminationFactor(Double priceDiscriminationFactor) {
        this.priceDiscriminationFactor = priceDiscriminationFactor;
    }

    public boolean hasAtLeastOneAffiliate(AffiliateCollection collection, Perfume perfume) {
        for (String affiliate : collection.getAffiliates()) {
            if (perfume.getAffiliateProducts().containsKey(affiliate)) {
                return true;
            }
        }
        return false;
    }

    public Boolean getIncludeSubstandard() {
        return includeSubstandard;
    }

    public void setIncludeSubstandard(Boolean includeSubstandard) {
        this.includeSubstandard = includeSubstandard;
    }
}
