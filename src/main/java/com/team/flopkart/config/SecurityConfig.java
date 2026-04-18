package com.team.flopkart.config;

import com.team.flopkart.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import java.util.List;

/**
 * Spring Security configuration.
 * NOT counted as any member's domain class.
 * Handles login, logout, role-based URL protection, and BCrypt.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {
 
    private final UserRepository userRepository;
 
    public SecurityConfig(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
 
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // Public pages
                .requestMatchers(
                    "/", 
                    "/products", "/products/**",
                    "/search", "/search/**",
                    "/auth/**",  // All auth pages public
                    "/h2-console/**",
                    "/css/**", "/js/**", "/images/**", "/error"
                ).permitAll()
                
                // Seller-only pages
                .requestMatchers("/seller/**").hasRole("SELLER")
                
                // Admin-only pages
                .requestMatchers("/admin/**").hasRole("ADMIN")
                
                // Customer pages (cart, orders, reviews)
                .requestMatchers("/cart/**", "/orders/**", "/checkout/**", "/reviews/**")
                    .hasAnyRole("CUSTOMER", "SELLER", "ADMIN")
                
                // Everything else needs login
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/auth/login")  // Default login page (shows choice)
                .loginProcessingUrl("/auth/login-process")  // Process login here
                .successHandler((request, response, authentication) -> {
                    // Redirect based on role after successful login
                    String role = authentication.getAuthorities().iterator().next().getAuthority();
                    
                    if ("ROLE_SELLER".equals(role)) {
                        response.sendRedirect("/seller/dashboard");
                    } else if ("ROLE_ADMIN".equals(role)) {
                        response.sendRedirect("/admin/dashboard");
                    } else {
                        // CUSTOMER or default
                        response.sendRedirect("/");
                    }
                })
                .failureHandler((request, response, exception) -> {
                    // Redirect back to appropriate login page with error
                    String referer = request.getHeader("Referer");
                    String redirectUrl = "/auth/login?error=true";
                    
                    if (referer != null) {
                        if (referer.contains("/seller/login")) {
                            redirectUrl = "/auth/seller/login?error=true";
                        } else if (referer.contains("/customer/login")) {
                            redirectUrl = "/auth/customer/login?error=true";
                        }
                    }
                    
                    response.sendRedirect(redirectUrl);
                })
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/auth/logout")
                .logoutSuccessUrl("/auth/login?logout=true")
                .permitAll()
            )
            // Allow H2 console in dev (frame options)
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/h2-console/**")
            )
            .headers(headers -> headers
                .frameOptions(frame -> frame.sameOrigin())
            );
 
        return http.build();
    }
 
    /**
     * Loads user from DB by email for Spring Security authentication.
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return email -> userRepository.findByEmail(email)
            .map(user -> new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                user.getEnabled(),
                true, true, true,
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
            ))
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
    }
 
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}