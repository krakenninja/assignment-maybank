package my.com.maybank.usermgmt.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Application codes
 * @since 1.0.0
 * @author ChristopherCKW
 */
@NoArgsConstructor(
    access = AccessLevel.PRIVATE
)
public final class AppCodes
{
    /**
     * Error code to indicate the auth failure
     * @since 1.0.0
     */
    public static final Long ERROR_CODE_AUTH_FAILURE = 20001l;
    
    /**
     * Error code to indicate the auth bad credentials
     * @since 1.0.0
     */
    public static final Long ERROR_CODE_AUTH_BAD_CREDENTIALS = 20002l;
    
    /**
     * Error code to indicate the auth forbidden failure
     * @since 1.0.0
     */
    public static final Long ERROR_CODE_AUTH_FORBIDDEN = 20003l;
    
    /**
     * Error code to indicate the auth user not found
     * @since 1.0.0
     */
    public static final Long ERROR_CODE_AUTH_USER_NOT_FOUND = 20004l;
}
