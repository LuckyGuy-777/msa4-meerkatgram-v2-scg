package com.msa4meerkatgramv2scg.global.errors;


import com.msa4meerkatgramv2scg.global.response.GlobalRes;
import com.msa4meerkatgramv2scg.global.response.constant.CustomResponseCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;
import tools.jackson.databind.ObjectMapper;


@Component
@Order(-2) //1. Spring의 기본 ErrorWebExceptionHandler(-1) 보다 먼저 실행시키기 위해 '-2' 설정함  숫자가 작을수록 먼저 실행
@RequiredArgsConstructor
public class GlobalErrorWebExceptionHandler implements WebExceptionHandler {

    private final ObjectMapper objectMapper;

    @Override
    @NonNull
    public Mono<Void> handle(@NonNull ServerWebExchange exchange, @NonNull Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();

        CustomResponseCode customResponseCode = (ex instanceof ResponseStatusException res
                && res.getStatusCode().value() == 404)
                ? CustomResponseCode.NOT_FOUND_ERROR
                : CustomResponseCode.SYSTEM_ERROR;

        response.setStatusCode(customResponseCode.getHttpStatus()); //  해당 에러코드 세팅 (http Status 변경)
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON); // 리스폰스의, 헤더를 가져와서, 컨텐츠 타입 변경

        byte[] bytes = objectMapper.writeValueAsBytes(GlobalRes.from(customResponseCode));
        return response.writeWith(Mono.just(response.bufferFactory().wrap(bytes)));
    }
}