package my.com.maybank.usermgmt.security.auth;

import jakarta.annotation.Nonnull;
import java.util.Optional;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import my.com.maybank.core.configuration.security.BasicAuthenticationProvider;
import my.com.maybank.schema.entity.User;
import my.com.maybank.usermgmt.service.UserService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

/**
 * Default basic authentication provider that uses the {@link UserService} for 
 * authenticating user(s)
 * @since 1.0.0
 * @author ChristopherCKW
 */
@Slf4j
@Accessors(
    fluent = false,
    chain = true
)
@Setter
@Getter
@RequiredArgsConstructor
@Component
@EnableMethodSecurity
public class DefaultBasicAuthenticationProvider
       implements BasicAuthenticationProvider
{
    @NonNull
    @Nonnull
    private final UserService userService;

    @Nonnull
    @Override
    public Authentication authenticate(@Nonnull
                                       final Authentication authentication)
           throws AuthenticationException
    {
        try
        {
            final User userCriteria = new User().setUsername(
                authentication.getName()
            ).setPlainPassword(
                (String)authentication.getCredentials()
            );
            return getUserService().findUser(
                userCriteria
            ).map(
                // found the user by the 'username'
                userToProcess -> Optional.of(
                    getUserService().authenticateUser(
                        userCriteria
                    )
                ).filter(
                    isUserAuthenticatedToProcess -> isUserAuthenticatedToProcess
                ).map(
                    // authenticated
                    isUserAuthenticatedToProcess -> {
                        log.info(
                            String.format(
                                "Authenticate username '%s' IS SUCCESSFUL --- granted authorities %n\t%s",
                                userToProcess.getUsername(),
                                userToProcess.getAuthorities()
                            )
                        );
                        return new UsernamePasswordAuthenticationToken(
                            userToProcess, // principal object 
                            userCriteria.getPlainPassword(), // plain password
                            userToProcess.getAuthorities() // authorities
                        );
                    }
                ).orElseThrow(
                    () -> new BadCredentialsException(
                        String.format(
                            "Authenticate username '%s' IS UNSUCCESSFUL - VERIFY RESULT FALSE ; expected verify result TRUE",
                            authentication.getName()
                        )
                    )
                )
            ).orElseThrow(
                () -> new UsernameNotFoundException(
                    String.format(
                        "Basic authentication for username '%s' USER NOT FOUND",
                        userCriteria.getUsername()
                    )
                )
            );
        }
        catch(AuthenticationException e)
        {
            log.error(
                "Basic authentication for username '{}' ENCOUNTERED FAILURE ; {}",
                authentication.getName(),
                e.getMessage(),
                e
            );
            throw e;
        }
        catch(Exception e)
        {
            log.error(
                "Basic authentication for username '{}' ENCOUNTERED FAILURE ; {}",
                authentication.getName(),
                e.getMessage(),
                e
            );
            throw new BadCredentialsException(
                "Basic authentication credentials IS INVALID",
                e
            );
        }
    }
}
