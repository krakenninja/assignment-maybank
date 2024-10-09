package my.com.maybank.core.exception;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.StandardException;
import my.com.maybank.core.annotations.AppCode;
import my.com.maybank.core.constants.DefaultCodes;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Resource not found exception
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
    value = HttpStatus.NOT_FOUND
)
@AppCode(
    DefaultCodes.ERROR_CODE_RESOURCE_NOT_FOUND
)
public class ResourceNotFoundException
       extends InternalException
{
}
