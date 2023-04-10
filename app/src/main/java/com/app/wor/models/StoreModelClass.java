package com.app.wor.models;

public class StoreModelClass {
    private String id;
    private String westCode;
    private String  customerId;
    private String street;
    private String city;
    private String chain;

    public StoreModelClass() {}

    public StoreModelClass(String id, String westCode, String  customerId, String street, String city, String chain) {
        this.id = id;
        this.westCode = westCode;
        this.customerId = customerId;
        this.street = street;
        this.city = city;
        this.chain = chain;
    }

    public String getId() {
        return id;
    }

    public String getWestCode() {
        return westCode;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getStreet() {
        return street;
    }

    public String getCity() {
        return city;
    }

    public String getChain() {
        return chain;
    }
}
