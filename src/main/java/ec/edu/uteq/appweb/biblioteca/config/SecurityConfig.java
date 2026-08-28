package ec.edu.uteq.appweb.biblioteca.config;

import ec.edu.uteq.appweb.biblioteca.security.JwtAuthenticationFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            JwtAuthenticationFilter filtroJwt) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .sessionManagement(sesion -> sesion
                        .sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS))

                .formLogin(formulario -> formulario.disable())
                .httpBasic(basica -> basica.disable())
                .logout(salida -> salida.disable())
                .requestCache(cache -> cache.disable())

                .authorizeHttpRequests(peticiones -> peticiones
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/auth/login")
                        .permitAll()

                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs",
                                "/v3/api-docs/**",
                                "/api/docs",
                                "/actuator/health",
                                "/actuator/health/**")
                        .permitAll()

                        .requestMatchers("/api/v1/**")
                        .authenticated()

                        .anyRequest()
                        .denyAll())

                .exceptionHandling(errores -> errores
                        .authenticationEntryPoint(
                                (peticion, respuesta, excepcion) -> {
                                    respuesta.setStatus(401);
                                    respuesta.setCharacterEncoding("UTF-8");
                                    respuesta.setContentType(
                                            "application/problem+json");
                                    respuesta.setHeader(
                                            "WWW-Authenticate", "Bearer");

                                    respuesta.getWriter().write("""
                                            {
                                              "type": "about:blank",
                                              "title": "No autenticado",
                                              "status": 401,
                                              "detail": "Debe enviar un token JWT valido."
                                            }
                                            """);
                                })

                        .accessDeniedHandler(
                                (peticion, respuesta, excepcion) -> {
                                    respuesta.setStatus(403);
                                    respuesta.setCharacterEncoding("UTF-8");
                                    respuesta.setContentType(
                                            "application/problem+json");

                                    respuesta.getWriter().write("""
                                            {
                                              "type": "about:blank",
                                              "title": "Acceso denegado",
                                              "status": 403,
                                              "detail": "No tiene permisos para realizar esta operacion."
                                            }
                                            """);
                                }))

                .addFilterBefore(
                        filtroJwt,
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter>
            registroFiltroJwt(JwtAuthenticationFilter filtroJwt) {

        FilterRegistrationBean<JwtAuthenticationFilter> registro =
                new FilterRegistrationBean<>(filtroJwt);

        registro.setEnabled(false);
        return registro;
    }
}