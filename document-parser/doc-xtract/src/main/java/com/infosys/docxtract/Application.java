package com.infosys.docxtract;

import org.springframework.boot.autoconfigure.*;
import org.springframework.boot.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.cors.reactive.CorsUtils;
import org.springframework.web.server.WebFilter;
import reactor.core.publisher.Mono;

@SpringBootApplication
public class Application
{
    public static void main(final String[] args) {
        SpringApplication.run((Class)Application.class, args);
    }

    @Configuration
    public class CorsConfiguration
    {
        private static final String ALLOWED_HEADERS = "Content-Type, Authorization, Accept";
        private static final String ALLOWED_METHODS = "GET, PUT, POST";
        private static final String ALLOWED_ORIGIN = "*";
        private static final String MAX_AGE = "3600";

        @Bean
        public WebFilter corsFilter() {
            return (ctx, chain) -> {
                final ServerHttpRequest request = ctx.getRequest();
                if (CorsUtils.isCorsRequest(request)) {
                    final ServerHttpResponse response = ctx.getResponse();
                    final HttpHeaders headers = response.getHeaders();
                    headers.add("Access-Control-Allow-Origin", "*");
                    headers.add("Access-Control-Allow-Methods", "GET, PUT, POST");
                    headers.add("Access-Control-Max-Age", "3600");
                    headers.add("Access-Control-Allow-Headers", "Content-Type, Authorization, Accept");
                    if (request.getMethod() == HttpMethod.OPTIONS) {
                        response.setStatusCode(HttpStatus.OK);
                        return Mono.empty();
                    }
                }
                return chain.filter(ctx);
            };
        }
    }
}