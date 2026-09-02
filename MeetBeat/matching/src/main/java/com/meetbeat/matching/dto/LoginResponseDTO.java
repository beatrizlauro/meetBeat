package com.meetbeat.matching.dto;

public class LoginResponseDTO {

    private boolean sucesso;
    private String mensagem;
    private Long usuarioId;
    private String nome;
    private boolean onboardingCompleto;
    private String token;

    public LoginResponseDTO(boolean sucesso, String mensagem, Long usuarioId, String nome, boolean onboardingCompleto) {
        this.sucesso = sucesso;
        this.mensagem = mensagem;
        this.usuarioId = usuarioId;
        this.nome = nome;
        this.onboardingCompleto = onboardingCompleto;
    }

    public boolean isSucesso() { return sucesso; }
    public String getMensagem() { return mensagem; }
    public Long getUsuarioId() { return usuarioId; }
    public String getNome() { return nome; }
    public boolean isOnboardingCompleto() { return onboardingCompleto; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
}
