package my.com.maybank.accmgmt.component;

import jakarta.annotation.Nonnull;

/**
 * Message producer
 * @param <M>                                   Message type to produce to send 
 *                                              to a producer pipeline
 * @since 1.0.0
 * @author ChristopherCKW
 */
public interface MessageProducer<M>
{
    /**
     * Send the message
     * @param message                           Message to produce to producer 
     *                                          pipeline. Must not be {@code null}
     * @since 1.0.0
     */
    public void sendMessage(@Nonnull
                            final M message);
}
