package my.com.maybank.accmgmt.component;

import jakarta.annotation.Nonnull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.function.Consumer;

/**
 * Message reader
 * @param <I>                                   Instruction type to read and 
 *                                              process
 * @param <M>                                   Message type to use for callback
 * @param <R>                                   Response type (i.e. {@link Long} 
 *                                              {@link Void} etc.)
 * @since 1.0.0
 * @see my.com.maybank.accmgmt.component.CsvConsumer
 * @author ChristopherCKW
 */
public interface MessageReader<I, M, R>
{
    /**
     * Process the {@link I} instruction that should produce {@link M} which 
     * then is used in the callback {@code consume}
     * @param instruction                       CSV file path. Must not be 
     *                                          {@code null} or blank/empty
     * @param consume                           Consumer callback. Must not be 
     *                                          {@code null}
     * @return                                  Response type that may be useful 
     *                                          for the API consumer
     * @since 1.0.0
     */
    public R readAndCallback(@Nonnull
                             @NotBlank
                             @NotEmpty
                             final I instruction, 
                             @Nonnull
                             final Consumer<M> consume);
}
