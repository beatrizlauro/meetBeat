package com.meetbeat.matching.dto;

import java.util.List;

public class MatchSuggestionDTO {

    private Long id;
    private String nome;
    private Long score;
    private List<String> generosComuns;
    private String fotoShow;
    private String bio;

    public MatchSuggestionDTO(Long id, String nome, Long score, List<String> generosComuns, String fotoShow, String bio) {
        this.id = id;
        this.nome = nome;
        this.score = score;
        this.generosComuns = generosComuns;
        this.fotoShow = fotoShow;
        this.bio = bio;
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public Long getScore() { return score; }
    public List<String> getGenerosComuns() { return generosComuns; }
    public String getFotoShow() { return fotoShow; }
    public String getBio() { return bio; }
}
