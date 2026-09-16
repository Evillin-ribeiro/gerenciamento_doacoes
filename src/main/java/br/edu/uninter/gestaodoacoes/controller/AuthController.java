package br.edu.uninter.gestaodoacoes.controller;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.edu.uninter.gestaodoacoes.dto.LoginRequestDTO;
import br.edu.uninter.gestaodoacoes.dto.LoginResponseDTO;
import br.edu.uninter.gestaodoacoes.model.RoleUsuario;
import br.edu.uninter.gestaodoacoes.model.Usuario;
import br.edu.uninter.gestaodoacoes.repository.UsuarioRepository;
import br.edu.uninter.gestaodoacoes.service.JwtService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;

    public AuthController(AuthenticationManager authenticationManager,
                           UserDetailsService userDetailsService,
                           UsuarioRepository usuarioRepository,
                           JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.usuarioRepository = usuarioRepository;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public LoginResponseDTO login(@Valid @RequestBody LoginRequestDTO request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.senha()));

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.email());
        String token = jwtService.gerarToken(userDetails);

        Usuario usuario = usuarioRepository.findByEmail(request.email()).orElseThrow();
        RoleUsuario role = usuario.getRole();

        return new LoginResponseDTO(token, usuario.getId(), usuario.getNome(), role);
    }
}
