package mype_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/public/tienda/productos").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/public/tienda/envios").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/public/roles/sistema").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/public/roles/tienda").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/tienda/checkout").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/tienda/pedidos/*/pago").authenticated()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));

        return http.build();
    }
}

