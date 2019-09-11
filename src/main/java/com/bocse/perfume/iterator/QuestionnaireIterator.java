package com.bocse.perfume.iterator;

import com.bocse.perfume.data.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.lang.time.StopWatch;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.ICsvListReader;
import org.supercsv.prefs.CsvPreference;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Logger;

/**
 * Created by bogdan.bocse on 1/25/2016.
 */
public class QuestionnaireIterator {
    private final static Logger logger = Logger.getLogger(QuestionnaireIterator.class.toString());
    Map<Gender, Map<String, Question>> questionnaireMapping;
    Map<Gender, Map<String, PublicQuestion>> publicQuestionnaireMapping;
    private Boolean useLocalPicture = true;

    public QuestionnaireIterator() {

    }

    public Map<Gender, Map<String, Question>> getQuestionnaireMapping() {
        return questionnaireMapping;
    }

    public Map<Gender, Map<String, PublicQuestion>> getPublicQuestionnaireMapping() {
        return publicQuestionnaireMapping;
    }

    public Map<String, Question> parseFile(String filename) throws IOException {
        return readProductsFromCSV(getListReaderFromFile(filename));
    }

    public Map<Gender, Map<String, Question>> parseFiles(String filenameFemale, String filenameMale) throws IOException {
        Map<String, Question> maleQuestionnaire = readProductsFromCSV(getListReaderFromFile(filenameMale));
        Map<String, Question> femaleQuestionnaire = readProductsFromCSV(getListReaderFromFile(filenameFemale));

        Map<Gender, Map<String, Question>> map = new HashMap<>();
        map.put(Gender.MALE, maleQuestionnaire);
        map.put(Gender.FEMALE, femaleQuestionnaire);
        questionnaireMapping = map;
        generatePublicQuestionnaire();
        return map;
    }

    private ICsvListReader getListReaderFromFile(String filename) throws FileNotFoundException {
        ICsvListReader listReader = null;
        listReader = new CsvListReader(new FileReader(filename), CsvPreference.STANDARD_PREFERENCE);
        return listReader;
    }

    public Map<Gender, Map<String, Question>> readProductFromJsonAndKeep(File file) throws IOException {
        Map<Gender, Map<String, Question>> questionnaireMappingLocal = null;
        logger.info("Loading questionare in memory from " + file.getAbsolutePath());
        StopWatch iteatorWatch = new StopWatch();
        iteatorWatch.start();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Reader reader = null;
        try {
            //FileReader reader;
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
            Type listType = new TypeToken<Map<Gender, Map<String, Question>>>() {
            }.getType();
            questionnaireMappingLocal = gson.fromJson(reader, listType);
//            for (QuestionMapping questionMapping: questionnaireMappingLocal.values())
//            {
//                questionMapping.setAnswers(new ArrayList<>(questionMapping.getAffinity().keySet()));
//            }

            iteatorWatch.stop();
            logger.info("Loaded " + questionnaireMappingLocal.size() + " questionnaire items in " + iteatorWatch.getTime() + "ms");
            questionnaireMapping = questionnaireMappingLocal;
            generatePublicQuestionnaire();
            return questionnaireMappingLocal;
        } finally {

            if (reader != null)
                reader.close();
            System.gc();
        }
    }

    private Map<String, Question> readProductsFromCSV(ICsvListReader listReader) throws IOException {
        logger.info("Parsing affiliate products");
        int blacklistCount = 0;
        String questionText = null;
        String questionId = null;
        Question question = null;
        Map<String, Question> questionnaire = new LinkedHashMap<>();


        try {


            String[] header = listReader.getHeader(true);
            List<String> fields;
            while ((fields = listReader.read()) != null) {
                try {
                    logger.info(fields.toString());
                    String indexString = fields.get(0);
                    String idString = fields.get(1);

                    String questionString = fields.get(2);
                    String modelAnswer = fields.get(3);
                    String textAnswer = fields.get(4);
                    if (modelAnswer != null) {
                        modelAnswer = modelAnswer.trim().toLowerCase();
                    } else {
                        continue;
                    }
                    String relevanceString = fields.get(5);
                    String pictureURL = fields.get(6);
                    if (indexString != null && questionString != null && !questionString.toLowerCase().contains("http")) {
                        questionId = indexString;
                        questionText = questionString;

                        question = new Question();
                        question.setText(questionText);
                        question.setId(idString);
                        if (idString == null) {
                            logger.warning("Null question id" + fields.toString());
                            continue;
                        }
                        question.setIndex(Integer.valueOf(questionId.trim()));
                        if (relevanceString != null) {
                            question.setRelevance(Integer.valueOf(relevanceString));
                            if (question.getRelevance() < 1) {
                                logger.warning("Skipped zero relevance question: " + fields.toString());
                                continue;
                            }
                        } else {
                            logger.warning("No relevance information available " + fields.toString());
                            question.setRelevance(10);
                        }

                        questionnaire.put(question.getId(), question);
                    }
                    question.getAnswers().putIfAbsent(modelAnswer, new Answer());
                    if (useLocalPicture) {
                        if (pictureURL != null && !pictureURL.isEmpty()) {
                            question.getAnswers().get(modelAnswer).setAnswerPictureURL("/images/quiz/" + question.getId() + "-" + modelAnswer + ".jpg");
                        }
                    } else {
                        question.getAnswers().get(modelAnswer).setAnswerPictureURL(pictureURL);
                    }
                    if (question.getId().equals("color")) {
                        question.getAnswers().get(modelAnswer).setAnswerColor(modelAnswer);
                    }
                    question.getAnswers().get(modelAnswer).setAnswerText(fields.get(4));
                    question.getAnswers().get(modelAnswer).setAnswerId(modelAnswer);
                    if (fields.get(7) != null) {
                        List<NoteType> affinity = new ArrayList<>();
                        for (int i = 7; i <= 11; i++) {
                            if (fields.get(i) != null)
                                affinity.add(NoteType.valueOf(fields.get(i)));
                        }
                        question.getAnswers().get(modelAnswer).setAffinity(affinity);

                    }
                    if (fields.get(12) != null) {
                        List<NoteType> aversion = new ArrayList<>();
                        for (int i = 12; i <= 16; i++) {
                            if (fields.get(i) != null)
                                aversion.add(NoteType.valueOf(fields.get(i)));
                        }
                        question.getAnswers().putIfAbsent(modelAnswer, new Answer());
                        question.getAnswers().get(modelAnswer).setAversion(aversion);
                    }


                } finally {
                }
            }
        } finally {

        }
        //generatePublicQuestionnaire();
        return questionnaire;
    }

    private void generatePublicQuestionnaire() {
        Map<Gender, Map<String, PublicQuestion>> publicMap = new HashMap<>();

        for (Map.Entry<Gender, Map<String, Question>> entryQuestionnaire : questionnaireMapping.entrySet()) {

            publicMap.putIfAbsent(entryQuestionnaire.getKey(), new HashMap<>());
            for (Map.Entry<String, Question> entryQuestion : entryQuestionnaire.getValue().entrySet()) {
                PublicQuestion question = new PublicQuestion();
                publicMap.get(entryQuestionnaire.getKey()).put(entryQuestion.getKey(), question);
                question.setId(entryQuestion.getValue().getId());
                question.setRelevance(entryQuestion.getValue().getRelevance());
                question.setIndex(entryQuestion.getValue().getIndex());
                for (Map.Entry<String, Answer> entryAnswer : entryQuestion.getValue().getAnswers().entrySet()) {
                    PublicAnswer answer = new PublicAnswer();
                    question.getAnswers().put(entryAnswer.getKey(), answer);
                    answer.setAnswerId(entryAnswer.getValue().getAnswerId());
                    answer.setQuestionId(question.getId());
                }
            }
        }
        publicQuestionnaireMapping = publicMap;
    }
}
