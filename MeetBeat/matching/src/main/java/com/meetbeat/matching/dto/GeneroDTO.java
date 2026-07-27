package com.meetbeat.matching.dto;

public class GeneroDTO {

    private Long id;
    private String nome;

    public GeneroDTO(Long id, String nome) {
        this.id = id;
        this.nome = nome;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }
}