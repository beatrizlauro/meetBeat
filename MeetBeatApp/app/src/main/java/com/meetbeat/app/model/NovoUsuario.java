package com.meetbeat.app.model;

public class NovoUsuario {

    private String nome;
    private String username;
    private String bio;
    private String senha;

    public NovoUsuario(String nome, String username, String bio, String senha) {
        this.nome = nome;
        this.username = username;
        this.bio = bio;
        this.senha = senha;
    }
}