package com.bocse.perfume.parser;

import com.bocse.perfume.data.Gender;
import com.bocse.perfume.data.Perfume;
import com.bocse.perfume.requester.HttpRequester;
import com.bocse.perfume.utils.TextUtils;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

/**
 * Created by bocse on 29.11.2015.
 */
public class PerfumeParser implements IPerfumeParser {
    private final static Logger logger = Logger.getLogger(PerfumeParser.class.toString());
    private static final Boolean legacyHash = false;
    private static final String hashSeed = "ScentSee";

    private HttpUriRequest createPerfumeDocumentRequest(final String url) {
        String newUrl = url.replaceAll(" ", "%20");
        final HttpGet httpGet = new HttpGet(newUrl);
        final RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(9000).setConnectTimeout(9000).setSocketTimeout(9000).build();
        httpGet.setConfig(requestConfig);
        logger.info("Executing request " + httpGet.getRequestLine());
        return httpGet;

    }

    private HttpUriRequest createBrandsDocumentRequest() {

        // NOTICE REGARDING COPYRIGHT
        // For the purpose of respecting copyright law, the actual URLs will not be provided, as the publisher cannot ascertain the legality of crawling such pages in the future.
        // Before running this code, please make sure to double-check that you are still allowed to crawl the sources which you select, in accordance with any legal provisions at the time of running this code yourself.

        //TODO: replace with source URL
        final String url = "https://[replace with source URL only after validating you are legally permitted to read and store information from that source]";
        ////////
        final HttpGet httpGet = new HttpGet(url);
        final RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(9000).setConnectTimeout(9000).setSocketTimeout(9000).build();
        httpGet.setConfig(requestConfig);
        logger.info("Executing request " + httpGet.getRequestLine());
        return httpGet;

    }

    @Override
    public Map<String, String> parseBrands() throws IOException, InterruptedException {
        Map<String, String> brandsMap = new HashMap<>();

        Document perfumeDocument = HttpRequester.getDocument(createBrandsDocumentRequest());

        //Elements brandElements=perfumeDocument.select("body > div.body-wrapper > div > div.main > div");
        Elements brandElements = perfumeDocument.select("div.list-col-3 > a");
        for (Element brandElement : brandElements) {
            String brandName = brandElement.text();
            String brandURL = brandElement.attr("href");

            brandsMap.put(brandName, brandURL);
        }
        return brandsMap;
    }

    @Override
    public Map<String, String> parserBrand(final String url) throws IOException, InterruptedException {
        Map<String, String> urlMap = new HashMap<>();
        int retrievedItems = 0;
        int pageIndex = 0;
        do {
            Map<String, String> retrievedMap = parseBrandPage(url, pageIndex);
            retrievedItems = retrievedMap.size();
            if (retrievedItems > 0) {
                urlMap.putAll(retrievedMap);
                pageIndex++;
            }
        } while (retrievedItems > 0);
        logger.info("Retrieved " + urlMap.size() + " from " + url);
        return urlMap;
    }

    @Override
    public Map<String, String> parseBrandPage(final String url, Integer page) throws IOException, InterruptedException {
        Map<String, String> urlMap = new HashMap<>();
        String newURL = url.replaceAll("/Perfumes/", "/Brands/") + "/All/NameAscdending/" + (page * 20);
        Document perfumeListDocument = HttpRequester.getDocument(createPerfumeDocumentRequest(newURL));
        Elements perfumeElements = perfumeListDocument.select("div.name > a");
        for (int elementIndex = 0; elementIndex < perfumeElements.size(); elementIndex++) {
            String perfumeURL = perfumeElements.get(elementIndex).attr("href");
            String perfumeName = perfumeElements.get(elementIndex).text();
            urlMap.put(perfumeName, perfumeURL);
        }
        return urlMap;

    }

    private List<String> cleanupIngredients(List<String> ingredients) {
        List<String> response = new ArrayList();
        for (String ingredient : ingredients) {
            String ingredientCopy = ingredient.replaceAll("[^\\p{L}\\p{Nd} ]+", "");
            if (ingredientCopy.trim().equals(""))
                continue;
            if (ingredientCopy.length() <= 1)
                continue;

            response.add(ingredientCopy.toLowerCase());
        }
        return response;
    }

    @Override
    public Perfume parsePerfumeDocument(final String url) throws IOException, InterruptedException {
        //Map<String, Object> theMap = new HashMap<>();
        Perfume perfume = new Perfume();
        Document perfumeDocument = HttpRequester.getDocument(createPerfumeDocumentRequest(url));
        perfume.setUrl(url);

        Gender gender;

        if (perfumeDocument.select("#pd_inf > div.p-details-box > main > div.perfum_details_holder_male > div ").size() > 0)
            gender = Gender.MALE;
        else if (perfumeDocument.select("#pd_inf > div.p-details-box > main > div.perfum_details_holder_female > div ").size() > 0)
            gender = Gender.FEMALE;
        else if (perfumeDocument.select("#pd_inf > div.p-details-box > main > div.perfum_details_holder_uni > div ").size() > 0)
            gender = Gender.UNI;
        else {
            throw new IllegalStateException("Parser page with unexpected format.");
        }
        perfume.setGender(gender);
        String genderString = gender.toString().toLowerCase();


        String perfumeName = perfumeDocument.select("#pd_inf > div.p-details-box > main > div.perfum_details_holder_" + genderString + " > h1").text();

        perfume.setName(perfumeName);

        String brand = perfumeDocument.select("#pd_inf > div.p-details-box > main > div.perfum_details_holder_" + genderString + " > h1 > span > span > a:nth-child(1) > span").text();
        int brandIndex = perfume.getName().lastIndexOf(brand);
        if (brandIndex > 1) {
            perfume.setName(perfume.getName().substring(0, brandIndex - 1));
        }
        perfume.setName(perfume.getName().replaceAll(brand, ""));
        perfume.setBrand(brand);
        if (legacyHash) {
            perfume.setId(TextUtils.hash(perfume.getUrl()));
        } else {
            String stringToHash = hashSeed + " " +
                    TextUtils.cleanupAndFlatten(
                            perfume.getBrand() + " " +
                                    perfume.getName() + " " +
                                    perfume.getGender().toString());
            perfume.setId(TextUtils.hash(stringToHash));
        }
        String description = perfumeDocument.select("div.summary-box").text();
        perfume.setDescription(description);
        if (description.toLowerCase().contains("discontinued"))
            perfume.setInProduction(false);

        String yearOfReleaseString = perfumeDocument.select("#pd_inf > div.p-details-box > main > div.perfum_details_holder_" + genderString + " > h1 > span > span > a:nth-child(2)").text().replaceAll("[()]", "");


        try {
            Integer yearOfRelease = Integer.valueOf(yearOfReleaseString);
            perfume.setYear(yearOfRelease);
        } catch
                (NumberFormatException nfex) {
            //whatever
        }

        //#thumb1 > img
        Elements imageElements = perfumeDocument.select("#perfum_image_holder > img");
        if (imageElements.size() > 0) {
            perfume.setPictureURL(imageElements.get(0).attr("src"));
        } else {
            Elements imageElementsPlanB = perfumeDocument.select("#thumb1 > img");
            if (imageElementsPlanB.size() > 0) {
                perfume.setPictureURL(imageElementsPlanB.get(0).attr("src"));
            } else {
                Elements imageElementsFallback = perfumeDocument.select("#perfum_image_holder_no_imagery > img");
                if (imageElementsFallback.size() > 0) {
                    perfume.setPictureURL(imageElementsFallback.get(0).attr("src"));
                } else {
                    perfume.setPictureURL("");
                }
            }
        }
        String topNotesText = perfumeDocument.select("#pd_inf > div.p-details-box > main > div.perfum_details_holder_" + genderString + "_second > div.perfum_notes_holder > table > tbody > tr:nth-child(1) > td:nth-child(2)").text();
        List<String> topNotes = cleanupIngredients(Arrays.asList(topNotesText.split(", ")));
        perfume.setTopNotes(topNotes);

        String heartNotesText = perfumeDocument.select("#pd_inf > div.p-details-box > main > div.perfum_details_holder_" + genderString + "_second > div.perfum_notes_holder > table > tbody > tr:nth-child(2) > td:nth-child(2)").text();
        List<String> heartNotes = cleanupIngredients(Arrays.asList(heartNotesText.split(", ")));
        perfume.setHeartNotes(heartNotes);

        String baseNotesText = perfumeDocument.select("#pd_inf > div.p-details-box > main > div.perfum_details_holder_" + genderString + "_second > div.perfum_notes_holder > table > tbody > tr:nth-child(3) > td:nth-child(2)").text();
        List<String> baseNotes = cleanupIngredients(Arrays.asList(baseNotesText.split(", ")));
        perfume.setBaseNotes(baseNotes);

        if (topNotes.size() + heartNotes.size() + baseNotes.size() == 0) {
            String mixedNotesText = perfumeDocument.select("#pd_inf > div.p-details-box > main > div.perfum_details_holder_" + genderString + "_second > div.perfum_notes_holder > table > tbody > tr > td").text();
            List<String> mixedNotes = cleanupIngredients(Arrays.asList(mixedNotesText.split(", ")));
            perfume.setMixedNotes(mixedNotes);
            perfume.setSubstandard(true);
        } else {
            List<String> mixedNotes = new ArrayList<>();
            mixedNotes.addAll(topNotes);
            mixedNotes.addAll(heartNotes);
            mixedNotes.addAll(baseNotes);
            perfume.setMixedNotes(mixedNotes);
        }


        Elements ratingElements = perfumeDocument.select("#pd_inf > div.p-details-box > main > div.perfum_details_holder_" + genderString + "_second > div.perfum_ratings_holder > div");

        for (int i = 0; i < ratingElements.size(); i++) {
            int ii = i + 1;
            final String ratingElementLookupPath = "#pd_inf > div.p-details-box > main > div.perfum_details_holder_" + genderString + "_second > div.perfum_ratings_holder > div:nth-child(" + ii + ")";
            if (ratingElements.get(i).text().toLowerCase().contains("scent")) {
                String scentString = perfumeDocument.select(ratingElementLookupPath).text().trim().toLowerCase().replaceAll("[A-Za-z ]", "");
                Double scentRating = parseIntegerBeforePercent(scentString);
                Integer scentPopularity = parseIntegerBetweenBrackets(scentString);
                perfume.setScentR(scentRating);
                perfume.setScentP(scentPopularity);
            } else if (ratingElements.get(i).text().toLowerCase().contains("longevity")) {
                String longevityString = perfumeDocument.select(ratingElementLookupPath).text().trim().toLowerCase().replaceAll("[A-Za-z ]", "");
                Double longevityRating = parseIntegerBeforePercent(longevityString);
                Integer longevityPopularity = parseIntegerBetweenBrackets(longevityString);
                perfume.setLongevityR(longevityRating);
                perfume.setLongevityP(longevityPopularity);
            } else if (ratingElements.get(i).text().toLowerCase().contains("sillage")) {
                String sillageString = perfumeDocument.select(ratingElementLookupPath).text().trim().toLowerCase().replaceAll("[A-Za-z ]", "");
                Double sillageRating = parseIntegerBeforePercent(sillageString);
                Integer sillagePopularity = parseIntegerBetweenBrackets(sillageString);
                perfume.setSillageR(sillageRating);
                perfume.setSillageP(sillagePopularity);
            } else if (ratingElements.get(i).text().toLowerCase().contains("bottle")) {
                String bottleString = perfumeDocument.select(ratingElementLookupPath).text().trim().toLowerCase().replaceAll("[A-Za-z ]", "");
                Double bottleRating = parseIntegerBeforePercent(bottleString);
                Integer bottlePopularity = parseIntegerBetweenBrackets(bottleString);
                perfume.setBottleR(bottleRating);
                perfume.setBottleP(bottlePopularity);
            }
        }

        return perfume;

    }

    private Double parseIntegerBeforePercent(String ratingString) {
        if (ratingString != null && !ratingString.isEmpty() && ratingString.contains("(")) {
            String ratingCropped = ratingString.substring(0, ratingString.indexOf('(')).trim();
            try {
                Double result = Double.valueOf(ratingCropped);
                return result;
            } catch (NumberFormatException nfex) {
                return 0.0;
            }

        } else {
            return 0.0;
        }
    }

    private Integer parseIntegerBetweenBrackets(String ratingString) {
        if (ratingString != null && !ratingString.isEmpty() && ratingString.contains("(") && ratingString.contains(")")) {
            String popularityCropped = ratingString.substring(ratingString.indexOf('('), ratingString.indexOf(')')).replaceAll("[^\\d.]", "").trim();
            //System.out.println(popularityScentCropped);
            try {
                Integer result = Integer.valueOf(popularityCropped);
                return result;
            } catch (NumberFormatException nfex) {
                return 0;
            }
        } else {
            return 0;
        }
    }

    @Override
    public boolean getPerfumePictures(Perfume perfume, String path) throws IOException, InterruptedException {
        List<String> extensions = Arrays.asList(".png", ".jpg", ".jpeg");
        String extension = null;
        if (perfume.getPictureURL() == null)
            return false;
        if (perfume.getPictureURL().isEmpty())
            return false;
        for (String candidateExtension : extensions) {
            if (perfume.getPictureURL().endsWith(candidateExtension)) {
                extension = candidateExtension;
                break;
            }
        }
        if (extension == null) {
            throw new IllegalStateException("Unknown extension in " + perfume.getPictureURL());
        }
        File pictureDestination = new File(path + perfume.getId() + extension);
        final HttpGet httpGet = new HttpGet(perfume.getPictureURL());

        final RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(9000).setConnectTimeout(9000).setSocketTimeout(9000).build();
        httpGet.setConfig(requestConfig);
        logger.info("Executing request " + httpGet.getRequestLine());

        long contentLength = HttpRequester.downloadToFile(httpGet, pictureDestination, true);
        if (contentLength >= 7590 && contentLength <= 7640) {
            perfume.setPictureURL("");
            return false;
        }
        return true;
    }
}
