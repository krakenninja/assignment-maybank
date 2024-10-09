package my.com.maybank.schema;

import lombok.NoArgsConstructor;

/**
 * Entity constants
 * @since 1.0.0
 * @author ChristopherCKW
 */
@NoArgsConstructor
public final class EntityConstants
{
    /**
     * User table name
     * @since 1.0.0
     */
    public static final String TABLE_NAME_USER = "tbl_user";
    
    /**
     * Role table name
     * @since 1.0.0
     */
    public static final String TABLE_NAME_ROLE = "tbl_role";
    
    /**
     * User roles table name
     * @since 1.0.0
     */
    public static final String TABLE_NAME_USER_ROLES = "tbl_user_roles";
    
    /**
     * Identifier column name
     * <p>
     * Usually a common internal identifier that is a primary key type
     * </p>
     * @since 1.0.0
     */
    public static final String COLUMN_NAME_ID = "col_id";
    
    /**
     * User identifier, usually used as FK naming
     * @since 1.0.0
     */
    public static final String COLUMN_NAME_USER_ID = "col_user_id";
    
    /**
     * Role identifier, usually used as FK naming
     * @since 1.0.0
     */
    public static final String COLUMN_NAME_ROLE_ID = "col_role_id";
    
    /**
     * Username column name
     * @since 1.0.0
     */
    public static final String COLUMN_NAME_USERNAME = "col_username";
    
    /**
     * Password column name
     * @since 1.0.0
     */
    public static final String COLUMN_NAME_PASSWORD = "col_pwd";
    
    /**
     * Role name column name
     * @since 1.0.0
     */
    public static final String COLUMN_NAME_ROLE_NAME = "col_role_name";
    
    /**
     * Created date/time
     * @since 1.0.0
     */
    public static final String COLUMN_NAME_CREATED = "col_created";
    
    /**
     * Modified date/time
     * @since 1.0.0
     */
    public static final String COLUMN_NAME_MODIFIED = "col_modified";
}
