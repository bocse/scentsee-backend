package com.bocse.perfume.data;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by bogdan.bocse on 1/25/2016.
 */
public class PublicQuestion implements Comparable<PublicQuestion> {
    private Integer index;
    private String id;

    private Integer relevance;
    private Map<String, PublicAnswer> answers = new LinkedHashMap<>();

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getRelevance() {
        return relevance;
    }

    public void setRelevance(Integer relevance) {
        this.relevance = relevance;
    }

    public Map<String, PublicAnswer> getAnswers() {
        return answers;
    }

    public void setAnswers(Map<String, PublicAnswer> answers) {
        this.answers = answers;
    }

    @Override
    public int compareTo(PublicQuestion o) {
        return -this.getRelevance().compareTo(o.getRelevance());
    }
}
