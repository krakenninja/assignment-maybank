package my.com.maybank.core.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import my.com.maybank.core.exception.InternalException;
import my.com.maybank.core.exception.ResourceNotFoundException;

/**
 * Message model
 * @since 1.0.0
 * @author ChristopherCKW
 */
public record MessageModel(String key, Serializable value)
{
    /**
     * Property key message {@code type}
     * @since 1.0.0
     * @see MessageModel.MessageType
     */
    public static final String MESSAGE_TYPE = "type";
    
    /**
     * Property key message {@code code}
     * <p>
     * Usually a number value
     * </p>
     * @since 1.0.0
     */
    public static final String MESSAGE_CODE = "code";
    
    /**
     * Property key message {@code desc} as the detail description
     * @since 1.0.0
     */
    public static final String MESSAGE_DESC = "desc";
    
    /**
     * Basic error message with description and code
     * @param desc                              Error message. Must not be 
     *                                          {@code null}
     * @param code                              Error code. Must not be 
     *                                          {@code null}
     * @return                                  {@link MessageModelResponse}, 
     *                                          never {@code null}
     * @since 1.0.0
     */
    @Nonnull
    public static MessageModelResponse errorMessage(@Nonnull
                                                    final String desc,
                                                    @Nonnull
                                                    final Long code)
    {
        return new MessageModelResponse(
            Stream.of(
                messageType(
                    MessageType.ERROR
                ),
                messageDescription(
                    desc
                ),
                messageCode(
                    Optional.ofNullable(
                        code
                    ).orElseThrow(
                        () -> new IllegalArgumentException(
                            "Error message `code` IS REQUIRED"
                        )
                    )
                )
            ).collect(
                Collectors.toCollection(
                    ArrayList::new
                )
            )
        ).initialize();
    }
    
    /**
     * Basic warn message with description or code
     * @param desc                              Warning message. Must not be 
     *                                          {@code null}
     * @return                                  {@link MessageModelResponse}, 
     *                                          never {@code null}
     * @since 1.0.0
     */
    @Nonnull
    public static MessageModelResponse warnMessage(@Nonnull
                                                   final String desc)
    {
        return warnMessage(
            desc,
            null
        );
    }
    
    /**
     * Basic warn message with description or code
     * @param desc                              Warning message. Must not be 
     *                                          {@code null}
     * @param code                              Optional warning code
     * @return                                  {@link MessageModelResponse}, 
     *                                          never {@code null}
     * @since 1.0.0
     */
    @Nonnull
    public static MessageModelResponse warnMessage(@Nonnull
                                                   final String desc,
                                                   @Nullable
                                                   final Long code)
    {
        return new MessageModelResponse(
            Stream.of(
                messageType(
                    MessageType.WARN
                ),
                messageDescription(
                    desc
                ),
                Optional.ofNullable(
                    code
                ).map(
                    codeToProcess -> messageCode(
                        codeToProcess
                    )
                ).orElse(
                    null
                )
            ).filter(
                messageModelToProcess -> Objects.nonNull(
                    messageModelToProcess
                )
            ).collect(
                Collectors.toCollection(
                    ArrayList::new
                )
            )
        ).initialize();
    }
    
    /**
     * Basic info message with description or code
     * @param desc                              Information message. Must not be 
     *                                          {@code null}
     * @return                                  {@link MessageModelResponse}, 
     *                                          never {@code null}
     * @since 1.0.0
     */
    @Nonnull
    public static MessageModelResponse infoMessage(@Nonnull
                                                   final String desc)
    {
        return infoMessage(
            desc,
            null
        );
    }
    
    /**
     * Basic info message with description or code
     * @param desc                              Information message. Must not be 
     *                                          {@code null}
     * @param code                              Optional information code
     * @return                                  {@link MessageModelResponse}, 
     *                                          never {@code null}
     * @since 1.0.0
     */
    @Nonnull
    public static MessageModelResponse infoMessage(@Nonnull
                                                   final String desc,
                                                   @Nullable
                                                   final Long code)
    {
        return new MessageModelResponse(
            Stream.of(
                messageType(
                    MessageType.INFO
                ),
                messageDescription(
                    desc
                ),
                Optional.ofNullable(
                    code
                ).map(
                    codeToProcess -> messageCode(
                        codeToProcess
                    )
                ).orElse(
                    null
                )
            ).filter(
                messageModelToProcess -> Objects.nonNull(
                    messageModelToProcess
                )
            ).collect(
                Collectors.toCollection(
                    ArrayList::new
                )
            )
        ).initialize();
    }
    
    /**
     * Message type
     * @param messageType                       Message type. Must not be 
     *                                          {@code null}
     * @return                                  {@link MessageModel} message 
     *                                          type
     * @since 1.0.0
     */
    @Nonnull
    protected static MessageModel messageType(@Nonnull
                                              final MessageType messageType)
    {
        return new MessageModel(
            MESSAGE_TYPE, 
            messageType
        );
    }
    
    /**
     * Message code
     * @param messageCode                       Message code. Must not be 
     *                                          {@code null}
     * @return                                  {@link MessageModel} message 
     *                                          code
     * @since 1.0.0
     */
    @Nonnull
    protected static MessageModel messageCode(@Nonnull
                                              final Long messageCode)
    {
        return new MessageModel(
            MESSAGE_CODE, 
            messageCode
        );
    }
    
    /**
     * Message description
     * @param messageDesc                       Message description. Must not be 
     *                                          {@code null}
     * @return                                  {@link MessageModel} message 
     *                                          description
     * @since 1.0.0
     */
    @Nonnull
    protected static MessageModel messageDescription(@Nonnull
                                                     final String messageDesc)
    {
        return new MessageModel(
            MESSAGE_DESC, 
            messageDesc
        );
    }
    
    /**
     * Message type
     * @since 1.0.0
     */
    public static enum MessageType
    {
        /**
         * Error message type (recommended include {@link #MESSAGE_CODE})
         * @since 1.0.0
         */
        ERROR,
        
        /**
         * Warning message type (may include {@link #MESSAGE_CODE})
         * @since 1.0.0
         */
        WARN,
        
        /**
         * Info message type (may include {@link #MESSAGE_CODE})
         * @since 1.0.0
         */
        INFO
    }
    
    /**
     * Message model response
     * @since 1.0.0
     */
    @RequiredArgsConstructor(
        access = AccessLevel.PROTECTED
    )
    public static final class MessageModelResponse
           extends LinkedHashMap<String, Serializable>
    {
        @JsonIgnore
        @NonNull
        @Nonnull
        private final List<MessageModel> messageModels;
        
        /**
         * Initialize this {@link MessageModelResponse} with the 
         * {@link #messageModels} data
         * @return                              Self-reference of this 
         *                                      {@link MessageModelResponse} 
         *                                      object
         * @since 1.0.0
         */
        private MessageModelResponse initialize()
        {
            messageModels.stream().filter(
                messageModelToProcess -> Objects.nonNull(
                    messageModelToProcess.key()
                )
            ).filter(
                messageModelToProcess -> Objects.nonNull(
                    messageModelToProcess.value()
                )
            ).forEach(
                messageModelToProcess -> put(
                    messageModelToProcess.key(), 
                    messageModelToProcess.value()
                )
            );
            
            return this;
        }
        
        /**
         * Get code
         * @return                              Optional containing the 
         *                                      {@code code} value if present
         * @since 1.0.0
         */
        @Nonnull
        public Optional<Long> getCode()
        {
            return toList().stream().filter(
                messageModelToProcess -> messageModelToProcess.key().equals(
                    MESSAGE_CODE
                )
            ).map(
                messageModelToProcess -> (Long)messageModelToProcess.value()
            ).findFirst();
        }
        
        /**
         * Get description
         * @return                              Optional containing the 
         *                                      {@code description} value if 
         *                                      present
         * @since 1.0.0
         */
        @Nonnull
        public Optional<String> getDescription()
        {
            return toList().stream().filter(
                messageModelToProcess -> messageModelToProcess.key().equals(
                    MESSAGE_DESC
                )
            ).map(
                messageModelToProcess -> (String)messageModelToProcess.value()
            ).findFirst();
        }
        
        /**
         * Get type
         * @return                              Optional containing the 
         *                                      {@link MessageType} value if 
         *                                      present
         * @since 1.0.0
         */
        @Nonnull
        public MessageType getType()
        {
            return toList().stream().filter(
                messageModelToProcess -> messageModelToProcess.key().equals(
                    MESSAGE_TYPE
                )
            ).map(
                messageModelToProcess -> (MessageType)messageModelToProcess.value()
            ).findFirst().orElseThrow(
                () -> new ResourceNotFoundException(
                    String.format(
                        "Message '%s' record property IS NOT FOUND",
                        MESSAGE_TYPE
                    )
                )
            );
        }
        
        /**
         * Converts this object to a {@link List} representation
         * @return                              {@link List} representation of 
         *                                      this model
         * @since 1.0.0 
         */
        public List<MessageModel> toList()
        {
            return entrySet().stream().filter(
                entryToProcess -> Objects.nonNull(
                    entryToProcess.getKey()
                )
            ).filter(
                entryToProcess -> Objects.nonNull(
                    entryToProcess.getValue()
                )
            ).map(
                entryToProcess -> new MessageModel(
                    entryToProcess.getKey(), 
                    entryToProcess.getValue()
                )
            ).toList();
        }
        
        /**
         * Converts this object to a {@link Map} representation
         * @return                              {@link Map} representation of 
         *                                      this model
         * @since 1.0.0 
         */
        @Nonnull
        public Map<String, Serializable> toMap()
        {
            return toList().stream().filter(
                messageModelToProcess -> Objects.nonNull(
                    messageModelToProcess.key()
                )
            ).filter(
                messageModelToProcess -> Objects.nonNull(
                    messageModelToProcess.value()
                )
            ).collect(
                Collectors.toMap(
                    MessageModel::key,
                    MessageModel::value
                )
            );
        }
        
        /**
         * Converts this object to a {@code JSON} formatted string representation
         * @return                              {@code JSON} string representation 
         *                                      of this model
         * @since 1.0.0 
         */
        @Nonnull
        public String toJson()
        {
            final ObjectMapper om = new JsonMapper();
            try
            {
                return om.writeValueAsString(
                    this
                );
            }
            catch(Exception e)
            {
                throw new InternalException(
                    String.format(
                        "Serialize to JSON string using mapper `%s` type ENCOUNTERED FAILURE ; %s",
                        om.getClass().getName(),
                        e.getMessage()
                    ),
                    e
                );
            }
        }
    }
    
    /**
     * Equal implementation by comparing the {@link MessageModel#key}
     * @param other                             Other object to check equality
     * @return                                  {@code true} if equals, 
     *                                          {@code false} otherwise
     * @since 1.0.0
     */
    @Override
    public boolean equals(final Object other)
    {
        if(Objects.equals(
            this, 
            other
        ))
        {
            return true;
        }
        if(Objects.isNull(other) || getClass() != other.getClass())
        {
            return true;
        }
        return Objects.equals(
            key(), 
            ((MessageModel)other).key()
        );
    }
    
    /**
     * Hashcode implementation by the {@link MessageModel#key}
     * @return                                  Hashcode, see also {@link String#hashCode()}
     * @since 1.0.0
     */
    @Override
    public int hashCode()
    {
        return Objects.hash(
            new Object[] {
                key()
            }
        );
    }
    
    /**
     * Print debug view of this object
     * @return                                  Debug view of this object
     * @since 1.0.0
     */
    @Override
    public String toString()
    {
        return String.format(
            "%s{key=%s, value=%s}",
            getClass().getSimpleName(),
            key(),
            Optional.ofNullable(
                value()
            ).map(
                valueToProcess -> valueToProcess.toString()
            ).orElse(
                null
            )
        );
    }
}
