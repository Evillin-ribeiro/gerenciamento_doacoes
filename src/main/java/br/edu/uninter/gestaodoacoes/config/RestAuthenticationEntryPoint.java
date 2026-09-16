package br.edu.uninter.gestaodoacoes.config;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import br.edu.uninter.gestaodoacoes.exception.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Substitui a pagina de erro padrao do Spring Security por um JSON consistente com o resto da API
 * quando uma rota autenticada e acessada sem token (ou com token invalido/expirado).
 */
@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                          AuthenticationException authException) throws IOException {
        var body = new ApiErrorResponse(HttpStatus.UNAUTHORIZED.value(), HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                "Autenticacao necessaria. Envie um token valido no header Authorization.");
        JsonErrorWriter.write(response, HttpStatus.UNAUTHORIZED.value(), body);
    }
}
