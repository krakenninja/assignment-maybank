package my.com.maybank.core.models;

import jakarta.annotation.Nonnull;
import java.io.Serializable;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * User model
 * @param <I>                                   User model identifier type
 * @since 1.0.0
 * @author ChristopherCKW
 */
public interface UserModel<I extends Serializable>
       extends UserDetails
{
    /**
     * Get the user model identifier
     * @return                                  Identifier
     * @since 1.0.0
     */
    @Nonnull
    I getId();
}
