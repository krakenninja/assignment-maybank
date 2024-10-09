package my.com.maybank.accmgmt.component.impl;

import jakarta.annotation.Nonnull;
import java.util.List;
import java.util.function.Consumer;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import my.com.maybank.accmgmt.component.MessageReader;
import my.com.maybank.accmgmt.models.AccountTransactionCriteria;
import my.com.maybank.accmgmt.models.AccountTransactionsBatchJob;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Account transaction criteria Kafka message reader delegate to resolve cyclic 
 * dependency
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
public class AccountTransactionsCriteriaKafkaMessageReaderDelegate
       implements MessageReader<AccountTransactionCriteria, AccountTransactionsBatchJob, List<AccountTransactionsBatchJob>>
{
    @Nonnull
    @NonNull
    private final ApplicationContext applicationContext;
    
    @Override
    public List<AccountTransactionsBatchJob> readAndCallback(final AccountTransactionCriteria instruction, 
                                                             final Consumer<AccountTransactionsBatchJob> consume)
    {
        return getApplicationContext().getBean(
            AccountTransactionsCriteriaKafkaMessageReader.class
        ).readAndCallback(
            instruction, 
            consume
        );
    }
}
