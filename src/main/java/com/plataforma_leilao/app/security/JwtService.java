package com.plataforma_leilao.app.security;

import com.plataforma_leilao.app.model.EPermissao;
import com.plataforma_leilao.app.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class JwtService {

    private final SecretKey secretKey;
    private final long expirationMs;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-ms}") long expirationMs
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    public String gerarToken(User user) {
        Date agora = new Date();
        Date expiracao = new Date(agora.getTime() + expirationMs);
        String jti = UUID.randomUUID().toString();

        return Jwts.builder()
                .id(jti)
                .subject(user.getEmail())
                .claim("userId", user.getId())
                .claim("admin", user.isAdmin())
                .claim("permissoes", listarPermissoes(user))
                .issuedAt(agora)
                .expiration(expiracao)
                .signWith(secretKey)
                .compact();
    }

    public Claims lerClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean tokenValido(String token) {
        try {
            lerClaims(token);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public String extrairEmail(String token) {
        return lerClaims(token).getSubject();
    }

    public List<String> listarPermissoes(User user) {
        if (user.isAdmin()) {
            return Arrays.stream(EPermissao.values()).map(Enum::name).toList();
        }
        if (user.getGrupo() == null || user.getGrupo().getPermissoes() == null) {
            return List.of();
        }
        return user.getGrupo().getPermissoes().stream().map(Enum::name).toList();
    }
}
