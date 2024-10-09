package my.com.maybank.accmgmt.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.annotation.Nullable;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import my.com.maybank.schema.entity.Account;
import my.com.maybank.schema.entity.Transaction;

/**
 * Account transaction criteria
 * @since 1.0.0
 * @author ChristopherCKW
 */
@ToString(
    doNotUseGetters = true
)
@JsonInclude(
    JsonInclude.Include.NON_NULL
)
@Accessors(
    fluent = false,
    chain = true
)
@Getter
@Setter
public class AccountTransactionCriteria
       implements Serializable
{
    /**
     * Next page (usually would be a numeric value ; but certain systems may 
     * also return a cursor name that can be "string" value) to get
     * @since 1.0.0
     */
    @Nullable
    private String nextPage;
    
    /**
     * Transaction search by :
     * <p>
     * <ul>
     *   <li>{@link Account#getCustomerId()} - if present exact</li>
     *   <li>{@link Account#getAccountNumber()} - if present exact/wildcard</li>
     *   <li>{@link Transaction#getDescription()} - if present exact/wildcard</li>
     * </ul>
     * </p>
     * @since 1.0.0
     */
    private Transaction transaction;
    
    /**
     * A criteria to indicate "fields" that we want to update for all the 
     * matching transactions found
     * <p>
     * So this means if you want to update ONE(1) specific targeted transaction, 
     * you must know the {@link Transaction#getId()} and clearly specify it in 
     * the {@link #transaction} parameter
     * </p>
     * <p>
     * If user attempt to provide a field that was annotated with 
     * {@link jakarta.persistence.Column#updatable()} to {@code false}, depending 
     * on the operation ; it will fail if you supply its value
     * </p>
     * @since 1.0.0
     */
    private Transaction updateTransaction;

    /**
     * For batch process ; internal use
     * @since 1.0.0
     */
    @JsonIgnore
    @Nullable
    private AccountTransactionsBatchJob.Operation operation;
    
    /**
     * For batch process ; internal use
     * @since 1.0.0
     */
    @JsonIgnore
    @Nullable
    private Boolean ignoreMaxPageSizeLimit;
}
