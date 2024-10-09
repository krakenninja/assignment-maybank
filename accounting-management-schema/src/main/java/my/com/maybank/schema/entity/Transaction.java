package my.com.maybank.schema.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nonnull;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import my.com.maybank.schema.EntityConstants;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * Transaction entity type to map against the provided sample "dataSource.txt"
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
    name = EntityConstants.TABLE_NAME_ACCOUNT_TRANSACTION
)
public class Transaction
{
    /**
     * A unique identifier (as the primary key)
     * @since 1.0.0
     */
    @EqualsAndHashCode.Include
    @JsonIgnore
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(
        name = EntityConstants.COLUMN_NAME_ID,
        updatable = false
    )
    private UUID id;
    
    /**
     * Account that made this transaction
     * @since 1.0.0
     */
    @ManyToOne
    @JoinColumn(
        name = EntityConstants.COLUMN_NAME_ACCOUNT_NUMBER, 
        nullable = false,
        foreignKey = @ForeignKey(
            name = "fk_account_trx_accno"
        ),
        updatable = false
    )
    private Account account;
    
    /**
     * Amount of this transaction
     * @since 1.0.0
     */
    @Column(
        name = EntityConstants.COLUMN_NAME_AMOUNT,
        nullable = false,
        updatable = false
    )
    private Double amount;
    
    /**
     * Description of this transaction
     * @since 1.0.0
     */
    @Column(
        name = EntityConstants.COLUMN_NAME_DESCRIPTION,
        length = 255,
        nullable = false
    )
    private String description;
    
    /**
     * Date only of this transaction
     * @since 1.0.0
     */
    @Temporal(
        TemporalType.DATE
    )
    @Column(
        name = EntityConstants.COLUMN_NAME_TRX_DATE,
        nullable = false,
        updatable = false
    )
    private Date date;
    
    /**
     * Time only of this transaction
     * @since 1.0.0
     */
    @Temporal(
        TemporalType.TIME
    )
    @Column(
        name = EntityConstants.COLUMN_NAME_TRX_TIME,
        nullable = false,
        updatable = false
    )
    private Date time;
    
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
}
