package com.meetbeat.matching.dto;

public class MatchSuggestionDTO {

    private Long id;
    private String nome;
    private Long score;

    public MatchSuggestionDTO(Long id, String nome, Long score) {
        this.id = id;
        this.nome = nome;
        this.score = score;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public Long getScore() {
        return score;
    }
}