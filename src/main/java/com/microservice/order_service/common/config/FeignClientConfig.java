package com.microservice.order_service.common.config;


import feign.Feign;
import feign.RequestInterceptor;
import io.github.resilience4j.feign.FeignDecorators;
import io.github.resilience4j.feign.Resilience4jFeign;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
public class FeignClientConfig {

    //For add auth token to every feign client (Http) request.
    @Bean
    public RequestInterceptor requestInterceptor() {
        return template -> {

            RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();

            if (requestAttributes instanceof ServletRequestAttributes attrs) {
                String authHeader = attrs.getRequest().getHeader("Authorization");

                if (authHeader != null) {
                    template.header("Authorization", authHeader);
                }
            }
        };
    }

    //For circuit breaker (Handle failure service)
    //Feign with Resilience4j filters (circuit breaker + retry + bulkhead support).
    @Bean
    public FeignDecorators feignDecorators() {
        return FeignDecorators.builder().build();
    }

    @Bean
    public Feign.Builder feignBuilder(FeignDecorators decorators) {
        return Resilience4jFeign.builder(decorators);
    }
}