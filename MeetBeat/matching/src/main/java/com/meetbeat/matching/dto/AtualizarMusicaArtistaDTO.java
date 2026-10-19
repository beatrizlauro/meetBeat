package com.meetbeat.matching.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class AtualizarMusicaArtistaDTO {

    @Size(max = 120, message = "O nome da música favorita deve ter no máximo 120 caracteres")
    @Pattern(regexp = RegrasEntrada.SEM_EMOJI, message = RegrasEntrada.MENSAGEM_SEM_EMOJI)
    private String musicaFavorita;

    @Size(max = 120, message = "O nome do artista favorito deve ter no máximo 120 caracteres")
    @Pattern(regexp = RegrasEntrada.SEM_EMOJI, message = RegrasEntrada.MENSAGEM_SEM_EMOJI)
    private String artistaFavorito;

    public String getMusicaFavorita() { return musicaFavorita; }
    public void setMusicaFavorita(String musicaFavorita) { this.musicaFavorita = musicaFavorita; }

    public String getArtistaFavorito() { return artistaFavorito; }
    public void setArtistaFavorito(String artistaFavorito) { this.artistaFavorito = artistaFavorito; }
}
