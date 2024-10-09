package my.com.maybank.accmgmt.component.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Nonnull;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import my.com.maybank.accmgmt.configuration.DefaultAppService;
import my.com.maybank.accmgmt.models.AccountTransaction;
import my.com.maybank.core.configuration.service.AppService;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.stereotype.Component;
import my.com.maybank.accmgmt.component.MessageProducer;
import my.com.maybank.accmgmt.models.AccountTransactionsBatchJob;
import my.com.maybank.core.exception.InternalException;

/**
 * {@link my.com.maybank.accmgmt.models.AccountTransactionsBatchJob} message 
 * producer 
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
public class AccountTransactionsBatchMessageKafkaProducer
       implements MessageProducer<AccountTransactionsBatchJob>
{
    @NonNull
    @Nonnull
    private final AppService appService;
    
    @NonNull
    @Nonnull
    private final ObjectMapper objectMapper;
    
    @Getter(
        AccessLevel.PROTECTED
    )
    @Setter(
        AccessLevel.PROTECTED
    )
    @Nonnull
    private KafkaTemplate<String, String> kafkaTemplate;
    
    @PostConstruct
    protected void init()
    {
        this.kafkaTemplate = new KafkaTemplate<>(
            producerFactory()
        );
        log.info(
            "Kafka producer INITIALIZED"
        );
    }
    
    @PreDestroy
    protected void destroy()
    {
        getKafkaTemplate().destroy();
        log.info(
            "Kafka producer DESTROYED"
        );
    }
    
    protected ProducerFactory<String, String> producerFactory()
    {
        final Map<String, Object> configProps = new HashMap<>();
        configProps.put(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
            ((DefaultAppService)appService).getAccountingKafkaEndpoint()
        );
        configProps.put(
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
            StringSerializer.class
        );
        configProps.put(
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
            StringSerializer.class
        );
        return new DefaultKafkaProducerFactory<>(
            configProps
        );
    }
    
    @Override
    public void sendMessage(@Nonnull
                            final AccountTransactionsBatchJob accountTransactionsBatchJob)
    {
        log.debug(
            "Kafka endpoint {} consuming ...\n\t{}",
            ((DefaultAppService)appService).getAccountingKafkaEndpoint(),
            accountTransactionsBatchJob
        );
        
        final AtomicReference<String> accountTransactionsBatchJobAsJson = new AtomicReference<>();
        final String kafkaTopic = ((DefaultAppService)appService).getAccountingKafkaTopic();
        try
        {
            accountTransactionsBatchJobAsJson.set(
                objectMapper.writeValueAsString(
                    accountTransactionsBatchJob
                )
            );
            getKafkaTemplate().send(
                kafkaTopic, 
                accountTransactionsBatchJobAsJson.get()
            );
            log.info(
                "Account transaction batch job sent to {} SUCCESS ; payload ---\n\t{}", 
                ((DefaultAppService)appService).getAccountingKafkaEndpoint(),
                accountTransactionsBatchJobAsJson.get()
            );
            accountTransactionsBatchJobAsJson.set(
                null
            );
        }
        catch(Exception e)
        {
            log.error(
                "Account transaction batch job sent to {} FAILURE ; {}, payload ---\n\t{}", 
                ((DefaultAppService)appService).getAccountingKafkaEndpoint(),
                e.getMessage(),
                accountTransactionsBatchJobAsJson.get(),
                e
            );
            throw new InternalException(
                String.format(
                    "Account transaction batch job sent to %s FAILURE ; %s",
                    ((DefaultAppService)appService).getAccountingKafkaEndpoint(),
                    e.getMessage()
                ),
                e
            );
        }
    }
}
