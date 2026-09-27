package com.meetbeat.app.model;

public class NovoUsuario {

    private String nome;
    private String username;
    private String email;
    private String bio;
    private String senha;

    public NovoUsuario(String nome, String username, String email, String bio, String senha) {
        this.nome = nome;
        this.username = username;
        this.email = email;
        this.bio = bio;
        this.senha = senha;
    }
}