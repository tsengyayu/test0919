package com.example.test0919.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
//
import javax.sql.DataSource;

import javax.sql.DataSource;

import static org.springframework.security.config.Customizer.withDefaults;


@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    UserDetailsService userDetailsService(JdbcTemplate jdbc) {
        return username -> jdbc.query("""
             SELECT username, password, role, enabled
             FROM app_users WHERE username = ?
        """, rs -> {
            if (!rs.next()) throw new UsernameNotFoundException(username);
            var auth = new SimpleGrantedAuthority("ROLE_" + rs.getString("role"));
            return org.springframework.security.core.userdetails.User
                    .withUsername(rs.getString("username"))
                    .password(rs.getString("password"))   // 明文
                    .authorities(auth)
                    .disabled(!rs.getBoolean("enabled"))
                    .build();
        }, username);
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.ignoringRequestMatchers("/profiles/**","/createAppUser", "/api/**"))
//                .headers(h -> h.frameOptions(f -> f.disable()))
                .httpBasic(withDefaults())
                .authorizeHttpRequests(reg -> reg
                        .requestMatchers("/").permitAll()
                        // 允許未登入呼叫建立帳號 API
                        .requestMatchers(HttpMethod.POST, "/createAppUser").permitAll()
                        .requestMatchers("/createAppUser").permitAll() // 如需 GET 也放行就留這行
                        // 業務 API 規則
                        .requestMatchers(HttpMethod.POST,    "/profiles/**").hasAnyRole("ADMIN","USER")
                        .requestMatchers(HttpMethod.GET,    "/api/**").hasAnyRole("ADMIN","USER")
                        .requestMatchers(HttpMethod.POST,   "/api/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT,    "/api/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH,  "/api/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/**").hasRole("ADMIN")
                        .anyRequest().permitAll()
                );
        return http.build();
    }

    // 明文（只給開發用）
    @Bean
    PasswordEncoder passwordEncoder() {
        return org.springframework.security.crypto.password.NoOpPasswordEncoder.getInstance();
    }

}
