package com.bocse.perfume.data;

import com.bocse.perfume.utils.TextUtils;
import org.jsondoc.core.annotation.ApiObject;
import org.jsondoc.core.annotation.ApiObjectField;

/**
 * Created by bocse on 05.12.2015.
 */
@ApiObject(name = "AffiliatePerfume", description = "Defines a physical perfume available in the stock of one affiliate.")
public class AffiliatePerfume {


    @ApiObjectField(name = "searchableName", description = "Conveninent concatenation between brand and name")
    private String searchableName = "";
    @ApiObjectField(name = "name", description = "Name of the perfume (i.e. not the brand). Eg: J'adore")
    private String name = "";
    @ApiObjectField(name = "brand", description = "Brand to which the perfume belongs. Eg: Dior, Armani, Amouage")
    private String brand = "";
    @ApiObjectField(name = "quantity", description = "Quantity, measure in ml (milliliter) for this particular stock keeping unit.")
    private Double quantity;
    @ApiObjectField(name = "currency", description = "Currency in which the price is represented")
    private String currency;
    @ApiObjectField(name = "affiliateURL", description = "The link to the vendor product page where the perfume can be purchased.")
    private String AffiliateURL;
    @ApiObjectField(name = "photoURL", description = "Link to a picture of the perfume, as shown on the vendor website")
    private String photoURL;
    @ApiObjectField(name = "price", description = "Price of the perfume, in the currency (See 'currency' field)")
    private Float price;
    @ApiObjectField(name = "gender", description = "Gender for which this perfume is designed (FEMALE, MALE, UNI)")
    private Gender gender = Gender.UNI;
    @ApiObjectField(name = "vendor", description = "Name of the vendor offering this perfume")
    private String vendor;

    public String getSearchableName() {
        return searchableName;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
        this.searchableName = TextUtils.cleanupAndFlatten(brand.trim().toLowerCase() + " " + name.trim().toLowerCase());
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }


    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        this.searchableName = TextUtils.cleanupAndFlatten(brand.trim().toLowerCase() + " " + name.trim().toLowerCase());
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getAffiliateURL() {
        return AffiliateURL;
    }

    public void setAffiliateURL(String affiliateURL) {
        AffiliateURL = affiliateURL;
    }

    public String getPhotoURL() {
        return photoURL;
    }

    public void setPhotoURL(String photoURL) {
        this.photoURL = photoURL;
    }

    public Float getPrice() {
        return price;
    }

    public void setPrice(Float price) {
        this.price = price;
    }


    public Double getQuantity() {
        return quantity;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AffiliatePerfume that = (AffiliatePerfume) o;

        if (!brand.equals(that.brand)) return false;
        if (currency != null ? !currency.equals(that.currency) : that.currency != null) return false;
        if (gender != that.gender) return false;
        if (!name.equals(that.name)) return false;
        if (price != null ? !price.equals(that.price) : that.price != null) return false;
        if (quantity != null ? !quantity.equals(that.quantity) : that.quantity != null) return false;
        if (!vendor.equals(that.vendor)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + brand.hashCode();
        result = 31 * result + gender.hashCode();
        result = 31 * result + vendor.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "AffiliatePerfume{" +
                "brand='" + brand + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
