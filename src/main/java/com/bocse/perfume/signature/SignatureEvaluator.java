package com.bocse.perfume.signature;

import com.bocse.perfume.data.NoteType;
import com.bocse.perfume.data.Perfume;
import com.bocse.perfume.data.RecommendedPerfume;
import com.bocse.perfume.iterator.Reloadable;
import com.bocse.perfume.requester.HttpRequester;
import com.bocse.perfume.utils.TextUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.lang.time.StopWatch;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.joda.time.DateTime;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Logger;

/**
 * Created by bocse on 27.12.2015.
 */
public class SignatureEvaluator implements Reloadable {
    private final static Logger logger = Logger.getLogger(SignatureEvaluator.class.toString());
    private final int maxDominantClasses = 5;

    private DateTime assetLastModified = null;
    private DateTime lastLoadTime = null;
    private DateTime lastSwapTime = null;

    private Map<String, Set<NoteType>> noteTypeMap;
    private Map<String, Set<NoteType>> backgroundNoteTypeMap;

    private Map<NoteType, String> classUrl = new HashMap<>();
    private Map<NoteType, List<String>> classUrlSets = new HashMap<>();

    public SignatureEvaluator() {
        initUrls();
    }

    private void initUrls() {
        classUrl.put(NoteType.ALDEHYDE, "[ URLs must be replaced in the code] ");

    }

    @Override
    public void iterateAndKeep(File file) throws IOException {
        logger.info("Loading classes in memory from " + file.getAbsolutePath());
        StopWatch iteatorWatch = new StopWatch();
        iteatorWatch.start();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        Reader reader = null;
        try {
            //FileReader reader;
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
            assetLastModified = new DateTime(file.lastModified());
            Type listType = new TypeToken<Map<String, Set<NoteType>>>() {
            }.getType();
            Map<String, Set<NoteType>> localNoteTypeMap = gson.fromJson(reader, listType);
            backgroundNoteTypeMap = localNoteTypeMap;
            iteatorWatch.stop();
            lastLoadTime = DateTime.now();
            logger.info("Loaded " + localNoteTypeMap.size() + " items in " + iteatorWatch.getTime() + "ms");
            return;
        } finally {

            if (reader != null)
                reader.close();
            System.gc();
        }
    }

    public Map<NoteType, Double> evaluateSignatureFromNoteList(List<String> notes) {
        Map<NoteType, Double> signature = new HashMap<>();
        if (notes == null)
            return null;
        for (String note : notes) {
            Set<NoteType> signaturePart = noteTypeMap.get(note);
            if (signaturePart == null) {
                signaturePart = new HashSet<>();
                signaturePart.add(NoteType.UNKNOWN);
            }
            for (NoteType noteType : signaturePart) {
                signature.putIfAbsent(noteType, 0.0);
                signature.put(noteType, signature.get(noteType) + 1.0);
            }
        }
        return signature;
    }

    public Map<NoteType, Double> signatureCulling(Map<NoteType, Double> originalSignature) {
        Map<NoteType, Double> culledSignature = new HashMap<>();
        Double dominance = 0.0;
        for (Map.Entry<NoteType, Double> entry : originalSignature.entrySet()) {
            if (entry.getValue() > dominance)
                dominance = entry.getValue();
        }
        for (Map.Entry<NoteType, Double> entry : originalSignature.entrySet()) {
            if (entry.getValue() == dominance) {
                culledSignature.put(entry.getKey(), entry.getValue());
            }
        }
        return culledSignature;
    }

    public void embedPerfumeSignature(Perfume perfume) {
        perfume.setTopSignature(evaluateSignatureFromNoteList(perfume.getTopNotes()));
        perfume.setHeartSignature(evaluateSignatureFromNoteList(perfume.getHeartNotes()));
        perfume.setBaseSignature(evaluateSignatureFromNoteList(perfume.getBaseNotes()));
        perfume.setMixedSignature(evaluateSignatureFromNoteList(perfume.getMixedNotes()));
    }

    public Map<NoteType, Double> signatureDifference(Map<NoteType, Double> signature1, Map<NoteType, Double> signature2) {
        Map<NoteType, Double> difference = new HashMap<>();
        for (Map.Entry<NoteType, Double> entry : signature1.entrySet()) {
            if (signature2.containsKey(entry.getKey()))
                difference.putIfAbsent(entry.getKey(), entry.getValue() - signature2.get(entry.getKey()));
            else
                difference.putIfAbsent(entry.getKey(), entry.getValue());
        }
        for (Map.Entry<NoteType, Double> entry : signature2.entrySet()) {
            if (!signature1.containsKey(entry.getKey()))
                difference.putIfAbsent(entry.getKey(), -entry.getValue());
        }
        return difference;
    }

    public Map<NoteType, Double> orderSignature(Map<NoteType, Double> noteMap) {
        List<Map.Entry<NoteType, Double>> list =
                new LinkedList<Map.Entry<NoteType, Double>>(noteMap.entrySet());


        Collections.sort(list, new Comparator<Map.Entry<NoteType, Double>>() {
            public int compare(Map.Entry<NoteType, Double> o1, Map.Entry<NoteType, Double> o2) {
                return -(o1.getValue()).compareTo(o2.getValue());
            }
        });
        Map<NoteType, Double> result = new LinkedHashMap<NoteType, Double>();


        for (Map.Entry<NoteType, Double> entry : list) {
            if (entry.getKey().equals(NoteType.UNKNOWN) || entry.getKey().equals(NoteType.NON_CLASSIFIED)) {
                continue;
            }
            result.put(entry.getKey(), entry.getValue());

        }
        return result;
    }

    public void embedDominantClasses(RecommendedPerfume recommended, Perfume perfume) {

        Map<NoteType, Double> noteMap = this.evaluateSignatureFromNoteList(perfume.getMixedNotes());
        List<Map.Entry<NoteType, Double>> list =
                new LinkedList<Map.Entry<NoteType, Double>>(noteMap.entrySet());
        Double sumWeight = 1.0;
        for (Double weight : noteMap.values()) {
            sumWeight += weight;
        }

        Collections.sort(list, new Comparator<Map.Entry<NoteType, Double>>() {
            public int compare(Map.Entry<NoteType, Double> o1, Map.Entry<NoteType, Double> o2) {
                return -(o1.getValue()).compareTo(o2.getValue());
            }
        });
        Map<NoteType, Double> result = new LinkedHashMap<NoteType, Double>();

        int classIndex = 0;
        for (Map.Entry<NoteType, Double> entry : list) {
            if (entry.getKey().equals(NoteType.UNKNOWN) || entry.getKey().equals(NoteType.NON_CLASSIFIED)) {
                continue;
            }
            result.put(entry.getKey(), Math.round((entry.getValue()) / sumWeight * 100.0) + 0.0);
            classIndex++;
            if (classIndex >= maxDominantClasses)
                break;
        }
        recommended.setDominantClasses(result);
        //return result;
    }

    public void embedPerfumeCulledSignature(Perfume perfume) {
        perfume.setTopSignature(signatureCulling(evaluateSignatureFromNoteList(perfume.getTopNotes())));
        perfume.setHeartSignature(signatureCulling(evaluateSignatureFromNoteList(perfume.getHeartNotes())));
        perfume.setBaseSignature(signatureCulling(evaluateSignatureFromNoteList(perfume.getBaseNotes())));
        perfume.setMixedSignature(signatureCulling(evaluateSignatureFromNoteList(perfume.getMixedNotes())));
    }

    public Map<String, Set<NoteType>> getAllNoteClasses() throws IOException, InterruptedException {
        Map<String, Set<NoteType>> allMap = new HashMap<>();
        for (Map.Entry<NoteType, String> entry : classUrl.entrySet()) {
            //allMap.putAll(getClassNotes(entry.getValue(), entry.getKey()));
            Map<String, NoteType> partialMap = getClassNotes(entry.getValue(), entry.getKey());
            for (Map.Entry<String, NoteType> partialEntry : partialMap.entrySet()) {
                allMap.putIfAbsent(partialEntry.getKey(), new HashSet<>());
                allMap.get(partialEntry.getKey()).add(partialEntry.getValue());
            }
        }
        for (Map.Entry<NoteType, List<String>> entry : classUrlSets.entrySet()) {
            Map<String, NoteType> partialMap = new HashMap<>();
            for (String subEntry : entry.getValue()) {
                partialMap.putAll(getClassNotes(subEntry, entry.getKey()));
            }
            for (Map.Entry<String, NoteType> partialEntry : partialMap.entrySet()) {
                allMap.putIfAbsent(partialEntry.getKey(), new HashSet<>());
                allMap.get(partialEntry.getKey()).add(partialEntry.getValue());
            }
        }
        return allMap;
    }


    private HttpUriRequest createNoteClassDocumentRequest(final String url) {
        String newUrl = url.replaceAll(" ", "%20");
        final HttpGet httpGet = new HttpGet(newUrl);
        final RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(9000).setConnectTimeout(9000).setSocketTimeout(9000).build();
        httpGet.setConfig(requestConfig);
        logger.info("Executing request " + httpGet.getRequestLine());
        return httpGet;

    }

    public Map<String, NoteType> getClassNotes(String url, NoteType noteType) throws IOException, InterruptedException {
        Document notesDocument = HttpRequester.getDocument(createNoteClassDocumentRequest(url));
        Map<String, NoteType> map = new HashMap<>();
        Elements elements = notesDocument.select("body > div.body-wrapper > div > div.main > div.notes_group > div > a");
        for (Element element : elements) {
            String name = TextUtils.cleanupAndFlatten(element.text().trim().toLowerCase());
            map.put(name, noteType);
        }
        return map;
    }

    public Map<String, Set<NoteType>> getNoteTypeMap() {
        return noteTypeMap;
    }


    @Override
    public boolean swap() {
        if (backgroundNoteTypeMap != null) {
            noteTypeMap = backgroundNoteTypeMap;
            lastSwapTime = DateTime.now();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void mirror() {
        noteTypeMap = backgroundNoteTypeMap;
    }

    @Override
    public boolean cleanupBackground() {
        backgroundNoteTypeMap = null;
        return true;
    }

    @Override
    public Boolean shouldReload(File file) {
        if (assetLastModified == null || lastLoadTime == null)
            return true;
        else {
            DateTime newAssetDate = new DateTime(file.lastModified());
            if (newAssetDate.getMillis() > assetLastModified.getMillis())
                return true;
            else
                return false;
        }
    }
}
