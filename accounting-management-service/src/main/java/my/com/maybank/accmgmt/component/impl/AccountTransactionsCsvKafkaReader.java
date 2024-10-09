package my.com.maybank.accmgmt.component.impl;

import jakarta.annotation.Nonnull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import my.com.maybank.accmgmt.configuration.DefaultAppService;
import my.com.maybank.accmgmt.models.AccountTransaction;
import my.com.maybank.accmgmt.models.AccountTransactionsBatchJob;
import my.com.maybank.accmgmt.models.AccountTransactionsCsvFields;
import my.com.maybank.core.configuration.service.AppService;
import my.com.maybank.core.exception.InternalException;
import org.springframework.stereotype.Component;
import org.supercsv.io.CsvBeanReader;
import org.supercsv.prefs.CsvPreference;
import my.com.maybank.accmgmt.component.CsvMessageReader;

/**
 * {@link my.com.maybank.accmgmt.models.AccountTransaction} CSV reader
 * @since 1.0.0
 * @see my.com.maybank.accmgmt.component.impl.AccountTransactionsCsvConsumer
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
public class AccountTransactionsCsvKafkaReader
       implements CsvMessageReader<AccountTransactionsBatchJob.Operation, AccountTransactionsBatchJob>
{
    @NonNull
    @Nonnull
    private final AppService appService;
    
    /**
     * Read the CSV file and perform the callback for the converted line to 
     * object {@link my.com.maybank.accmgmt.models.AccountTransactions} type
     * @param filePath                          CSV file path to read/process. 
     *                                          Must not be {@code null} or 
     *                                          blank/empty
     * @param operation                         Operation to act on
     * @param consume                           Callback operation. Must not be 
     *                                          {@code null}
     * @return                                  Total number of CSV lines read
     * @since 1.0.0
     */
    @Override
    public long readAndCallback(@Nonnull
                                @NotBlank
                                @NotEmpty
                                final String filePath, 
                                @Nonnull
                                final AccountTransactionsBatchJob.Operation operation,
                                @Nonnull
                                final Consumer<AccountTransactionsBatchJob> consume)
    {
        final AtomicInteger processedLinesCount = new AtomicInteger();
        try(CsvBeanReader beanReader = new CsvBeanReader(
            new InputStreamReader(
                new FileInputStream(
                    filePath
                )
            ), 
            new CsvPreference.Builder(
                '"', 
                '|', 
                "\r\n"
            ).build()
        ))
        {
            // consume the header line (assuming we'll have the first line as 
            // header as a practise or standard)
            beanReader.getHeader(
                true
            );
            
            final Integer batchSize = ((DefaultAppService)appService).getAccountingKafkaTransactionJobBatchSize();
            final AtomicReference<AccountTransactionsBatchJob> accountTransactionsBatchJob = new AtomicReference<>(
                new AccountTransactionsBatchJob().setBatchId(
                    UUID.randomUUID().toString()
                ).setBatchSize(
                    batchSize
                ).setOperation(
                    operation
                ).setBatchAccountTransaction(
                    new ArrayList<>()
                )
            );
            int i = 0;
            AccountTransaction accountTransaction;
            while(Objects.nonNull(
                accountTransaction = beanReader.read(
                    AccountTransaction.class,
                    AccountTransactionsCsvFields.getFieldMappings(), 
                    AccountTransactionsCsvFields.getCellProcessors()
                )
            ))
            {
                if(i>0 && i%batchSize==0)
                {
                    consume.accept(
                        accountTransactionsBatchJob.get()
                    );
                    
                    accountTransactionsBatchJob.set(
                        new AccountTransactionsBatchJob().setBatchId(
                            UUID.randomUUID().toString()
                        ).setBatchSize(
                            batchSize
                        ).setOperation(
                            operation
                        ).setBatchAccountTransaction(
                            new ArrayList<>()
                        )
                    );
                }
                
                accountTransactionsBatchJob.get().getBatchAccountTransaction().add(
                    accountTransaction
                );
                
                processedLinesCount.incrementAndGet();
                
                i++;
            }
            if(Objects.nonNull(accountTransactionsBatchJob.get()) && 
                !accountTransactionsBatchJob.get().getBatchAccountTransaction().isEmpty())
            {
                consume.accept(
                    accountTransactionsBatchJob.get()
                );
            }
            accountTransactionsBatchJob.set(
                null
            );
            
            log.info("Read CSV file '{}' COMPLETED ; number of ({}) lines processed",
                filePath,
                processedLinesCount.get()
            );
        }
        catch(Exception e)
        {
            log.error("Error while reading file: " +  filePath, e);
            throw new InternalException(
                String.format("Read CSV file '%s' ENCOUNTERED FAILURE ; %s, last line read (%d)",
                    filePath,
                    e.getMessage(),
                    processedLinesCount.get()
                ),
                e
            );
        }
        return processedLinesCount.get();
    }
}
