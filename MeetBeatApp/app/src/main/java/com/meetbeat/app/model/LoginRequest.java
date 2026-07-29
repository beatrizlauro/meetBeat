package com.meetbeat.app.model;

public class LoginRequest {

    private String nome;
    private String senha;

    public LoginRequest(String nome, String senha) {
        this.nome = nome;
        this.senha = senha;
    }
}