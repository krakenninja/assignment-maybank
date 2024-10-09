package my.com.maybank.usermgmt.configuration;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import my.com.maybank.core.configuration.security.AppSecurity;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

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
    @Nullable
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
        @Size(
            min = 16, // min salt
            max = 32 // max salt (128 bits)
        )
        private String salt;
    }
}
