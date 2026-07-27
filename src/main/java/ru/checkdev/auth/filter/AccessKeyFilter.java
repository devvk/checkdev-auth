package ru.checkdev.auth.filter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class AccessKeyFilter extends OncePerRequestFilter {

    private static final String ACCESS_KEY_HEADER = "X-Access-Key";

    @Value("${access.key}")
    private String accessKey;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        String requestAccessKey = request.getHeader(ACCESS_KEY_HEADER);

        if (!accessKey.equals(requestAccessKey)) {
            response.sendError(HttpStatus.FORBIDDEN.value());
            return;
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getServletPath().startsWith("/profiles/tg/");
    }
}
