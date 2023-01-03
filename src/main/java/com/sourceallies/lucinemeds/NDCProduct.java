package com.sourceallies.lucinemeds;

import com.fasterxml.jackson.annotation.JsonProperty;

public class NDCProduct {
    @JsonProperty("product_ndc")
    String productNDC;
    @JsonProperty("generic_name")
    String genericName;
    @JsonProperty("labeler_name")
    String labelerName;
    @JsonProperty("brand_name")
    String brandName;

    @Override
    public String toString() {
        return "Product:" +
        " productNDC " + productNDC +
        " genericName " + genericName +
        " labelerName " + labelerName +
        " brandName " + brandName;
    }

    public String getBrandName() {
        return brandName;
    }
    public String getGenericName() {
        return genericName;
    }
    public String getLabelerName() {
        return labelerName;
    }
    public String getProductNDC() {
        return productNDC;
    }

    public String getCode() {
        return productNDC;
    }
}
