package my.com.maybank.schema.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nonnull;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import java.time.LocalDateTime;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import my.com.maybank.schema.EntityConstants;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * User account entity type to map against the provided sample "dataSource.txt"
 * @since 1.0.0
 * @author ChristopherCKW
 */
@JsonInclude(
    JsonInclude.Include.NON_NULL
)
@Accessors(
    fluent = false,
    chain = true
)
@Getter
@Setter
@EqualsAndHashCode(
    doNotUseGetters = true
)
@Entity
@Table(
    name = EntityConstants.TABLE_NAME_USER_ACCOUNT,
    indexes = @Index(
        name = "idx_account_custid", 
        columnList = EntityConstants.COLUMN_NAME_CUSTOMER_ID
    )
)
public class Account
{
    /**
     * Account number (primary key)
     * @since 1.0.0
     */
    @EqualsAndHashCode.Include
    @Id
    @Column(
        name = EntityConstants.COLUMN_NAME_ACCOUNT_NUMBER,
        length = 255,
        nullable = false
    )
    private String accountNumber;
    
    /**
     * Customer identifier
     * @since 1.0.0
     * @see my.com.maybank.schema.entity.User#id
     */
    @EqualsAndHashCode.Include
    @Min(
        value = 1L
    )
    @Column(
        name = EntityConstants.COLUMN_NAME_CUSTOMER_ID,
        nullable = false,
        updatable = false
    )
    private Long customerId;
    
    /**
     * Created date/time
     * @since 1.0.0
     */
    @Nonnull
    @CreationTimestamp
    @Column(
        name = EntityConstants.COLUMN_NAME_CREATED
    )
    private LocalDateTime created;
    
    /**
     * Modified date/time
     * @since 1.0.0
     */
    @Nonnull
    @UpdateTimestamp
    @Column(
        name = EntityConstants.COLUMN_NAME_MODIFIED
    )
    private LocalDateTime modified;
    
    /**
     * One account with its many transactions
     * @since 1.0.0
     */
    @JsonProperty(
        access = JsonProperty.Access.WRITE_ONLY
    )
    @OneToMany(
        fetch = FetchType.LAZY,
        mappedBy = "account" // see Transaction#account field
    )
    private List<Transaction> transactions;
}
