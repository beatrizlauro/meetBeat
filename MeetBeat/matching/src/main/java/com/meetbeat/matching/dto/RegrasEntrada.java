package com.meetbeat.matching.dto;

/**
 * Formatos aceitos nos campos de texto enviados pelo aplicativo, usados nas anotações @Pattern
 * dos DTOs. O aplicativo aplica as mesmas regras enquanto o usuário digita, mas é a API que as
 * garante, já que qualquer cliente pode enviar requisições diretamente.
 */
public final class RegrasEntrada {

    private RegrasEntrada() {
    }

    /** Letras minúsculas sem acento, números, ponto e sublinhado; sem ponto no início, no fim ou repetido. */
    public static final String USERNAME = "^(?!\\.)(?!.*\\.\\.)[a-z0-9._]+(?<!\\.)$";
    public static final String MENSAGEM_USERNAME =
            "O nome de usuário pode ter apenas letras minúsculas sem acento, números, ponto e sublinhado, "
                    + "sem começar ou terminar com ponto";

    /** Letras (inclusive acentuadas), espaços, apóstrofo, hífen e ponto; começa com letra. */
    public static final String NOME = "^\\p{L}[\\p{L}\\p{M} '’.-]*$";
    public static final String MENSAGEM_NOME =
            "O nome de exibição pode ter apenas letras, espaços, apóstrofo, hífen e ponto";

    /**
     * E-mail com uma única "@", parte antes da "@" sem ponto no início, no fim ou repetido, e domínio
     * com pelo menos um ponto e terminação de 2 letras ou mais (ex.: nome@gmail.com). Complementa o
     * {@code @Email}, que aceita endereços sem ponto no domínio, como "nome@gmail".
     */
    public static final String EMAIL =
            "^(?!\\.)(?!.*\\.\\.)[A-Za-z0-9._%+-]+(?<!\\.)@(?:[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?\\.)+[A-Za-z]{2,}$";
    public static final String MENSAGEM_EMAIL = "Informe um e-mail válido, como nome@gmail.com";

    /** Igual a EMAIL, mas aceitando vazio, para campos em que o e-mail é opcional (edição de perfil). */
    public static final String EMAIL_OU_VAZIO = "^$|" + EMAIL;

    /** Qualquer texto sem emojis e outros símbolos gráficos. */
    public static final String SEM_EMOJI = "^[^\\p{So}\\p{Co}\\x{200D}\\x{FE0F}]*$";
    public static final String MENSAGEM_SEM_EMOJI = "Este campo não aceita emojis";
}
