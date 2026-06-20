package com.saurav.SpringAzureOpenAI;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;
@Component
public class AppFilter extends OncePerRequestFilter {

    private static final String FILTER_EXECUTED = "FILTER_EXECUTED";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        HttpSession session = request.getSession();
        if (session.getAttribute(FILTER_EXECUTED) == null) {
            // Run only once per session
            System.out.println("First request in this session");
            session.setAttribute("sessionId", UUID.randomUUID().toString());
            session.setAttribute(FILTER_EXECUTED, true);
        }

        filterChain.doFilter(request,response);
    }
}
