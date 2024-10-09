package my.com.maybank.core.constants;

import lombok.NoArgsConstructor;

/**
 * Default roles
 * @since 1.0.0
 * @author ChristopherCKW
 */
@NoArgsConstructor
public final class DefaultRoles
{
    /**
     * Default {@code ROLE_USER} to indicate this is a user role
     * @since 1.0.0
     */
    public static final String ROLE_USER = "ROLE_USER";
    
    /**
     * Default {@code ROLE_ADMIN} to indicate this is a admin role
     * @since 1.0.0
     */
    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    
    /**
     * Default {@code ROLE_ADMIN} to indicate this is a customer role
     * @since 1.0.0
     */
    public static final String ROLE_CUSTOMER = "ROLE_CUSTOMER";
    
    /**
     * Prefix {@code ROLE_} ; in Spring Security, role-authority are determined 
     * by its prefixed value, otherwise it assumes as "permission-authority"
     * @since 1.0.0
     */
    public static final String PREFIX_ROLE = "ROLE_";
}
