package com.meetbeat.matching.dto;

public class SwipeResponseDTO {

    private boolean match;
    private String mensagem;

    public SwipeResponseDTO(boolean match, String mensagem) {
        this.match = match;
        this.mensagem = mensagem;
    }

    public boolean isMatch() {
        return match;
    }

    public String getMensagem() {
        return mensagem;
    }
}