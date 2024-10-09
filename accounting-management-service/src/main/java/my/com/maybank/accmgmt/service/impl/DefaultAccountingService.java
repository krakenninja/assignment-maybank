package my.com.maybank.accmgmt.service.impl;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.persistence.Column;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.NoResultException;
import jakarta.persistence.NonUniqueResultException;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import my.com.maybank.accmgmt.configuration.DefaultAppService;
import my.com.maybank.accmgmt.models.AccountTransaction;
import my.com.maybank.accmgmt.models.AccountTransactionCriteria;
import my.com.maybank.accmgmt.models.AccountTransactions;
import my.com.maybank.accmgmt.repository.AccountRepository;
import my.com.maybank.accmgmt.repository.TransactionRepository;
import my.com.maybank.accmgmt.service.AccountingService;
import my.com.maybank.core.configuration.service.AppService;
import my.com.maybank.schema.entity.Account;
import my.com.maybank.schema.entity.Transaction;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import my.com.maybank.accmgmt.component.MessageProducer;
import my.com.maybank.accmgmt.models.AccountTransactionsBatchJob;
import my.com.maybank.accmgmt.component.CsvMessageReader;
import my.com.maybank.accmgmt.component.impl.AccountTransactionsCriteriaKafkaMessageReaderDelegate;
import my.com.maybank.core.exception.BadParameterException;
import my.com.maybank.core.exception.InternalException;

/**
 * Default {@link AccountingService} implementation
 * @since 1.0.0
 * @author ChristopherCKW
 */
@Slf4j
@Getter
@Setter
@RequiredArgsConstructor
@Service
public class DefaultAccountingService
       implements AccountingService
{
    @Nonnull
    @NonNull
    private final AppService appService;
    
    @Nonnull
    @NonNull
    private final AccountRepository accountRepository;
    
    @Nonnull
    @NonNull
    private final TransactionRepository transactionRepository;
    
    @Nonnull
    @NonNull
    private final CsvMessageReader<AccountTransactionsBatchJob.Operation, AccountTransactionsBatchJob> accountTransactionCsvReader;
    
    @Nonnull
    @NonNull
    private final MessageProducer<AccountTransactionsBatchJob> accountTransactionMessageProducer;
    
    @Nonnull
    @NonNull
    private final AccountTransactionsCriteriaKafkaMessageReaderDelegate accountTransactionsCriteriaKafkaMessageReader;
    
    /**
     * Process the CSV file, put in batches then push to Kafka which then upon 
     * consume, it calls {@link #batchConsumerOperationAccountTransactions(my.com.maybank.accmgmt.models.AccountTransactionsBatchJob)} 
     * and executes the batch standing operation order (to either CREATE or 
     * UPDATE)
     * @param csvPath
     * @return                                  Number of processed results to 
     *                                          create
     * @since 1.0.0
     */
    @Override
    public long batchProducerCreateAccountTransactions(@Nonnull
                                                       final Path csvPath)
    {
        final long csvProcessedResultSize = accountTransactionCsvReader.readAndCallback(
            csvPath.toString(), 
            AccountTransactionsBatchJob.Operation.CREATE,
            accountTransactionMessageProducer::sendMessage
        );
        log.info(
            "Total number of CSV records size({}) of account transaction(s) sent to create COMPLETED",
            csvProcessedResultSize
        );
        return csvProcessedResultSize;
    }
    
    /**
     * Process the criteria to query for the results to be updated, put in 
     * batches then push to Kafka when then upon consume, it calls 
     * {@link #batchConsumerOperationAccountTransactions(my.com.maybank.accmgmt.models.AccountTransactionsBatchJob)} 
     * and executes the batch standing operation order (to either CREATE or 
     * UPDATE)
     * @param criteria
     * @return                                  Number of processed results to 
     *                                          update
     * @since 1.0.0
     */
    @Override
    public long batchProducerUpdateAccountTransactions(@Nonnull
                                                       final AccountTransactionCriteria criteria)
    {
        try
        {
            // check at first whether user had provided any update details
            final Transaction transactionCriteriaToUpdate = criteria.setIgnoreMaxPageSizeLimit(
                Boolean.TRUE
            ).getUpdateTransaction();
            checkTransactionUpdateCriteria(
                transactionCriteriaToUpdate
            );
            
            // business logic requirement ; description to update is required
            Optional.ofNullable(
                transactionCriteriaToUpdate.getDescription()
            ).filter(
                transactionDescriptionToUpdate -> !transactionDescriptionToUpdate.trim().equals(
                    ""
                )
            ).orElseThrow(
                () -> new BadParameterException(
                    "Transaction criteria detail 'description' to update IS NULL OR EMPTY"
                )
            );
            
            final AtomicInteger countProcessedResultSize = new AtomicInteger();
            final List<AccountTransactionsBatchJob> batchesSent = accountTransactionsCriteriaKafkaMessageReader.readAndCallback(
                criteria.setOperation(
                    AccountTransactionsBatchJob.Operation.UPDATE
                ), 
                accountTransactionMessageProducer::sendMessage
            );
            batchesSent.stream().forEach(
                batchSentToProcess -> countProcessedResultSize.addAndGet(
                    batchSentToProcess.getBatchAccountTransaction().size()
                )
            );
            log.info(
                "Total number of transaction records size({}) of account transaction(s) batch(es) size({}) sent to update COMPLETED ; batch details --- \n\t{}",
                countProcessedResultSize.get(),
                batchesSent.size(),
                batchesSent
            );
            return countProcessedResultSize.get();
        }
        catch(NoResultException e)
        {
            log.warn(
                "No matching result(s) TO BATCH UPDATE ; using criteria \n\t{}",
                criteria
            );
            return 0;
        }
    }
    
    /**
     * Process the batch operations accordingly to its standing operation 
     * instruction to either CREATE or UPDATE
     * @param accountTransactionsBatchJob
     * @return                                  Number of processed results that 
     *                                          was created/updated
     * @since 1.0.0
     */
    @Transactional
    @Override
    public long batchConsumerOperationAccountTransactions(@Nonnull
                                                          final AccountTransactionsBatchJob accountTransactionsBatchJob)
    {
        final AtomicLong processedCount = new AtomicLong();
        log.info(
            "Processing batch ID({}) ...",
            accountTransactionsBatchJob.getBatchId()
        );
        
        final Function<String, Date> strToDate = s -> {
            try
            {
                return ((DefaultAppService)appService).getSimpleDateFormat().parse(
                    s
                );
            }
            catch(Exception e)
            {
                throw new IllegalArgumentException(
                    String.format(
                        "Parse date '%s' string ENCOUNTERED FAILURE ; %s",
                        s,
                        e.getMessage()
                    ),
                    e
                );
            }
        };
        final Function<String, Date> strToTime = s -> {
            try
            {
                return ((DefaultAppService)appService).getSimpleTimeFormat().parse(
                    s
                );
            }
            catch(Exception e)
            {
                throw new IllegalArgumentException(
                    String.format(
                        "Parse date '%s' string ENCOUNTERED FAILURE ; %s",
                        s,
                        e.getMessage()
                    ),
                    e
                );
            }
        };
        switch(accountTransactionsBatchJob.getOperation())
        {
            case CREATE -> processedCount.set(
                getTransactionRepository().saveAll(
                    accountTransactionsBatchJob.getBatchAccountTransaction().stream().map(
                        accountTransactionToProcess -> new Transaction().setAccount(
                            findUserAccount(
                                new Account().setAccountNumber(
                                    accountTransactionToProcess.getAccountNumber()
                                ).setCustomerId(
                                    accountTransactionToProcess.getCustomerId()
                                )
                            )
                        ).setAmount(
                            accountTransactionToProcess.getTrxAmount()
                        ).setDate(
                            strToDate.apply(
                                accountTransactionToProcess.getTrxDate()
                            )
                        ).setDescription(
                            accountTransactionToProcess.getDescription()
                        ).setTime(
                            strToTime.apply(
                                accountTransactionToProcess.getTrxTime()
                            )
                        )
                    ).toList()
                ).size()
            );
            case UPDATE -> processedCount.set(
                getTransactionRepository().saveAll(
                    accountTransactionsBatchJob.getBatchAccountTransaction().stream().map(
                        accountTransactionToProcess -> Optional.ofNullable(
                            accountTransactionToProcess.getId()
                        ).map(
                            accountTransactionIdToProcess -> getTransactionRepository().findById(
                                UUID.fromString(
                                    accountTransactionIdToProcess
                                )
                            ).map(
                                transactionInDbToProcess -> {
                                    // update the description if available
                                    Optional.ofNullable(
                                        accountTransactionToProcess.getDescription()
                                    ).filter(
                                        accountTransactionDescriptionToProcess -> !accountTransactionDescriptionToProcess.trim().equals(
                                            ""
                                        )
                                    ).ifPresent(
                                        transactionInDbToProcess::setDescription
                                    );
                                    // update modified
                                    transactionInDbToProcess.setModified(
                                        LocalDateTime.now()
                                    );
                                    // return it back
                                    return transactionInDbToProcess;
                                }
                            ).orElseThrow(
                                // batch asking for a no longer existing transaction? 
                                // why? should we ignore OR throw ..for now throw?
                                () -> new EntityNotFoundException(
                                    String.format(
                                        "Batch ID(%s) account transaction ID(%s) IS NOT FOUND",
                                        accountTransactionsBatchJob.getBatchId(),
                                        accountTransactionIdToProcess
                                    )
                                )
                            )
                        ).orElseThrow(
                            // this should not happen .. if happens, somehow the 
                            // batch has some issues ; ideally it was already handled
                            () -> new InternalException(
                                String.format(
                                    "Batch ID(%s) account transaction ID IS NULL OR INVALID ; expecting transaction identifier value, received '%s'",
                                    accountTransactionsBatchJob.getBatchId(),
                                    accountTransactionToProcess.getId()
                                )
                            )
                        )
                    ).toList()
                ).size()
            );
            default -> throw new UnsupportedOperationException(
                String.format(
                    "Transaction batch ID(%s) job OPERATION(%s) IS UNSUPPORTED",
                    accountTransactionsBatchJob.getBatchId(),
                    accountTransactionsBatchJob.getOperation()
                )
            );
        }
        return processedCount.get();
    }

    @Nonnull
    @Transactional(
        readOnly = true
    )
    @Override
    public AccountTransactions retrieveAccountTransactions(@Nonnull
                                                           final AccountTransactionCriteria accountSearchCritera)
    {
        final Optional<Pageable> getPage = Optional.ofNullable(
            accountSearchCritera.getNextPage()
        ).filter(
            nextPageToProcess -> !nextPageToProcess.trim().equals(
                ""
            )
        ).map(
            nextPageToProcess -> nextPageBuilder(
                nextPageToProcess, 
                ((DefaultAppService)getAppService()).getPageSizeMaxLimit(),
                Optional.ofNullable(
                    accountSearchCritera.getIgnoreMaxPageSizeLimit()
                ).orElse(
                    Boolean.FALSE
                )
            )
        );
        
        final Slice<Transaction> accountTransactions = findAccountTransactions(
            accountSearchCritera.getTransaction(), 
            getPage
        );
        
        final SimpleDateFormat sdfDate = ((DefaultAppService)getAppService()).getSimpleDateFormat();
        
        final SimpleDateFormat sdfTime = ((DefaultAppService)getAppService()).getSimpleTimeFormat();
        
        return new AccountTransactions().setAccountTransactions(
            Optional.of(
                accountTransactions.hasContent()
            ).filter(
                hasAccountTransactionsToProcess -> hasAccountTransactionsToProcess
            ).map(
                hasAccountTransactionsToProcess -> accountTransactions.getContent().stream().map(
                    accountTransactionToProcess -> new AccountTransaction().setId(
                        accountTransactionToProcess.getId().toString()
                    ).setAccountNumber(
                        accountTransactionToProcess.getAccount().getAccountNumber()
                    ).setCustomerId(
                        accountTransactionToProcess.getAccount().getCustomerId()
                    ).setDescription(
                        accountTransactionToProcess.getDescription()
                    ).setTrxAmount(
                        accountTransactionToProcess.getAmount()
                    ).setTrxDate(
                        sdfDate.format(
                            accountTransactionToProcess.getDate()
                        )
                    ).setTrxTime(
                        sdfTime.format(
                            accountTransactionToProcess.getTime()
                        )
                    )
                ).toList()
            ).orElse(
                new ArrayList<>()
            )
        ).setNextPage(
            Optional.of(
                accountTransactions.hasNext()
            ).filter(
                hasNextAccountTransactionsToProcess -> hasNextAccountTransactionsToProcess
            ).map(
                hasNextAccountTransactionsToProcess -> nextPageBuilder(
                    accountTransactions.nextPageable().getPageNumber(),
                    accountTransactions.nextPageable().getPageSize(),
                    ((DefaultAppService)getAppService()).getPageSizeMaxLimit(),
                    Optional.ofNullable(
                        accountSearchCritera.getIgnoreMaxPageSizeLimit()
                    ).orElse(
                        Boolean.FALSE
                    )
                )
            ).orElse(
                null
            )
        );
    }

    /**
     * Find user account
     * @param account                           Account search criteria (support 
     *                                          {@link Account#customerId}=exact 
     *                                          and {@link Account#accountNumber}=containing). 
     *                                          Must not be {@code null}
     * @return                                  {@link Slice} results containing 
     *                                          the matching {@link Account}(s), 
     *                                          never {@code null}
     * @throws EntityNotFoundException          If the {@link Account} entity 
     *                                          is not found
     * @throws NonUniqueResultException         If the {@link Account} entity 
     *                                          is not unique (try to use exact 
     *                                          for the {@link Account#accountNumber})
     * @since 1.0.0
     */
    @Nonnull
    private Account findUserAccount(@Nonnull
                                    final Account account)
    {
        final Slice<Account> userAccounts = findUserAccounts(
            account,
            Optional.empty()
        );
        // we have no result
        if(!userAccounts.hasContent())
        {
            throw new EntityNotFoundException(
                Optional.ofNullable(
                    account.getCustomerId()
                ).filter(
                    customerIdFilterToProcess -> customerIdFilterToProcess>0
                ).map(
                    customerIdFilterToProcess -> String.format(
                        "Entity `account::customerId` AND `account::accountNumber` NOT FOUND ; expecting object `%s` type by `customerId` field value '%d' AND `accountNumber` field value '%s'",
                        Account.class.getName(),
                        customerIdFilterToProcess,
                        account.getAccountNumber()
                    )
                ).orElseGet(
                    () -> String.format(
                        "Entity `account::accountNumber` NOT FOUND ; expecting object `%s` type by `accountNumber` field value '%s'",
                        Account.class.getName(),
                        account.getAccountNumber()
                    )
                )
            );
        }
        // we have multiple result   
        else if(userAccounts.hasNext() || userAccounts.getContent().size()>1)
        {
            // we have non-unique result
            throw new NonUniqueResultException(
                Optional.ofNullable(
                    account.getCustomerId()
                ).filter(
                    customerIdFilterToProcess -> customerIdFilterToProcess>0
                ).map(
                    customerIdFilterToProcess -> String.format(
                        "Entity `account::customerId` AND `account::accountNumber` NOT UNIQUE ; expecting ONE(1) object `%s` type by `customerId` field value '%d' AND `accountNumber` field value '%s'",
                        Account.class.getName(),
                        customerIdFilterToProcess,
                        account.getAccountNumber()
                    )
                ).orElseGet(
                    () -> String.format(
                        "Entity `account::accountNumber` NOT UNIQUE ; expecting ONE(1) object `%s` type by `accountNumber` field value '%s'",
                        Account.class.getName(),
                        account.getAccountNumber()
                    )
                )
            );
        }
        // ok, seems to be good
        return userAccounts.getContent().get(
            0
        );
    }
    
    /**
     * Find user accounts
     * @param account                           Account search criteria (support 
     *                                          {@link Account#customerId}=exact 
     *                                          and {@link Account#accountNumber}=containing). 
     *                                          Must not be {@code null}
     * @param pageable                          An optional {@link Pageable} OR 
     *                                          it will default. Must not be 
     *                                          {@code null}, use {@link Optional#empty()}
     * @return                                  {@link Slice} results containing 
     *                                          the matching {@link Account}(s), 
     *                                          never {@code null}
     * @since 1.0.0
     */
    @Nonnull
    private Slice<Account> findUserAccounts(@Nonnull
                                            final Account account,
                                            @Nonnull
                                            final Optional<Pageable> pageable)
    {
        // the default is to get ONE(1) item only
        final Pageable pageablePreference = pageable.orElseGet(
            // construct a default
            () -> PageRequest.of(
                0, // first page
                1 // number of results in this page
            )
        );
        return getAccountRepository().findByCustomerIdAndAccountNumberWildcard(
            account.getCustomerId(),
            account.getAccountNumber(),
            pageablePreference
        );
    }
    
    /**
     * Find account transactions
     * @param transaction                       Transaction search criteria (support 
     *                                          {@link Account#customerId}=exact 
     *                                          and {@link Account#accountNumber}=containing). 
     *                                          Must not be {@code null}
     * @param pageable                          An optional {@link Pageable} OR 
     *                                          it will default. Must not be 
     *                                          {@code null}, use {@link Optional#empty()}
     * @return                                  {@link Slice} results containing 
     *                                          the matching {@link Account}(s), 
     *                                          never {@code null}
     * @since 1.0.0
     */
    @Nonnull
    private Slice<Transaction> findAccountTransactions(@Nonnull
                                                       final Transaction transaction,
                                                       @Nonnull
                                                       final Optional<Pageable> pageable)
    {
        // the default is to get ONE(1) item only
        final Pageable pageablePreference = pageable.orElseGet(
            // construct a default
            () -> PageRequest.of(
                0, // first page
                ((DefaultAppService)appService).getPageSizeMaxLimit() // number of results in this page
            )
        );
        log.info(
            "Finding account transaction using pageable page-number {} | page-size {}  --- \n\t{}",
            pageablePreference.getPageNumber(),
            pageablePreference.getPageSize(),
            pageablePreference
        );
        
        return getTransactionRepository().findByCustomerIdOrAccountNumberOrDescriptionWildcard(
            Optional.ofNullable(
                transaction.getAccount()
            ).map(
                transactionAccountToProcess -> transactionAccountToProcess.getCustomerId()
            ).orElseGet(
                () -> {
                    log.warn(
                        "Query parameter 'customerId' IS NULL ; defaulting to 0"
                    );
                    return 0l;
                }
            ),
            Optional.ofNullable(
                transaction.getAccount()
            ).map(
                transactionAccountToProcess -> transactionAccountToProcess.getAccountNumber()
            ).orElseGet(
                () -> {
                    log.warn(
                        "Query parameter 'accountNumber' IS NULL ; defaulting to \"\""
                    );
                    return "";
                }
            ), 
            Optional.ofNullable(
                transaction.getDescription()
            ).orElseGet(
                () -> {
                    log.warn(
                        "Query parameter 'description' IS NULL ; defaulting to \"\""
                    );
                    return "";
                }
            ),
            pageablePreference
        );
    }
    
    /**
     * Check transaction update criteria
     * @param transactionCriteriaToUpdate        Transaction criteria to update
     * @since 1.0.0
     */
    private void checkTransactionUpdateCriteria(@Nullable
                                                final Transaction transactionCriteriaToUpdate)
    {
        Optional.ofNullable(
            transactionCriteriaToUpdate
        ).ifPresentOrElse(
            transactionCriteriaToUpdateToProcess -> {
                try
                {
                    Arrays.stream(
                        transactionCriteriaToUpdateToProcess.getClass().getDeclaredFields()
                    ).forEach(
                        fieldToProcess -> Optional.ofNullable(
                            fieldToProcess.getAnnotation(
                                Column.class
                            )
                        ).ifPresentOrElse(
                            fieldAnnotationToProcess -> Optional.of(
                                fieldAnnotationToProcess.updatable()
                            ).filter(
                                fieldIsNotUpdatableToProcess -> !fieldIsNotUpdatableToProcess
                            ).ifPresent(
                                // the field is marked as not updatable 
                                // ...so lets see if the user had provided 
                                // value for it (if its not primitive)
                                fieldIsNotUpdatableToProcess -> Optional.of(
                                    fieldToProcess.getType().isPrimitive()
                                ).filter(
                                    // primitives always have values, so we 
                                    // can't determine for now ; if this 
                                    // happens...please ensure your objects 
                                    // never use primitives if possible
                                    fieldIsNotPrimitiveToProcess -> !fieldIsNotPrimitiveToProcess
                                ).ifPresent(
                                    fieldIsNotPrimitiveToProcess -> {
                                        try
                                        {
                                            fieldToProcess.setAccessible(
                                                true
                                            );

                                            if(Objects.nonNull(
                                                fieldToProcess.get(
                                                    transactionCriteriaToUpdateToProcess
                                                )
                                            ))
                                            {
                                                throw new BadParameterException(
                                                    String.format(
                                                        "Transaction criteria detail '%s' to update IS NOT PERMITTED",
                                                        fieldToProcess.getName()
                                                    )
                                                );
                                            }
                                        }
                                        catch(BadParameterException e)
                                        {
                                            throw e;
                                        }
                                        catch(Exception e)
                                        {
                                            log.debug(
                                                "Get field `{}` value ENCOUNTERED FAILURE ; {}",
                                                fieldToProcess,
                                                e.getMessage(),
                                                e
                                            );
                                        }
                                    }
                                )
                            ),
                            () -> log.debug(
                                "Ignoring field `{}` NOT ANNOTATED WITH `{}` TYPE",
                                fieldToProcess,
                                Column.class.getName()
                            )
                        )
                    );
                }
                catch(Exception e)
                {
                    throw new InternalException(
                        String.format(
                            "Transaction criteria details to update CHECK FAILURE ; %s",
                            e.getMessage()
                        ),
                        e 
                    );
                }
            },
            // nothing to update...missing this instruction what to update
            () -> {
                throw new BadParameterException(
                    "No transaction criteria detail field(s) to update"
                );
            }
        );
    }
}
