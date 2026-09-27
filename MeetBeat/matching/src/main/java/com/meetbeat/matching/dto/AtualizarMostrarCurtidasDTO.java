package com.meetbeat.matching.dto;

import jakarta.validation.constraints.NotNull;

public class AtualizarMostrarCurtidasDTO {

    @NotNull(message = "O campo mostrarCurtidas é obrigatório")
    private Boolean mostrarCurtidas;

    public Boolean getMostrarCurtidas() { return mostrarCurtidas; }
    public void setMostrarCurtidas(Boolean mostrarCurtidas) { this.mostrarCurtidas = mostrarCurtidas; }
}
