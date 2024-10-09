package my.com.maybank.accmgmt.repository;

import jakarta.annotation.Nonnull;
import java.util.UUID;
import my.com.maybank.schema.entity.Transaction;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Account repository
 * @since 1.0.0
 * @see my.com.maybank.schema.entity.Transaction
 * @author ChristopherCKW
 */
public interface TransactionRepository
       extends JpaRepository<Transaction, UUID>
{
    /**
     * Find by {@link Transaction#account} -&gt; {@link Account#accountNumber} 
     * containing the {@code accountNumber} value OR 
     * {@link Transaction#account} -&gt; {@link Account#customerId} exact OR 
     * {@link Transaction#description} containing the {@code description} value
     * @param customerId                        Customer identifier.  Must not be 
     *                                          {@code null} or invalid (min 1)
     * @param accountNumber                     Account number. Must not be 
     *                                          {@code null} or blank/empty
     * @param description                       Description. Must not be 
     *                                          {@code null} or blank/empty
     * @param pageable                          Pageable "configuration". Must 
     *                                          not be {@code null}
     * @return                                  {@link org.springframework.data.domain.Slice} 
     *                                          of {@link Account}(s). 
     *                                          {@link org.springframework.data.domain.Page}  
     *                                          will be expensive, as it'll return 
     *                                          the full count of rows
     * @since 1.0.0
     */
    @Nonnull
    @Query(
        "FROM Transaction t " + 
        "WHERE (:customerId = 0 OR t.account.customerId = :customerId) AND " + 
        "(:accountNumber = '' OR t.account.accountNumber LIKE CONCAT('%',:accountNumber,'%')) AND " + 
        "(:description = '' OR UPPER(t.description) LIKE CONCAT('%',UPPER(:description),'%'))"
    )
    Slice<Transaction> findByCustomerIdOrAccountNumberOrDescriptionWildcard(@Param("customerId")
                                                                            final Long customerId,
                                                                            @Param("accountNumber")
                                                                            final String accountNumber,
                                                                            @Param("description")
                                                                            final String description,
                                                                            @Nonnull
                                                                            final Pageable pageable);
}
