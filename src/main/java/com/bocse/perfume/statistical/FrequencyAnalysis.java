package com.bocse.perfume.statistical;

import com.bocse.perfume.data.NoteType;
import com.bocse.perfume.data.Perfume;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by bogdan.bocse on 3/24/2016.
 */
public class FrequencyAnalysis implements InterfaceFrequencyAnalysis {
    private final static Logger logger = Logger.getLogger(FrequencyAnalysis.class.toString());
    private final Double rarityFallback = 0.0;
    private final Double frequnecyFallBack = 0.0;

    private Map<NoteType, Double> topNoteTypeFrequency = new HashMap<>();
    private Map<NoteType, Double> heartNoteTypeFrequency = new HashMap<>();
    private Map<NoteType, Double> baseNoteTypeFrequency = new HashMap<>();
    private Map<NoteType, Double> mixedNoteTypeFrequency = new HashMap<>();

    private Map<String, Double> topNoteFrequency = new HashMap<>();
    private Map<String, Double> heartNoteFrequency = new HashMap<>();
    private Map<String, Double> baseNoteFrequency = new HashMap<>();
    private Map<String, Double> mixedNoteFrequency = new HashMap<>();

    private List<Perfume> perfumeList;

    public FrequencyAnalysis(List<Perfume> perfumeList) {
        this.perfumeList = perfumeList;
    }

    @Override
    public void process() {
        Long startTime = System.currentTimeMillis();
        processForAllPerfumes();
        normalizeAllMaps();
        Long endTime = System.currentTimeMillis();
        logger.info("Frequency analysis done in " + (endTime - startTime) + "ms.");
        //printMap(mixedNoteFrequency);
        //System.out.println("----");
        //printMap(mixedNoteTypeFrequency);
        //System.out.println("----");
    }

    private void processForAllPerfumes() {
        Double perfumeCount = perfumeList.size() + 0.0;
        for (Perfume perfume : perfumeList) {
            processNoteTypesForPerfume(perfume);
            processNotesForPerfume(perfume);
        }
    }

    private void processNotesForPerfume(Perfume perfume) {
        processNotesForMap(perfume.getBaseNotes(), baseNoteFrequency);
        processNotesForMap(perfume.getHeartNotes(), heartNoteFrequency);
        processNotesForMap(perfume.getTopNotes(), topNoteFrequency);
        processNotesForMap(perfume.getMixedNotes(), mixedNoteFrequency);
    }

    private void processNoteTypesForPerfume(Perfume perfume) {
        processNoteTypesForMap(perfume.getBaseSignature(), baseNoteTypeFrequency);
        processNoteTypesForMap(perfume.getHeartSignature(), heartNoteTypeFrequency);
        processNoteTypesForMap(perfume.getTopSignature(), topNoteTypeFrequency);
        processNoteTypesForMap(perfume.getMixedSignature(), mixedNoteTypeFrequency);
    }

    private void processNoteTypesForMap(Map<NoteType, Double> perfumeSignature, Map<NoteType, Double> globalMap) {
        for (Map.Entry<NoteType, Double> perfumeSignatureEntry : perfumeSignature.entrySet()) {
            globalMap.putIfAbsent(perfumeSignatureEntry.getKey(), 0.0);
            globalMap.put(perfumeSignatureEntry.getKey(), globalMap.get(perfumeSignatureEntry.getKey()) + perfumeSignatureEntry.getValue());
        }
    }

    private void processNotesForMap(List<String> perfumeNotes, Map<String, Double> globalMap) {
        for (String note : perfumeNotes) {
            globalMap.putIfAbsent(note, 0.0);
            globalMap.put(note, globalMap.get(note) + 1.0);
        }
    }

    private void normalizeAllMaps() {
        normalizeNoteTypeMap(topNoteTypeFrequency);
        normalizeNoteTypeMap(heartNoteTypeFrequency);
        normalizeNoteTypeMap(baseNoteTypeFrequency);
        normalizeNoteTypeMap(mixedNoteTypeFrequency);

        normalizeNoteMap(topNoteFrequency);
        normalizeNoteMap(heartNoteFrequency);
        normalizeNoteMap(baseNoteFrequency);
        normalizeNoteMap(mixedNoteFrequency);

    }

    private void normalizeNoteTypeMap(Map<NoteType, Double> globalMap) {
        Double sum = 0.0;
        for (Double value : globalMap.values()) {
            sum += value;
        }
        for (Map.Entry<NoteType, Double> entry : globalMap.entrySet()) {
            Double normalizedValue = Math.log(entry.getValue() / sum);
            entry.setValue(normalizedValue);
        }
    }

    private void normalizeNoteMap(Map<String, Double> globalMap) {
        Double sum = 0.0;
        for (Double value : globalMap.values()) {
            sum += value;
        }
        for (Map.Entry<String, Double> entry : globalMap.entrySet()) {
            Double normalizedValue = Math.log(entry.getValue() / sum);
            entry.setValue(normalizedValue);
        }
    }

    private void printMap(Map<?, Double> map) {
        for (Map.Entry<?, Double> entry : map.entrySet()) {
            System.out.println(entry.getKey() + " : " + (-entry.getValue()));
        }
    }

    private Double getNoteRariry(String note, Map<String, Double> map) {
        Double noteRarity = map.get(note);
        if (noteRarity != null) {
            return -noteRarity;
        } else {
            return rarityFallback;
        }
    }

    private Double getNoteFrequency(String note, Map<String, Double> map) {
        Double noteRarity = map.get(note);
        if (noteRarity != null) {
            return Math.exp(noteRarity);
        } else {
            return frequnecyFallBack;
        }
    }

    @Override
    public Double getTopNoteRarity(String note) {
        return getNoteRariry(note, topNoteFrequency);
    }

    @Override
    public Double getHeartNoteRarity(String note) {
        return getNoteRariry(note, heartNoteFrequency);
    }

    @Override
    public Double getBaseNoteRarity(String note) {
        return getNoteRariry(note, baseNoteFrequency);
    }

    @Override
    public Double getTopNoteFrequency(String note) {
        return getNoteFrequency(note, topNoteFrequency);
    }

    @Override
    public Double getHeartNoteFrequency(String note) {
        return getNoteFrequency(note, heartNoteFrequency);
    }

    @Override
    public Double getBaseNoteFrequency(String note) {
        return getNoteFrequency(note, baseNoteFrequency);
    }

    @Override
    public Double getMixedNoteFrequency(String note) {
        return getNoteFrequency(note, mixedNoteFrequency);
    }

    @Override
    public Double getTopNoteTypeFrequency(NoteType noteType) {
        return getNoteTypeFrequency(noteType, topNoteTypeFrequency);
    }

    @Override
    public Double getHeartNoteTypeFrequency(NoteType noteType) {
        return getNoteTypeFrequency(noteType, heartNoteTypeFrequency);
    }

    @Override
    public Double getBaseNoteTypeFrequency(NoteType noteType) {
        return getNoteTypeFrequency(noteType, baseNoteTypeFrequency);
    }

    @Override
    public Double getMixedNoteTypeFrequency(NoteType noteType) {
        return getNoteTypeFrequency(noteType, mixedNoteTypeFrequency);
    }

    @Override
    public Double getMixedNoteRarity(String note) {
        return getNoteRariry(note, mixedNoteFrequency);
    }

    private Double getNoteTypeRariry(NoteType noteType, Map<NoteType, Double> map) {
        Double noteRarity = map.get(noteType);
        if (noteRarity != null) {
            return -noteRarity;
        } else {
            return rarityFallback;
        }
    }

    private Double getNoteTypeFrequency(NoteType noteType, Map<NoteType, Double> map) {
        Double noteRarity = map.get(noteType);
        if (noteRarity != null) {
            return Math.exp(noteRarity);
        } else {
            return frequnecyFallBack;
        }
    }

    @Override
    public Double getTopNoteTypeRarity(NoteType noteType) {
        return getNoteTypeRariry(noteType, topNoteTypeFrequency);
    }

    @Override
    public Double getHeartNoteTypeRarity(NoteType noteType) {
        return getNoteTypeRariry(noteType, heartNoteTypeFrequency);
    }

    @Override
    public Double getBaseNoteTypeRarity(NoteType noteType) {
        return getNoteTypeRariry(noteType, baseNoteTypeFrequency);
    }

    @Override
    public Double getMixedNoteTypeRarity(NoteType noteType) {
        return getNoteTypeRariry(noteType, mixedNoteTypeFrequency);
    }

}