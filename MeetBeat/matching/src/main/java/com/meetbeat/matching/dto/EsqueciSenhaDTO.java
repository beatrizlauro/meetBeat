package com.meetbeat.matching.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class EsqueciSenhaDTO {

    @NotBlank(message = "O e-mail é obrigatório")
    @Email(message = "Informe um e-mail válido")
    private String email;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
