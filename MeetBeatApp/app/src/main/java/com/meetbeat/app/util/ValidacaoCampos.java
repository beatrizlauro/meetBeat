package com.meetbeat.app.util;

import android.text.InputFilter;
import android.widget.EditText;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Tratamento dos campos de texto: filtros que impedem a digitação de caracteres inválidos
 * (ex.: emoji no nome de usuário) e validações feitas antes do envio à API. As regras são as
 * mesmas da classe RegrasEntrada do backend, que continua validando tudo do lado do servidor.
 */
public class ValidacaoCampos {

    public enum Regra {
        /** Letras minúsculas sem acento, números, ponto e sublinhado (maiúsculas e acentos são convertidos). */
        USERNAME("Use apenas letras sem acento, números, ponto (.) e sublinhado (_)"),
        /** Letras (inclusive acentuadas), espaços, apóstrofo, hífen e ponto. */
        NOME("O nome pode ter apenas letras, espaços, apóstrofo e hífen"),
        /** Qualquer texto, exceto emojis. */
        SEM_EMOJI("Este campo não aceita emojis"),
        /** Qualquer texto sem espaços (ex.: e-mail). */
        SEM_ESPACOS("Este campo não aceita espaços");

        final String mensagem;

        Regra(String mensagem) {
            this.mensagem = mensagem;
        }
    }

    public static final int USERNAME_MIN = 3;
    public static final int USERNAME_MAX = 30;
    public static final int NOME_MIN = 2;
    public static final int NOME_MAX = 80;
    public static final int SENHA_MIN = 6;
    public static final int SENHA_MAX = 72;

    private ValidacaoCampos() {
    }

    /**
     * Adiciona ao campo o filtro da regra, mantendo os filtros que ele já tinha (como o maxLength
     * do XML). Quando um caractere é recusado, o campo mostra o motivo, para a pessoa não achar
     * que o teclado travou.
     */
    public static void restringir(EditText campo, Regra regra) {
        InputFilter filtro = (origem, inicio, fim, destino, dInicio, dFim) -> {
            StringBuilder aceito = new StringBuilder();
            boolean recusou = false;
            boolean alterou = false;

            String trecho = origem.subSequence(inicio, fim).toString();
            for (int i = 0; i < trecho.length(); ) {
                int cp = trecho.codePointAt(i);
                i += Character.charCount(cp);

                String convertido = converter(cp, regra);
                if (convertido == null) {
                    recusou = true;
                    alterou = true;
                } else {
                    if (!convertido.equals(new String(Character.toChars(cp)))) alterou = true;
                    aceito.append(convertido);
                }
            }

            if (recusou) {
                campo.post(() -> campo.setError(regra.mensagem));
            }
            return alterou ? aceito.toString() : null; // null = mantém o texto como foi digitado
        };

        InputFilter[] atuais = campo.getFilters();
        InputFilter[] novos = Arrays.copyOf(atuais, atuais.length + 1);
        novos[atuais.length] = filtro;
        campo.setFilters(novos);
    }

    /** Devolve o texto que deve entrar no lugar do caractere, ou null para recusá-lo. */
    private static String converter(int cp, Regra regra) {
        switch (regra) {
            case USERNAME: {
                // "J" vira "j" e "ã" vira "a", em vez de simplesmente sumirem
                String base = Normalizer.normalize(new String(Character.toChars(cp)), Normalizer.Form.NFD)
                        .replaceAll("\\p{M}", "")
                        .toLowerCase(Locale.ROOT);
                if (base.length() == 1) {
                    char c = base.charAt(0);
                    if ((c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || c == '.' || c == '_') {
                        return base;
                    }
                }
                return null;
            }
            case NOME: {
                int tipo = Character.getType(cp);
                boolean ok = Character.isLetter(cp)
                        || tipo == Character.NON_SPACING_MARK
                        || tipo == Character.COMBINING_SPACING_MARK
                        || cp == ' ' || cp == '\'' || cp == '’' || cp == '-' || cp == '.';
                return ok ? new String(Character.toChars(cp)) : null;
            }
            case SEM_EMOJI:
                return ehEmoji(cp) ? null : new String(Character.toChars(cp));
            case SEM_ESPACOS:
                return Character.isWhitespace(cp) ? null : new String(Character.toChars(cp));
            default:
                return new String(Character.toChars(cp));
        }
    }

    private static boolean ehEmoji(int cp) {
        int tipo = Character.getType(cp);
        return tipo == Character.OTHER_SYMBOL          // a maioria dos emojis e pictogramas
                || tipo == Character.PRIVATE_USE
                || tipo == Character.SURROGATE
                || cp == 0x200D                          // junção de emojis compostos
                || cp == 0xFE0F                          // seletor de variação "estilo emoji"
                || (cp >= 0x1F000 && cp <= 0x1FAFF);    // blocos de emoji, bandeiras e tons de pele
    }

    // ---------------------------------------------------------------------------------------
    // Validações no envio. Devolvem a mensagem de erro, ou null se o valor for válido.
    // ---------------------------------------------------------------------------------------

    public static String erroUsername(String username) {
        if (username.isEmpty()) return "Digite um nome de usuário";
        if (username.length() < USERNAME_MIN || username.length() > USERNAME_MAX) {
            return "O nome de usuário deve ter entre " + USERNAME_MIN + " e " + USERNAME_MAX + " caracteres";
        }
        if (!username.matches("[a-z0-9._]+")) return Regra.USERNAME.mensagem;
        if (username.startsWith(".") || username.endsWith(".")) return "O nome de usuário não pode começar nem terminar com ponto";
        if (username.contains("..")) return "O nome de usuário não pode ter dois pontos seguidos";
        return null;
    }

    public static String erroNome(String nome) {
        if (nome.isEmpty()) return "Digite um nome";
        if (nome.length() < NOME_MIN) return "O nome deve ter pelo menos " + NOME_MIN + " letras";
        if (!Character.isLetter(nome.codePointAt(0))) return "O nome deve começar com uma letra";
        return null;
    }

    // Mesma regra de RegrasEntrada.EMAIL no backend: uma "@", nada de ponto no início/fim ou repetido
    // antes dela, e domínio com ponto e terminação de pelo menos 2 letras (ex.: nome@gmail.com)
    private static final Pattern EMAIL = Pattern.compile(
            "^(?!\\.)(?!.*\\.\\.)[A-Za-z0-9._%+-]+(?<!\\.)@(?:[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?\\.)+[A-Za-z]{2,}$");

    public static String erroEmail(String email, boolean obrigatorio) {
        if (email.isEmpty()) return obrigatorio ? "Digite seu e-mail" : null;

        // Mensagens específicas para os erros mais comuns, dizendo o que falta
        int arrobas = email.length() - email.replace("@", "").length();
        if (arrobas == 0) return "O e-mail precisa ter um @ (ex.: nome@gmail.com)";
        if (arrobas > 1) return "O e-mail deve ter apenas um @";
        String antes = email.substring(0, email.indexOf('@'));
        String dominio = email.substring(email.indexOf('@') + 1);
        if (antes.isEmpty()) return "Falta a parte antes do @ (ex.: nome@gmail.com)";
        if (!dominio.contains(".")) return "Falta o domínio completo depois do @ (ex.: @gmail.com)";

        return EMAIL.matcher(email).matches() ? null : "Digite um e-mail válido, como nome@gmail.com";
    }

    /**
     * Valida o campo assim que a pessoa sai dele, para o erro aparecer antes de tocar em salvar.
     * Campo vazio não é marcado aqui; a obrigatoriedade é conferida no envio.
     */
    public static void validarEmailAoSair(EditText campo) {
        campo.setOnFocusChangeListener((v, temFoco) -> {
            if (temFoco) return;
            String email = campo.getText().toString().trim();
            if (!email.isEmpty()) {
                campo.setError(erroEmail(email.toLowerCase(Locale.ROOT), false));
            }
        });
    }

    public static String erroSenha(String senha) {
        if (senha.length() < SENHA_MIN || senha.length() > SENHA_MAX) {
            return "A senha deve ter entre " + SENHA_MIN + " e " + SENHA_MAX + " caracteres";
        }
        return null;
    }

    /** Remove espaços nas pontas e junta espaços repetidos ("Ana   Silva" vira "Ana Silva"). */
    public static String normalizarEspacos(String texto) {
        return texto.trim().replaceAll("\\s+", " ");
    }

    /** Mostra o erro no próprio campo e leva o foco até ele. Devolve true se havia erro. */
    public static boolean mostrarErro(EditText campo, String erro) {
        if (erro == null) return false;
        campo.setError(erro);
        campo.requestFocus();
        return true;
    }
}
