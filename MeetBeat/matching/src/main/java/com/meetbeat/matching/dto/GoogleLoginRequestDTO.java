package com.meetbeat.matching.dto;

import jakarta.validation.constraints.NotBlank;

public class GoogleLoginRequestDTO {

    @NotBlank(message = "O token de identificação do Google é obrigatório")
    private String idToken;

    public String getIdToken() { return idToken; }
    public void setIdToken(String idToken) { this.idToken = idToken; }
}
