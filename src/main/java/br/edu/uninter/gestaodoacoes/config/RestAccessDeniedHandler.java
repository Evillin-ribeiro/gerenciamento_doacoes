package br.edu.uninter.gestaodoacoes.config;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import br.edu.uninter.gestaodoacoes.exception.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Substitui a resposta padrao do Spring Security por um JSON consistente quando um usuario autenticado
 * tenta acessar uma rota que exige um papel (role) que ele nao possui (ex.: VOLUNTARIO em rota so-ADMIN).
 */
@Component
public class RestAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                        AccessDeniedException accessDeniedException) throws IOException {
        var body = new ApiErrorResponse(HttpStatus.FORBIDDEN.value(), HttpStatus.FORBIDDEN.getReasonPhrase(),
                "Voce nao tem permissao para acessar este recurso.");
        JsonErrorWriter.write(response, HttpStatus.FORBIDDEN.value(), body);
    }
}
