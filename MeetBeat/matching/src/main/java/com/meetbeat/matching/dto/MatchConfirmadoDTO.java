package com.meetbeat.matching.dto;

import java.util.List;

public class MatchConfirmadoDTO {

    private Long id;
    private String nome;
    private String fotoShow;
    private List<String> generosComuns;

    public MatchConfirmadoDTO(Long id, String nome, String fotoShow, List<String> generosComuns) {
        this.id = id;
        this.nome = nome;
        this.fotoShow = fotoShow;
        this.generosComuns = generosComuns;
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getFotoShow() { return fotoShow; }
    public List<String> getGenerosComuns() { return generosComuns; }
}