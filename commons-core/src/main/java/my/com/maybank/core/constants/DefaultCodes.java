package my.com.maybank.core.constants;

import lombok.NoArgsConstructor;

/**
 * Default codes
 * @since 1.0.0
 * @author ChristopherCKW
 */
@NoArgsConstructor
public final class DefaultCodes
{
    /**
     * Error code to indicate internal failure
     * @since 1.0.0
     */
    public static final long ERROR_CODE_INTERNAL_FAILURE = 10001l;
    
    /**
     * Error code to indicate bad parameter
     * @since 1.0.0
     */
    public static final long ERROR_CODE_BAD_PARAMETER_FAILURE = 10002l;
    
    /**
     * Error code to indicate authentication failure
     * @since 1.0.0
     */
    public static final long ERROR_CODE_AUTHENTICATION_FAILURE = 10003l;
    
    /**
     * Error code to indicate permission denied
     * @since 1.0.0
     */
    public static final long ERROR_CODE_PERMISSION_DENIED = 10004l;
    
    /**
     * Error code to indicate resource not found
     * @since 1.0.0
     */
    public static final long ERROR_CODE_RESOURCE_NOT_FOUND = 10005l;
    
    /**
     * Error code to indicate incapability to process the entity
     * @since 1.0.0
     */
    public static final long ERROR_CODE_UNPROCESSABLE_ENTITY = 10006l;
}
