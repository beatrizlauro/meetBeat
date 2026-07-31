package com.meetbeat.matching.model;

import org.springframework.data.neo4j.core.schema.*;
import java.util.HashSet;
import java.util.Set;

@Node("Pessoa")
public class UserNode {

    @Id
    @GeneratedValue
    private Long id;

    private String nome;
    private String bio;
    private String fotoShow;

    @Relationship(type = "GOSTA_DE", direction = Relationship.Direction.OUTGOING)
    private Set<MusicInterest> generos = new HashSet<>();

    public UserNode() {
    }

    public UserNode(String nome, String bio) {
        this.nome = nome;
        this.bio = bio;
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

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public Set<MusicInterest> getGeneros() {
        return generos;
    }

    public void setGeneros(Set<MusicInterest> generos) {
        this.generos = generos;
    }

    public void adicionarGenero(MusicInterest genero) {
        this.generos.add(genero);
    }

    public String getFotoShow() {
        return fotoShow;
    }

    public void setFotoShow(String fotoShow) {
        this.fotoShow = fotoShow;
    }

    private String senhaHash;

    public String getSenhaHash() {
        return senhaHash;
    }

    public void setSenhaHash(String senhaHash) {
        this.senhaHash = senhaHash;
    }

    private String username;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    private Boolean onboardingCompleto;

    public Boolean getOnboardingCompleto() {
        return onboardingCompleto;
    }

    public void setOnboardingCompleto(Boolean onboardingCompleto) {
        this.onboardingCompleto = onboardingCompleto;
    }
    
}