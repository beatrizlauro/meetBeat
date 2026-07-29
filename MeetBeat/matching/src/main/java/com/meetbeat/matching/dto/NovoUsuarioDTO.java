package com.meetbeat.matching.dto;

public class NovoUsuarioDTO {

    private String nome;
    private String username;
    private String bio;
    private String senha;

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }
}