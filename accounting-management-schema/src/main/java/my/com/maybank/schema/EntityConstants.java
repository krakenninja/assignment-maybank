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
     * User account table name
     * @since 1.0.0
     */
    public static final String TABLE_NAME_USER_ACCOUNT = "tbl_user_account";
    
    /**
     * Account transaction table name
     * @since 1.0.0
     */
    public static final String TABLE_NAME_ACCOUNT_TRANSACTION = "tbl_account_transaction";
    
    /**
     * Account number column name
     * @since 1.0.0
     */
    public static final String COLUMN_NAME_ACCOUNT_NUMBER = "col_account_number";
    
    /**
     * Customer identifier column name
     * @since 1.0.0
     */
    public static final String COLUMN_NAME_CUSTOMER_ID = "col_customer_id";
    
    /**
     * Amount column name
     * @since 1.0.0
     */
    public static final String COLUMN_NAME_AMOUNT = "col_amount";
    
    /**
     * Description column name
     * @since 1.0.0
     */
    public static final String COLUMN_NAME_DESCRIPTION = "col_description";
    
    /**
     * Date column name
     * @since 1.0.0
     */
    public static final String COLUMN_NAME_TRX_DATE = "col_trx_date";
    
    /**
     * Time column name
     * @since 1.0.0
     */
    public static final String COLUMN_NAME_TRX_TIME = "col_trx_time";
    
    /**
     * Identifier column name
     * <p>
     * Usually a common internal identifier that is a primary key type
     * </p>
     * @since 1.0.0
     */
    public static final String COLUMN_NAME_ID = "col_id";
    
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
