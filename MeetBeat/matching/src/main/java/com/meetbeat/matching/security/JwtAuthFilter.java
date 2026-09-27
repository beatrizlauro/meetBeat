package com.meetbeat.matching.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    public static final String ATRIBUTO_USUARIO_ID = "usuarioAutenticadoId";

    private static final Set<String> ROTAS_PUBLICAS = Set.of(
            "/usuarios/login",
            "/usuarios/login-google",
            "/usuarios/esqueci-senha",
            "/usuarios/redefinir-senha"
    );

    private final JwtService jwtService;

    public JwtAuthFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        if (ROTAS_PUBLICAS.contains(path)) {
            return true;
        }
        if ("/usuarios".equals(path) && "POST".equals(request.getMethod())) {
            return true; // cadastro de novo usuário
        }
        return path.startsWith("/uploads/") || path.startsWith("/actuator");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token de autenticação ausente");
            return;
        }

        try {
            Long usuarioId = jwtService.extrairUsuarioId(header.substring(7));
            request.setAttribute(ATRIBUTO_USUARIO_ID, usuarioId);
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token de autenticação inválido ou expirado");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
