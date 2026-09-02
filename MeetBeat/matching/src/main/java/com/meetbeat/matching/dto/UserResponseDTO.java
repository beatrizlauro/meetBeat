package com.meetbeat.matching.dto;

import com.meetbeat.matching.model.UserNode;

import java.util.List;

public class UserResponseDTO {

    private Long id;
    private String nome;
    private String username;
    private String bio;
    private String fotoShow;
    private String fotoPerfil;
    private String musicaFavorita;
    private String artistaFavorito;
    private Boolean onboardingCompleto;
    private List<String> fotosPosts;

    public static UserResponseDTO from(UserNode usuario) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.id = usuario.getId();
        dto.nome = usuario.getNome();
        dto.username = usuario.getUsername();
        dto.bio = usuario.getBio();
        dto.fotoShow = usuario.getFotoShow();
        dto.fotoPerfil = usuario.getFotoPerfil();
        dto.musicaFavorita = usuario.getMusicaFavorita();
        dto.artistaFavorito = usuario.getArtistaFavorito();
        dto.onboardingCompleto = usuario.getOnboardingCompleto();
        dto.fotosPosts = usuario.getFotosPosts();
        return dto;
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getUsername() { return username; }
    public String getBio() { return bio; }
    public String getFotoShow() { return fotoShow; }
    public String getFotoPerfil() { return fotoPerfil; }
    public String getMusicaFavorita() { return musicaFavorita; }
    public String getArtistaFavorito() { return artistaFavorito; }
    public Boolean getOnboardingCompleto() { return onboardingCompleto; }
    public List<String> getFotosPosts() { return fotosPosts; }
}
