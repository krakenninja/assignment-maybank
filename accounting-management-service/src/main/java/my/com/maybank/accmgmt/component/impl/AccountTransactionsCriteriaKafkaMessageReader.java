package my.com.maybank.accmgmt.component.impl;

import jakarta.annotation.Nonnull;
import jakarta.persistence.NoResultException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import my.com.maybank.accmgmt.component.MessageReader;
import my.com.maybank.accmgmt.configuration.DefaultAppService;
import my.com.maybank.accmgmt.models.AccountTransaction;
import my.com.maybank.accmgmt.models.AccountTransactionCriteria;
import my.com.maybank.accmgmt.models.AccountTransactions;
import my.com.maybank.accmgmt.models.AccountTransactionsBatchJob;
import my.com.maybank.accmgmt.service.AccountingService;
import my.com.maybank.core.configuration.service.AppService;
import org.springframework.stereotype.Component;

/**
 * Account transaction criteria Kafka message reader
 * @since 1.0.0
 * @author ChristopherCKW
 */
@Slf4j
@Accessors(
    fluent = false,
    chain = true
)
@Getter
@Setter
@RequiredArgsConstructor
@Component
public class AccountTransactionsCriteriaKafkaMessageReader 
       implements MessageReader<AccountTransactionCriteria, AccountTransactionsBatchJob, List<AccountTransactionsBatchJob>>
{
    @NonNull
    @Nonnull
    private AppService appService;
    
    @NonNull
    @Nonnull
    private AccountingService accountingService;
    
    @Override
    public List<AccountTransactionsBatchJob> readAndCallback(@Nonnull
                                                             final AccountTransactionCriteria criteria, 
                                                             @Nonnull
                                                             final Consumer<AccountTransactionsBatchJob> consume) 
    {
        final List<AccountTransactionsBatchJob> createdAccountTransactionsBatchJobs = new CopyOnWriteArrayList<>();
        return Optional.ofNullable(
            criteria.getOperation()
        ).filter(
            operationToProcess -> operationToProcess.equals(
                AccountTransactionsBatchJob.Operation.UPDATE
            )
        ).map(
            operationToProcess -> {
                // get the batch size limit to set into the pagination detail
                final int startPage = 0;
                final Integer accountingKafkaTransactionJobBatchSize = ((DefaultAppService)getAppService()).getAccountingKafkaTransactionJobBatchSize();
                
                readAndCallback(
                    criteria.setNextPage(
                        getAccountingService().nextPageBuilder(
                            startPage, 
                            accountingKafkaTransactionJobBatchSize, 
                            accountingKafkaTransactionJobBatchSize,
                            Boolean.TRUE
                        )
                    ).setIgnoreMaxPageSizeLimit(
                        Boolean.TRUE
                    ), 
                    consume,
                    createdAccountTransactionsBatchJobs,
                    accountingKafkaTransactionJobBatchSize
                );
                
                return createdAccountTransactionsBatchJobs;
            }
        ).orElseThrow(
            () -> new IllegalArgumentException(
                String.format(
                    "Required parameter `criteria::operation` IS NULL OR INVALID ; expected %s got %s", 
                    AccountTransactionsBatchJob.Operation.UPDATE,
                    criteria.getOperation()
                )
            )
        );
    }
    
    /**
     * Read and callback internal
     * @param criteria
     * @param consume
     * @param createdAccountTransactionsBatchJobs 
     * @param batchSize 
     * @since 1.0.0
     */
    private void readAndCallback(@Nonnull
                                 final AccountTransactionCriteria criteria, 
                                 @Nonnull
                                 final Consumer<AccountTransactionsBatchJob> consume,
                                 @Nonnull
                                 final List<AccountTransactionsBatchJob> createdAccountTransactionsBatchJobs,
                                 final int batchSize) 
    {
        Optional.ofNullable(
            criteria.getOperation()
        ).filter(
            operationToProcess -> operationToProcess.equals(
                AccountTransactionsBatchJob.Operation.UPDATE
            )
        ).ifPresentOrElse(
            operationToProcess -> {
                // get this batch account transactions batch-oaged to "update"
                final AccountTransactions accountTransactionsBatch = getAccountingService().retrieveAccountTransactions(
                    criteria
                );
                log.info(
                    "Retrieved account transaction(s) --- \n\tCriteria : \n\t\t{}\n\tResults : \n\t\t{}",
                    criteria,
                    accountTransactionsBatch
                );
                
                // validate the update results ; the pulled records MUST HAVE 
                // te ID that we want to update with
                Optional.ofNullable(
                    accountTransactionsBatch.getAccountTransactions()
                ).filter(
                    accountTransactionToProcess -> !accountTransactionToProcess.isEmpty()
                ).ifPresentOrElse(
                    accountTransactionsToProcess -> Optional.of(
                        accountTransactionsToProcess.stream().map(
                            // set the description to "update"
                            accountTransactionToProcess -> Optional.ofNullable(
                                criteria.getUpdateTransaction()
                            ).map(
                                criteriaUpdateTransactionToProcess -> {
                                    // update the description
                                    Optional.ofNullable(
                                        criteriaUpdateTransactionToProcess.getDescription()
                                    ).filter(
                                        descriptionToUpdate -> !descriptionToUpdate.trim().equals(
                                            ""
                                        )
                                    ).ifPresent(
                                        accountTransactionToProcess::setDescription
                                    );
                                    return accountTransactionToProcess;
                                }
                            ).orElse(
                                accountTransactionToProcess
                            )
                        ).allMatch(
                            accountTransactionToProcess -> Objects.nonNull(
                                accountTransactionToProcess.getId()
                            )
                        )
                    ).filter(
                        hasValidAccountTransactionIdsToProcess -> hasValidAccountTransactionIdsToProcess
                    ).orElseThrow(
                        // this won't happen, but we add this to ensure a robust check
                        () -> new IllegalStateException(
                            String.format(
                                "Result(s) for batch criteria IS MISSING REQUIRED ID FOR UPDATE OPERATION ; one-of result is missing identifier --- %n\t%s",
                                accountTransactionsToProcess
                            )
                        )
                    ),
                    () -> {
                        // odly the criteria returned no results ; something 
                        // got deleted OR perhaps a bad criteria?
                        throw new NoResultException(
                            String.format(
                                "No result(s) for batch criteria RETURNED NO RESULTS ; using batch criteria --- %n\t%s",
                                criteria
                            )
                        );
                    }
                );
                
                processAccountTransactionsBatch(
                    accountTransactionsBatch,
                    operationToProcess,
                    criteria,
                    consume,
                    createdAccountTransactionsBatchJobs,
                    batchSize
                );
            },
            () -> {
                throw new IllegalArgumentException(
                    String.format(
                        "Required parameter `criteria::operation` IS NULL OR INVALID ; expected %s got %s", 
                        AccountTransactionsBatchJob.Operation.UPDATE,
                        criteria.getOperation()
                    )
                );
            }
        );
    }
    
    /**
     * Process account transactions batch
     * @param accountTransactionsBatch
     * @param operation
     * @param criteria
     * @param consume
     * @param createdAccountTransactionsBatchJobs
     * @param batchSize
     * @return                                  The consumed {@link AccountTransactionsBatchJob}
     * @since 1.0.0
     */
    @Nonnull
    private AccountTransactionsBatchJob processAccountTransactionsBatch(@Nonnull
                                                                        final AccountTransactions accountTransactionsBatch,
                                                                        @Nonnull
                                                                        final AccountTransactionsBatchJob.Operation operation,
                                                                        @Nonnull
                                                                        final AccountTransactionCriteria criteria,
                                                                        @Nonnull
                                                                        final Consumer<AccountTransactionsBatchJob> consume,
                                                                        @Nonnull
                                                                        final List<AccountTransactionsBatchJob> createdAccountTransactionsBatchJobs,
                                                                        final int batchSize)
    {
        // create and send the batch job
        final AccountTransactionsBatchJob accountTransactionsBatchJob = new AccountTransactionsBatchJob().setBatchId(
            UUID.randomUUID().toString()
        ).setBatchSize(
            batchSize
        ).setOperation(
            operation
        ).setBatchAccountTransaction(
            Optional.ofNullable(
                accountTransactionsBatch.getAccountTransactions()
            ).orElseThrow()
        );
        
        // send to kafka
        consume.accept(
            accountTransactionsBatchJob
        );
        
        // add the details of this jobs as it was successfully sent to kafka
        createdAccountTransactionsBatchJobs.add(
            accountTransactionsBatchJob
        );
        
        // do we have next?
        Optional.ofNullable(
            accountTransactionsBatch.getNextPage()
        ).filter(
            nextPageBatchToProcess -> !nextPageBatchToProcess.trim().equals(
                ""
            )
        ).map(
            nextPageBatchToProcess -> getAccountingService().nextPageBuilder(
                nextPageBatchToProcess,
                batchSize,
                Boolean.TRUE
            )
        ).filter(
            // we already processed 0, if we still get 0...ignore it
            nextPageableBatchToProcess -> nextPageableBatchToProcess.getPageNumber()>0
        ).ifPresent(
            nextPageableBatchToProcess -> readAndCallback(
                criteria.setNextPage(
                    getAccountingService().nextPageBuilder(
                        nextPageableBatchToProcess,
                        batchSize,
                        Boolean.TRUE
                    )
                ).setIgnoreMaxPageSizeLimit(
                    Boolean.TRUE
                ), 
                consume,
                createdAccountTransactionsBatchJobs,
                batchSize
            )
        );
        
        // all good
        return accountTransactionsBatchJob;
    }
}
