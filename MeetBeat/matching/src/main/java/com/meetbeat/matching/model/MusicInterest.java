package com.meetbeat.matching.model;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Node;

@Node("Genero")
public class MusicInterest {

    @Id
    @GeneratedValue
    private Long id;

    private String nome;

    public MusicInterest() {
    }

    public MusicInterest(String nome) {
        this.nome = nome;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }
}