package tn.SGII_Ville.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Configuration Spring Security avec JWT
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Désactiver CSRF car on utilise JWT
            .csrf(csrf -> csrf.disable())
            
            // Configuration CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // Configuration des autorisations
            .authorizeHttpRequests(auth -> auth
                // Ressources statiques (frontend Angular)
                .requestMatchers(
                    "/", 
                    "/index.html",
                    "/assets/**",
                    "/favicon.ico",
                    "/*.js", 
                    "/*.css",
                    "/*.png", 
                    "/*.jpg", 
                    "/*.jpeg", 
                    "/*.svg", 
                    "/*.gif",
                    "/static/**"
                ).permitAll()
                
                // Endpoints publics
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers("/error").permitAll()
                .requestMatchers("/api/demandes/uploads/**").permitAll()  // <-- AJOUTEZ CETTE LIGNE

                
                // Endpoints ADMIN
                .requestMatchers("/api/admin/**").hasAnyAuthority("ROLE_ADMINISTRATEUR", "ROLE_ADMIN")
                .requestMatchers("/api/users/**").hasAnyAuthority("ROLE_ADMINISTRATEUR", "ROLE_ADMIN")
                
                // Endpoints DEMANDES
                .requestMatchers("/api/demandes/**").hasAnyAuthority("ROLE_CITOYEN", "ROLE_ADMINISTRATEUR", "ROLE_CHEF_SERVICE", "ROLE_TECHNICIEN", "ROLE_ADMIN")
                
                // Endpoints INTERVENTIONS
                .requestMatchers("/api/interventions/**").hasAnyAuthority("ROLE_CHEF_SERVICE", "ROLE_ADMINISTRATEUR", "ROLE_TECHNICIEN", "ROLE_ADMIN")
                
                // Endpoints EQUIPEMENTS et RESSOURCES
                .requestMatchers("/api/equipements/**").hasAnyAuthority("ROLE_CHEF_SERVICE", "ROLE_ADMINISTRATEUR", "ROLE_ADMIN")
                .requestMatchers("/api/materiels/**").hasAnyAuthority("ROLE_CHEF_SERVICE", "ROLE_ADMINISTRATEUR", "ROLE_ADMIN")
                .requestMatchers("/api/demandes-ajout/**").hasAnyAuthority("ROLE_CHEF_SERVICE", "ROLE_ADMINISTRATEUR", "ROLE_ADMIN")
                
                // Endpoints TECHNICIEN (accessible par admin aussi pour consultation)
                .requestMatchers("/api/technicien/**").hasAnyAuthority("ROLE_TECHNICIEN", "ROLE_ADMINISTRATEUR", "ROLE_ADMIN", "ROLE_CHEF_SERVICE")
                
                // Endpoints MAIN_DOEUVRE
                .requestMatchers("/api/main-doeuvre/**").hasAnyAuthority("ROLE_MAIN_DOEUVRE", "ROLE_ADMINISTRATEUR", "ROLE_ADMIN", "ROLE_CHEF_SERVICE")
                
                
                // Toutes les autres requêtes nécessitent une authentification
                .anyRequest().authenticated()
            )
            
            // Politique de session stateless (sans état)
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            // Ajouter le filtre JWT avant le filtre d'authentification par défaut
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Configuration CORS
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Origines autorisées (à adapter selon votre frontend)
        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:3000",
            "http://localhost:4200",
            "http://localhost:8080"
        ));
        
        // Méthodes HTTP autorisées
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        ));
        
        // Headers autorisés
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type",
            "Accept",
            "X-Requested-With",
            "Access-Control-Allow-Origin",
            "Access-Control-Allow-Headers",
            "Access-Control-Allow-Methods"
        ));
        
        // Exposer les headers
        configuration.setExposedHeaders(List.of("Authorization"));
        
        // Autoriser les credentials
        configuration.setAllowCredentials(true);
        
        // Durée de cache de la configuration CORS
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }

    /**
     * Bean pour l'encodage des mots de passe
     */
    @Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
}