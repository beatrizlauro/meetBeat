package com.meetbeat.app.model;

import java.util.List;

public class UsuarioCompleto {

    private long id;
    private String nome;
    private String username;
    private String bio;
    private String fotoShow;
    private String fotoPerfil;
    private String musicaFavorita;
    private String artistaFavorito;
    private List<String> fotosPosts;
    private boolean mostrarCurtidas;

    public long getId() { return id; }
    public String getNome() { return nome; }
    public String getUsername() { return username; }
    public String getBio() { return bio; }
    public String getFotoShow() { return fotoShow; }
    public String getFotoPerfil() { return fotoPerfil; }
    public String getMusicaFavorita() { return musicaFavorita; }
    public String getArtistaFavorito() { return artistaFavorito; }
    public List<String> getFotosPosts() { return fotosPosts; }
    public boolean isMostrarCurtidas() { return mostrarCurtidas; }
}