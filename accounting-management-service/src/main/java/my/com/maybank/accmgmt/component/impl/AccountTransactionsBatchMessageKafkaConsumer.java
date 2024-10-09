package my.com.maybank.accmgmt.component.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Nonnull;
import java.lang.reflect.Type;
import java.util.concurrent.atomic.AtomicReference;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import my.com.maybank.accmgmt.component.MessageConsumer;
import my.com.maybank.accmgmt.configuration.DefaultAppService;
import my.com.maybank.accmgmt.models.AccountTransactionsBatchJob;
import my.com.maybank.accmgmt.service.AccountingService;
import my.com.maybank.core.configuration.service.AppService;
import my.com.maybank.core.exception.InternalException;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.stereotype.Component;

/**
 * {@link my.com.maybank.accmgmt.models.AccountTransactionsBatchJob} message 
 * consumer 
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
public class AccountTransactionsBatchMessageKafkaConsumer
       implements MessageConsumer<String>,
                  MessageListener<String, String> 
{
    @NonNull
    @Nonnull
    private final AppService appService;
    
    @NonNull
    @Nonnull
    private final ObjectMapper objectMapper;
    
    @NonNull
    @Nonnull
    private final AccountingService accountingService;
    
    @Override
    public void onMessage(final ConsumerRecord<String, String> data)
    {
        log.debug(
            "Received consumer record data SUCCESS ; \n\tkey={} \n\tvalue={}",
            data.key(),
            data.value()
        );
        listenMessage(
            data.value()
        );
    }
    
    @Override
    public void listenMessage(final String message)
    {
        log.info(
            "Kafka endpoint {} consuming ...\n\t{}",
            ((DefaultAppService)appService).getAccountingKafkaEndpoint(),
            message
        );
        
        final AtomicReference<AccountTransactionsBatchJob> accountTransactionsBatchJobAsType = new AtomicReference<>();
        try
        {
            accountTransactionsBatchJobAsType.set(
                objectMapper.readValue(
                    message,
                    new TypeReference<AccountTransactionsBatchJob>() {
                        @Override
                        public Type getType()
                        {
                            return AccountTransactionsBatchJob.class;
                        }
                    }
                )
            );
            
            final long completedBatchProcess = accountingService.batchConsumerOperationAccountTransactions(
                accountTransactionsBatchJobAsType.get()
            );
            log.info(
                "Account transaction batch job ID({}) operation ({}) COMPLETED ; total no. of ITEMS PROCESSED({})",
                accountTransactionsBatchJobAsType.get().getBatchId(),
                accountTransactionsBatchJobAsType.get().getOperation(),
                completedBatchProcess
            );
        }
        catch(Exception e)
        {
            log.error(
                "Account transaction batch job listen from {} FAILURE ; {}, payload ---\n\t{}", 
                ((DefaultAppService)appService).getAccountingKafkaEndpoint(),
                e.getMessage(),
                message,
                e
            );
            throw new InternalException(
                String.format(
                    "Account transaction batch job listen from %s FAILURE ; %s",
                    ((DefaultAppService)appService).getAccountingKafkaEndpoint(),
                    e.getMessage()
                ),
                e
            );
        }
    }
}
