package com.meetbeat.matching.dto;

public class LoginResponseDTO {

    private boolean sucesso;
    private String mensagem;
    private Long usuarioId;
    private String nome;

    public LoginResponseDTO(boolean sucesso, String mensagem, Long usuarioId, String nome) {
        this.sucesso = sucesso;
        this.mensagem = mensagem;
        this.usuarioId = usuarioId;
        this.nome = nome;
    }

    public boolean isSucesso() { return sucesso; }
    public String getMensagem() { return mensagem; }
    public Long getUsuarioId() { return usuarioId; }
    public String getNome() { return nome; }
}