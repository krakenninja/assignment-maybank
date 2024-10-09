package my.com.maybank.core.configuration.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Nonnull;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import my.com.maybank.core.exception.InternalException;
import my.com.maybank.core.exception.handler.GlobalExceptionHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

/**
 * Rest authentication entry point
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
public class BasicAuthenticationRestEntryPoint 
       implements AuthenticationEntryPoint, 
                  AccessDeniedHandler
{
    @NonNull
    @Nonnull
    private final GlobalExceptionHandler exceptionHandler;
    
    @NonNull
    @Nonnull
    private final ObjectMapper objectMapper;
    
    @Override
    public void commence(final HttpServletRequest request, 
                         final HttpServletResponse response, 
                         final AuthenticationException authException)
           throws IOException, 
                  ServletException
    {
        final AtomicBoolean defaultErrorResponse = new AtomicBoolean(
            false
        );
        findInternalExceptionCause(
            authException
        ).ifPresentOrElse(
            internalExceptionToProcess -> {
                try
                {
                    writeResponseEntityToHttpServletResponse(
                        exceptionHandler.handleInternalExceptions(
                            internalExceptionToProcess
                        ),
                        response
                    );
                }
                catch(Exception e)
                {
                    log.error(
                        "Write response of internal exception `{}` type ENCOUNTERED FAILURE ; {}",
                        internalExceptionToProcess.getClass().getName(),
                        e.getMessage(),
                        e
                    );
                    defaultErrorResponse.set(
                        true
                    );
                }
            },
            () -> {
                log.debug(
                    "Internal exception cause from `{}` type NOT FOUND",
                    authException.getClass().getName(),
                    authException
                );
                try
                {
                    writeResponseEntityToHttpServletResponse(
                        exceptionHandler.handleExceptions(
                            authException
                        ),
                        response
                    );
                }
                catch(Exception e)
                {
                    log.error(
                        "Write response of exception `{}` type ENCOUNTERED FAILURE ; {}",
                        authException.getClass().getName(),
                        e.getMessage(),
                        e
                    );
                    defaultErrorResponse.set(
                        true
                    );
                }
            }
        );
        
        if(defaultErrorResponse.get())
        {
            response.sendError(
                HttpServletResponse.SC_UNAUTHORIZED, 
                "Unauthorized resource access"
            );
        }
    }

    @Override
    public void handle(final HttpServletRequest request, 
                       final HttpServletResponse response, 
                       final AccessDeniedException accessDeniedException)
           throws IOException, 
                  ServletException 
    {
        final AtomicBoolean defaultErrorResponse = new AtomicBoolean(
            false
        );
        findInternalExceptionCause(
            accessDeniedException
        ).ifPresentOrElse(
            internalExceptionToProcess -> {
                try
                {
                    writeResponseEntityToHttpServletResponse(
                        exceptionHandler.handleInternalExceptions(
                            internalExceptionToProcess
                        ),
                        response
                    );
                }
                catch(Exception e)
                {
                    log.error(
                        "Write response of internal exception `{}` type ENCOUNTERED FAILURE ; {}",
                        internalExceptionToProcess.getClass().getName(),
                        e.getMessage(),
                        e
                    );
                    defaultErrorResponse.set(
                        true
                    );
                }
            },
            () -> {
                log.debug(
                    "Internal exception cause from `{}` type NOT FOUND",
                    accessDeniedException.getClass().getName(),
                    accessDeniedException
                );
                try
                {
                    writeResponseEntityToHttpServletResponse(
                        exceptionHandler.handleExceptions(
                            accessDeniedException
                        ),
                        response
                    );
                }
                catch(Exception e)
                {
                    log.error(
                        "Write response of exception `{}` type ENCOUNTERED FAILURE ; {}",
                        accessDeniedException.getClass().getName(),
                        e.getMessage(),
                        e
                    );
                    defaultErrorResponse.set(
                        true
                    );
                }
            }
        );
        if(defaultErrorResponse.get())
        {
            response.sendError(
                HttpServletResponse.SC_FORBIDDEN, 
                "Forbidden resource access"
            );
        }
    }
    
    private Optional<InternalException> findInternalExceptionCause(final Throwable t)
    {
        return Optional.ofNullable(
            Optional.ofNullable(
                t
            ).filter(
                tToProcess -> t instanceof InternalException
            ).map(
                tToProcess -> (InternalException)tToProcess
            ).orElseGet(
                () -> Optional.ofNullable(
                    t
                ).filter(
                    tToProcess -> Objects.nonNull(
                        tToProcess.getCause()
                    )
                ).map(
                    tToProcess -> Optional.ofNullable(
                        tToProcess.getCause()
                    ).filter(
                        causeToProcess -> causeToProcess instanceof InternalException
                    ).map(
                        causeToProcess -> (InternalException)causeToProcess
                    ).orElseGet(
                        () -> findInternalExceptionCause(
                            tToProcess.getCause()
                        ).orElse(
                            null
                        )
                    )
                ).orElse(
                    null
                )
            )
        );
    }
    
    private void writeResponseEntityToHttpServletResponse(final ResponseEntity<?> responseEntity, 
                                                          final HttpServletResponse response)
            throws IOException
    {
        // set status code
        response.setStatus(
            responseEntity.getStatusCode().value()
        );

        // set headers
        responseEntity.getHeaders().forEach(
            (key, values) -> values.forEach(
                value -> response.addHeader(
                    key, 
                    value
                )
            )
        );

        // write body if there is one
        Optional.ofNullable(
            responseEntity.getBody()
        ).ifPresent(
            bodyToProcess -> Optional.of(
                bodyToProcess instanceof CharSequence
            ).filter(
                isCharSequenceToProcess -> isCharSequenceToProcess
            ).ifPresentOrElse(
                isCharSequenceToProcess -> {
                    try
                    {
                        response.getWriter().write(
                            ((CharSequence)bodyToProcess).toString()
                        );
                    }
                    catch(Exception e)
                    {
                        log.error(
                            "Unable to write response entity body `{}` type as PLAIN ENCOUNTERED FAILURE ; {}",
                            bodyToProcess.getClass().getName(),
                            e
                        );
                    }
                },
                () -> {
                    try
                    {
                        response.getWriter().write(
                            objectMapper.writeValueAsString(
                                bodyToProcess
                            )
                        );
                    }
                    catch(Exception e)
                    {
                        log.error(
                            "Unable to write response entity body `{}` type as JSON ENCOUNTERED FAILURE ; {}",
                            bodyToProcess.getClass().getName(),
                            e
                        );
                    }
                }
            )
        );
    }
}