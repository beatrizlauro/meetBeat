package com.meetbeat.app.model;

public class RedefinirSenhaRequest {

    private String email;
    private String codigo;
    private String novaSenha;

    public RedefinirSenhaRequest(String email, String codigo, String novaSenha) {
        this.email = email;
        this.codigo = codigo;
        this.novaSenha = novaSenha;
    }
}
