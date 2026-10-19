package com.meetbeat.matching.dto;

import java.util.List;

public class MatchConfirmadoDTO {

    private Long id;
    private String nome;
    private String fotoShow;
    private List<String> generosComuns;
    private String bio;

    public MatchConfirmadoDTO(Long id, String nome, String fotoShow, List<String> generosComuns, String bio) {
        this.id = id;
        this.nome = nome;
        this.fotoShow = fotoShow;
        this.generosComuns = generosComuns;
        this.bio = bio;
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getFotoShow() { return fotoShow; }
    public List<String> getGenerosComuns() { return generosComuns; }
    public String getBio() { return bio; }
}
