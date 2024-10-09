package my.com.maybank.core.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import my.com.maybank.core.constants.DefaultCodes;
import org.springframework.core.annotation.AliasFor;

/**
 * Application code annotation 
 * @since 1.0.0
 * @author ChristopherCKW
 */
@Target({
    ElementType.TYPE, 
    ElementType.METHOD
})
@Retention(
    RetentionPolicy.RUNTIME
)
@Documented
public @interface AppCode
{
    /**
     * Alias for {@link #code}, defaults to {@link DefaultCodes#ERROR_CODE_INTERNAL_FAILURE}
     * @return 
     * @since 1.0.0
     */
    @AliasFor(
        "code"
    )
    long value() default DefaultCodes.ERROR_CODE_INTERNAL_FAILURE;

    /**
     * Application code, defaults to {@link DefaultCodes#ERROR_CODE_INTERNAL_FAILURE}
     * @return 
     * @since 1.0.0
     */
    @AliasFor(
        "value"
    )
    long code() default DefaultCodes.ERROR_CODE_INTERNAL_FAILURE;
}
