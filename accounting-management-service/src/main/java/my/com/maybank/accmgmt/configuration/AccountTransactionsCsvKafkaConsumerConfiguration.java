package my.com.maybank.accmgmt.configuration;

import jakarta.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import my.com.maybank.accmgmt.component.MessageConsumer;
import my.com.maybank.core.configuration.service.AppService;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;

/**
 * Configuration for accounting Kafka consumer
 * @since 1.0.0
 * @author ChristopherCKW
 */
@Configuration
public class AccountTransactionsCsvKafkaConsumerConfiguration
{
    @Bean
    public ConsumerFactory<String, String> consumerFactory(@Nonnull
                                                           final AppService appService)
    {
        final Map<String, Object> configProps = new HashMap<>();
        configProps.put(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
            ((DefaultAppService)appService).getAccountingKafkaEndpoint()
        );
        configProps.put(
            ConsumerConfig.GROUP_ID_CONFIG,
            ((DefaultAppService)appService).getAccountingKafkaConsumerGroupId()
        );
        configProps.put(
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
            StringDeserializer.class
        );
        configProps.put(
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
            StringDeserializer.class
        );
        return new DefaultKafkaConsumerFactory<>(
            configProps
        );
    }
    
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(@Nonnull
                                                                                                 final AppService appService)
    {
        final ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(
            consumerFactory(
                appService
            )
        );
        return factory;
    }
    
    @Bean
    public ConcurrentMessageListenerContainer<String, String> messageListenerContainer(@Nonnull
                                                                                       final AppService appService,
                                                                                       @Nonnull
                                                                                       final MessageConsumer messageConsumer)
    {
        final ConcurrentMessageListenerContainer<String, String> messageListenerContainer = kafkaListenerContainerFactory(
            appService
        ).createContainer(
            ((DefaultAppService)appService).getAccountingKafkaTopic()
        );
        messageListenerContainer.setupMessageListener(
            messageConsumer
        );
        return messageListenerContainer;
    }
}
