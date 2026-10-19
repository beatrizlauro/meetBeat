package com.meetbeat.app.model;

import java.util.List;

public class MatchConfirmado {

    private long id;
    private String nome;
    private String fotoShow;
    private List<String> generosComuns;
    private String bio;

    public long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getFotoShow() {
        return fotoShow;
    }

    public List<String> getGenerosComuns() {
        return generosComuns;
    }

    public String getBio() {
        return bio;
    }
}