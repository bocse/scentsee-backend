package com.bocse.perfume.data;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by bogdan.bocse on 1/25/2016.
 */
public class Question implements Comparable<Question> {
    private Integer index;
    private String id;
    private String text;
    private Integer relevance;
    private Map<String, Answer> answers = new LinkedHashMap<>();

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public Integer getRelevance() {
        return relevance;
    }

    public void setRelevance(Integer relevance) {
        this.relevance = relevance;
    }

    public Map<String, Answer> getAnswers() {
        return answers;
    }

    @Override
    public int compareTo(Question o) {
        return -this.getRelevance().compareTo(o.getRelevance());
    }
}
