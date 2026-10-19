package com.meetbeat.app.model;

import java.util.List;

public class MatchSuggestion {

    private long id;
    private String nome;
    private long score;
    private List<String> generosComuns;
    private String fotoShow;
    private String bio;

    public long getId() { return id; }
    public String getNome() { return nome; }
    public long getScore() { return score; }
    public List<String> getGenerosComuns() { return generosComuns; }
    public String getFotoShow() { return fotoShow; }
    public String getBio() { return bio; }
}