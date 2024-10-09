package my.com.maybank.core.configuration.security.impl;

import jakarta.annotation.Nonnull;
import java.util.Arrays;
import java.util.Optional;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import my.com.maybank.core.configuration.security.AppSecurity;
import my.com.maybank.core.configuration.security.BasicAuthenticationDelegate;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.stereotype.Component;

/**
 * Default basic authentication delegate
 * @since 1.0.0
 * @author ChristopherCKW
 */
@Slf4j
@Accessors(
    fluent = false,
    chain = true
)
@Getter
@Setter
@RequiredArgsConstructor
@Component
public class DefaultBasicAuthenticationDelegate
       implements BasicAuthenticationDelegate
{
    @NonNull
    @Nonnull
    private final AppSecurity appSecurity;
    
    @Nonnull
    @Override
    public AuthorizeHttpRequestsConfigurer.AuthorizationManagerRequestMatcherRegistry authorizeHttpRequests(@Nonnull
                                                                                                            final AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry authorizationManagerRequestMatcherRegistry)
    {
        log.info(
            "Using application security configuration `{}` type",
            getAppSecurity().getClass().getName()
        );
        return Optional.ofNullable(
            getAppSecurity().getHttpSecurity()
        ).map(
            httpSecurityToProcess -> Optional.ofNullable(
                httpSecurityToProcess.getRequestMatcher()
            ).map(
                requestMatcherToProcess -> Optional.ofNullable(
                    requestMatcherToProcess.getPermitAll()
                ).filter(
                    requestMatcherPermitAllToProcess -> requestMatcherPermitAllToProcess.length>0
                ).map(
                    requestMatcherPermitAllToProcess -> {
                        log.info(
                            "Delegated additional request matcher(s) permit all --- \n\t{}",
                            Arrays.toString(
                                requestMatcherPermitAllToProcess
                            )
                        );
                        return authorizationManagerRequestMatcherRegistry.requestMatchers(
                            requestMatcherPermitAllToProcess
                        ).permitAll();
                    }
                ).orElse(
                    authorizationManagerRequestMatcherRegistry
                )
            ).orElse(
                authorizationManagerRequestMatcherRegistry
            )
        ).orElse(
            authorizationManagerRequestMatcherRegistry
        );
    }
}
