package com.meetbeat.matching.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class NovoUsuarioDTO {

    @NotBlank(message = "O nome de exibição é obrigatório")
    @Size(min = 2, max = 80, message = "O nome de exibição deve ter entre 2 e 80 caracteres")
    @Pattern(regexp = RegrasEntrada.NOME, message = RegrasEntrada.MENSAGEM_NOME)
    private String nome;

    @NotBlank(message = "O nome de usuário é obrigatório")
    @Size(min = 3, max = 30, message = "O nome de usuário deve ter entre 3 e 30 caracteres")
    @Pattern(regexp = RegrasEntrada.USERNAME, message = RegrasEntrada.MENSAGEM_USERNAME)
    private String username;

    @NotBlank(message = "O e-mail é obrigatório")
    @Email(message = "Informe um e-mail válido")
    @Pattern(regexp = RegrasEntrada.EMAIL, message = RegrasEntrada.MENSAGEM_EMAIL)
    @Size(max = 254, message = "O e-mail deve ter no máximo 254 caracteres")
    private String email;

    @Size(max = 300, message = "A biografia deve ter no máximo 300 caracteres")
    private String bio;

    @NotBlank(message = "A senha é obrigatória")
    @Size(min = 6, max = 72, message = "A senha deve ter entre 6 e 72 caracteres")
    private String senha;

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }
}
