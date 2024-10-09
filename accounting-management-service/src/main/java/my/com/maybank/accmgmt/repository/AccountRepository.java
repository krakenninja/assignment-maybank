package my.com.maybank.accmgmt.repository;

import jakarta.annotation.Nonnull;
import my.com.maybank.schema.entity.Account;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Account repository
 * @since 1.0.0
 * @see my.com.maybank.schema.entity.Account
 * @author ChristopherCKW
 */
public interface AccountRepository
       extends JpaRepository<Account, String>
{
    /**
     * Find by {@link Account#accountNumber} containing the {@code accountNumber} 
     * value AND {@link Account#customerId} exact
     * @param customerId                        Customer identifier.  Must not be 
     *                                          {@code null} or invalid (min 1)
     * @param accountNumber                     Account number. Must not be 
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
        "FROM Account a " + 
        "WHERE (:customerId IS NULL OR a.customerId = :customerId) AND " + 
        "(:accountNumber IS NULL OR a.accountNumber LIKE CONCAT('%',:accountNumber,'%'))"
    )
    Slice<Account> findByCustomerIdAndAccountNumberWildcard(@Param("customerId")
                                                            final Long customerId,
                                                            @Param("accountNumber")
                                                            final String accountNumber,
                                                            @Nonnull
                                                            final Pageable pageable);
}
