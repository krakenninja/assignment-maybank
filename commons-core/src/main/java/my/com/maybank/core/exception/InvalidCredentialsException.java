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
 * Permission denied exception
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
    value = HttpStatus.UNAUTHORIZED
)
@AppCode(
    DefaultCodes.ERROR_CODE_AUTHENTICATION_FAILURE
)
public class InvalidCredentialsException
       extends InternalException
{
}
