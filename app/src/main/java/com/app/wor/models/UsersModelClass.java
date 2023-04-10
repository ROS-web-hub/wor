package com.app.wor.models;

public class UsersModelClass {
    private String id;
    private String username;
    private String surname;
    private String email;
    private String password;

    public UsersModelClass() {}

    public UsersModelClass(String id, String username, String surname, String email, String password) {
        this.id = id;
        this.username = username;
        this.surname = surname;
        this.email = email;
        this.password = password;
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getSurname() {
        return surname;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}
