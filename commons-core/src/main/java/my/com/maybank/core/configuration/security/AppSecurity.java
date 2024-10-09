package my.com.maybank.core.configuration.security;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Application security configuration - {@code app.security}
 * @since 1.0.0
 * @author ChristopherCKW
 */
@Accessors(
    fluent = false,
    chain = true
)
@Getter
@Setter
public class AppSecurity
{
    /**
     * HTTP security
     * @since 1.0.0
     */
    @Nullable
    private HttpSecurity httpSecurity;
    
    /**
     * HTTP security configuration
     * @since 1.0.0
     */
    @Accessors(
        fluent = false,
        chain = true
    )
    @Getter
    @Setter
    public static class HttpSecurity
    {
        @Nonnull
        private RequestMatcher requestMatcher;
    }
    
    /**
     * Request matcher configuration
     * @since 1.0.0
     */
    @Accessors(
        fluent = false,
        chain = true
    )
    @Getter
    @Setter
    public static class RequestMatcher
    {
        @Nonnull
        private String[] permitAll;
    }
}
