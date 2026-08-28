package ec.edu.uteq.appweb.biblioteca.security;

import ec.edu.uteq.appweb.biblioteca.domain.Rol;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest peticion,
            HttpServletResponse respuesta,
            FilterChain cadena) throws ServletException, IOException {

        String cabecera = peticion.getHeader("Authorization");

        if (cabecera == null
                || !cabecera.regionMatches(true, 0, "Bearer ", 0, 7)) {
            cadena.doFilter(peticion, respuesta);
            return;
        }

        String token = cabecera.substring(7).trim();

        try {
            Claims claims = jwtService.extraerClaims(token);
            String username = claims.getSubject();
            Rol rol = Rol.valueOf(claims.get("rol", String.class));

            SimpleGrantedAuthority autoridad =
                    new SimpleGrantedAuthority("ROLE_" + rol.name());

            UsernamePasswordAuthenticationToken autenticacion =
                    new UsernamePasswordAuthenticationToken(
                            username,
                            null,
                            List.of(autoridad));

            autenticacion.setDetails(
                    new WebAuthenticationDetailsSource()
                            .buildDetails(peticion));

            SecurityContextHolder.getContext()
                    .setAuthentication(autenticacion);

        } catch (JwtException | IllegalArgumentException ex) {
            SecurityContextHolder.clearContext();
        }

        cadena.doFilter(peticion, respuesta);
    }
}