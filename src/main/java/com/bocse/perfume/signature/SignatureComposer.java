package com.bocse.perfume.signature;

import com.bocse.perfume.data.NoteType;
import com.bocse.perfume.data.Perfume;
import com.bocse.perfume.data.PerfumeSegmentedSignature;
import com.bocse.perfume.data.Question;
import com.bocse.perfume.utils.SignatureUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by bocse on 26.01.2016.
 */
public class SignatureComposer {
    private final static Logger logger = Logger.getLogger(SignatureComposer.class.toString());
    private Map<String, Question> questionMapping = null;

    public SignatureComposer() {

    }

    public SignatureComposer(Map<String, Question> questionMapping) {
        this.questionMapping = questionMapping;
    }

    public Map<NoteType, Double> composeSignature(Map<String, String> answers) {
        return composeSignature(answers, true);
    }

    public PerfumeSegmentedSignature composeSignature(List<Perfume> affinityList, Double affinityWeight,
                                                      List<Perfume> adversePerfumes, Double adversityWeight,
                                                      Double topWeight, Double heartWeight, Double baseWeight) {
        if (adversityWeight > 0.0) {
            throw new IllegalStateException("Adversity weight is positive.");
        }
        PerfumeSegmentedSignature signature = new PerfumeSegmentedSignature();
        for (Perfume affinePerfume : affinityList) {
            SignatureUtils.addSignatureAccumulator(signature.getTop(), affinePerfume.getTopSignature(), affinityWeight * topWeight);
            SignatureUtils.addSignatureAccumulator(signature.getHeart(), affinePerfume.getHeartSignature(), affinityWeight * heartWeight);
            SignatureUtils.addSignatureAccumulator(signature.getBase(), affinePerfume.getBaseSignature(), affinityWeight * baseWeight);

            SignatureUtils.addSignatureAccumulator(signature.getMixed(), affinePerfume.getTopSignature(), affinityWeight * topWeight);
            SignatureUtils.addSignatureAccumulator(signature.getMixed(), affinePerfume.getHeartSignature(), affinityWeight * heartWeight);
            SignatureUtils.addSignatureAccumulator(signature.getMixed(), affinePerfume.getBaseSignature(), affinityWeight * baseWeight);

        }

        for (Perfume adversePerfume : adversePerfumes) {
            SignatureUtils.addSignatureAccumulator(signature.getTop(), adversePerfume.getTopSignature(), adversityWeight * topWeight);
            SignatureUtils.addSignatureAccumulator(signature.getHeart(), adversePerfume.getHeartSignature(), adversityWeight * heartWeight);
            SignatureUtils.addSignatureAccumulator(signature.getBase(), adversePerfume.getBaseSignature(), adversityWeight * baseWeight);

            SignatureUtils.addSignatureAccumulator(signature.getTop(), adversePerfume.getTopSignature(), adversityWeight * topWeight);
            SignatureUtils.addSignatureAccumulator(signature.getHeart(), adversePerfume.getHeartSignature(), adversityWeight * heartWeight);
            SignatureUtils.addSignatureAccumulator(signature.getBase(), adversePerfume.getBaseSignature(), adversityWeight * baseWeight);
        }

        SignatureUtils.cullSignature(signature.getBase());
        SignatureUtils.cullSignature(signature.getTop());
        SignatureUtils.cullSignature(signature.getHeart());

        return signature;
    }

    public PerfumeSegmentedSignature composeSignature(List<Perfume> affinityList,
                                                      List<Perfume> adversePerfumes,
                                                      Double topWeight, Double heartWeight, Double baseWeight)

    {
        return composeSignature(affinityList, 1.0, adversePerfumes, -1.0, topWeight, heartWeight, baseWeight);
    }

    public Map<NoteType, Double> composeSignature(Map<String, String> answers, Boolean useAversion) {
        if (questionMapping == null)
            throw new IllegalStateException("Questionnaire mapping not defined");
        Map<NoteType, Double> signature = new HashMap<>();
        for (Map.Entry<String, String> entry : answers.entrySet()) {
            Question question = questionMapping.get(entry.getKey());
            if (question != null) {
                Integer relevance = question.getRelevance();
                if (question.getAnswers().containsKey(entry.getValue())) {
                    List<NoteType> affineNotes = question.getAnswers().get(entry.getValue()).getAffinity();
                    List<NoteType> averseNotes = question.getAnswers().get(entry.getValue()).getAversion();
                    if (affineNotes != null) {
                        for (NoteType affineNote : affineNotes) {
                            signature.putIfAbsent(affineNote, 0.0);
                            signature.put(affineNote, signature.get(affineNote) + relevance);
                        }
                    }
                    if (useAversion) {
                        if (averseNotes != null) {
                            for (NoteType averseNote : averseNotes) {
                                signature.putIfAbsent(averseNote, 0.0);
                                signature.put(averseNote, signature.get(averseNote) - relevance);
                            }
                        }
                    }
                } else {
                    logger.warning("Unkown answer " + entry.getValue() + " to question " + entry.getKey());
                }
            } else {
                logger.warning("Unknown question " + entry.getKey());
            }
        }
        return signature;
    }
}
