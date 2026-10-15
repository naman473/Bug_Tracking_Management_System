package com.startechies.bugtracker.config;

import com.startechies.bugtracker.model.Role;
import com.startechies.bugtracker.repository.UserRepository;
import com.startechies.bugtracker.service.UserDetailsServiceImpl;
import com.startechies.bugtracker.util.Routes;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.time.LocalDateTime;
import java.util.Objects;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;
    private final UserRepository         userRepository;

    public SecurityConfig(UserDetailsServiceImpl userDetailsService,
                          UserRepository userRepository) {
        this.userDetailsService = userDetailsService;
        this.userRepository     = userRepository;
    }

    @Bean public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    @Bean
    public AuthenticationManager authenticationManager() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return new ProviderManager(provider);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                        "/", Routes.HOME, Routes.REGISTER, Routes.LOGIN,
                        Routes.IMPORT_USERS, "/error").permitAll()
                .requestMatchers("/bugs/**").authenticated()
                .requestMatchers("/manager/**").hasAuthority(Role.PROJECT_MANAGER.authority)
                .requestMatchers("/developer/**").hasAuthority(Role.DEVELOPER.authority)
                .requestMatchers("/tester/**").hasAuthority(Role.TESTER.authority)
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage(Routes.LOGIN)
                .loginProcessingUrl(Routes.LOGIN)
                .usernameParameter("email")
                .passwordParameter("psw")
                .successHandler(roleBasedSuccessHandler())
                .failureUrl(Routes.LOGIN + "?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl(Routes.LOGOUT)
                .logoutSuccessUrl(Routes.HOME)
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .sessionManagement(session -> session
                .sessionFixation().migrateSession()
                .maximumSessions(3)
                .expiredUrl(Routes.LOGIN + "?expired=true")
            )
            .exceptionHandling(ex -> ex
                .accessDeniedPage("/error")
            );
        return http.build();
    }

    /**
     * Redirects to the role-specific dashboard after login.
     * Also records last-login timestamp (best-effort; non-blocking).
     */
    private AuthenticationSuccessHandler roleBasedSuccessHandler() {
        return (request, response, authentication) -> {
            Role role = authentication.getAuthorities().stream()
                    .map(a -> Role.fromAuthority(a.getAuthority()))
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElse(null);

            try {
                userRepository.updateLastLogin(authentication.getName(), LocalDateTime.now());
            } catch (Exception ignored) { /* non-critical */ }

            String target = role != null ? role.dashboardPath : Routes.HOME;
            response.sendRedirect(request.getContextPath() + target);
        };
    }
}
