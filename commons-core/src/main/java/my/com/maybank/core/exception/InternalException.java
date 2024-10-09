package my.com.maybank.core.exception;

import jakarta.annotation.Nullable;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.StandardException;
import my.com.maybank.core.annotations.AppCode;
import my.com.maybank.core.constants.DefaultCodes;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Internal exception
 * @since 1.0.0
 * @author ChristopherCKW
 */
@Accessors(
    fluent=false,
    chain=true
)
@Getter
@Setter
@StandardException
@ResponseStatus(
    value = HttpStatus.INTERNAL_SERVER_ERROR
)
@AppCode(
    DefaultCodes.ERROR_CODE_INTERNAL_FAILURE
)
public class InternalException
       extends RuntimeException
{
    /**
     * Code (if this is null, it defaults to take the annotated 
     * {@link my.com.maybank.core.annotations.AppCode#code()})
     * @since 1.0.0
     */
    @Nullable
    private Long code;
}
