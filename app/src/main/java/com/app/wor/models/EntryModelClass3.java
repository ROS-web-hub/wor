package com.app.wor.models;

import java.lang.String;
import java.lang.String;

import java.util.List;

public class EntryModelClass3 {
    private String id;
    private String date;
    private String userId;
    private String username;
    private StoreModelClass store;
    private String question1;
    private String question2;
    private List<ProductModelClass> productsList;
    private List<String> urlListQ4;
    private List<String> urlListQ5;

    public EntryModelClass3() {}

    public EntryModelClass3(String id, String date, String userId, String username, StoreModelClass store,
                            String question1, String question2, List<ProductModelClass> productsList,
                            List<String> urlListQ4, List<String> urlListQ5) {
        this.id = id;
        this.date = date;
        this.userId = userId;
        this.username = username;
        this.store = store;
        this.question1 = question1;
        this.question2 = question2;
        this.productsList = productsList;
        this.urlListQ4 = urlListQ4;
        this.urlListQ5 = urlListQ5;
    }

    public String getId() {
        return id;
    }

    public String getDate() {
        return date;
    }

    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public StoreModelClass getStore() {
        return store;
    }

    public String getQuestion1() {
        return question1;
    }

    public String getQuestion2() {
        return question2;
    }

    public List<ProductModelClass> getProductsList() {
        return productsList;
    }

    public List<String> getUrlListQ4() {
        return urlListQ4;
    }

    public List<String> getUrlListQ5() {
        return urlListQ5;
    }
}
