package capstone.cycle.common.security.config;

import capstone.cycle.common.domain.error.ExceptionHandlerFilter;
import capstone.cycle.common.security.filter.JwtAuthorizationFilter;
import capstone.cycle.common.security.service.RedisTokenService;
import capstone.cycle.refreshtoken.scheduler.RefreshTokenCleanupScheduler;
import capstone.cycle.refreshtoken.service.RefreshTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Bean
    public RefreshTokenCleanupScheduler refreshTokenCleanupScheduler(RedisTokenService redisTokenService) {
        return new RefreshTokenCleanupScheduler(redisTokenService);
    }

    private static final String[] SWAGGER_URIS = {
            /* swagger v2 */
            "/v2/api-docs",
            "/swagger-resources",
            "/swagger-resources/**",
            "/configuration/ui",
            "/configuration/security",
            "/swagger-ui.html",
            "/webjars/**",
            /* swagger v3 */
            "/v3/api-docs/**",
            "/swagger-ui/**"
    };

    JwtAuthorizationFilter jwtAuthorizationFilter;

    ExceptionHandlerFilter exceptionHandlerFilter;

    @Autowired
    public SecurityConfig(JwtAuthorizationFilter jwtAuthorizationFilter, ExceptionHandlerFilter exceptionHandlerFilter) {
        this.jwtAuthorizationFilter = jwtAuthorizationFilter;
        this.exceptionHandlerFilter = exceptionHandlerFilter;
    }

    @Bean
    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorizeRequests ->
                        authorizeRequests
                                .requestMatchers("/",
                                        "/api/u/v1/token/refresh",
                                        "/api/u/v1/token",
                                        "/api/u/v1/social-login",
                                        "/api-docs",
                                        "/api-docs/**",
                                        "/swagger-ui/**",
                                        "/error",
                                        "/",
                                        "/swagger-config",
                                        "/api/n/v1/**",
//                                        "/api/p/v1/**",
                                        "/v3/api-docs/**",
                                        "/v3/api-docs/swagger-config"

//                                        "/v3/api-docs/post"
                                ).permitAll()
                                .requestMatchers(SWAGGER_URIS).permitAll()
                                .requestMatchers("/api/p/v1/**").hasAnyRole("USER", "ADMIN")
                                .requestMatchers("/api/u/v1/**").hasAnyRole("USER", "ADMIN")
                                .requestMatchers("/api/c/v1/**").hasAnyRole("USER", "ADMIN")
                )
                .csrf(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable)
                .addFilterBefore(jwtAuthorizationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(exceptionHandlerFilter, JwtAuthorizationFilter.class)
                .logout(logout -> logout.logoutSuccessUrl("/").permitAll());


        return http.build();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().requestMatchers(
                "/favicon.ico",
                "/swagger-ui/**",
                "/",
                "/swagger-config",
                "/v3/api-docs/**",
                "/error",
                /* swagger v2 */
                "/v2/api-docs",
                "/swagger-resources",
                "/swagger-resources/**",
                "/configuration/ui",
                "/configuration/security",
                "/swagger-ui.html",
                "/webjars/**",
                /* swagger v3 */
                "/v3/api-docs/**",
                "/swagger-ui/**",
                "/v3/api-docs/swagger-config"
//                "/v3/api-docs/post"

        );
    }

}
