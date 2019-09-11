package com.bocse.perfume.data;

/**
 * Created by bogdan.bocse on 12/2/2015.
 */
public class Note {
    private String name;
    private String group;
    private FragranticaNoteType fragranticaNoteType;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public FragranticaNoteType getFragranticaNoteType() {
        return fragranticaNoteType;
    }

    public void setFragranticaNoteType(FragranticaNoteType fragranticaNoteType) {
        this.fragranticaNoteType = fragranticaNoteType;
    }
}
