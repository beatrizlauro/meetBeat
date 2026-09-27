package com.meetbeat.app.model;

import java.util.List;

public class CurtidaRecebida {

    private long id;
    private String nome;
    private String fotoShow;
    private List<String> generosComuns;

    public long getId() { return id; }
    public String getNome() { return nome; }
    public String getFotoShow() { return fotoShow; }
    public List<String> getGenerosComuns() { return generosComuns; }
}
