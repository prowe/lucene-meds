package com.sourceallies.lucinemeds.loader;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Product {
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
}
