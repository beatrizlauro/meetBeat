package com.meetbeat.app.model;

public class AtualizarPerfil {

    private String nome;
    private String username;
    private String email;
    private String musicaFavorita;
    private String artistaFavorito;
    private String novaSenha;

    public AtualizarPerfil(String nome, String username, String email, String musicaFavorita, String artistaFavorito, String novaSenha) {
        this.nome = nome;
        this.username = username;
        this.email = email;
        this.musicaFavorita = musicaFavorita;
        this.artistaFavorito = artistaFavorito;
        this.novaSenha = novaSenha;
    }
}
