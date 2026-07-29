package com.meetbeat.app.model;

public class LoginResponse {

    private boolean sucesso;
    private String mensagem;
    private Long usuarioId;
    private String nome;

    public boolean isSucesso() {
        return sucesso;
    }

    public String getMensagem() {
        return mensagem;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public String getNome() {
        return nome;
    }
}