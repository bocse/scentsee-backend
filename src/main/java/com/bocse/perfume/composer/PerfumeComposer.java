package com.bocse.perfume.composer;

import com.bocse.perfume.data.*;
import com.bocse.perfume.raw.RawMaterialCollection;
import com.bocse.perfume.signature.SignatureComposer;
import com.bocse.perfume.signature.SignatureEvaluator;
import com.bocse.perfume.statistical.CollocationAnalysis;
import com.bocse.perfume.statistical.FrequencyAnalysis;
import com.bocse.perfume.utils.TextUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;

import java.util.*;
import java.util.logging.Logger;

/**
 * Created by bogdan.bocse on 13/07/16.
 */
public class PerfumeComposer {
    private final static Logger logger = Logger.getLogger(PerfumeComposer.class.toString());
    private final static double probabilityScaling = 1000.0;
    private final Map<Gender, Map<String, Question>> questionnaireMapping;
    private final SignatureEvaluator signatureEvaluator;
    private final FrequencyAnalysis frequencyAnalysis;
    private final CollocationAnalysis collocationAnalysis;
    private final RawMaterialCollection rawMaterialCollection;
    private final List<Perfume> perfumeList;
    private final String vendor;

    private Map<NoteType, TreeMap<Double, String>> topTypeFrequencyProbabilityMap = new HashMap<>();
    private Map<NoteType, TreeMap<Double, String>> heartTypeFrequencyProbabilityMap = new HashMap<>();
    private Map<NoteType, TreeMap<Double, String>> baseTypeFrequencyProbabilityMap = new HashMap<>();
    private Map<NoteType, TreeMap<Double, String>> mixedTypeFrequencyProbabilityMap = new HashMap<>();

    private Map<NoteType, List<Map.Entry<String, Double>>> topTypeFrequencyDeterministicMap = new HashMap<>();
    private Map<NoteType, List<Map.Entry<String, Double>>> heartTypeFrequencyDeterministicMap = new HashMap<>();
    private Map<NoteType, List<Map.Entry<String, Double>>> baseTypeFrequencyDeterministicMap = new HashMap<>();
    private Map<NoteType, List<Map.Entry<String, Double>>> mixedTypeFrequencyDeterministicMap = new HashMap<>();


    public PerfumeComposer(
            List<Perfume> perfumeList,
            Map<Gender, Map<String, Question>> questionnaireMapping,
            SignatureEvaluator signatureEvaluator,
            RawMaterialCollection rawMaterialCollection,
            FrequencyAnalysis frequencyAnalysis,
            CollocationAnalysis collocationAnalysis,
            String vendor
    ) {
        this.questionnaireMapping = questionnaireMapping;
        this.signatureEvaluator = signatureEvaluator;
        this.frequencyAnalysis = frequencyAnalysis;
        this.perfumeList = perfumeList;
        this.rawMaterialCollection = rawMaterialCollection;
        this.collocationAnalysis = collocationAnalysis;
        this.vendor = vendor;
    }

    private Map<String, Double> prepareCollocationScores(ComposedPerfume composedPerfume) {
        Map<String, Set<NoteType>> noteTypeMap = signatureEvaluator.getNoteTypeMap();

        Set<String> mixedNotes = new HashSet<>();
        if (composedPerfume.getBaseChosenNotes() != null)
            mixedNotes.addAll(composedPerfume.getBaseChosenNotes().keySet());
        if (composedPerfume.getHeartChosenNotes() != null)
            mixedNotes.addAll(composedPerfume.getHeartChosenNotes().keySet());
        if (composedPerfume.getTopChosenNotes() != null)
            mixedNotes.addAll(composedPerfume.getTopChosenNotes().keySet());

        Map<String, Double> collocationScores = new HashMap<>();
        for (Map.Entry<String, Set<NoteType>> noteEntry : noteTypeMap.entrySet()) {
            String noteName = noteEntry.getKey();
            if (mixedNotes.contains(noteName)) {
                collocationScores.put(noteName, 0.0);
            } else {

                Double collocationScore = 1.0;
                if (getNoteInStock(noteName)) {
                    for (String chosenNote : mixedNotes) {
                        Double collocationIncrement1 = collocationAnalysis.collocation(noteName, chosenNote);
                        Double collocationIncrement2 = collocationAnalysis.collocation(chosenNote, noteName);
                        collocationScore += collocationIncrement1;
                    }
                }
                collocationScores.put(noteName, collocationScore);
            }
        }
        return collocationScores;
    }

    private void prepareTypeFrequencyMap(Map<String, Double> collocationScores) {
        Map<String, Set<NoteType>> noteTypeMap = signatureEvaluator.getNoteTypeMap();
        for (NoteType noteType : NoteType.values()) {
            topTypeFrequencyProbabilityMap.put(noteType, new TreeMap<>());
            heartTypeFrequencyProbabilityMap.put(noteType, new TreeMap<>());
            baseTypeFrequencyProbabilityMap.put(noteType, new TreeMap<>());
            mixedTypeFrequencyProbabilityMap.put(noteType, new TreeMap<>());

            topTypeFrequencyDeterministicMap.put(noteType, new ArrayList<>());
            heartTypeFrequencyDeterministicMap.put(noteType, new ArrayList<>());
            baseTypeFrequencyDeterministicMap.put(noteType, new ArrayList<>());
            mixedTypeFrequencyDeterministicMap.put(noteType, new ArrayList<>());


        }
        for (Map.Entry<String, Set<NoteType>> noteEntry : noteTypeMap.entrySet()) {
            String noteName = noteEntry.getKey();
            int splitFactor = noteEntry.getValue().size();
            if (getNoteInStock(noteName)) {
                for (NoteType noteType : noteEntry.getValue()) {
                    Double collocationScore = collocationScores.get(noteName);
                    if (collocationScore == null) {
                        throw new IllegalStateException("Collocation analysis: missing note " + noteName);
                    }
                    Double topProbability = probabilityScaling * collocationScore *
                            (frequencyAnalysis.getTopNoteFrequency(noteName) / splitFactor) *
                            getPriceWeight(noteName);

                    if (topProbability > 0.0) {
                        addNote(topTypeFrequencyProbabilityMap.get(noteType), noteEntry.getKey(), topProbability);
                        addNote(topTypeFrequencyDeterministicMap.get(noteType), noteEntry.getKey(), topProbability);
                    }

                    Double heartProbability = probabilityScaling * collocationScore *
                            (frequencyAnalysis.getHeartNoteFrequency(noteName) / splitFactor) *
                            getPriceWeight(noteName);

                    if (heartProbability > 0.0) {
                        addNote(heartTypeFrequencyProbabilityMap.get(noteType), noteEntry.getKey(), heartProbability);
                        addNote(heartTypeFrequencyDeterministicMap.get(noteType), noteEntry.getKey(), heartProbability);
                    }

                    Double baseProbability = probabilityScaling * collocationScore *
                            (frequencyAnalysis.getBaseNoteFrequency(noteName) / splitFactor) *
                            getPriceWeight(noteName);

                    if (baseProbability > 0.0) {
                        addNote(baseTypeFrequencyProbabilityMap.get(noteType), noteEntry.getKey(), baseProbability);
                        addNote(baseTypeFrequencyDeterministicMap.get(noteType), noteEntry.getKey(), baseProbability);
                    }
                    Double mixedProbability = probabilityScaling * collocationScore *
                            (frequencyAnalysis.getMixedNoteFrequency(noteName) / splitFactor) *
                            getPriceWeight(noteName);

                    if (mixedProbability > 0.0) {
                        addNote(mixedTypeFrequencyProbabilityMap.get(noteType), noteEntry.getKey(), mixedProbability);
                        addNote(mixedTypeFrequencyDeterministicMap.get(noteType), noteEntry.getKey(), mixedProbability);

                    }
                }
            }
        }

        Comparator<Map.Entry<String, Double>> comparator = new Comparator<Map.Entry<String, Double>>() {

            @Override
            public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2) {
                return -o1.getValue().compareTo(o2.getValue());
            }
        };

        for (List<Map.Entry<String, Double>> entry : mixedTypeFrequencyDeterministicMap.values()) {
            Collections.sort(entry, comparator);
        }
        for (List<Map.Entry<String, Double>> entry : topTypeFrequencyDeterministicMap.values()) {
            Collections.sort(entry, comparator);
        }
        for (List<Map.Entry<String, Double>> entry : heartTypeFrequencyDeterministicMap.values()) {
            Collections.sort(entry, comparator);
        }
        for (List<Map.Entry<String, Double>> entry : baseTypeFrequencyDeterministicMap.values()) {
            Collections.sort(entry, comparator);
        }
    }

    private Double getPriceWeight(String note) {
        //TODO: implement logic for considering the unit price of a note
        return 1.0;
    }

    private Boolean getNoteInStock(String note) {
        if (vendor != null && !vendor.isEmpty())
            return rawMaterialCollection.isInStock(note, vendor);
        else
            return rawMaterialCollection.isInStock(note);
    }


    private void addNote(TreeMap<Double, String> map, String note, Double probability) {
        Double cursor = 0.0;
        if (!map.isEmpty())
            cursor = map.lastKey();
        map.put(cursor + probability, note);
    }

    private void addNote(List<Map.Entry<String, Double>> list, String note, Double probability) {
        Map.Entry<String, Double> entry = new AbstractMap.SimpleEntry<String, Double>(note, probability);
        list.add(entry);
    }

    private String randomPull(TreeMap<Double, String> map, Double randomNumber) {
        if (map.isEmpty())
            return "";
        return map.higherEntry(map.lastKey() * randomNumber).getValue();
    }

    private String orderedPull(List<Map.Entry<String, Double>> list) {

        if (list.isEmpty())
            return "";
        Map.Entry<String, Double> entry = list.remove(0);
        return entry.getKey();
    }

    public ComposedPerfume getSuggestions(ComposedPerfume composedPerfume, Double shuffleFactor, Boolean regroup) {
        Map<String, String> answers = composedPerfume.getOriginatingAnswers();
        Gender gender = composedPerfume.getGender();
        if (vendor != null && !rawMaterialCollection.getVendors().contains(vendor))
            throw new IllegalStateException("Unknown vendor " + vendor);
        Map<String, Double> collocationScores = prepareCollocationScores(composedPerfume);
        prepareTypeFrequencyMap(collocationScores);
        SignatureComposer signatureComposer = new SignatureComposer(questionnaireMapping.get(gender));
        Map<NoteType, Double> composedSignature = signatureComposer.composeSignature(answers);
        LinkedHashMap<NoteType, Double> orderedList = orderSignature(composedSignature);
        String vendorName = composedPerfume.getRawMaterialsVendorName();
        logger.info("Signature: " + orderedList.toString());
        Long seed;
        //seed = 7111719232951L;
        seed = seedFromAnswers(answers);
        Random random = new Random(seed);
        Map<String, Double> topSet = new HashMap<>();
        Map<String, Double> heartSet = new HashMap<>();
        Map<String, Double> baseSet = new HashMap<>();
        Map<String, Double> mixedSet = new HashMap<>();

        Map<String, Double> chosenMixSet = new HashMap<>();

        if (composedPerfume.getBaseChosenNotes() != null)
            chosenMixSet.putAll(composedPerfume.getBaseChosenNotes());
        if (composedPerfume.getHeartChosenNotes() != null)
            chosenMixSet.putAll(composedPerfume.getHeartChosenNotes());
        if (composedPerfume.getTopChosenNotes() != null)
            chosenMixSet.putAll(composedPerfume.getTopChosenNotes());

        double signatureSum = 0.0;
        for (Map.Entry<NoteType, Double> noteTypeEntry : orderedList.entrySet()) {
            if (noteTypeEntry.getValue() > 0.0)
                signatureSum += noteTypeEntry.getValue();
        }


        for (Map.Entry<NoteType, Double> noteTypeEntry : orderedList.entrySet()) {
            double maxPicks = noteTypeEntry.getValue() * 10.0 / signatureSum;
            for (int pickIndex = 0; pickIndex < maxPicks; pickIndex++) {
                String topPick = "";
                String heartPick = "";
                String basePick = "";
                if (random.nextDouble() < shuffleFactor) {

                    topPick = randomPull(topTypeFrequencyProbabilityMap.get(noteTypeEntry.getKey()), random.nextDouble());
                    heartPick = randomPull(heartTypeFrequencyProbabilityMap.get(noteTypeEntry.getKey()), random.nextDouble());
                    basePick = randomPull(baseTypeFrequencyProbabilityMap.get(noteTypeEntry.getKey()), random.nextDouble());

                } else {
                    topPick = orderedPull(topTypeFrequencyDeterministicMap.get(noteTypeEntry.getKey()));
                    heartPick = orderedPull(heartTypeFrequencyDeterministicMap.get(noteTypeEntry.getKey()));
                    basePick = orderedPull(baseTypeFrequencyDeterministicMap.get(noteTypeEntry.getKey()));

                }
                if (!topPick.isEmpty() && !mixedSet.containsKey(topPick)) {
                    Double weight = noteTypeEntry.getValue();
                    topSet.put(topPick, weight);
                    mixedSet.put(topPick, weight);
                }
                if (!heartPick.isEmpty() && !mixedSet.containsKey(heartPick)) {
                    Double weight = noteTypeEntry.getValue();
                    heartSet.put(heartPick, weight);
                    mixedSet.put(heartPick, weight);
                }
                if (!basePick.isEmpty() && !mixedSet.containsKey(basePick)) {
                    Double weight = noteTypeEntry.getValue();
                    baseSet.put(basePick, weight);
                    mixedSet.put(basePick, weight);
                }
            }
        }

        if (regroup) {
            Map<PerfumeSegment, Map<String, Double>> reshuffledMap = reshuffleNotes(mixedSet);
            composedPerfume.setTopSuggestedNotes(reshuffledMap.get(PerfumeSegment.TOP));
            composedPerfume.setHeartSuggestedNotes(reshuffledMap.get(PerfumeSegment.HEART));
            composedPerfume.setBaseSuggestedNotes(reshuffledMap.get(PerfumeSegment.BASE));


        } else {

            composedPerfume.setTopSuggestedNotes(topSet);
            composedPerfume.setHeartSuggestedNotes(heartSet);
            composedPerfume.setBaseSuggestedNotes(baseSet);

        }
        Set<String> notesForName = new HashSet<>();
        notesForName.addAll(mixedSet.keySet());
        if (chosenMixSet != null)
            notesForName.addAll(chosenMixSet.keySet());
        composedPerfume.setSuggestedNames(generateRandomNames(notesForName, gender, 50, seed ^ System.nanoTime(), 2, 6, 0.2));
        return composedPerfume;
    }

    private Map<PerfumeSegment, Map<String, Double>> reshuffleNotes(Map<String, Double> mixedNotes) {
        Map<PerfumeSegment, Map<String, Double>> recommendedMap = new HashMap<>();
        Map<String, Double> topSet = new HashMap<>();
        Map<String, Double> heartSet = new HashMap<>();
        Map<String, Double> baseSet = new HashMap<>();
        for (Map.Entry<String, Double> note : mixedNotes.entrySet()) {
            PerfumeSegment segment = getMostLikelySegment(note.getKey());
            recommendedMap.putIfAbsent(segment, new HashMap<>());
            recommendedMap.get(segment).put(note.getKey(), note.getValue());
        }
        return recommendedMap;
    }

    private PerfumeSegment getMostLikelySegment(String noteName) {
        double topFrequency = frequencyAnalysis.getTopNoteFrequency(noteName);
        double heartFrequency = frequencyAnalysis.getHeartNoteFrequency(noteName);
        double baseFrequency = frequencyAnalysis.getBaseNoteFrequency(noteName);

        double maxFrequency = Math.max(topFrequency, Math.max(heartFrequency, baseFrequency));
        if (topFrequency == maxFrequency)
            return PerfumeSegment.TOP;
        if (heartFrequency == maxFrequency)
            return PerfumeSegment.HEART;
        if (baseFrequency == maxFrequency)
            return PerfumeSegment.BASE;
        else
            return null;
    }

    private LinkedHashMap<NoteType, Double> orderSignature(Map<NoteType, Double> noteMap) {
        List<Map.Entry<NoteType, Double>> list =
                new LinkedList<Map.Entry<NoteType, Double>>(noteMap.entrySet());

        Collections.sort(list, new Comparator<Map.Entry<NoteType, Double>>() {
            public int compare(Map.Entry<NoteType, Double> o1, Map.Entry<NoteType, Double> o2) {
                return -(o1.getValue()).compareTo(o2.getValue());
            }
        });

        LinkedHashMap<NoteType, Double> linkedHashMap = new LinkedHashMap<NoteType, Double>();

        int classIndex = 0;
        for (Map.Entry<NoteType, Double> entry : list) {
            if (entry.getKey().equals(NoteType.UNKNOWN) || entry.getKey().equals(NoteType.NON_CLASSIFIED)) {
                continue;
            }
            linkedHashMap.put(entry.getKey(), entry.getValue());


        }
        return linkedHashMap;
    }

    private List<String> generateRandomNames(Set<String> noteSeed, Gender gender, int nameCount, Long seed, int minLength, int maxLength, double spaceProbability) {
        Set<String> syllableSet = new HashSet<>();
        List<String> syllableList = new ArrayList<>();
        List<String> generatedNames = new ArrayList<>();
        int maxConsonant = 3;
        if (gender.equals(Gender.FEMALE))
            maxConsonant = 1;
        for (String note : noteSeed) {
            syllableSet.addAll(TextUtils.syllableSplit(note, maxConsonant));
        }

        syllableList.addAll(syllableSet);


        Random random = new Random(seed);

        for (int i = 0; i < nameCount; i++) {
            int count = minLength + random.nextInt(maxLength - minLength + 1);
            Set<Integer> chosenList = new HashSet<>();
            StringBuffer sb = new StringBuffer();
            int maxIteration = 1000;
            for (int j = 0; j < count; j++) {
                int syllableIndex = random.nextInt(syllableList.size());
                if (!chosenList.contains(syllableIndex)) {
                    String syllable = syllableList.get(syllableIndex);
                    if (random.nextDouble() < 0.0001)
                        syllable = StringUtils.reverse(syllable);
                    else {
                        if (j == count - 1 && gender.equals(Gender.MALE) && random.nextDouble() < 0.5)
                            syllable = StringUtils.reverse(syllable);
                    }
                    sb.append(syllable);
                    if (random.nextDouble() < spaceProbability)
                        sb.append(" ");
                    chosenList.add(syllableIndex);
                } else {
                    j = j - 1;
                }
                if (maxIteration <= 0)
                    break;
                maxIteration--;
            }
            generatedNames.add(WordUtils.capitalize(sb.toString().trim()));
        }
        Collections.sort(generatedNames);
        return generatedNames;
    }

    private Long seedFromAnswers(Map<String, String> answers) {
        Long seed = 1117192325235243427L;
        StringBuffer sb = new StringBuffer();
        for (String answer : answers.values()) {
            seed = seed ^ answer.hashCode();
            sb.append(answer);
        }

        return seed ^ sb.toString().hashCode();
    }
}
