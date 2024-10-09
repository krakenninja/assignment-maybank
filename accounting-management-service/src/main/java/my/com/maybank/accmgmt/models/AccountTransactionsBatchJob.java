package my.com.maybank.accmgmt.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.annotation.Nonnull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * Account transactions batch job
 * @since 1.0.0
 * @author ChristopherCKW
 */
@JsonInclude(
    JsonInclude.Include.NON_NULL
)
@ToString(
    doNotUseGetters = true
)
@Accessors(
    fluent = false,
    chain = true
)
@Getter
@Setter
public class AccountTransactionsBatchJob
       implements Serializable
{
    /**
     * Operation to identify the type of batch job to run
     * @since 1.0.0
     */
    public enum Operation
    {
        /**
         * Create
         * @since 1.0.0
         */
        CREATE,
        
        /**
         * Update
         * @since 1.0.0
         */
        UPDATE
    }
    
    @Nonnull
    @NotBlank
    @NotEmpty
    private String batchId;
    
    /**
     * Record the max batch-size ; this is not the size of 
     * {@code batchAccountTransaction}
     * @since 1.0.0
     */
    @Nonnull
    private Integer batchSize;
    
    /**
     * Operation to perform for this batch job
     * @since 1.0.0
     */
    @Nonnull
    private Operation operation;
    
    /**
     * Batch of {@link AccountTransaction} to act on its defined {@link #operation}
     * @since 1.0.0
     * @see my.com.maybank.accmgmt.configuration.DefaultAppService#getAccountingKafkaTransactionJobBatchSize()
     */
    @Nonnull
    private List<AccountTransaction> batchAccountTransaction;
}
