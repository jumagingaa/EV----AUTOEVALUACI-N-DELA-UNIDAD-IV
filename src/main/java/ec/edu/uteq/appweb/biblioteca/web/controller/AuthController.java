package ec.edu.uteq.appweb.biblioteca.web.controller;

import ec.edu.uteq.appweb.biblioteca.domain.Usuario;
import ec.edu.uteq.appweb.biblioteca.repository.UsuarioRepository;
import ec.edu.uteq.appweb.biblioteca.security.JwtService;
import ec.edu.uteq.appweb.biblioteca.web.dto.ApiResponse;
import ec.edu.uteq.appweb.biblioteca.web.dto.LoginRequest;
import ec.edu.uteq.appweb.biblioteca.web.dto.LoginResponse;
import jakarta.validation.Valid;
import java.util.Optional;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final UsuarioRepository usuarios;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthController(
            UsuarioRepository usuarios,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {

        this.usuarios = usuarios;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Valid @RequestBody LoginRequest solicitud) {

        Optional<Usuario> encontrado =
                usuarios.findByUsernameAndActivoTrue(
                        solicitud.username());

        if (encontrado.isEmpty()) {
            return credencialesInvalidas();
        }

        Usuario usuario = encontrado.get();

        if (!passwordEncoder.matches(
                solicitud.password(),
                usuario.getPasswordHash())) {

            return credencialesInvalidas();
        }

        String token = jwtService.generar(usuario);

        LoginResponse datos = new LoginResponse(
                usuario.getUsername(),
                usuario.getRol().name(),
                "Bearer",
                jwtService.expiracionEnSegundos());

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer " + token)
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .body(ApiResponse.ok(
                        datos, "Inicio de sesion correcto"));
    }

    private ResponseEntity<ProblemDetail> credencialesInvalidas() {

        ProblemDetail problema = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNAUTHORIZED,
                "Usuario o contrasena incorrectos");

        problema.setTitle("Credenciales invalidas");

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .header(HttpHeaders.WWW_AUTHENTICATE, "Bearer")
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problema);
    }
}