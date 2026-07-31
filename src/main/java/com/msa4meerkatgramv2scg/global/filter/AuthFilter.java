package com.msa4meerkatgramv2scg.global.filter;

import com.msa4meerkatgramv2scg.global.jwt.JwtConfig;
import com.msa4meerkatgramv2scg.global.jwt.JwtProvider;
import com.msa4meerkatgramv2scg.global.response.GlobalRes;
import com.msa4meerkatgramv2scg.global.response.constant.CustomResponseCode;
import io.jsonwebtoken.Claims;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import tools.jackson.databind.ObjectMapper;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AuthFilter implements GlobalFilter, Ordered {

    private final JwtProvider jwtProvider;
    private final JwtConfig jwtConfig;
    private final ObjectMapper objectMapper;

    @Override
    @NonNull
    public Mono<Void> filter(@NonNull ServerWebExchange exchange,@NonNull GatewayFilterChain chain) {

        try{
            // HTTP Header에서 액세스토큰 추출
            Optional<String> optionalToken = jwtProvider.extractAccessToken(exchange);

            // 액세스토큰이 없을경우, 인증과정을 건너뛰고, 다음필터로 진행하도록 하는 처리.
            if(optionalToken.isEmpty()){

                // 필터체인을 가져와서, 필터에, 익스체인을 전달해주면, 이 정보 그대로, 다음 필터로 넘어간다.
                return chain.filter(exchange);
            }

            // JWT 파싱 및 검증
            Claims claims = jwtProvider.extractClaims(optionalToken.get());

            // 하위서비스로 전달할, Http Header 세팅 처리
            ServerHttpRequest serverRequest = exchange.getRequest().mutate()
                    .headers(httpHeaders -> httpHeaders.remove(jwtConfig.headerKey())) // 불필요해진 토큰 외부 노출을 막기위해 제거
                    .header("X-User-Id", claims.getSubject()) // 유저 ID 세팅
                    .header("X-User-Role",claims.get("role",String.class))
                    .build();

            return chain.filter(exchange.mutate().request(serverRequest).build());

        } catch (Exception e) {
            return this.unauthorized(exchange);
        }

    }


    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(CustomResponseCode.SCG_INVALID_TOKEN_ERROR.getHttpStatus());
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        byte[] bytes = objectMapper.writeValueAsBytes(GlobalRes.from(CustomResponseCode.SCG_INVALID_TOKEN_ERROR));
        return response.writeWith(Mono.just(response.bufferFactory().wrap(bytes)));
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
/**
 *
 * 필터의 실행 순서 결정
 *  - 게이트웨이의 기본 라우팅(0) 보다, 먼저 실행되어야 하므로 -1 을 설정
 *
 */
// 게이트웨이는, 각 어떤기능에 인증기능이 필요한지는 알수가 없다.
// 그렇기에, 토큰이 없을경우에는, 그다음 기능으로 바로 진행되게끔 만든다.

// 필터가 하나하나 처리가 끝나면, 다음필터로 넘어갈 수 있도록 처리를 해줘야함.

// 스프링 프레임워크는, 필터쪽에서 처리가 다 끝나고 나면, 그다음 처리로 넘어감.
// 지금 우리는 필터쪽을 만들고 있음.


// 클레임이 뭐지?
//

// 클레임에 있는 role 에 있는걸 값으로 추가할것이다.
// 그런데  getSubject() 으로는 프라이빗 클레임을 가져올 수가 없다.

