package br.edu.uninter.gestaodoacoes.config;

import java.io.IOException;

import org.springframework.http.MediaType;

import br.edu.uninter.gestaodoacoes.exception.ApiErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Usado pelos handlers de erro do Spring Security (401/403), que rodam fora do MVC dispatcher
 * e por isso nao tem acesso ao ObjectMapper/HttpMessageConverter gerenciado pelo Spring MVC.
 */
final class JsonErrorWriter {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private JsonErrorWriter() {
    }

    static void write(HttpServletResponse response, int status, ApiErrorResponse body) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        OBJECT_MAPPER.writeValue(response.getWriter(), body);
    }
}
