package com.meetbeat.matching.dto;

import jakarta.validation.constraints.Size;

public class AtualizarBioDTO {

    @Size(max = 300, message = "A biografia deve ter no máximo 300 caracteres")
    private String bio;

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
}
