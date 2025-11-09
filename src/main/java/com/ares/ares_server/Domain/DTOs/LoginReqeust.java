package com.ares.ares_server.Domain.DTOs;

/// asta mai mult doar orientativ ar trebui login req dto si login res dto
public class LoginReqeust {
    private String email;
    private String password;

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    public LoginReqeust(String email, String password) {
        this.email = email;
        this.password = password;

    }
}

