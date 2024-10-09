package my.com.maybank.accmgmt.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.annotation.Nonnull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import my.com.maybank.accmgmt.configuration.DefaultAppService;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ParseDouble;
import org.supercsv.cellprocessor.ParseLong;
import org.supercsv.cellprocessor.ift.CellProcessor;

/**
 * {@link my.com.maybank.accmgmt.models.AccountTransaction} CSV column, 
 * processor and Java fields
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
@RequiredArgsConstructor
public enum AccountTransactionsCsvFields
{
    ACCOUNT_NUMBER(
        "ACCOUNT_NUMBER",
        "accountNumber",
        new Optional()
    ),
    
    TRX_AMOUNT(
        "TRX_AMOUNT",
        "trxAmount",
        new Optional(
            new ParseDouble()
        )
    ),
    
    DESCRIPTION(
        "DESCRIPTION",
        "description",
        new Optional()
    ),
    
    TRX_DATE(
        "TRX_DATE",
        "trxDate",
        new Optional()
    ),
    
    TRX_TIME(
        "TRX_TIME",
        "trxTime",
        new Optional()
    ),
    
    CUSTOMER_ID(
        "CUSTOMER_ID",
        "customerId",
        new Optional(
            new ParseLong()
        )
    )
    
    ;
    
    /**
     * CSV column name of the formatted account transactions file
     * @since 1.0.0
     */
    @NonNull
    @Nonnull
    @NotBlank
    @NotEmpty
    private final String colName;
    
    /**
     * Field name of the formatted account transactions file
     * @since 1.0.0
     */
    @NonNull
    @Nonnull
    @NotBlank
    @NotEmpty
    private final String fieldName;
    
    /**
     * CSV column processor to handle its value reading capability
     * @since 1.0.0
     */
    @NonNull
    @Nonnull
    private final CellProcessor colCellProcessor;
    
    /**
     * Get the column mappings (in the order of the enum)
     * <p>
     * It is expected that the CSV file format is complied to the order of this 
     * enum
     * </p>
     * @return                                  Column mappings
     * @since 1.0.0
     */
    @Nonnull
    public static String[] getColumnMappings()
    {
        return Stream.of(
            values()
        ).map(
            AccountTransactionsCsvFields::getColName
        ).toArray(
            String[]::new
        );
    }
    
    /**
     * Get the Java {@link my.com.maybank.accmgmt.models.AccountTransaction} 
     * field mappings (in the order of the enum)
     * <p>
     * It is expected that the CSV file format is complied to the order of this 
     * enum
     * </p>
     * @return                                  Java field mappings
     * @since 1.0.0
     */
    @Nonnull
    public static String[] getFieldMappings()
    {
        return Stream.of(
            values()
        ).map(
            AccountTransactionsCsvFields::getFieldName
        ).toArray(
            String[]::new
        );
    }

    /**
     * Get the cell processors (in the order of the enum)
     * <p>
     * It is expected that the CSV file format is complied to the order of this 
     * enum
     * </p>
     * @return                                  Column processors
     * @since 1.0.0
     */
    @Nonnull
    public static CellProcessor[] getCellProcessors()
    {
        return Stream.of(
            values()
        ).map(
            AccountTransactionsCsvFields::getColCellProcessor
        ).toArray(
            CellProcessor[]::new
        );
    }
}
