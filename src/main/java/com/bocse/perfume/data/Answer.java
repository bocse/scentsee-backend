package com.bocse.perfume.data;

import java.util.List;

/**
 * Created by bocse on 29.01.2016.
 */
public class Answer {
    private String questionId;
    private String answerId;
    private String answerText;
    private String answerPictureURL;
    private String answerColor;
    private List<NoteType> affinity;
    private List<NoteType> aversion;

    public String getQuestionId() {
        return questionId;
    }

    public void setQuestionId(String questionId) {
        this.questionId = questionId;
    }

    public String getAnswerId() {
        return answerId;
    }

    public void setAnswerId(String answerId) {
        this.answerId = answerId;
    }

    public String getAnswerText() {
        return answerText;
    }

    public void setAnswerText(String answerText) {
        this.answerText = answerText;
    }

    public String getAnswerPictureURL() {
        return answerPictureURL;
    }

    public void setAnswerPictureURL(String answerPictureURL) {
        this.answerPictureURL = answerPictureURL;
    }

    public List<NoteType> getAffinity() {
        return affinity;
    }

    public void setAffinity(List<NoteType> affinity) {
        this.affinity = affinity;
    }

    public List<NoteType> getAversion() {
        return aversion;
    }

    public void setAversion(List<NoteType> aversion) {
        this.aversion = aversion;
    }

    public String getAnswerColor() {
        return answerColor;
    }

    public void setAnswerColor(String answerColor) {
        this.answerColor = answerColor;
    }
}
