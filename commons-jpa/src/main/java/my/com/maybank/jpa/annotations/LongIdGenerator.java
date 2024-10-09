
package my.com.maybank.jpa.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.hibernate.annotations.IdGeneratorType;

/**
 * Configuration for {@link my.com.maybank.jpa.utils.LongIdGenerator}
 * @since 1.0.0
 * @author ChristopherCKW
 */
@IdGeneratorType(
    my.com.maybank.jpa.utils.LongIdGenerator.class
)
@Retention(
    RetentionPolicy.RUNTIME
)
@Target({
    ElementType.METHOD,
    ElementType.FIELD
})
public @interface LongIdGenerator
{
}
