package com.bocse.perfume.provisioning;

import com.bocse.perfume.data.Answer;
import com.bocse.perfume.data.Gender;
import com.bocse.perfume.data.NoteTypeTranslatorRo;
import com.bocse.perfume.data.Question;
import com.bocse.perfume.iterator.QuestionnaireIterator;
import com.bocse.perfume.serializer.JsonSerializer;
import org.apache.commons.configuration.ConfigurationException;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by bocse on 30.11.2015.
 */
public class QuestionnaireLoadMain {
    private final static Logger logger = Logger.getLogger(QuestionnaireLoadMain.class.toString());

    ///home/bocse/projects/parfume/src/main/resources/q-text.csv  /home/bocse/perfumes/q/questionnaireMapping.json /home/bocse/perfumes/q/questionsFrontEnd.json /home/bocse/perfumes/q/aromaticTypesFrontEnd.json /home/bocse/projects/parfume/src/main/resources/noteDescription.csv /home/bocse/perfumes/q/aromaticTypesDescriptionFrontEnd.json
    //0 - questionnaire csv in
    //1 - questionnaire json out
    //2 - questionnaire json stripped out
    //3 - noteType Labels out
    //4 - noteType description CSV in
    //5 - notertpe description json out
    public static void main(String[] args) throws IOException, InterruptedException, ConfigurationException {
        new QuestionnaireLoadMain().execute(args);
    }

    private void createJson(String args[]) throws IOException {
        String questionnaireFemalePath = args[0];
        String questinnaireMalePath = args[1];
        String questionnareiJson = args[2];
        String questionnareiJsonStripped = args[3];
        String noteTypesLabels = args[4];
        String noteTypesCsv = args[5];
        String noteTypesJson = args[6];

        QuestionnaireIterator questionnaireIterator = new QuestionnaireIterator();
        Map<Gender, Map<String, Question>> questionnaires = questionnaireIterator.parseFiles(questionnaireFemalePath, questinnaireMalePath);
        JsonSerializer.serialize(questionnareiJson, questionnaires, true);

        for (Question question : questionnaires.get(Gender.FEMALE).values()) {
            for (Answer answer : question.getAnswers().values()) {
                answer.setAffinity(null);
                answer.setAversion(null);
            }
        }
        for (Question question : questionnaires.get(Gender.MALE).values()) {
            for (Answer answer : question.getAnswers().values()) {
                answer.setAffinity(null);
                answer.setAversion(null);
            }
        }

        JsonSerializer.serialize(questionnareiJsonStripped, questionnaires, true);
        JsonSerializer.serialize(questionnareiJsonStripped + ".f.json", questionnaires.get(Gender.FEMALE).values(), true);
        JsonSerializer.serialize(questionnareiJsonStripped + ".m.json", questionnaires.get(Gender.MALE).values(), true);
        NoteTypeTranslatorRo noteTypeTranslatorRo = new NoteTypeTranslatorRo();
        JsonSerializer.serialize(noteTypesLabels, noteTypeTranslatorRo.getLabels());
        JsonSerializer.serialize(noteTypesJson, noteTypeTranslatorRo.loadDescriptionsFromFile(noteTypesCsv));
    }

    /*
    @Deprecated
    private void doStats(String args[]) throws IOException {
        QuestionnaireIterator questionnaireIterator=new QuestionnaireIterator();
        Map<String, Question> loadedMapping=questionnaireIterator.readProductFromJsonAndKeep(new File(args[1]));
        Map<NoteType, Integer> affinity=new HashMap<>();
        Map<NoteType, Integer> aversion=new HashMap<>();
        for (Map.Entry<String, Question> entry: loadedMapping.entrySet())
        {
            for (Answer answer: entry.getValue().getAnswers().values()) {
                if (answer.getAffinity()!=null)
                {
                    for (NoteType note: answer.getAffinity())
                    {
                        affinity.putIfAbsent(note, 0);
                        affinity.put(note, affinity.get(note)+1);
                    }
                }
                if (answer.getAversion()!=null)
                {
                    for (NoteType note: answer.getAversion())
                    {
                        aversion.putIfAbsent(note, 0);
                        aversion.put(note, aversion.get(note)+1);
                    }
                }
            }
        }
        System.out.println("Affinity distribution: ");
        for (Map.Entry<NoteType, Integer> entry: affinity.entrySet())
        {
            System.out.println(entry.getKey()+"\t"+entry.getValue());
        }


        System.out.println("Aversion distribution: ");
        for (Map.Entry<NoteType, Integer> entry: aversion.entrySet())
        {
            System.out.println(entry.getKey()+"\t"+entry.getValue());
        }
        logger.info("Affinity:" +affinity.toString());

        logger.info("Aversion distributin: "+aversion.toString());

        return ;
//        Map<String, QuestionMapping> questionMappings=new HashMap<>();
//
//        QuestionMapping questionMapping=new QuestionMapping();
//        Question question =new Question();
//        question.setId("skin-complexion");
//        question.setIndex(Math.abs(question.getId().hashCode()));
//        question.setText("Ce nuanță are tenul persoanei care va folosi parfumul?");
//          new ArrayList<NoteType>() {{
//            add(NoteType.SPICY);
//            add(NoteType.WOODY);
//            add(NoteType.ORIENTAL);
//            add(NoteType.MUSK);
//        }};
//        questionMapping.setQuestion(question);
//        questionMapping.getAffinity().put("închis", new ArrayList<NoteType>() {{
//            add(NoteType.SPICY);
//            add(NoteType.WOODY);
//            add(NoteType.ORIENTAL);
//            add(NoteType.MUSK);
//        }});
//        questionMapping.getAffinity().put("mediu", new ArrayList<NoteType>() {{
//            add(NoteType.CITRIC);
//            add(NoteType.FRUITY);
//            add(NoteType.POWDERY);
//        }});
//
//        questionMapping.getAffinity().put("deschis", new ArrayList<NoteType>() {{
//            add(NoteType.ALDEHYDE);
//            add(NoteType.FRUITY);
//            add(NoteType.GOURMANDY);
//            add(NoteType.GREEN);
//        }});
//
//        questionMapping.getAversion().put("închis", new ArrayList<NoteType>() {{
//            add(NoteType.CITRIC);
//            add(NoteType.ALDEHYDE);
//            add(NoteType.AQUATIC);
//        }});
//
//        questionMapping.getAversion().put("mediu", new ArrayList<NoteType>() {{
//            add(NoteType.MUSK);
//            add(NoteType.WOODY);
//            add(NoteType.SPICY);
//        }});
//
//        questionMapping.getAversion().put("deschis", new ArrayList<NoteType>() {{
//            add(NoteType.WOODY);
//            add(NoteType.MINERAL);
//            add(NoteType.ORIENTAL);
//            add(NoteType.SPICY);
//        }});
//
//        questionMappings.put(questionMapping.getQuestion().getId(), questionMapping);
//        JsonSerializer.serialize("C:\\Temp\\perfumeMapping.json", questionMappings, true);
    }
    */
    public void execute(String args[]) throws IOException {
        createJson(args);
        //doStats(args);
    }
}
