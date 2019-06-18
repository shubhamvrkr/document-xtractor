package com.infosys.docxtract.routes;

import com.infosys.docxtract.handler.*;
import org.springframework.http.*;
import org.springframework.web.reactive.function.server.*;
import org.springframework.context.annotation.*;

import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;

@Configuration
public class Routes {
    @Bean
    public RouterFunction<ServerResponse> routerFunction(final RequestHandler requestHandler) {
        return RouterFunctions.route(POST("/template/create/{crop}").and(accept(MediaType.MULTIPART_FORM_DATA)), requestHandler::createTemplate)
                .andRoute(RequestPredicates.POST("/template/save").and(accept(MediaType.APPLICATION_JSON)), requestHandler::saveTemplate)
                .andRoute(RequestPredicates.POST("/template/process/{crop}").and(accept(MediaType.MULTIPART_FORM_DATA)), requestHandler::processTemplate);
    }
}