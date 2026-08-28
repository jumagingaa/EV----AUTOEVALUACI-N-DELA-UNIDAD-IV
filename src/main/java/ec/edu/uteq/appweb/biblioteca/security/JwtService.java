package ec.edu.uteq.appweb.biblioteca.security;

import ec.edu.uteq.appweb.biblioteca.domain.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final SecretKey clave;
    private final Duration duracion;

    public JwtService(
            @Value("${app.jwt.secreto}") String secreto,
            @Value("${app.jwt.expiracion-minutos}") long minutos) {

        if (secreto == null || secreto.isBlank()) {
            throw new IllegalArgumentException(
                    "Debe configurar la variable JWT_SECRETO");
        }

        if (minutos <= 0) {
            throw new IllegalArgumentException(
                    "La expiracion del JWT debe ser positiva");
        }

        this.clave = Keys.hmacShaKeyFor(
                Decoders.BASE64.decode(secreto));
        this.duracion = Duration.ofMinutes(minutos);
    }

    public String generar(Usuario usuario) {
        Instant ahora = Instant.now();

        return Jwts.builder()
                .subject(usuario.getUsername())
                .claim("rol", usuario.getRol().name())
                .id(UUID.randomUUID().toString())
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(ahora.plus(duracion)))
                .signWith(clave, Jwts.SIG.HS256)
                .compact();
    }

    public Claims extraerClaims(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(clave)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        if (claims.getExpiration() == null
                || claims.getSubject() == null
                || claims.getSubject().isBlank()
                || claims.get("rol", String.class) == null) {
            throw new JwtException(
                    "El token no contiene los datos requeridos");
        }

        return claims;
    }

    public String extraerUsername(String token) {
        return extraerClaims(token).getSubject();
    }

    public String extraerRol(String token) {
        return extraerClaims(token).get("rol", String.class);
    }

    public String extraerJti(String token) {
        return extraerClaims(token).getId();
    }

    public boolean esValido(String token) {
        try {
            extraerClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    public long expiracionEnSegundos() {
        return duracion.toSeconds();
    }
}