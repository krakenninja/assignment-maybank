package my.com.maybank.usermgmt.service;

import jakarta.annotation.Nonnull;
import java.util.Optional;
import my.com.maybank.schema.entity.User;

/**
 * User service definition
 * @since 1.0.0
 * @author ChristopherCKW
 */
public interface UserService
{
    /**
     * Create user
     * @param user                              User to create, must not be 
     *                                          {@code null}
     * @return                                  Created {@link User}, never 
     *                                          {@code null}
     * @since 1.0.0
     */
    User createUser(@Nonnull
                    final User user);
    
    /**
     * Update user
     * @param user                              User to update, must not be 
     *                                          {@code null}
     * @return                                  Updated {@link User}, never 
     *                                          {@code null}
     * @since 1.0.0
     */
    User updateUser(@Nonnull
                    final User user);
    
    /**
     * Find user
     * @param user                              User to find, must not be 
     *                                          {@code null}
     * @return                                  Optional {@link User} if found, 
     *                                          never {@code null}
     * @since 1.0.0
     */
    Optional<User> findUser(@Nonnull
                            final User user);
    
    /**
     * Authenticate user
     * @param user                              User to authenticate, must not be 
     *                                          {@code null}
     * @return                                  {@code true} if successfully 
     *                                          authenticated, {@code false} 
     *                                          otherwise
     * @since 1.0.0
     */
    boolean authenticateUser(@Nonnull
                             final User user);
}
