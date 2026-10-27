package com.izimboa.common.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Inscrit au journal chaque appel de l'API refusé (4xx) ou en erreur (5xx) : méthode, chemin, statut.
 * Ni corps ni paramètres (pas de données personnelles). Sert à diagnostiquer les applications mobiles.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class ClientErrorLogFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(ClientErrorLogFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        try {
            chain.doFilter(request, response);
        } finally {
            int status = response.getStatus();
            if (status >= 400 && status != 404 && request.getRequestURI().startsWith("/v1/")) {
                log.warn("API {} {} -> {}", request.getMethod(), request.getRequestURI(), status);
            }
        }
    }
}
