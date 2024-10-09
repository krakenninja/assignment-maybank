package my.com.maybank.accmgmt.service;

import jakarta.annotation.Nonnull;
import jakarta.validation.constraints.Min;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;
import my.com.maybank.accmgmt.models.AccountTransactionCriteria;
import my.com.maybank.accmgmt.models.AccountTransactions;
import my.com.maybank.accmgmt.models.AccountTransactionsBatchJob;
import org.slf4j.Logger;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

/**
 * Accounting service definition
 * @since 1.0.0
 * @author ChristopherCKW
 */
public interface AccountingService
{
    /**
     * Batch produce the {@link my.com.maybank.accmgmt.models.AccountTransactionsBatchJob} 
     * that was read from CSV path
     * @param csvPath                           CSV path that must exist. Must 
     *                                          not be {@code null}
     * @return                                  Number of lines converted to 
     *                                          batch {@link my.com.maybank.accmgmt.models.AccountTransaction} 
     * @since 1.0.0
     */
    long batchProducerCreateAccountTransactions(@Nonnull
                                                final Path csvPath);
    
    /**
     * Batch produce the {@link my.com.maybank.accmgmt.models.AccountTransactionsBatchJob} 
     * from the update search "criteria"
     * @param criteria                          Account search criteria. Must 
     *                                          not be {@code null}
     * @return                                  Number of batch items processed
     * @since 1.0.0
     */
    long batchProducerUpdateAccountTransactions(@Nonnull
                                                final AccountTransactionCriteria criteria);
    
    /**
     * Batch consume the {@link my.com.maybank.accmgmt.models.AccountTransactionsBatchJob} 
     * that was produced
     * @param accountTransactionsBatchJob       Batch of account transactions to 
     *                                          act on based on the {@link AccountTransactionsBatchJob#getOperation()}
     * @return                                  Number of batch items processed
     * @since 1.0.0
     */
    long batchConsumerOperationAccountTransactions(@Nonnull
                                                   final AccountTransactionsBatchJob accountTransactionsBatchJob);
    
    
    /**
     * Retrieve account transactions (pageable/paginated)
     * @param criteria                          Account search criteria. Must 
     *                                          not be {@code null}
     * @return                                  {@link AccountTransactions}
     * @since 1.0.0
     */
    @Nonnull
    AccountTransactions retrieveAccountTransactions(@Nonnull
                                                    final AccountTransactionCriteria criteria);
    
    /**
     * Next page builder
     * @param pageable                          {@link Pageable}. Must not be 
     *                                          {@code null}
     * @param maxPageSizeLimit                  Max page size limit
     * @param ignoreMaxPageSizeLimit            Use {@code true} to ignore 
     *                                          max-page size limit ; essentially 
     *                                          this is needed for batch process 
     *                                          flow
     * @return                                  Next page string value
     * @since 1.0.0
     */
    default String nextPageBuilder(@Nonnull
                                   final Pageable pageable,
                                   @Min(
                                       1
                                   )
                                   final int maxPageSizeLimit,  // ideally max should be 50, see application.yml
                                   final boolean ignoreMaxPageSizeLimit)
    {
        return nextPageBuilder(
            pageable.getPageNumber(),
            pageable.getPageSize(),
            maxPageSizeLimit,
            ignoreMaxPageSizeLimit
        );
    }
    
    /**
     * Next page builder
     * @param startPage                         Start page. Must be greater or 
     *                                          equals to {@code 0}
     * @param maxPage                           Max per-page. Must be greater or 
     *                                          equals to {@code 1}
     * @param maxPageSizeLimit                  Max page size limit
     * @param ignoreMaxPageSizeLimit            Use {@code true} to ignore 
     *                                          max-page size limit ; essentially 
     *                                          this is needed for batch process 
     *                                          flow
     * @return                                  Next page string value
     * @since 1.0.0
     */
    default String nextPageBuilder(@Min(
                                       0
                                   )
                                   final int startPage,
                                   @Min(
                                       1
                                   )
                                   final int maxPage,
                                   @Min(
                                       1
                                   )
                                   final int maxPageSizeLimit,  // ideally max should be 50, see application.yml
                                   final boolean ignoreMaxPageSizeLimit)
    {
        return String.format(
            "%d-%d",
            Optional.of(
                startPage
            ).filter(
                startPageToProcess -> startPageToProcess>=0
            ).orElseGet(
                () -> {
                    logger().ifPresent(
                        logToProcess -> logToProcess.warn(
                            "Next page page-number to process '{}' IS INVALID ; expecting index based page no. `>=0` splited formatted (pgno-pgsize) elements, return default",
                            startPage,
                            maxPageSizeLimit
                        )
                    );
                    return 0;
                }
            ),
            Optional.of(
                maxPage
            ).filter(
                maxPageToProcess -> maxPageToProcess>0
            ).map(
                maxPageToProcess -> Optional.of(
                    ignoreMaxPageSizeLimit
                ).filter(
                    ignoreMaxPageSizeLimitToProcess -> !ignoreMaxPageSizeLimitToProcess
                ).map(
                    ignoreMaxPageSizeLimitToProcess -> Optional.of(
                        maxPageToProcess
                    ).filter(
                        pageSizeLimitCheckToProcess -> pageSizeLimitCheckToProcess<=maxPageSizeLimit
                    ).orElseGet(
                        () -> {
                            logger().ifPresent(
                                logToProcess -> logToProcess.warn(
                                    "Next page page-size to process '{}' IS INVALID ; expecting within max-limit ({}) splited formatted (pgno-pgsize) elements, return default",
                                    maxPageToProcess,
                                    maxPageSizeLimit
                                )
                            );
                            return maxPageSizeLimit;
                        }
                    )
                ).orElse(
                    // ignored rule to check max page-size limit
                    maxPageToProcess
                )
            ).orElseGet(
                () -> {
                    logger().ifPresent(
                        logToProcess -> logToProcess.warn(
                            "Next page page-size to process '{}' IS INVALID ; expecting page size `>0` splited formatted (pgno-pgsize) elements, return default",
                            maxPage,
                            maxPageSizeLimit
                        )
                    );
                    return maxPageSizeLimit;
                }
            )
        );
    }
    
    /**
     * Next page builder
     * @param nextPage                          Next page formatted string
     * @param maxPageSizeLimit                  Max page size limit
     * @param ignoreMaxPageSizeLimit            Use {@code true} to ignore 
     *                                          max-page size limit ; essentially 
     *                                          this is needed for batch process 
     *                                          flow
     * @return                                  {@link Pageable} if able to 
     *                                          parse the {@code nextPage} OR 
     *                                          a default starting at page 
     *                                          {@code 0} and max at 
     *                                          {@code maxPageSizeLimit}
     * @since 1.0.0
     */
    default Pageable nextPageBuilder(@Nonnull
                                     final String nextPage,
                                     @Min(
                                         1
                                     )
                                     final int maxPageSizeLimit,
                                     final boolean ignoreMaxPageSizeLimit)
    {
        return Optional.ofNullable(
            nextPage
        ).filter(
            nextPageToProcess -> !nextPageToProcess.trim().equals(
                ""
            )
        ).map(
            nextPageToProcess -> Optional.ofNullable(
                nextPage.split(
                    "-", 
                    2
                )
            ).filter(
                nextPageSplitToProcess -> nextPageSplitToProcess.length==2
            ).map(
                nextPageSplitToProcess -> PageRequest.of(
                    Optional.of(
                        Integer.valueOf(
                            nextPageSplitToProcess[0]
                        )
                    ).filter(
                        pageNumberToProcess -> pageNumberToProcess>-1
                    ).orElseGet(
                        () -> {
                            logger().ifPresent(
                                logToProcess -> logToProcess.warn(
                                    "Next page page-number to process '{}' IS INVALID ; expecting index based page no. `>=0` splited formatted (pgno-pgsize) elements, return default",
                                    nextPageSplitToProcess[0],
                                    maxPageSizeLimit
                                )
                            );
                            return 0;
                        }
                    ), 
                    Optional.of(
                        Integer.valueOf(
                            nextPageSplitToProcess[1]
                        )
                    ).filter(
                        pageSizeToProcess -> pageSizeToProcess>0
                    ).map(
                        pageSizeToProcess -> Optional.of(
                            ignoreMaxPageSizeLimit
                        ).filter(
                            ignoreMaxPageSizeLimitToProcess -> !ignoreMaxPageSizeLimitToProcess
                        ).map(
                            ignoreMaxPageSizeLimitToProcess -> Optional.of(
                                pageSizeToProcess
                            ).filter(
                                pageSizeLimitCheckToProcess -> pageSizeLimitCheckToProcess<=maxPageSizeLimit
                            ).orElseGet(
                                () -> {
                                    logger().ifPresent(
                                        logToProcess -> logToProcess.warn(
                                            "Next page page-size to process '{}' IS INVALID ; expecting within max-limit ({}) splited formatted (pgno-pgsize) elements, return default",
                                            nextPageSplitToProcess[1],
                                            maxPageSizeLimit
                                        )
                                    );
                                    return maxPageSizeLimit;
                                }
                            )
                        ).orElse(
                            // ignored rule to check max page-size limit
                            pageSizeToProcess
                        )
                    ).orElseGet(
                        () -> {
                            logger().ifPresent(
                                logToProcess -> logToProcess.warn(
                                    "Next page page-size to process '{}' IS INVALID ; expecting page size `>0` splited formatted (pgno-pgsize) elements, return default",
                                    nextPageSplitToProcess[1],
                                    maxPageSizeLimit
                                )
                            );
                            return maxPageSizeLimit;
                        }
                    )
                )
            ).orElseGet(
                () -> {
                    logger().ifPresent(
                        logToProcess -> logToProcess.warn(
                            "Next page to process '{}' IS INVALID ; expecting length TWO(2) splited formatted (pgno-pgsize) elements, return default",
                            nextPage
                        )
                    );
                    // default it
                    return PageRequest.of(
                        0,
                        maxPageSizeLimit
                    );
                }
            )
        ).orElseGet(
            () -> {
                logger().ifPresent(
                    logToProcess -> logToProcess.debug(
                        "Next page to process NOT PROVIDED ; using default",
                        nextPage
                    )
                );
                // default it
                return PageRequest.of(
                    0,
                    maxPageSizeLimit
                );
            }
        );
    }
    
    /**
     * Get the class logger if available
     * @return                                  Optional {@link Logger}
     * @since 1.0.0
     */
    @Nonnull
    private Optional<Logger> logger()
    {
        try
        {
            return Arrays.stream(
                getClass().getDeclaredFields()
            ).filter(
                declaredFieldToProcess -> Logger.class.isAssignableFrom(
                    declaredFieldToProcess.getType()
                )
            ).findFirst().map(
                declaredFieldToProcess -> {
                    try
                    {
                        declaredFieldToProcess.setAccessible(
                            true
                        );
                        return (Logger)declaredFieldToProcess.get(
                            this
                        );
                    }
                    catch(Exception e) // ignore, we're not interested
                    {
                        return null;
                    }
                }
            );
        }
        catch(Exception e) // ignore, we're not interested
        {
            return Optional.empty();
        }
    }
}
