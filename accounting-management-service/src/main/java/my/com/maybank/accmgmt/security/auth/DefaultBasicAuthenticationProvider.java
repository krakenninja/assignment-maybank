package my.com.maybank.accmgmt.security.auth;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.annotation.Nonnull;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import my.com.maybank.accmgmt.configuration.DefaultAppSecurity;
import my.com.maybank.accmgmt.constants.AppCodes;
import my.com.maybank.core.configuration.security.BasicAuthenticationProvider;
import my.com.maybank.core.exception.AuthenticationException;
import my.com.maybank.core.exception.InternalException;
import my.com.maybank.core.exception.InvalidCredentialsException;
import my.com.maybank.core.exception.UnprocessableEntityException;
import my.com.maybank.core.utils.HttpUtils;
import my.com.maybank.schema.entity.User;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

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
    private final DefaultAppSecurity appSecurity;
    
    /**
     * Get the user service web client
     * @return                                  {@link WebClient}, never 
     *                                          {@code null}
     * @since 1.0.0
     */
    @Nonnull
    protected WebClient getUserServiceWebClient()
    {
        return HttpUtils.getWebClientBuilderWithTimeout(
            getAppSecurity().getAuthnUser().getAuthApi().getEndpoint(),
            getAppSecurity().getAuthnUser().getAuthApi().getTimeunitTimeout(),
            getAppSecurity().getAuthnUser().getAuthApi().getConnectTimeout(),
            getAppSecurity().getAuthnUser().getAuthApi().getReadTimeout()
        ).build();
    }
    
    /**
     * Authenticate the {@code user} and retrieve the user details (if successful)
     * @param user                              User to authenticate. Must not 
     *                                          be {@code null}
     * @return                                  {@link User}, never {@code null}
     * @since 1.0.0
     */
    @Nonnull
    protected User authenticate(@Nonnull
                                final User user)
    {
        final AtomicReference<InternalException> authApiError = new AtomicReference<>();
        final String jsonContent = getUserServiceWebClient().method(
            getAppSecurity().getAuthnUser().getAuthApi().getMethod()
        ).uri(
            getAppSecurity().getAuthnUser().getAuthApi().getPath()
        ).body(
            Mono.just(
                user
            ),
            User.class
        ).retrieve().onStatus(
            HttpStatusCode::isError, clientResponse -> {
                return clientResponse.bodyToMono(
                    String.class
                ).flatMap(
                    errorBody -> {
                        return Mono.error(
                            new AuthenticationException(
                                String.format(
                                    "Authentication FAILURE ENCOUNTERED HTTP ERROR %s[%d] ; %s",
                                    clientResponse.statusCode().toString(),
                                    clientResponse.statusCode().value(),
                                    errorBody
                                )
                            ).setCode(
                                AppCodes.ERROR_CODE_AUTH_FAILURE
                            )
                        );
                    }
                );
            }
        ).bodyToMono(
            String.class
        ).doOnError(
            error -> {
                authApiError.set(
                    (InternalException)error
                );
            }
        ).block();
        
        if(Objects.nonNull(
            authApiError.get()
        ))
        {
            throw authApiError.get();
        }
        
        final User authenticatedUser;
        try
        {
            authenticatedUser = new JsonMapper().registerModule(
                new JavaTimeModule()
            ).disable(
                DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES
            ).readValue(
                jsonContent, 
                new TypeReference<User>() {}
            );
            log.info(
                "JSON deserialization to object `{}` type SUCCESSFUL ; using JSON ---\n\t{}\n",
                User.class.getName(),
                jsonContent
            );
        }
        catch(Exception e)
        {
            throw new UnprocessableEntityException(
                String.format(
                    "JSON deserialization to object `%s` type ENCOUNTERED FAILURE ; %s, content --- %n\t",
                    User.class.getName(),
                    e.getMessage(),
                    jsonContent
                ),
                e
            );
        }
        return authenticatedUser;
    }
    
    @Nonnull
    @Override
    public Authentication authenticate(@Nonnull
                                       final Authentication authentication)
           throws org.springframework.security.core.AuthenticationException
    {
        try
        {
            final User userCriteria = new User().setUsername(
                authentication.getName()
            ).setPlainPassword(
                (String)authentication.getCredentials()
            );
            
            return Optional.ofNullable(
                authenticate(
                    userCriteria
                )
            ).map(
                // authenticated
                userToProcess -> {
                    log.info(
                        String.format(
                            "Authenticate username '%s' IS SUCCESSFUL --- granted authorities %n\t%s",
                            userToProcess.getUsername(),
                            userToProcess.getRoles() // authorities
                        )
                    );
                    return new UsernamePasswordAuthenticationToken(
                        userToProcess, // principal object 
                        userCriteria.getPlainPassword(), // plain password
                        userToProcess.getRoles() // authorities
                    );
                }
            ).orElseThrow(
                () -> new InvalidCredentialsException(
                    String.format(
                        "Authenticate username '%s' IS UNSUCCESSFUL - VERIFY RESULT FALSE ; expected verify result TRUE",
                        authentication.getName()
                    )
                ).setCode(
                    AppCodes.ERROR_CODE_AUTH_FAILURE
                )
            );
        }
        catch(InternalException e)
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
            throw new InvalidCredentialsException(
                "Basic authentication credentials IS INVALID",
                e
            ).setCode(
                AppCodes.ERROR_CODE_AUTH_FAILURE
            );
        }
    }
}
