package my.com.maybank.usermgmt.repository;

import jakarta.annotation.Nonnull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.Optional;
import my.com.maybank.schema.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * User repository
 * @since 1.0.0
 * @see my.com.maybank.schema.entity.User
 * @author ChristopherCKW
 */
@Repository
public interface UserRepository
       extends JpaRepository<User, Long>
{
    /**
     * Find {@link User} by the exact-case-insensitive {@code username}
     * @param username                          Username. Must not be 
     *                                          {@code null}, blank/empty
     * @return                                  An {@link Optional} non-null 
     *                                          object and containing the 
     *                                          {@link User} object if found
     * @since 1.0.0
     */
    @Nonnull
    public Optional<User> findByUsernameIgnoreCase(@Nonnull 
                                                   @NotBlank
                                                   @NotEmpty
                                                   final String username);
}
