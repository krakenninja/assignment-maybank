package my.com.maybank.core.utils;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import jakarta.annotation.Nonnull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.apache.commons.codec.binary.Base64;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

/**
 * HTTP utility
 * @since 1.0.0
 */
@NoArgsConstructor
public final class HttpUtils
{
    /**
     * Regular expression pattern for HTTP URI compliance
     * @since 1.0.0
     */
    public static final String REGEX_HTTP_COMPLIANCE = "^(https?:\\/\\/)?([a-zA-Z0-9\\-\\.]+)(:\\d{1,5})?(\\/[^\\s]*)?(\\?[^\\s]*)?$";
    
    /**
     * Authorization type processor/parser
     * @since 1.0.0
     */
    @Getter
    @RequiredArgsConstructor
    public static enum AuthorizationType
    {
        /**
         * Bearer token type
         * @since 1.0.0
         */
        BEARER_TOKEN(
            AuthorizationType.AUTHZ_TYPE_BEARER_TOKEN,
            authorization -> Optional.ofNullable(
                authorization
            ).filter(
                authorizationToProcess -> authorizationToProcess.startsWith(
                    AuthorizationType.AUTHZ_TYPE_BEARER_TOKEN
                )
            ).map(
                authorizationToProcess -> Stream.of(
                    new AbstractMap.SimpleEntry<>(
                        AuthorizationType.AUTHZ_CRED_TOKEN,
                        authorizationToProcess.substring(
                            AuthorizationType.AUTHZ_TYPE_BEARER_TOKEN.length()
                        )
                    )
                ).collect(
                    Collectors.toMap(
                        Map.Entry::getKey, 
                        Map.Entry::getValue
                    )
                )
            ).orElse(
                Collections.emptyMap()
            )
        ),
        
        /**
         * Basic auth type
         * @since 1.0.0
         */
        BASIC_AUTH(
            AuthorizationType.AUTHZ_TYPE_BASIC_AUTH,
            authorization -> Optional.ofNullable(
                authorization
            ).filter(
                authorizationToProcess -> authorizationToProcess.startsWith(
                    AuthorizationType.AUTHZ_TYPE_BASIC_AUTH
                )
            ).map(
                authorizationToProcess -> Optional.ofNullable(
                    new String(
                        Base64.decodeBase64(
                            authorizationToProcess.substring(
                                AuthorizationType.AUTHZ_TYPE_BASIC_AUTH.length()
                            )
                        ), 
                        StandardCharsets.UTF_8
                    ).split(
                        ":", 
                        2
                    )
                ).filter(
                    basicAuthUsernamePasswordToProcess -> basicAuthUsernamePasswordToProcess.length==2
                ).map(
                    basicAuthUsernamePasswordToProcess -> Stream.of(
                        new AbstractMap.SimpleEntry<>(
                            AuthorizationType.AUTHZ_CRED_USERNAME,
                            basicAuthUsernamePasswordToProcess[0]
                        ),
                        new AbstractMap.SimpleEntry<>(
                            AuthorizationType.AUTHZ_CRED_PASSWORD,
                            basicAuthUsernamePasswordToProcess[1]
                        )
                    ).collect(
                        Collectors.toMap(
                            Map.Entry::getKey, 
                            Map.Entry::getValue
                        )
                    )
                ).orElse(
                    Collections.emptyMap()
                )
            ).orElse(
                Collections.emptyMap()
            )
        )
        
        ;
        
        /**
         * Authorization bearer token type prefix
         * @since 1.0.0
         * @see #getPrefix() 
         */
        public static final String AUTHZ_TYPE_BEARER_TOKEN = "Bearer ";
        
        /**
         * Authorization basic auth type prefix
         * @since 1.0.0
         * @see #getPrefix() 
         */
        public static final String AUTHZ_TYPE_BASIC_AUTH = "Basic ";
        
        /**
         * Credential type 
         * <p>
         * This will be the enum name of {@link AuthorizationType#name()}
         * </p>
         * @since 1.0.0
         * @see #parser() 
         */
        public static final String AUTHZ_CRED_TYPE = "type";
        
        /**
         * Credential token 
         * <p>
         * If authorization type is : {@link AuthorizationType#BEARER_TOKEN}
         * </p>
         * @since 1.0.0
         * @see #parser() 
         */
        public static final String AUTHZ_CRED_TOKEN = "token";
        
        /**
         * Credential username 
         * <p>
         * If authorization type is : {@link AuthorizationType#AUTHZ_TYPE_BASIC_AUTH}
         * </p>
         * @since 1.0.0
         * @see #parser() 
         */
        public static final String AUTHZ_CRED_USERNAME = "username";
        
        /**
         * Credential password 
         * <p>
         * If authorization type is : {@link AuthorizationType#AUTHZ_TYPE_BASIC_AUTH}
         * </p>
         * @since 1.0.0
         * @see #parser() 
         */
        public static final String AUTHZ_CRED_PASSWORD = "password";
        
        /**
         * AuthZ prefix hint
         * @since 1.0.0
         */
        @Nonnull
        private final String prefix;
        
        /**
         * AuthZ parser
         * @since 1.0.0
         */
        @Accessors(
            fluent = true
        )
        @Getter
        @Nonnull
        private final Function<String, Map<String, String>> parser;
        
        /**
         * Parse the {@code authorization} value and obtain the credentials ; 
         * see constants {@code AUTHZ_CRED_} (as the key) to get the value
         * @param authorization
         * @return 
         */
        public static Map<String, String> parse(@Nonnull
                                                final String authorization)
        {
            if(authorization.startsWith(AuthorizationType.AUTHZ_TYPE_BEARER_TOKEN))
            {
                final Map<String, String> result = AuthorizationType.BEARER_TOKEN.parser().apply(
                    authorization
                );
                result.put(
                    AuthorizationType.AUTHZ_CRED_TYPE,
                    AuthorizationType.AUTHZ_TYPE_BEARER_TOKEN
                );
                return result;
            }
            else if(authorization.startsWith(AuthorizationType.AUTHZ_TYPE_BASIC_AUTH))
            {
                final Map<String, String> result = AuthorizationType.BASIC_AUTH.parser().apply(
                    authorization
                );
                result.put(
                    AuthorizationType.AUTHZ_CRED_TYPE,
                    AuthorizationType.AUTHZ_TYPE_BASIC_AUTH
                );
                return result;
            }
            throw new IllegalArgumentException(
                String.format(
                    "Parser `%s` NOT FOUND FOR AUTHORIZATION VALUE '%s'",
                    AuthorizationType.class.getCanonicalName(),
                    authorization
                )
            );
        }
    }
    
    /**
     * Get web client builder
     * @param baseUrl                           Base URL for this builder instance. 
     *                                          Must not be {@code null} and 
     *                                          must be a well-formed HTTP(S) 
     *                                          URL
     * @param timeUnit                          Time unit to interpret value of 
     *                                          {@code connectTimeout} and 
     *                                          {@code readTimeout} (recommended 
     *                                          {@link TimeUnit#MILLISECONDS}). 
     *                                          Must not be {@code null}
     * @param connectTimeout                    Connection timeout (recommended 30). 
     *                                          Must be greater than {@code 1}
     * @param readTimeout                       Read timeout (recommended 30). 
     *                                          Must be  greater than {@code 1}
     * @return                                  {@link WebClient.Builder} instance
     * @since 1.0.0
     */
    @Nonnull
    public static WebClient.Builder getWebClientBuilderWithTimeout(@Nonnull
                                                                   @Pattern(
                                                                       regexp=HttpUtils.REGEX_HTTP_COMPLIANCE
                                                                   )
                                                                   final String baseUrl,
                                                                   @Nonnull
                                                                   final TimeUnit timeUnit,
                                                                   @Min(
                                                                       1
                                                                   )
                                                                   final long connectTimeout,
                                                                   @Min(
                                                                       1
                                                                   )
                                                                   final long readTimeout)
    {
        return getWebClientBuilder(
            baseUrl,
            Optional.of(
                getHttpClientWithTimeout(
                    timeUnit,
                    connectTimeout,
                    readTimeout
                )
            )
        );
    }
    
    /**
     * Get web client builder
     * @param baseUrl                           Base URL for this builder instance. 
     *                                          Must not be {@code null} and 
     *                                          must be a well-formed HTTP(S) 
     *                                          URL
     * @param httpClient                        Optional {@link HttpClient}. Must 
     *                                          not be {@code null}, use 
     *                                          {@link Optional#empty()} instead
     * @return                                  {@link WebClient.Builder} instance
     * @since 1.0.0
     */
    @Nonnull
    public static WebClient.Builder getWebClientBuilder(@Nonnull
                                                        @Pattern(
                                                            regexp=HttpUtils.REGEX_HTTP_COMPLIANCE
                                                        )
                                                        final String baseUrl,
                                                        @Nonnull
                                                        final Optional<HttpClient> httpClient)
    {
        final WebClient.Builder webClientBuilder = WebClient.builder().baseUrl(
            baseUrl
        );
        httpClient.ifPresent(
            httpClientToProcess -> webClientBuilder.clientConnector(
                new ReactorClientHttpConnector(httpClientToProcess)
            )
        );
        return webClientBuilder;
    }
    
    /**
     * HTTP client with timeout feature support
     * @param timeUnit                          Time unit to interpret value of 
     *                                          {@code connectTimeout} and 
     *                                          {@code readTimeout} (recommended 
     *                                          {@link TimeUnit#MILLISECONDS}). 
     *                                          Must not be {@code null}
     * @param connectTimeout                    Connection timeout (recommended 30). 
     *                                          Must be greater than {@code 1}
     * @param readTimeout                       Read timeout (recommended 30). 
     *                                          Must be  greater than {@code 1}
     * @return                                  {@link HttpClient} with timeout 
     *                                          support, never {@code null}
     * @since 1.0.0
     */
    @Nonnull
    public static HttpClient getHttpClientWithTimeout(@Nonnull
                                                      final TimeUnit timeUnit,
                                                      @Min(
                                                          1
                                                      )
                                                      final long connectTimeout,
                                                      @Min(
                                                          1
                                                      )
                                                      final long readTimeout)
    {
        return HttpClient.create().option(
            ChannelOption.CONNECT_TIMEOUT_MILLIS, 
            (int)timeUnit.convert(
                connectTimeout,
                TimeUnit.MILLISECONDS
            )
        ).responseTimeout(
            Duration.of(
                readTimeout,
                timeUnit.toChronoUnit()
            )
        ).doOnConnected(
            connection -> connection.addHandlerLast(
                new ReadTimeoutHandler(
                    readTimeout, 
                    timeUnit
                )
            ).addHandlerLast(
                new WriteTimeoutHandler(
                    readTimeout, 
                    timeUnit
                )
            )
        );
    }
}
