package br.edu.uninter.gestaodoacoes.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Matriz de acesso da API (RF10/RF11):
 * - Publico: fluxo do doador (cadastro, criar doacao, upload de comprovante, ver dados bancarios/estoque/horarios,
 *   consultar disponibilidade e agendar) e login.
 * - Autenticado (ADMIN ou VOLUNTARIO): gestao operacional (confirmar/cancelar doacao, download de comprovante,
 *   listar doadores/doacoes/agendamentos, gerenciar estoque, distribuicoes, relatorios).
 * - Somente ADMIN: usuarios, CRUD de horarios de atendimento, atualizar dados bancarios.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;
    private final RestAccessDeniedHandler restAccessDeniedHandler;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                           RestAuthenticationEntryPoint restAuthenticationEntryPoint,
                           RestAccessDeniedHandler restAccessDeniedHandler) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.restAuthenticationEntryPoint = restAuthenticationEntryPoint;
        this.restAccessDeniedHandler = restAccessDeniedHandler;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(UserDetailsService userDetailsService,
                                                         PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return new org.springframework.security.authentication.ProviderManager(provider);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(eh -> eh
                        .authenticationEntryPoint(restAuthenticationEntryPoint)
                        .accessDeniedHandler(restAccessDeniedHandler))
                .authorizeHttpRequests(auth -> auth
                        // Documentacao / Swagger
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

                        // Recursos estaticos e paginas Thymeleaf publicas
                        .requestMatchers("/", "/css/**", "/js/**", "/webjars/**").permitAll()
                        .requestMatchers("/doadores/**", "/doacoes/**", "/agendamentos/**").permitAll()
                        // Shell do painel admin: a protecao real acontece nas chamadas fetch() a /api/**
                        .requestMatchers("/admin/**").permitAll()

                        // Autenticacao
                        .requestMatchers("/api/auth/**").permitAll()

                        // Fluxo publico do doador
                        .requestMatchers(HttpMethod.POST, "/api/doadores").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/doacoes").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/doacoes/{id:[0-9]+}").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/doacoes/*/comprovante").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/dados-bancarios").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/estoque/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/horarios-atendimento/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/agendamentos/disponibilidade").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/agendamentos").permitAll()

                        // Somente ADMIN
                        .requestMatchers("/api/usuarios/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/horarios-atendimento").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/horarios-atendimento/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/horarios-atendimento/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/dados-bancarios").hasRole("ADMIN")

                        // Todo o restante exige login (ADMIN ou VOLUNTARIO)
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
