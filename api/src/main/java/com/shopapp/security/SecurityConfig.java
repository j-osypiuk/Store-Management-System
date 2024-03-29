package com.shopapp.security;

import com.shopapp.user.UserServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.http.HttpMethod.*;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final String CUSTOMER_ROLE = "CUSTOMER";
    private final String EMPLOYEE_ROLE = "EMPLOYEE";
    private final String ADMIN_ROLE = "ADMIN";
    private final UserServiceImpl userService;

    public SecurityConfig(UserServiceImpl userService) {
        this.userService = userService;
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return this.userService;
    }
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, UserAccessDecisionManager decisionManager) throws Exception {
        return http.csrf(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> {
                    // address
                    auth.requestMatchers("/api/address/{id}")
                            .hasAnyRole(CUSTOMER_ROLE, EMPLOYEE_ROLE, ADMIN_ROLE);
                    // category, discount, product, product_photo
                    auth.requestMatchers(POST,
                                    "/api/category/**",
                                    "/api/discount/**",
                                    "/api/product/**",
                                    "/api/photo/product/**")
                            .hasAnyRole(EMPLOYEE_ROLE, ADMIN_ROLE);
                    auth.requestMatchers(PUT,
                                    "/api/category/**",
                                    "/api/discount/**",
                                    "/api/product/**")
                            .hasAnyRole(EMPLOYEE_ROLE, ADMIN_ROLE);
                    auth.requestMatchers(DELETE,
                                    "/api/category/**",
                                    "/api/discount/**",
                                    "/api/product/**",
                                    "/api/photo/product/**")
                            .hasAnyRole(EMPLOYEE_ROLE, ADMIN_ROLE);
                    // order
                    auth.requestMatchers(POST, "/api/order/{id}").access(decisionManager);
                    auth.requestMatchers(GET,
                                    "/api/order",
                                    "/api/order/{id}",
                                    "/api/order/product/{id}")
                            .hasAnyRole(EMPLOYEE_ROLE, ADMIN_ROLE);
                    auth.requestMatchers(GET, "/api/order/user/{id}").access(decisionManager);
                    auth.requestMatchers(PUT, "/api/order/{id}")
                            .hasAnyRole(EMPLOYEE_ROLE, ADMIN_ROLE);
                    // user
                    auth.requestMatchers(POST, "/api/user/employee")
                            .hasRole(ADMIN_ROLE);
                    auth.requestMatchers(GET, "/api/user")
                            .hasAnyRole(EMPLOYEE_ROLE, ADMIN_ROLE);
                    auth.requestMatchers(GET, "/api/user/{id}").access(decisionManager);
                    auth.requestMatchers(DELETE, "/api/user/**")
                            .hasAnyRole(EMPLOYEE_ROLE, ADMIN_ROLE);
                    auth.requestMatchers(PUT, "/api/user/{id}").access(decisionManager);
                    auth.requestMatchers(PATCH, "/api/user/{id}").access(decisionManager);

                    auth.anyRequest().permitAll();
                    }
                )
                .httpBasic(Customizer.withDefaults())
                .build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userDetailsService());
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        return authenticationProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
