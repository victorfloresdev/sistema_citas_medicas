package com.medigestion.actuator;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

/**
 * Filtro HTTP para observabilidad y trazabilidad distribuida (Hito 80%).
 * Inyecta un 'traceId' único en el MDC de Logback por cada petición entrante
 * y lo añade en las cabeceras HTTP de respuesta para auditoría hospitalaria.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TraceIdFilter implements Filter {

    public static final String TRACE_HEADER = "X-Trace-Id";
    public static final String MDC_KEY = "traceId";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        try {
            String traceId = UUID.randomUUID().toString().substring(0, 8);
            MDC.put(MDC_KEY, traceId);

            if (response instanceof HttpServletResponse httpServletResponse) {
                httpServletResponse.setHeader(TRACE_HEADER, traceId);
            }

            chain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_KEY);
        }
    }
}
