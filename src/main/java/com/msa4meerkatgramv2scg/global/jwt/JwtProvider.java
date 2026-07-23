package com.msa4meerkatgramv2scg.global.jwt;

import com.msa4meerkatgramv2scg.global.errors.custom.InvalidTokenException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import javax.crypto.SecretKey;
import java.util.Optional;

@Component
public class JwtProvider {
    private final SecretKey secretKey;
    private final JwtConfig jwtConfig;

    public JwtProvider(JwtConfig jwtConfig){
        this.jwtConfig= jwtConfig;
        this.secretKey= Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtConfig.secret()));
    }

    // 베어러토큰 추출
    public Optional<String> extractAccessToken(ServerWebExchange exchange){
        String bearerToken = exchange.getRequest().getHeaders().getFirst(jwtConfig.headerKey()); // 내가 넘겨주는 이름의 키를 찾음

        // 베어러 토큰이 null 이거나, jwtConfig.scheme() 으로 시작하는지 확인 하라
        if(bearerToken == null || !bearerToken.startsWith(jwtConfig.scheme())) {
            return Optional.empty();
        }

        // 베어러 토큰에서, 스키마의 길이만큼 잘라낸다. 그리하여, 토큰만 추출되서 반환된다.
        return Optional.of(bearerToken.substring(jwtConfig.scheme().length()).trim());
    }

    // 클레임 추출
    public Claims extractClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(this.secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    ;
        } catch (ExpiredJwtException e) {
            throw new InvalidTokenException("토큰이 만료 되었습니다.");
        } catch (UnsupportedJwtException e) {
            throw new InvalidTokenException("서명이 위조된 되었습니다.");
        } catch (MalformedJwtException e) {
            throw new InvalidTokenException("토큰 형식이 올바르지 않습니다.");
        } catch (JwtException | IllegalArgumentException e) {
            throw new InvalidTokenException("토큰 검증에 실패 했습니다..");
        }

    }
}
