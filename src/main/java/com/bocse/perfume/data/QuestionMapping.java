package com.bocse.perfume.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by bogdan.bocse on 1/25/2016.
 */
@Deprecated
public class QuestionMapping {
    private Question question;
    private Map<String, List<NoteType>> affinity = new HashMap<>();
    private Map<String, List<NoteType>> aversion = new HashMap<>();
    private Map<String, String> pictureURL = new HashMap<>();
    //private List<String> pictureURLList=new ArrayList<>();
    private List<String> answers = new ArrayList<>();

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }


    public Map<String, List<NoteType>> getAffinity() {
        return affinity;
    }


    public Map<String, List<NoteType>> getAversion() {
        return aversion;
    }


    public Map<String, String> getPictureURL() {
        return pictureURL;
    }


    public List<String> getAnswers() {
        return answers;
    }

    public void setAnswers(List<String> answers) {
        this.answers = answers;
    }
}
