package my.com.maybank.accmgmt.component;

import jakarta.annotation.Nonnull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.function.Consumer;

/**
 * CSV reader
 * @param <O>                                   Operation enum type
 * @param <M>                                   Message type
 * @since 1.0.0
 * @see my.com.maybank.accmgmt.component.CsvConsumer
 * @author ChristopherCKW
 */
public interface CsvMessageReader<O extends Enum<O>, M>
{
    /**
     * Read the CSV and consume the message
     * @param csvFilePath                       CSV file path. Must not be 
     *                                          {@code null} or blank/empty
     * @param operation                         Operation to act on the message
     * @param consume                           Consumer callback. Must not be 
     *                                          {@code null}
     * @return                                  Number of CSV lines read
     * @since 1.0.0
     */
    public long readAndCallback(@Nonnull
                                @NotBlank
                                @NotEmpty
                                final String csvFilePath, 
                                @Nonnull
                                final O operation,
                                @Nonnull
                                final Consumer<M> consume);
}
