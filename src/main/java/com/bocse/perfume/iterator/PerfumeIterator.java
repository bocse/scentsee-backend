package com.bocse.perfume.iterator;

import com.bocse.perfume.data.Perfume;
import com.bocse.perfume.utils.SynonymManager;
import com.bocse.perfume.utils.TextUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.lang.time.StopWatch;
import org.joda.time.DateTime;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Logger;

/**
 * Created by bocse on 02.12.2015.
 */
public class PerfumeIterator implements Reloadable {

    private final static Logger logger = Logger.getLogger(PerfumeIterator.class.toString());
    private boolean removeSubstandard = false;
    //private String path;
    private DateTime assetLastModified = null;
    private DateTime lastLoadTime = null;
    private DateTime lastSwapTime = null;

    private SynonymManager synonymManager = new SynonymManager();
    private volatile Set<String> brandsWithExcludedLogo = new HashSet<>();
    private volatile Set<String> brandsCompletelyExcluded = new HashSet<>();

    private volatile List<Perfume> backgroundPerfumeList;
    private volatile Map<Long, Perfume> backgroundPerfumeMap;
    private volatile Map<String, List<Perfume>> backgroundBrandMap;
    private volatile Map<Perfume, String> nameLexicographicContraction = new HashMap<>();
    private volatile List<Perfume> perfumeList;
    private volatile Map<Long, Perfume> perfumeMap;
    private volatile Map<String, List<Perfume>> brandMap;


    public PerfumeIterator() {
        synonymManager.defaultInit();
    }

    public synchronized void addBrandsWithExcludedLogo(List<String> brands) {
        if (brands.isEmpty())
            return;
        for (String excludedBrand : brands) {
            brandsWithExcludedLogo.add(excludedBrand.toLowerCase().trim());
        }
    }

    public synchronized void addBrandsCompletelyExcluded(List<String> brands) {
        if (brands.isEmpty())
            return;
        for (String excludedBrand : brands) {
            brandsCompletelyExcluded.add(excludedBrand.toLowerCase().trim());
        }
    }

    @Override
    public void iterateAndKeep(File file) throws IOException {
        logger.info("Loading perfumes in memory from " + file.getAbsolutePath());
        StopWatch iteratorWatch = new StopWatch();
        iteratorWatch.start();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Map<String, List<Perfume>> localBrandMap = new HashMap<>();
        Reader reader = null;
        try {
            //FileReader reader;
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
            assetLastModified = new DateTime(file.lastModified());
            Type listType = new TypeToken<ArrayList<Perfume>>() {
            }.getType();
            List<Perfume> localPerfumeList = gson.fromJson(reader, listType);
            //this.perfumeList = gson.fromJson(reader, listType);
            Map<Long, Perfume> localPerfumeMap = new HashMap<>();
            //perfumeMap = new HashMap<>();
            List<Perfume> localEditedPerfumeList = new ArrayList<>();
            for (Perfume perfume : localPerfumeList) {
                Long id = perfume.getId();
                //perfume.setId(id);
                String brandFlattened = TextUtils.cleanupAndFlatten(perfume.getBrand()).toLowerCase();
                //Being careful about what info we expose to the outside world
                perfume.setUrl("");
                if (brandsCompletelyExcluded.contains(brandFlattened.toLowerCase())) {
                    continue;
                }
                if (brandsWithExcludedLogo.contains(brandFlattened.toLowerCase())) {
                    perfume.setPictureURL("");
                }
                localEditedPerfumeList.add(perfume);
                perfume.setSearchableName(TextUtils.cleanupAndFlatten(perfume.getName() + " " + perfume.getBrand()).toLowerCase());
                String contraction = synonymManager.transform(perfume.getSearchableName());
                nameLexicographicContraction.put(perfume, contraction);
                localPerfumeMap.put(id, perfume);
                if (!localBrandMap.containsKey(brandFlattened))
                    localBrandMap.put(brandFlattened, new ArrayList<>());
                localBrandMap.get(brandFlattened).add(perfume);
            }
            this.backgroundPerfumeList = localEditedPerfumeList;
            this.backgroundPerfumeMap = localPerfumeMap;
            this.backgroundBrandMap = localBrandMap;
            this.lastLoadTime = DateTime.now();
            iteratorWatch.stop();
            logger.info("Loaded " + backgroundPerfumeList.size() + " items in " + iteratorWatch.getTime() + "ms");

            return;
        } finally {

            if (reader != null)
                reader.close();
            System.gc();
        }

    }

    public void serialize(File outputFile) throws IOException {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        boolean pretty = false;
        // if file doesnt exists, then create it
        if (!outputFile.exists()) {
            outputFile.createNewFile();
        }
        BufferedOutputStream bos = null;
        try {
            bos = new BufferedOutputStream(new FileOutputStream(outputFile));
            Gson gson;
            if (pretty) {
                gson = new GsonBuilder().setPrettyPrinting().create();
            } else {
                gson = new GsonBuilder().create();
            }
            String jsonString = gson.toJson(perfumeList);

            byte[] latin2JsonString = jsonString.getBytes("UTF-8");
            bos.write(latin2JsonString);
            stopWatch.stop();
            logger.info("Serializing perfumes in " + stopWatch.getTime() + "ms.");
        } finally {
            if (bos != null)
                bos.close();
        }

    }


    @Override
    public boolean swap() {
        if (backgroundPerfumeList != null && backgroundBrandMap != null && backgroundPerfumeMap != null) {
            perfumeList = backgroundPerfumeList;
            brandMap = backgroundBrandMap;
            perfumeMap = backgroundPerfumeMap;
            lastSwapTime = DateTime.now();
            logger.info("Swapped in new perfume collection.");
            return true;
        } else {
            logger.info("Swap failed - nothing to swap.");
            return false;
        }
    }

    @Override
    public void mirror() {
        backgroundPerfumeList = perfumeList;
        backgroundBrandMap = brandMap;
        backgroundPerfumeMap = perfumeMap;
    }

    @Override
    public boolean cleanupBackground() {
        backgroundPerfumeList = null;
        backgroundPerfumeMap = null;
        backgroundPerfumeList = null;
        System.gc();
        logger.info("Cleanup memory from background.");
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

    public List<Perfume> getPerfumeList() {
        return perfumeList;
    }

    public Map<Long, Perfume> getPerfumeMap() {
        return perfumeMap;

    }

    public Map<Perfume, String> getNameLexicographicContraction() {
        return nameLexicographicContraction;
    }

    public Map<String, List<Perfume>> getBrandMap() {
        return brandMap;
    }

    public List<Perfume> getBackgroundPerfumeList() {
        return backgroundPerfumeList;
    }

    public Map<Long, Perfume> getBackgroundPerfumeMap() {
        return backgroundPerfumeMap;
    }

    public Map<String, List<Perfume>> getBackgroundBrandMap() {
        return backgroundBrandMap;
    }

}
