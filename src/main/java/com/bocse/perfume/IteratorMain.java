package com.bocse.perfume;

import com.bocse.perfume.data.AffiliatePerfume;
import com.bocse.perfume.data.Gender;
import com.bocse.perfume.data.Perfume;
import com.bocse.perfume.iterator.PerfumeIterator;
import com.bocse.perfume.signature.SignatureEvaluator;
import com.bocse.perfume.similarity.SignatureSimilarity;
import com.bocse.perfume.statistical.DummyFrequencyAnalysis;
import com.bocse.perfume.statistical.FrequencyAnalysis;
import com.bocse.perfume.statistical.InterfaceFrequencyAnalysis;
import com.bocse.perfume.utils.TextUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.FileConfiguration;
import org.apache.commons.configuration.PropertiesConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by bogdan.bocse on 12/2/2015.
 */
public class IteratorMain {
    public final static FileConfiguration configuration = new PropertiesConfiguration();
    private final static Logger logger = Logger.getLogger(PerfumeMain.class.toString());

    public static void main(String[] args) throws IOException, InterruptedException, ConfigurationException {
        configuration.load(args[0]);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        PerfumeIterator perfumeIterator = new PerfumeIterator();

        perfumeIterator.iterateAndKeep(new File(configuration.getString("iterator.input.path")));
        perfumeIterator.swap();
        List<Perfume> perfumes = perfumeIterator.getPerfumeList();
        for (Perfume perfume : perfumeIterator.getPerfumeList()) {
            if (perfume.getName().toLowerCase().contains("poison") &&
                    perfume.getName().toLowerCase().contains("girl")) {
                logger.info(perfume.getSearchableName());
            }
        }
        SignatureEvaluator signatureEvaluator = new SignatureEvaluator();
        signatureEvaluator.iterateAndKeep(new File(configuration.getString("output.notes.path")));
        signatureEvaluator.swap();

        for (Perfume perfume : perfumeIterator.getPerfumeList()) {
            signatureEvaluator.embedPerfumeSignature(perfume);
        }

        InterfaceFrequencyAnalysis frequencyAnalysis = new FrequencyAnalysis(perfumeIterator.getPerfumeList());

        frequencyAnalysis.process();
        SignatureSimilarity signatureSimilarity = new SignatureSimilarity(new DummyFrequencyAnalysis());
        //Map<String, Note> noteMap = new HashMap<>();
        //noteMap=gson.fromJson(new FileReader(new File(configuration.getString("output.notes.path"))),  noteMap.getClass());
        //noteMap = fragranticaParser.parseNotes();
        //classificationSignatureEvaluator = new ClassificationSignatureEvaluator(noteMap);

        int strawberryCounter = 0;
        int aoroCounter = 0;
        int bothCounter = 0;
        int intersectionCounter = 0;
        for (Perfume perfume : perfumes) {
            boolean aoro = false;
            boolean strawberry = false;

            if (perfume.getAffiliateProducts().containsKey("strawberry")) {
                if (perfume.getAffiliateProducts().get("strawberry").size() > 0) {
                    logger.info(perfume.getUrl());
                    strawberryCounter++;
                    strawberry = true;
                }

            }

            if (perfume.getAffiliateProducts().containsKey("aoro")) {
                if (perfume.getAffiliateProducts().get("aoro").size() > 0) {
                    logger.info(perfume.getUrl());
                    aoroCounter++;
                    aoro = true;
                }
            }
            if (aoro && strawberry) {
                intersectionCounter++;
            }
            if (aoro || strawberry) {
                bothCounter++;
            }
        }
        logger.info("Stawberry " + strawberryCounter);
        logger.info("Aoro " + aoroCounter);
        logger.info("Intersection " + intersectionCounter);
        logger.info("Union " + bothCounter);
        Thread.sleep(1000);
        System.exit(0);
        logger.info("Starting write-back");
        for (Perfume perfume : perfumes) {
            perfume.setUrl(perfume.getUrl());
            perfume.setId(TextUtils.hash(perfume.getUrl()));
        }
        //JsonSerializer.serialize(configuration.getString("iterator.input.path")+"compact.json",perfumes);

        logger.info("Finished write-back");
        List<Perfume> chosenPerfumes = new ArrayList<>();
        int goodPerfumes = 0;
        List<Float> prices = new ArrayList<>();
        for (Perfume perfume : perfumes) {
            if (perfume.getAffiliateProducts().size() > 0 &&
                    !perfume.isSubstandard()) {
                goodPerfumes++;
                for (List<AffiliatePerfume> affiliateList : perfume.getAffiliateProducts().values())
                    for (AffiliatePerfume affiliatePerfume : affiliateList) {
                        prices.add(affiliatePerfume.getPrice());
                    }
            }
        }


        int goodParfumesNoVendor = 0;
        for (Perfume perfume : perfumes) {
            if (perfume.getAffiliateProducts().size() == 0 &&
                    !perfume.isSubstandard() && perfume.getInProduction()) {
                //logger.info(perfume.getBrand()+" - "+perfume.getName());
                goodParfumesNoVendor++;
            }
        }

        logger.info("Price: " + prices);
        float averagePrice = 0.0F;
        for (Float price : prices)
            averagePrice += price;
        averagePrice /= prices.size();
        logger.info("Average price: " + averagePrice);
        logger.info("Good perfumes with no vendor: " + goodParfumesNoVendor);
        logger.info("Good perfumes with vendor: " + goodPerfumes);
        //System.exit(0);
        for (Perfume perfume : perfumes) {
            if (perfume.getBrand().toLowerCase().contains("amouage") && perfume.getName().toLowerCase().equals("lyric woman")) {
                chosenPerfumes.add(perfume);
            }
            if (perfume.getName().toLowerCase().contains("eternity now") && perfume.getGender().equals(Gender.FEMALE)) {
                chosenPerfumes.add(perfume);
            }
            if (perfume.getName().toLowerCase().contains("datura noir") && perfume.getGender().equals(Gender.UNI)) {
                //chosenPerfumes.add(perfume);
            }

        }

    }
}
