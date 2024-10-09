package my.com.maybank.core.configuration.security;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

/**
 * Basic authentication provider
 * @since 1.0.0
 * @author ChristopherCKW
 */
public interface BasicAuthenticationProvider
       extends AuthenticationProvider
{
    @Override
    default boolean supports(final Class<?> authentication)
    {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(
            authentication
        );
    }
}
