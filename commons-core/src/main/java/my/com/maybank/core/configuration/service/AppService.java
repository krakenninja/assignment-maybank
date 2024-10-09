package my.com.maybank.core.configuration.service;

import my.com.maybank.core.configuration.security.*;
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
 * Application service configuration - {@code app.service}
 * @since 1.0.0
 * @author ChristopherCKW
 */
@Accessors(
    fluent = false,
    chain = true
)
@Getter
@Setter
public class AppService
{
    /**
     * Data formatting
     * @since 1.0.0
     */
    @Nullable
    private DataFormatting dataFormatting;
    
    /**
     * Data limiter
     * @since 1.0.0
     */
    @Nullable
    private DataLimiter dataLimiter;
    
    /**
     * Data path
     * @since 1.0.0
     */
    @Nullable
    private DataPath dataPath;
    
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
    public static class DataFormatting
    {
        @Nonnull
        private DateFormatting date;
        
        @Nonnull
        private TimeFormatting time;
    }
    
    /**
     * Date formatting configuration
     * @since 1.0.0
     */
    @Accessors(
        fluent = false,
        chain = true
    )
    @Getter
    @Setter
    public static class DateFormatting
    {
        @Nonnull
        private String simple;
    }
    
    /**
     * Time formatting configuration
     * @since 1.0.0
     */
    @Accessors(
        fluent = false,
        chain = true
    )
    @Getter
    @Setter
    public static class TimeFormatting
    {
        @Nonnull
        private String simple;
    }
    
    /**
     * Data limiter configuration
     * @since 1.0.0
     */
    @Accessors(
        fluent = false,
        chain = true
    )
    @Getter
    @Setter
    public static class DataLimiter
    {
        @Nonnull
        private PaginationLimiter pagination;
    }
    
    /**
     * Pagination limiter configuration
     * @since 1.0.0
     */
    @Accessors(
        fluent = false,
        chain = true
    )
    @Getter
    @Setter
    public static class PaginationLimiter
    {
        @Nonnull
        private Integer pageSize;
    }
    
    /**
     * Data path
     * @since 1.0.0
     */
    @Accessors(
        fluent = false,
        chain = true
    )
    @Getter
    @Setter
    public static class DataPath
    {
        @Nonnull
        private Upload upload;
    }
    
    /**
     * Upload data path
     * @since 1.0.0
     */
    @Accessors(
        fluent = false,
        chain = true
    )
    @Getter
    @Setter
    public static class Upload
    {
        @Nonnull
        private String destBasePath;
    }
}
