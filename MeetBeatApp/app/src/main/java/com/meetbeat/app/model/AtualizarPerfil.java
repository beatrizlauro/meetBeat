package com.meetbeat.app.model;

public class AtualizarPerfil {

    private String nome;
    private String username;
    private String musicaFavorita;
    private String artistaFavorito;
    private String novaSenha;

    public AtualizarPerfil(String nome, String username, String musicaFavorita, String artistaFavorito, String novaSenha) {
        this.nome = nome;
        this.username = username;
        this.musicaFavorita = musicaFavorita;
        this.artistaFavorito = artistaFavorito;
        this.novaSenha = novaSenha;
    }
}
