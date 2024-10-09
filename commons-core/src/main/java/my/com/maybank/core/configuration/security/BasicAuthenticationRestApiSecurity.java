package my.com.maybank.core.configuration.security;

import jakarta.annotation.Nonnull;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Basic authentication REST API security configuration
 * @since 1.0.0
 * @author ChristopherCKW
 */
@Slf4j
@RequiredArgsConstructor
@Configuration
@EnableMethodSecurity
public class BasicAuthenticationRestApiSecurity
{
    @NonNull
    @Nonnull
    private final BasicAuthenticationDelegate authenticationDelegate;
    
    @NonNull
    @Nonnull
    private final BasicAuthenticationProvider authenticationProvider;
    
    @NonNull
    @Nonnull
    private final BasicAuthenticationRestEntryPoint authenticationEntryPoint;
    
    @Bean
    public SecurityFilterChain securityFilterChain(final HttpSecurity http)
           throws Exception 
    {
        log.info(
            "Using authentication delegate `{}` type",
            authenticationDelegate.getClass().getName()
        );
        log.info(
            "Using authentication entry point `{}` type",
            authenticationEntryPoint.getClass().getName()
        );
        log.info(
            "Using authentication provider `{}` type",
            authenticationProvider.getClass().getName()
        );
        
        http.csrf(
            AbstractHttpConfigurer::disable
        ).authorizeHttpRequests(
            authzReq -> authenticationDelegate.authorizeHttpRequests(
                authzReq
            ).anyRequest().authenticated()
        ).httpBasic(
            httpSecurityHttpBasicConfigurer -> httpSecurityHttpBasicConfigurer.authenticationEntryPoint(
                authenticationEntryPoint
            )
        ).authenticationProvider(
            authenticationProvider
        );

        return http.build();
    }
}
