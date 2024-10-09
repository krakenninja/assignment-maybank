package my.com.maybank.accmgmt.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * Goal of this is to represent back a model that mapped closely back to the 
 * provided "{@code dataSource.txt}"
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
public class AccountTransaction
       implements Serializable
{
    /**
     * Usually internal use to map with 
     * {@link my.com.maybank.schema.entity.Transaction#getId()}
     * @since 1.0.0
     */
    @Nullable
    private String id;
    
    /**
     * Transacted account number
     * @since 1.0.0
     */
    @Nonnull
    @NotBlank
    @NotEmpty
    private String accountNumber;
    
    /**
     * Transacted amount
     * @since 1.0.0
     */
    @Nonnull
    @Min(
        1
    )
    private Double trxAmount;
    
    /**
     * Transacted description
     * @since 1.0.0
     */
    @Nonnull
    @NotBlank
    @NotEmpty
    private String description;
    
    /**
     * Transacted date (formatted {@code YYYY-MM-DD}) 
     * @since 1.0.0
     */
    @Nonnull
    @NotBlank
    @NotEmpty
    private String trxDate;
    
    /**
     * Transacted time (formatted {@code HH-MM-SS}) 
     * @since 1.0.0
     */
    @Nonnull
    @NotBlank
    @NotEmpty
    private String trxTime;
    
    /**
     * Customer identifier
     * @since 1.0.0
     */
    @Nonnull
    @Min(
        1
    )
    private Long customerId;
}
