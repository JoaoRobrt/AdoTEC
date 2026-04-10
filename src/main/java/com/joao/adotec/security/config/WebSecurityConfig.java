package com.joao.adotec.security.config;

import com.joao.adotec.enums.AppRole;
import com.joao.adotec.models.Role;
import com.joao.adotec.models.User;
import com.joao.adotec.repositories.RoleRepository;
import com.joao.adotec.repositories.UserRepository;
import com.joao.adotec.security.jwt.AuthEntryPointJwt;
import com.joao.adotec.security.jwt.AuthTokenFilter;
import com.joao.adotec.security.jwt.CustomAccessDeniedHandler;
import com.joao.adotec.security.services.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.Customizer;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Set;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
@RequiredArgsConstructor
public class WebSecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;
    private final AuthTokenFilter authTokenFilter;
    private final AuthEntryPointJwt unauthorizedHandler;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**").permitAll() // Swagger moved to WebSecurityCustomizer
                        .requestMatchers(HttpMethod.GET, "/pets", "/pets/**").permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(unauthorizedHandler)
                        .accessDeniedHandler(customAccessDeniedHandler)
                );

        http.authenticationProvider(authenticationProvider());
        http.addFilterBefore(authTokenFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000", "http://localhost:5173"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Accept"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html");
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CommandLineRunner initData(RoleRepository roleRepository, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            // 1. Create Roles if they don't exist
            if (roleRepository.count() == 0) {
                roleRepository.save(new Role(AppRole.ROLE_ADMIN));
                roleRepository.save(new Role(AppRole.ROLE_EMPLOYEE));
                roleRepository.save(new Role(AppRole.ROLE_ADOPTER));
            }

            // 2. Fetch Roles
            Role adminRole = roleRepository.findByRoleName(AppRole.ROLE_ADMIN).orElseThrow(() -> new RuntimeException("Error: ADMIN Role is not found."));
            Role employeeRole = roleRepository.findByRoleName(AppRole.ROLE_EMPLOYEE).orElseThrow(() -> new RuntimeException("Error: EMPLOYEE Role is not found."));
            Role adopterRole = roleRepository.findByRoleName(AppRole.ROLE_ADOPTER).orElseThrow(() -> new RuntimeException("Error: ADOPTER Role is not found."));

            // 3. Create Admin User
            if (!userRepository.existsByEmail("admin@adotec.com")) {
                User admin = new User("Admin User", "admin@adotec.com", passwordEncoder.encode("admin123"));
                admin.setRoles(Set.of(adminRole, employeeRole, adopterRole));
                userRepository.save(admin);
            }

            // 4. Create Employee User
            if (!userRepository.existsByEmail("employee@adotec.com")) {
                User employee = new User("Employee User", "employee@adotec.com", passwordEncoder.encode("employee123"));
                employee.setRoles(Set.of(employeeRole, adopterRole));
                userRepository.save(employee);
            }

            // 5. Create Adopter User
            if (!userRepository.existsByEmail("adopter@adotec.com")) {
                User adopter = new User("Adopter User", "adopter@adotec.com", passwordEncoder.encode("adopter123"));
                adopter.setRoles(Set.of(adopterRole));
                userRepository.save(adopter);
            }
        };
    }
}
