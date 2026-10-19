package com.meetbeat.matching.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class AtualizarPerfilDTO {

    @NotBlank(message = "O nome de exibição é obrigatório")
    @Size(min = 2, max = 80, message = "O nome de exibição deve ter entre 2 e 80 caracteres")
    @Pattern(regexp = RegrasEntrada.NOME, message = RegrasEntrada.MENSAGEM_NOME)
    private String nome;

    @NotBlank(message = "O nome de usuário é obrigatório")
    @Size(min = 3, max = 30, message = "O nome de usuário deve ter entre 3 e 30 caracteres")
    @Pattern(regexp = RegrasEntrada.USERNAME, message = RegrasEntrada.MENSAGEM_USERNAME)
    private String username;

    @Email(message = "Informe um e-mail válido")
    @Pattern(regexp = RegrasEntrada.EMAIL_OU_VAZIO, message = RegrasEntrada.MENSAGEM_EMAIL)
    @Size(max = 254, message = "O e-mail deve ter no máximo 254 caracteres")
    private String email; // opcional — null/vazio se a pessoa preferir não cadastrar

    @Size(max = 120, message = "O nome da música favorita deve ter no máximo 120 caracteres")
    @Pattern(regexp = RegrasEntrada.SEM_EMOJI, message = RegrasEntrada.MENSAGEM_SEM_EMOJI)
    private String musicaFavorita;

    @Size(max = 120, message = "O nome do artista favorito deve ter no máximo 120 caracteres")
    @Pattern(regexp = RegrasEntrada.SEM_EMOJI, message = RegrasEntrada.MENSAGEM_SEM_EMOJI)
    private String artistaFavorito;

    @Size(min = 6, max = 72, message = "A senha deve ter entre 6 e 72 caracteres")
    private String novaSenha; // opcional — null se não for trocar

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMusicaFavorita() { return musicaFavorita; }
    public void setMusicaFavorita(String musicaFavorita) { this.musicaFavorita = musicaFavorita; }

    public String getArtistaFavorito() { return artistaFavorito; }
    public void setArtistaFavorito(String artistaFavorito) { this.artistaFavorito = artistaFavorito; }

    public String getNovaSenha() { return novaSenha; }
    public void setNovaSenha(String novaSenha) { this.novaSenha = novaSenha; }
}
