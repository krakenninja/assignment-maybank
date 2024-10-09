package my.com.maybank.accmgmt.constants;

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
     * Error code to indicate generic upload failure
     * @since 1.0.0
     */
    public static final Long ERROR_CODE_UPLOAD_FAILURE = 40001l;
    
    /**
     * Error code to indicate the upload has no content
     * @since 1.0.0
     */
    public static final Long ERROR_CODE_UPLOAD_NO_CONTENT = 40002l;
    
    /**
     * Error code to indicate the update failure
     * @since 1.0.0
     */
    public static final Long ERROR_CODE_UPDATE_FAILURE = 30001l;
    
    /**
     * Error code to indicate the auth failure
     * @since 1.0.0
     */
    public static final Long ERROR_CODE_AUTH_FAILURE = 20001l;
    
    /**
     * Error code to indicate the auth forbidden failure
     * @since 1.0.0
     */
    public static final Long ERROR_CODE_AUTH_FORBIDDEN = 20002l;
}
