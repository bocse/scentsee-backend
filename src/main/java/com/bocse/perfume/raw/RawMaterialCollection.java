package com.bocse.perfume.raw;

import com.bocse.perfume.data.RawMaterial;
import com.bocse.perfume.signature.SignatureEvaluator;
import com.bocse.perfume.utils.TextUtils;
import org.apache.commons.lang.time.StopWatch;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

/**
 * Created by bogdan.bocse on 15/07/16.
 */
public class RawMaterialCollection {
    private final static Logger logger = Logger.getLogger(RawMaterialCollection.class.toString());
    private final SignatureEvaluator signatureEvaluator;
    private Map<String, Set<RawMaterial>> mismatchedRawMaterials = new HashMap<>();
    private Map<String, Set<RawMaterial>> rawMaterials = new HashMap<>();
    private Map<String, String> compactedNotes;
    private Map<String, Set<String>> apartNotes;
    private Map<String, Map<RawMaterial, Double>> noteMaterialMapping = new HashMap<>();
    private Set<String> vendors = new HashSet<>();
    private Set<String> blacklist;

    public RawMaterialCollection(SignatureEvaluator signatureEvaluator) {
        this.signatureEvaluator = signatureEvaluator;
        initBlacklist();
        prepareNotes();
    }

    private void initBlacklist() {
        blacklist = new HashSet<>();
        blacklist.add("absolute");
        blacklist.add("essential oil");
        blacklist.add("absolute");
        blacklist.add("hydrosol");
        blacklist.add("blend");
        blacklist.add("complete");
        blacklist.add("organic");
        blacklist.add("hexane");
        blacklist.add("ct geraniol");
        blacklist.add("ct linalool");
        blacklist.add("ct camphor");
        blacklist.add("clearance");
        blacklist.add("ct thujanol");
        //blacklist.add("");

    }

    private String removeParanthesisContents(String name) {
        return name.replaceAll("\\(.*\\)", " ");
    }

    private String removeBlacklistWord(String string) {
        for (String blacklistWord : blacklist) {
            string = string.replaceAll("\\W" + blacklistWord + "\\W|^" + blacklistWord + "\\W|\\W" + blacklistWord + "$", " ");
        }
        return string;
    }

    private void prepareNotes() {
        compactedNotes = new HashMap<>();

        apartNotes = new HashMap<>();

        for (String note : signatureEvaluator.getNoteTypeMap().keySet()) {
            compactedNotes.put(note.replaceAll(" ", ""), note);
            Set<String> parts = new HashSet<>();
            parts.addAll(Arrays.asList(note.split(" ")));
            apartNotes.put(note, parts);
        }


    }

    private Map<String, Double> matchNotes(RawMaterial rawMaterial) {
        Map<String, Double> matchedNotes = new HashMap<>();
        String[] rawMaterialNameParts = rawMaterial.getName().split(" ");
        if (signatureEvaluator.getNoteTypeMap().containsKey(rawMaterial.getName())) {
            matchedNotes.put(rawMaterial.getName(), 1.0);
            return matchedNotes;
        }
        String rawMaterialNameNoSpaces = rawMaterial.getName().replaceAll(" ", "");
        String noteFromCompacted = compactedNotes.get(rawMaterialNameNoSpaces);

        if (noteFromCompacted != null) {
            matchedNotes.put(noteFromCompacted, 0.99);
            return matchedNotes;
        }

        List<String> materialParts = Arrays.asList(rawMaterial.getName().split(" "));
        for (String note : signatureEvaluator.getNoteTypeMap().keySet()) {
            Set<String> noteParts = apartNotes.get(note);
            int matchedParts = 0;
            for (String materialPart : materialParts) {
                if (noteParts.contains(materialPart))
                    matchedParts++;
            }
            double matchRate = 1.0 * (double) matchedParts / materialParts.size();
            //TODO: determine match rate
            if (matchRate >= 0.50) {
                matchedNotes.put(note, matchRate);
            }
        }

        for (String note : signatureEvaluator.getNoteTypeMap().keySet()) {
            if (note.startsWith(rawMaterial.getName())) {
                matchedNotes.put(note, (double) rawMaterial.getName().length() / note.length());
            }
        }
        return matchedNotes;
    }

    public void loadFromTextFile(String vendorName, String path) throws IOException {
        BufferedReader br = null;
        int matchedNotes = 0;
        int missedNotes = 0;
        String line;
        try {
            logger.info("Loading raw materials from vendor " + vendorName);
            StopWatch iteatorWatch = new StopWatch();
            iteatorWatch.start();
            br = new BufferedReader(new FileReader(new File(path)));
            while ((line = br.readLine()) != null) {
                String lineCleanup = line.trim();
                String fullName = lineCleanup;

                String rawMaterialName = lineCleanup.split("[,-]")[0].toLowerCase();
                int indexOfCo2 = rawMaterialName.indexOf("co2");
                if (indexOfCo2 > 0)
                    rawMaterialName = rawMaterialName.substring(0, indexOfCo2);
                rawMaterialName = rawMaterialName.trim();
                rawMaterialName = removeParanthesisContents(rawMaterialName);
                rawMaterialName = removeBlacklistWord(rawMaterialName);
                rawMaterialName = TextUtils.cleanupAndFlatten(rawMaterialName).trim();

                if (rawMaterialName.isEmpty())
                    continue;
                RawMaterial rawMaterial = new RawMaterial();
                rawMaterial.setName(rawMaterialName);
                rawMaterial.setDescription(fullName);
                rawMaterial.setVendor(vendorName);

                //Set<NoteType> matchedNote=signatureEvaluator.getNoteTypeMap().get(rawMaterialName);
                Map<String, Double> matchedNotesMap = matchNotes(rawMaterial);
                if (!matchedNotesMap.isEmpty()) {
                    matchedNotes++;
                    for (Map.Entry<String, Double> noteEntry : matchedNotesMap.entrySet()) {
                        noteMaterialMapping.putIfAbsent(noteEntry.getKey(), new HashMap<>());
                        noteMaterialMapping.get(noteEntry.getKey()).put(rawMaterial, noteEntry.getValue());
                    }
                    rawMaterials.putIfAbsent(vendorName, new HashSet<>());
                    rawMaterials.get(vendorName).add(rawMaterial);
                } else {
                    mismatchedRawMaterials.putIfAbsent(vendorName, new HashSet<>());
                    mismatchedRawMaterials.get(vendorName).add(rawMaterial);
                    missedNotes++;
                }

            }
            iteatorWatch.stop();
            logger.info("Finished loading raw materials from vendor " + vendorName +
                    ". Matched materials " + matchedNotes + " missed " + missedNotes + " in " + iteatorWatch.getTime() + "ms");
            logger.info("Matched notes " + noteMaterialMapping.size());
            vendors.add(vendorName);
            //printMapping();
        } finally {
            if (br != null)
                br.close();
        }
    }

    private void printMapping() {
        for (Map.Entry<String, Map<RawMaterial, Double>> entry : noteMaterialMapping.entrySet()) {
            for (Map.Entry<RawMaterial, Double> materialEntry : entry.getValue().entrySet()) {
                logger.info(entry.getKey() + "\t->\t" + materialEntry.getKey().getName() + "\t" + materialEntry.getValue());
            }
        }
    }

    public boolean isInStock(String note) {

        return noteMaterialMapping.containsKey(note);
    }


    public boolean isInStock(String note, String vendor) {
        Map<RawMaterial, Double> availableMaterials = noteMaterialMapping.get(note);
        if (availableMaterials != null) {
            for (RawMaterial availableMaterial : availableMaterials.keySet()) {
                if (availableMaterial.getVendor().equals(vendor))
                    return true;
            }
        }
        return false;
    }

    public Double mappingLikihood(String note) {
        Map<RawMaterial, Double> availableMaterials = noteMaterialMapping.get(note);

        Double max = 0.0;
        if (availableMaterials != null) {
            for (Double value : availableMaterials.values()) {
                max = Math.max(max, value);
            }

        }
        return max;
    }

    public Double mappingLikihood(String note, String vendor) {
        Map<RawMaterial, Double> availableMaterials = noteMaterialMapping.get(note);

        Double max = 0.0;
        if (availableMaterials != null) {
            for (Map.Entry<RawMaterial, Double> entry : availableMaterials.entrySet()) {
                if (entry.getKey().getVendor().equals(vendor))
                    max = Math.max(max, entry.getValue());
            }

        }
        return max;
    }

    public Set<String> getVendors() {
        return vendors;
    }


}
