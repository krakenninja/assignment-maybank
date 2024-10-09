package my.com.maybank.usermgmt.repository;

import jakarta.annotation.Nonnull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import my.com.maybank.schema.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Role repository
 * @since 1.0.0
 * @see my.com.maybank.schema.entity.Role
 * @author ChristopherCKW
 */
@Repository
public interface RoleRepository
       extends JpaRepository<Role, UUID>
{
    /**
     * Find {@link Role} by the exact-case-insensitive {@code rolename}
     * @param rolename                          Role name. Must not be 
     *                                          {@code null}, blank/empty
     * @return                                  An {@link Optional} non-null 
     *                                          object and containing the 
     *                                          {@link Role} object if found
     * @since 1.0.0
     */
    @Nonnull
    public Optional<Role> findByRolenameIgnoreCase(@Nonnull 
                                                   @NotBlank
                                                   @NotEmpty
                                                   final String rolename);
    
    /**
     * Find all {@link Role} by the exact-case-insensitive {@code rolenames}
     * @param rolenames                         Role names. Must not be 
     *                                          {@code null}, blank/empty
     * @return                                  Set of {@link Role}, never 
     *                                          {@code null}
     * @since 1.0.0
     */
    @Nonnull
    @Query("SELECT r FROM Role r WHERE LOWER(r.rolename) IN :rolenames")
    public Set<Role> findAllByRolenameIgnoreCase(@Nonnull 
                                                 @NotEmpty
                                                 @Param(
                                                     "rolenames"
                                                 )
                                                 final Iterable<String> rolenames);
}
