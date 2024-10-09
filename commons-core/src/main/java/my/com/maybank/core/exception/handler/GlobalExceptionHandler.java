package my.com.maybank.core.exception.handler;

import jakarta.annotation.Nonnull;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import my.com.maybank.core.annotations.AppCode;
import my.com.maybank.core.constants.DefaultCodes;
import my.com.maybank.core.exception.InternalException;
import my.com.maybank.core.models.MessageModel;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Global exception handler
 * @since 1.0.0
 * @author ChristopherCKW
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler
{
    /**
     * Handle internal exceptions
     * @param e
     * @return 
     * @since 1.0.0
     */
    @Nonnull
    @ExceptionHandler({
        InternalException.class
    })
    public ResponseEntity<MessageModel.MessageModelResponse> handleInternalExceptions(@Nonnull
                                                                                      final InternalException e)
    {
        log.error(
            "Application ENCOUNTERED FAILURE (TYPE=`{}`) ; {}",
            e.getClass().getName(),
            e.getMessage(),
            e
        );
        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(
            HttpHeaders.CONTENT_TYPE,
            MediaType.APPLICATION_JSON_VALUE
        );
        return ResponseEntity.status(
            getHttpStatus(
                e,
                HttpStatus.INTERNAL_SERVER_ERROR
            )
        ).headers(
            httpHeaders
        ).body(
            MessageModel.errorMessage(
                String.format(
                    e.getMessage()
                ),
                getAppCode(
                    e,
                    DefaultCodes.ERROR_CODE_INTERNAL_FAILURE
                )
            )
        );
    }
    
    /**
     * Handle runtime exceptions
     * @param e
     * @return 
     * @since 1.0.0
     */
    @Nonnull
    @ExceptionHandler({
        RuntimeException.class
    })
    public ResponseEntity<MessageModel.MessageModelResponse> handleRuntimeExceptions(@Nonnull
                                                                                     final RuntimeException e)
    {
        return handleExceptions(
            e
        );
    }
    
    /**
     * Handle exceptions
     * @param e
     * @return 
     * @since 1.0.0
     */
    @Nonnull
    @ExceptionHandler({
        Exception.class
    })
    public ResponseEntity<MessageModel.MessageModelResponse> handleExceptions(@Nonnull
                                                                              final Exception e)
    {
        log.error(
            "Application ENCOUNTERED UNCAUGHT FAILURE (TYPE=`{}`) ; {}",
            e.getClass().getName(),
            e.getMessage(),
            e
        );
        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(
            HttpHeaders.CONTENT_TYPE,
            MediaType.APPLICATION_JSON_VALUE
        );
        return ResponseEntity.status(
            getHttpStatus(
                e, 
                HttpStatus.INTERNAL_SERVER_ERROR
            )
        ).headers(
            httpHeaders
        ).body(
            MessageModel.errorMessage(
                String.format(
                    e.getMessage()
                ),
                getAppCode(
                    e, 
                    DefaultCodes.ERROR_CODE_INTERNAL_FAILURE
                )
            )
        );
    }
    
    /**
     * HTTP status to response
     * @param e
     * @param defaultHttpStatus
     * @return 
     * @since 1.0.0
     */
    @Nonnull
    private HttpStatus getHttpStatus(@Nonnull
                                     final Exception e,
                                     @Nonnull
                                     final HttpStatus defaultHttpStatus)
    {
        return Optional.of(
            e instanceof AuthenticationException
        ).filter(
            eIsAuthenticationExceptionToProcess -> eIsAuthenticationExceptionToProcess
        ).map(
            eIsAuthenticationExceptionToProcess -> {
                if(e instanceof InsufficientAuthenticationException || e instanceof BadCredentialsException)
                {
                    return HttpStatus.UNAUTHORIZED;
                }
                else if(e instanceof AccountStatusException)
                {
                    return HttpStatus.FORBIDDEN;
                }
                else if(e instanceof AuthenticationServiceException)
                {
                    return HttpStatus.INTERNAL_SERVER_ERROR;
                }
                return HttpStatus.UNAUTHORIZED;
            }
        ).orElseGet(
            () -> Optional.ofNullable(
                e.getClass().getDeclaredAnnotation(
                    ResponseStatus.class
                )
            ).map(
                responseStatusToProcess -> Optional.ofNullable(
                    responseStatusToProcess.code()
                ).filter(
                    responseStatusCodeToProcess -> !responseStatusCodeToProcess.equals(
                        HttpStatus.INTERNAL_SERVER_ERROR
                    )
                ).map(
                    responseStatusCodeToProcess -> responseStatusToProcess.code()
                ).orElseGet(
                    () -> Optional.ofNullable(
                        responseStatusToProcess.value()
                    ).filter(
                        responseStatusValueToProcess -> !responseStatusValueToProcess.equals(
                            HttpStatus.INTERNAL_SERVER_ERROR
                        )
                    ).map(
                        responseStatusValueToProcess -> responseStatusToProcess.value()
                    ).orElse(
                        defaultHttpStatus
                    )
                )
            ).orElse(
                defaultHttpStatus
            )
        );
    }
    
    /**
     * Application code to response
     * @param e
     * @param defaultAppCode
     * @return 
     * @since 1.0.0
     */
    @Nonnull
    private Long getAppCode(@Nonnull
                            final Exception e,
                            @Nonnull
                            final Long defaultAppCode)
    {
        final Long codeFromException = Optional.of(
            e instanceof InternalException
        ).filter(
            isInternalExceptionToProcess -> isInternalExceptionToProcess
        ).map(
            isInternalExceptionToProcess -> ((InternalException)e).getCode()
        ).orElse(
            null
        );
        return Optional.of(
            e instanceof AuthenticationException
        ).filter(
            eIsAuthenticationExceptionToProcess -> eIsAuthenticationExceptionToProcess
        ).map(
            eIsAuthenticationExceptionToProcess -> {
                if(e instanceof InsufficientAuthenticationException || e instanceof BadCredentialsException)
                {
                    return DefaultCodes.ERROR_CODE_AUTHENTICATION_FAILURE;
                }
                else if(e instanceof AccountStatusException)
                {
                    return DefaultCodes.ERROR_CODE_PERMISSION_DENIED;
                }
                else if(e instanceof AuthenticationServiceException)
                {
                    return DefaultCodes.ERROR_CODE_INTERNAL_FAILURE;
                }
                return DefaultCodes.ERROR_CODE_AUTHENTICATION_FAILURE;
            }
        ).orElseGet(
            () -> Optional.ofNullable(
                codeFromException
            ).orElseGet(
                () -> Optional.ofNullable(
                    e.getClass().getDeclaredAnnotation(
                        AppCode.class
                    )
                ).map(
                    appCodeToProcess -> Optional.of(
                        appCodeToProcess.code()
                    ).filter(
                        codeIsDefaultToProcess -> codeIsDefaultToProcess>0
                    ).orElseGet(
                        () -> Optional.of(
                            appCodeToProcess.value()
                        ).filter(
                            codeIsDefaultToProcess -> codeIsDefaultToProcess>0
                        ).orElse(
                            defaultAppCode
                        )
                    )
                ).orElse(
                    defaultAppCode
                )
            )
        );
    }
}
