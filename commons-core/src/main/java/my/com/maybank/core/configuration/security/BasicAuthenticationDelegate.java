package my.com.maybank.core.configuration.security;

import jakarta.annotation.Nonnull;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;

/**
 * Basic authentication delegator
 * @since 1.0.0
 * @author ChristopherCKW
 */
public interface BasicAuthenticationDelegate
{
    /**
     * Delegate method {@link 
     * @param authorizationManagerRequestMatcherRegistry    Authorization manager 
     *                                                      request matcher 
     *                                                      registry. Must not 
     *                                                      be {@code null}
     * @return                                              Authorization manager 
     *                                                      request matcher 
     *                                                      registry, never 
     *                                                      {@code null}
     * @since 1.0.0
     */
    @Nonnull
    default AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry authorizeHttpRequests(@Nonnull
                                                                                                                           final AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry authorizationManagerRequestMatcherRegistry)
    {
        return authorizationManagerRequestMatcherRegistry;
    }
}
