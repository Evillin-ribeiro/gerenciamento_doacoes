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
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

                        .requestMatchers("/", "/css/**", "/js/**", "/webjars/**").permitAll()
                        .requestMatchers("/doadores/**", "/doacoes/**", "/agendamentos/**").permitAll()
                        .requestMatchers("/admin/**").permitAll()

                        .requestMatchers("/api/auth/**").permitAll()

                        .requestMatchers(HttpMethod.POST, "/api/doadores").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/doacoes").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/doacoes/{id:[0-9]+}").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/doacoes/*/comprovante").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/dados-bancarios").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/estoque/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/horarios-atendimento/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/agendamentos/disponibilidade").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/agendamentos").permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/usuarios/resumo").authenticated()

                        .requestMatchers("/api/usuarios/**").hasRole("ADMIN")
                        .requestMatchers("/api/relatorios/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/horarios-atendimento/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/dados-bancarios").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.POST, "/api/horarios-atendimento").hasAnyRole("ADMIN", "VOLUNTARIO")
                        .requestMatchers(HttpMethod.PUT, "/api/horarios-atendimento/**").hasAnyRole("ADMIN", "VOLUNTARIO")

                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
