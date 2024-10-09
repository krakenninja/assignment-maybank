package my.com.maybank.accmgmt.component;

import jakarta.annotation.Nonnull;

/**
 * Message consumer
 * @param <M>                                   Message type to listen to consume 
 *                                              from the pipeline
 * @since 1.0.0
 * @author ChristopherCKW
 */
public interface MessageConsumer<M>
{
    /**
     * Listen to the message to consume and do the processing required
     * @param message                           Message to consume and process. 
     *                                          Must not be {@code null}
     * @since 1.0.0
     */
    public void listenMessage(@Nonnull
                              final M message);
}
