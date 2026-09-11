package com.plataforma_leilao.app.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Lê o header Authorization e libera ou bloqueia a requisição.
 * Rotas públicas: login e cadastro.
 *
 * Observação: erro no Filter NÃO passa pelo @RestControllerAdvice,
 * por isso a resposta 401 é montada aqui.
 */
@Component
@Order(2)
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String path = request.getRequestURI();

        if (rotaPublica(path) || !path.startsWith("/api/")) {
            filterChain.doFilter(request, response);
            return;
        }

        String header = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (header == null || !header.startsWith("Bearer ")) {
            escreverUnauthorized(response);
            return;
        }

        String token = header.substring("Bearer ".length()).trim();

        if (!jwtService.tokenValido(token)) {
            escreverUnauthorized(response);
            return;
        }

        request.setAttribute("userEmail", jwtService.extrairEmail(token));
        filterChain.doFilter(request, response);
    }

    private boolean rotaPublica(String path) {
        return "/api/user/login".equals(path)
                || "/api/user/cadastrar".equals(path);
    }

    private void escreverUnauthorized(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        String json = "{\"code\":\"TOKEN_INVALIDO\",\"message\":\"Token ausente ou inválido. Faça login novamente.\"}";
        response.getWriter().write(json);
    }
}
