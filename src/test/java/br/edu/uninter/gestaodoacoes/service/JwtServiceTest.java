package br.edu.uninter.gestaodoacoes.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

class JwtServiceTest {

    private final String secretDeTeste = "chave-secreta-apenas-para-teste-unitario-com-32-bytes-ou-mais";

    private UserDetails criarUsuario() {
        return User.builder()
                .username("voluntario@teste.com")
                .password("hash-nao-usado-aqui")
                .authorities("ROLE_VOLUNTARIO")
                .build();
    }

    @Test
    void deveGerarEValidarTokenCorretamente() {
        JwtService jwtService = new JwtService(secretDeTeste, 60_000);
        UserDetails usuario = criarUsuario();

        String token = jwtService.gerarToken(usuario);

        assertThat(token).isNotBlank();
        assertThat(jwtService.extrairEmail(token)).isEqualTo("voluntario@teste.com");
        assertThat(jwtService.tokenValido(token, usuario)).isTrue();
    }

    @Test
    void deveInvalidarTokenExpirado() throws InterruptedException {
        JwtService jwtService = new JwtService(secretDeTeste, 1);
        UserDetails usuario = criarUsuario();

        String token = jwtService.gerarToken(usuario);
        Thread.sleep(20);

        assertThat(jwtService.tokenValido(token, usuario)).isFalse();
    }

    @Test
    void deveInvalidarTokenParaUsuarioDiferente() {
        JwtService jwtService = new JwtService(secretDeTeste, 60_000);
        UserDetails usuarioOriginal = criarUsuario();
        UserDetails outroUsuario = User.builder()
                .username("outro@teste.com").password("x").authorities("ROLE_ADMIN").build();

        String token = jwtService.gerarToken(usuarioOriginal);

        assertThat(jwtService.tokenValido(token, outroUsuario)).isFalse();
    }

    @Test
    void deveInvalidarTokenAssinadoComOutraChave() {
        JwtService jwtServiceA = new JwtService(secretDeTeste, 60_000);
        JwtService jwtServiceB = new JwtService("outra-chave-secreta-completamente-diferente-32bytes", 60_000);
        UserDetails usuario = criarUsuario();

        String token = jwtServiceA.gerarToken(usuario);

        assertThat(jwtServiceB.tokenValido(token, usuario)).isFalse();
    }
}
