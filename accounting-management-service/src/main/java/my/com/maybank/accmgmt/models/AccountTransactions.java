package my.com.maybank.accmgmt.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.io.Serializable;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * Goal of this is to represent back a model {@link AccountTransaction} paginated  
 * that mapped closely back to the provided "{@code dataSource.txt}" 
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
public class AccountTransactions
       implements Serializable
{
    /**
     * Account transactions
     * @since 1.0.0
     */
    @Nullable
    private List<AccountTransaction> accountTransactions;
    
    /**
     * Next page (usually would be a numeric value ; but certain systems may 
     * also return a cursor name that can be "string" value) to get
     * @since 1.0.0
     */
    @Nullable
    private String nextPage;
}
