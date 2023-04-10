package com.app.wor.models;

import java.util.List;

public class EntryModelClass2 {
    private String id;
    private String date;
    private String userId;
    private String username;
    private String store;
    private String question1;
    private String question2;
    private String productsList;
    private String urlListQ4;
    private String urlListQ5;

    public EntryModelClass2() {}

    public EntryModelClass2(String id, String date, String userId, String username, String store,
                            String question1, String question2, String productsList,
                            String urlListQ4, String urlListQ5) {
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

    public String getStore() {
        return store;
    }

    public String getQuestion1() {
        return question1;
    }

    public String getQuestion2() {
        return question2;
    }

    public String getProductsList() {
        return productsList;
    }

    public String getUrlListQ4() {
        return urlListQ4;
    }

    public String getUrlListQ5() {
        return urlListQ5;
    }
}
