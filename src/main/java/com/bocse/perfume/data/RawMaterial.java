package com.bocse.perfume.data;

import org.jsondoc.core.annotation.ApiObject;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by bogdan.bocse on 15/07/16.
 */
@ApiObject(name = "RawMaterial", description = "Defines an element of a perfume, such as an oil or an essence, which is available from a known vendor.")
public class RawMaterial {
    private String name;
    private String description;
    private String vendor;
    private Set<String> mappedNotes = new HashSet<>();

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof RawMaterial))
            return false;
        return name.equals(((RawMaterial) obj).getName());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public Set<String> getMappedNotes() {
        return mappedNotes;
    }

}
