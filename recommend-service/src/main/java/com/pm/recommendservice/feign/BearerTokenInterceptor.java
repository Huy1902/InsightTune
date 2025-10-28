package com.pm.recommendservice.feign;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.stereotype.Component;

@Component
public class BearerTokenInterceptor implements RequestInterceptor {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String TOKEN_TYPE = "Bearer";

    @Override
    public void apply(RequestTemplate requestTemplate) {
        String token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJib2JAZXhhbXBsZS5jb20iLCJhdXRob3JpdGllcyI6WyJST0xFX1VTRVIiXSwiaWF0IjoxNzYxNTMwMjM5LCJleHAiOjE3NjQxMjIyMzl9._IbwIuZw2jTIY4-s_jmgqRR_zGx2pOoQ0nOcVp6Tx18";

        if (token != null && !token.isEmpty()) {
            requestTemplate.header(AUTHORIZATION_HEADER, String.format("%s %s", TOKEN_TYPE, token));
        }
    }
}