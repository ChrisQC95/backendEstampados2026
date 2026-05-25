package mype_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1. Esto le dice a Spring: "Usa el CorsConfig.java que Chris ya creó"
                .cors(Customizer.withDefaults())

                // 2. Apagamos CSRF para que React pueda enviar POSTs
                .csrf(csrf -> csrf.disable())

                // 3. Reglas de rutas
                .authorizeHttpRequests(auth -> auth
                        // Dejamos pasar libremente el login para que puedan obtener su sesión
                        .requestMatchers("/api/auth/login").permitAll()
                        // CUALQUIER otra ruta de tu API exigirá un token válido
                        .anyRequest().authenticated())

                // 4. Activamos la validación automática de tokens con Firebase
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));

        return http.build();
    }
}