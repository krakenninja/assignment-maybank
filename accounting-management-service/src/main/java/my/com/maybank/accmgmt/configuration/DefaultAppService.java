package my.com.maybank.accmgmt.configuration;

import jakarta.annotation.Nonnull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import my.com.maybank.core.configuration.service.AppService;
import my.com.maybank.core.exception.InternalException;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesBindException;
import org.springframework.context.annotation.Configuration;

/**
 * Default application security configuration
 * @since 1.0.0
 * @author ChristopherCKW
 */
@Accessors(
    fluent = false,
    chain = true
)
@Getter
@Setter
@Configuration
@ConfigurationProperties(
    prefix = "app.service"
)
public class DefaultAppService
       extends AppService
{
    /**
     * Default simple date format pattern
     * @since 1.0.0
     */
    public static final String DEFAULT_SIMPLE_DATE_FORMAT_PATTERN = "yyyy-MM-dd";
    
    /**
     * Default simple time format pattern
     * @since 1.0.0
     */
    public static final String DEFAULT_SIMPLE_TIME_FORMAT_PATTERN = "hh:mm:ss";
    
    /**
     * Default page size max limit (hard limit) for security sake
     * @since 1.0.0
     */
    public static final int DEFAULT_PAGESIZE_MAX_LIMIT = 50;
    
    /**
     * Default simple date format {@link SimpleDateFormat}
     * @since 1.0.0
     */
    public static final SimpleDateFormat DEFAULT_SIMPLE_DATE_FORMAT = new SimpleDateFormat(
        DEFAULT_SIMPLE_DATE_FORMAT_PATTERN
    );
    
    /**
     * Default simple time format {@link SimpleDateFormat}
     * @since 1.0.0
     */
    public static final SimpleDateFormat DEFAULT_SIMPLE_TIME_FORMAT = new SimpleDateFormat(
        DEFAULT_SIMPLE_TIME_FORMAT_PATTERN
    );
    
    /**
     * Default simple date format {@link DateTimeFormatter}
     * @since 1.0.0
     */
    public static final DateTimeFormatter DEFAULT_SIMPLE_DATE_FORMATTER = DateTimeFormatter.ofPattern(
        DEFAULT_SIMPLE_DATE_FORMAT_PATTERN
    );
    
    /**
     * Default simple time format {@link DateTimeFormatter}
     * @since 1.0.0
     */
    public static final DateTimeFormatter DEFAULT_SIMPLE_TIME_FORMATTER = DateTimeFormatter.ofPattern(
        DEFAULT_SIMPLE_TIME_FORMAT_PATTERN
    );
    
    /**
     * Default accounting Kafka topic
     * @since 1.0.0
     */
    public static final String DEFAULT_ACCOUNTING_KAFKA_TOPIC = "test";
    
    /**
     * Default accounting Kafka consumer group-id
     * @since 1.0.0
     */
    public static final String DEFAULT_ACCOUNTING_KAFKA_CONSUMER_GROUPID = "consumer-persist";
    
    /**
     * Default accounting Kafka job batch size
     * @since 1.0.0
     */
    public static final Integer DEFAULT_ACCOUNTING_KAFKA_JOB_BATCH_SIZE = 10;
    
    /**
     * Accounting configuration
     * @since 1.0.0
     */
    @Nonnull
    private Accounting accounting;
    
    /**
     * Get page size max limit allowed by for any service that supports 
     * pagination
     * @return                                  A non-negative, non-zero page 
     *                                          size max limit
     * @since 1.0.0
     */
    @Nonnull
    public Integer getPageSizeMaxLimit()
    {
        return Optional.ofNullable(
            getDataLimiter()
        ).map(
            dataLimiterToProcess -> Optional.ofNullable(
                dataLimiterToProcess.getPagination()
            ).map(
                paginationLimiterToProcess -> Optional.ofNullable(
                    paginationLimiterToProcess.getPageSize()
                ).filter(
                    pageSizeMaxLimitToProcess -> pageSizeMaxLimitToProcess>0
                ).orElse(
                    DEFAULT_PAGESIZE_MAX_LIMIT
                )
            ).orElse(
                DEFAULT_PAGESIZE_MAX_LIMIT
            )
        ).orElse(
            DEFAULT_PAGESIZE_MAX_LIMIT
        );
    }
    
    /**
     * Get simple date format
     * @return                                  {@link SimpleDateFormat}, never 
     *                                          {@code null}
     * @since 1.0.0
     */
    @Nonnull
    public SimpleDateFormat getSimpleDateFormat()
    {
        return new SimpleDateFormat(
            Optional.ofNullable(
                getDataFormatting()
            ).map(dataFormattingToProcess -> Optional.ofNullable(
                    dataFormattingToProcess.getDate()
                ).map(dateFormattingToProcess -> Optional.ofNullable(
                        dateFormattingToProcess.getSimple()
                    ).filter(
                        simpleDateFormatToProcess -> !simpleDateFormatToProcess.trim().equals(
                            ""
                        )
                    ).orElse(DEFAULT_SIMPLE_DATE_FORMAT_PATTERN
                    )
                ).orElse(DEFAULT_SIMPLE_DATE_FORMAT_PATTERN
                )
            ).orElse(DEFAULT_SIMPLE_DATE_FORMAT_PATTERN
            )
        );
    }
    
    /**
     * Get simple time format
     * @return                                  {@link SimpleDateFormat}, never 
     *                                          {@code null}
     * @since 1.0.0
     */
    @Nonnull
    public SimpleDateFormat getSimpleTimeFormat()
    {
        return new SimpleDateFormat(
            Optional.ofNullable(
                getDataFormatting()
            ).map(dataFormattingToProcess -> Optional.ofNullable(
                    dataFormattingToProcess.getTime()
                ).map(timeFormattingToProcess -> Optional.ofNullable(
                        timeFormattingToProcess.getSimple()
                    ).filter(
                        simpleTimeFormatToProcess -> !simpleTimeFormatToProcess.trim().equals(
                            ""
                        )
                    ).orElse(DEFAULT_SIMPLE_TIME_FORMAT_PATTERN
                    )
                ).orElse(DEFAULT_SIMPLE_TIME_FORMAT_PATTERN
                )
            ).orElse(DEFAULT_SIMPLE_TIME_FORMAT_PATTERN
            )
        );
    }
    
    /**
     * Get upload destination base path
     * @return                                  {@link Path}, never {@code null}
     * @since 1.0.0
     */
    @Nonnull
    public Path getUploadDestBasePath()
    {
        return Paths.get(
            Optional.ofNullable(
                getDataPath()
            ).map(
                dataPathToProcess -> Optional.ofNullable(
                    dataPathToProcess.getUpload()
                ).map(
                    uploadToProcess -> Optional.ofNullable(
                        uploadToProcess.getDestBasePath()
                    ).filter(
                        destBasePathToProcess -> !destBasePathToProcess.trim().equals(
                            ""
                        )
                    ).orElse(
                        System.getProperty(
                            "java.io.tmpdir"
                        )
                    )
                ).orElse(
                    System.getProperty(
                        "java.io.tmpdir"
                    )
                )
            ).orElse(
                System.getProperty(
                    "java.io.tmpdir"
                )
            ).replaceAll(
                "\\\\", 
                "/"
            )
        );
    }
    
    /**
     * Get Accounting Kafka topic
     * @return                                  Accounting Kafka endpoint
     * @throws InternalException                If configuration is missing or 
     *                                          invalid
     * @since 1.0.0
     */
    @Nonnull
    public String getAccountingKafkaEndpoint()
    {
        return Optional.ofNullable(
            getAccounting()
        ).map(
            accountingToProcess -> Optional.ofNullable(
                accountingToProcess.getKafka()
            ).map(
                kafkaToProcess -> Optional.ofNullable(
                    kafkaToProcess.getEndpoint()
                ).filter(
                    endpointToProcess -> !endpointToProcess.trim().equals(
                        ""
                    )
                ).orElse(
                    null
                )
            ).orElse(
                null
            )
        ).orElseThrow(
            () -> new InternalException(
                "Configuration for property 'app.service.accounting.kafka.endpoint' IS MISSING OR INVALID"
            )
        );
    }
    
    /**
     * Get Accounting Kafka topic
     * @return                                  Topic name
     * @since 1.0.0
     */
    @Nonnull
    public String getAccountingKafkaTopic()
    {
        return Optional.ofNullable(
            getAccounting()
        ).map(
            accountingToProcess -> Optional.ofNullable(
                accountingToProcess.getKafka()
            ).map(
                kafkaToProcess -> Optional.ofNullable(
                    kafkaToProcess.getTopic()
                ).filter(
                    topicToProcess -> !topicToProcess.trim().equals(
                        ""
                    )
                ).orElse(
                    DEFAULT_ACCOUNTING_KAFKA_TOPIC
                )
            ).orElse(
                DEFAULT_ACCOUNTING_KAFKA_TOPIC
            )
        ).orElse(
            DEFAULT_ACCOUNTING_KAFKA_TOPIC
        );
    }
    
    /**
     * Get Accounting Kafka consumer group-id
     * @return                                  Group-Id
     * @since 1.0.0
     */
    @Nonnull
    public String getAccountingKafkaConsumerGroupId()
    {
        return Optional.ofNullable(
            getAccounting()
        ).map(accountingToProcess -> Optional.ofNullable(
                accountingToProcess.getKafka()
            ).map(kafkaToProcess -> Optional.ofNullable(
                    kafkaToProcess.getConsumerGroupId()
                ).filter(
                    consumerGroupIdToProcess -> !consumerGroupIdToProcess.trim().equals(
                        ""
                    )
                ).orElse(
                    DEFAULT_ACCOUNTING_KAFKA_CONSUMER_GROUPID
                )
            ).orElse(
                DEFAULT_ACCOUNTING_KAFKA_CONSUMER_GROUPID
            )
        ).orElse(
            DEFAULT_ACCOUNTING_KAFKA_CONSUMER_GROUPID
        );
    }
    
    /**
     * Get Accounting Kafka transaction job batch size
     * @return                                  Batch size
     * @since 1.0.0
     */
    @Nonnull
    public Integer getAccountingKafkaTransactionJobBatchSize()
    {
        return Optional.ofNullable(
            getAccounting()
        ).map(accountingToProcess -> Optional.ofNullable(
                accountingToProcess.getKafka()
            ).map(kafkaToProcess -> Optional.ofNullable(
                    kafkaToProcess.getTransactionJobBatchSize()
                ).filter(
                    transactionJobBatchSizeToProcess -> transactionJobBatchSizeToProcess>0
                ).orElse(
                    DEFAULT_ACCOUNTING_KAFKA_JOB_BATCH_SIZE
                )
            ).orElse(
                DEFAULT_ACCOUNTING_KAFKA_JOB_BATCH_SIZE
            )
        ).orElse(
            DEFAULT_ACCOUNTING_KAFKA_JOB_BATCH_SIZE
        );
    }
    
    /**
     * Accounting configuration
     * @since 1.0.0
     */
    @Accessors(
        fluent = false,
        chain = true
    )
    @Getter
    @Setter
    public static class Accounting
    {
        @Nonnull
        private Kafka kafka;
    }
    
    /**
     * Kafka accounting service configuration
     * @since 1.0.0
     */
    @Accessors(
        fluent = false,
        chain = true
    )
    @Getter
    @Setter
    public static class Kafka
    {
        @Nonnull
        @NotBlank
        @NotEmpty
        private String endpoint;
        
        @Nonnull
        @NotBlank
        @NotEmpty
        private String topic;
        
        @Nonnull
        @NotBlank
        @NotEmpty
        private String consumerGroupId;
        
        @Nonnull
        @Min(
            1l
        )
        private Integer transactionJobBatchSize;
    }
}
