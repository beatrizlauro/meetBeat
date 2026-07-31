package com.meetbeat.matching.dto;

public class AtualizarMusicaArtistaDTO {

    private String musicaFavorita;
    private String artistaFavorito;

    public String getMusicaFavorita() { return musicaFavorita; }
    public void setMusicaFavorita(String musicaFavorita) { this.musicaFavorita = musicaFavorita; }

    public String getArtistaFavorito() { return artistaFavorito; }
    public void setArtistaFavorito(String artistaFavorito) { this.artistaFavorito = artistaFavorito; }
}