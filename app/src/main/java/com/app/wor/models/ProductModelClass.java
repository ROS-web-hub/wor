package com.app.wor.models;

public class ProductModelClass {
    private String id;
    private String brandName;
    private String category;

    public ProductModelClass() {}

    public ProductModelClass(String id, String brandName, String category) {
        this.id = id;
        this.brandName = brandName;
        this.category = category;
    }

    public String getId() {
        return id;
    }

    public String getBrandName() {
        return brandName;
    }

    public String getCategory() {
        return category;
    }
}
