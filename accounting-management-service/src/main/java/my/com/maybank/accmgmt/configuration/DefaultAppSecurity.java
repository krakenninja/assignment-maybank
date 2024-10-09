package my.com.maybank.accmgmt.configuration;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Pattern;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import my.com.maybank.core.configuration.security.AppSecurity;
import my.com.maybank.core.utils.HttpUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

/**
 * Default application security configuration
 * @since 1.0.0
 * @author ChristopherCKW
 */
@Accessors(
    fluent = false,
    chain = true
)
@Getter
@Setter
@Configuration
@ConfigurationProperties(
    prefix = "app.security"
)
public class DefaultAppSecurity
       extends AppSecurity
{
    /**
     * Application Security - AuthN user configuration details
     * @since 1.0.0
     */
    @Nonnull
    private AuthnUser authnUser;
    
    /**
     * AuthN user configuration
     * @since 1.0.0
     */
    @Accessors(
        fluent = false,
        chain = true
    )
    @Getter
    @Setter
    public static class AuthnUser
    {
        @Nonnull
        private AuthApi authApi;
    }
    
    /**
     * Auth API user configuration
     * @since 1.0.0
     */
    @Accessors(
        fluent = false,
        chain = true
    )
    @Getter
    @Setter
    public static class AuthApi
    {
        /**
         * Auth base API
         * @since 1.0.0
         */
        @Nonnull
        @Pattern(
            regexp=HttpUtils.REGEX_HTTP_COMPLIANCE
        )
        private String endpoint;
        
        /**
         * Auth HTTP method API
         * @since 1.0.0
         */
        @Nonnull
        @Getter(
            AccessLevel.NONE
        )
        private String method;
        
        /**
         * Auth HTTP path API
         * @since 1.0.0
         */
        @Nonnull
        private String path;
        
        /**
         * Auth HTTP authorization value
         * <p>
         * For example, {@code Basic am9obi5kb2U6cGFzc3dvcmQ=}
         * </p>
         * @since 1.0.0
         */
        @Nonnull
        @Getter(
            AccessLevel.NONE
        )
        private String authorization;
        
        /**
         * Timeout time unit
         * @since 1.0.0
         * @see java.util.concurrent.TimeUnit
         * @see #connectTimeout
         * @see #readTimeout
         */
        @Nonnull
        @Getter(
            AccessLevel.NONE
        )
        private String timeunitTimeout;
        
        /**
         * Connect timeout
         * @since 1.0.0
         */
        @Nonnull
        private Long connectTimeout;
        
        /**
         * Read timeout
         * @since 1.0.0
         */
        @Nonnull
        private Long readTimeout;
        
        /**
         * Get the auth HTTP method API as object type
         * @return                              {@link HttpMethod}, never 
         *                                      {@code null}
         * @since 1.0.0
         */
        @Nonnull
        public HttpMethod getMethod()
        {
            return HttpMethod.valueOf(
                this.method
            );
        }
        
        /**
         * Parse and get the authorization credentials
         * <p>
         * Supported key lookup {@code HttpUtils.AuthorizationType#AUTHZ_CRED_xxxxx}
         * </p>
         * @return                              {@link Map} that contains the 
         *                                      credentials parsed from the 
         *                                      supported {@link #authorization} 
         *                                      value
         * @since 1.0.0
         */
        @Nonnull
        public Map<String, String> getAuthorization()
        {
            return HttpUtils.AuthorizationType.parse(
                this.authorization
            );
        }
        
        /**
         * Get timeout time unit
         * @return                              {@link TimeUnit}, never {@code null}
         * @since 1.0.0
         */
        @Nonnull
        public TimeUnit getTimeunitTimeout()
        {
            return TimeUnit.valueOf(
                this.timeunitTimeout
            );
        }
    }
}
