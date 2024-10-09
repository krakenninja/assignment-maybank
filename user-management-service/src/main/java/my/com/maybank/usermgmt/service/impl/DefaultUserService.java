package my.com.maybank.usermgmt.service.impl;

import com.password4j.Password;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import my.com.maybank.core.constants.DefaultRoles;
import my.com.maybank.core.exception.InternalException;
import my.com.maybank.schema.entity.Role;
import my.com.maybank.schema.entity.User;
import my.com.maybank.usermgmt.configuration.DefaultAppSecurity;
import my.com.maybank.usermgmt.repository.RoleRepository;
import my.com.maybank.usermgmt.repository.UserRepository;
import my.com.maybank.usermgmt.service.UserService;
import org.apache.commons.codec.binary.Base64;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Default {@link UserService} implementation
 * @since 1.0.0
 * @author ChristopherCKW
 */
@Slf4j
@Getter
@Setter
@RequiredArgsConstructor
@Service
public class DefaultUserService
       implements UserService
{
    @Nonnull
    @NonNull
    private final DefaultAppSecurity appSecurity;
    
    @Nonnull
    @NonNull
    private final UserRepository userRepository;
    
    @Nonnull
    @NonNull
    private final RoleRepository roleRepository;
    
    /**
     * Create new user
     * @param user                              {@link User} to create. Must never 
     *                                          {@code null}
     * @return                                  Created {@link User}, never 
     *                                          {@code null}
     */
    @Nonnull
    @Transactional
    @Override
    public User createUser(@Nonnull
                           final User user)
    {
        final Optional<User> existingUser = findUser(
            user
        );
        if(existingUser.isEmpty())
        {
            return getUserRepository().saveAndFlush(
                user.setPassword(
                    hashPassword(
                        user.getPassword()
                    )
                ).setRoles(
                    findRoles(
                        user.getRoles(),
                        true
                    )
                )
            );
        }
        throw new EntityExistsException(
            String.format(
                "Entity `user` ALREADY EXIST ; create new user object `%s` type with id `%s` | username `%s` is not permitted",
                User.class.getName(),
                existingUser.get().getId(),
                existingUser.get().getUsername()
            )
        );
    }
    
    @Nonnull
    @Transactional
    @Override
    public User updateUser(@Nonnull
                           final User user)
    {
        final User userExisting = Optional.ofNullable(
            user
        ).map(
            userToProcess -> Optional.ofNullable(
                userToProcess.getId()
            ).map(
                userIdToProcess -> getUserRepository().findById(
                    userIdToProcess
                ).orElseThrow(
                    () -> new EntityNotFoundException(
                        String.format(
                            "Entity `user::id` NOT FOUND ; expecting object `%s` type by `id` field value '%s'",
                            User.class.getName(),
                            userIdToProcess
                        )
                    )
                )
            ).orElseThrow(
                () -> new IllegalArgumentException(
                    "Bad parameter `user::id` IS NULL ; expecting `id` field"
                )
            )
        ).orElseThrow(
            () -> new IllegalArgumentException(
                String.format(
                    "Bad parameter `user` IS NULL ; expecting object `%s` type",
                    User.class.getName()
                )
            )
        );
        
        // update roles
        Optional.ofNullable(
            user.getRoles()
        ).filter(
            userRolesToProcess -> !userRolesToProcess.isEmpty()
        ).ifPresent(
            userRolesToProcess -> userExisting.setRoles(
                findRoles(
                    userRolesToProcess,
                    true
                )
            )
        );
        
        // update password
        Optional.ofNullable(
            user.getPassword()
        ).filter(
            userNewPasswordToProcess -> !userNewPasswordToProcess.trim().equals(
                ""
            )
        ).ifPresent(
            userNewPasswordToProcess -> Optional.ofNullable(
                user.getOldPassword()
            ).filter(
                userOldPasswordToProcess -> !userOldPasswordToProcess.trim().equals(
                    ""
                )
            ).ifPresent(
                userOldPasswordToProcess -> Optional.of(
                    verifyPassword(
                        userOldPasswordToProcess, 
                        userExisting.getPassword()
                    )
                ).filter(
                    userOldPasswordVerifiedToProcess -> userOldPasswordVerifiedToProcess
                ).ifPresentOrElse(
                    userOldPasswordVerifiedToProcess -> userExisting.setPassword(
                        hashPassword(
                            user.getPassword()
                        )
                    ),
                    () -> {
                        throw new BadCredentialsException(
                            "Invalid credentials VERIFY UNSUCCESSFUL"
                        );
                    }
                )
            )
        );
        
        // update username
        Optional.ofNullable(
            user.getUsername()
        ).filter(
            userNewUsernameToProcess -> !userNewUsernameToProcess.trim().equals(
                ""
            )
        ).ifPresent(
            userNewUsernameToProcess -> Optional.of(
                userNewUsernameToProcess.trim().equalsIgnoreCase(
                    userExisting.getUsername().trim()
                )
            ).filter(
                userNewUsernameUpdateRequiredToProcess -> !userNewUsernameUpdateRequiredToProcess
            ).ifPresent(
                userNewUsernameUpdateRequiredToProcess -> userExisting.setUsername(
                    userNewUsernameToProcess.trim()
                )
            )
        );
        
        // update modified
        userExisting.setModified(
            LocalDateTime.now()
        );
        
        return getUserRepository().saveAndFlush(
            userExisting
        );
    }

    /**
     * Find user 
     * <p>
     * By {@link User#getId()} or {@link User#getUsername()}
     * </p>
     * @param user                              User to find. Must not be 
     *                                          {@code null} and must meet at 
     *                                          least ONE(1) of the search 
     *                                          criteria
     * @return                                  An optional {@link User} if 
     *                                          found, never {@code null}
     * @since 1.0.0
     */
    @Transactional(
        readOnly = true
    )
    @Override
    public Optional<User> findUser(@Nonnull
                                   final User user)
    {
        return Optional.ofNullable(
            user.getId()
        ).map(
            userIdToProcess -> getUserRepository().findById(
                userIdToProcess
            )
        ).orElseGet(
            () -> Optional.ofNullable(
                user.getUsername()
            ).filter(
                userUsernameToProcess -> !userUsernameToProcess.trim().equals(
                    ""
                )
            ).map(
                userUsernameToProcess -> getUserRepository().findByUsernameIgnoreCase(
                    userUsernameToProcess
                )
            ).orElseThrow(
                () -> new IllegalArgumentException(
                    "Bad parameter `user::id` or `user::username` IS NULL OR INVALID ; expecting at least ONE(1) of the search criteria"
                )
            )
        );
    }
    
    /**
     * Authenticate user credential
     * <p>
     * By {@link User#getId()} or {@link User#getUsername()}
     * </p>
     * @param user                              User to authenticate. Must not be 
     *                                          {@code null} and must meet at 
     *                                          least ONE(1) of the search 
     *                                          criteria
     * @return                                  {@code true} if authenticate 
     *                                          success, {@code false} otherwise
     * @since 1.0.0
     */
    @Override
    public boolean authenticateUser(@Nonnull
                                    final User user)
    {
        return findUser(
            user
        ).map(
            existingUserToProcess -> {
                final boolean verifyPasswordResult = verifyPassword(
                    user.getPlainPassword(), 
                    existingUserToProcess.getPassword()
                );
                if(verifyPasswordResult)
                {
                    Optional.ofNullable(
                        user.getId()
                    ).ifPresentOrElse(
                        userIdToProcess -> log.info(
                            "Authenticate `user::id` SUCCESSFUL ; user by id '{}' password matched",
                            userIdToProcess
                        ),
                        () -> log.info(
                            "Authenticate `user::username` SUCCESSFUL ; user by username '{}' password matched",
                            user.getUsername()
                        )
                    );
                }
                else
                {
                    Optional.ofNullable(
                        user.getId()
                    ).ifPresentOrElse(
                        userIdToProcess -> log.warn(
                            "Authenticate `user::id` UNSUCCESSFUL - INVALID CREDENTIALS ; user by id '{}' password mismatch",
                            userIdToProcess
                        ),
                        () -> log.warn(
                            "Authenticate `user::username` UNSUCCESSFUL - INVALID CREDENTIALS ; user by username '{}' password mismatch",
                            user.getUsername()
                        )
                    );
                }
                return verifyPasswordResult;
            }
        ).orElseGet(
            () -> {
                Optional.ofNullable(
                    user.getId()
                ).ifPresentOrElse(
                    userIdToProcess -> log.warn(
                        "Authenticate `user::id` UNSUCCESSFUL - NOT FOUND ; user by id '{}' not exist",
                        userIdToProcess
                    ),
                    () -> log.warn(
                        "Authenticate `user::username` UNSUCCESSFUL - NOT FOUND ; user by username '{}' not exist",
                        user.getUsername()
                    )
                );
                return Boolean.FALSE;
            }
        );
    }

    /**
     * Find roles 
     * <p>
     * Supports mixed set of {@link Role} by {@link Role#getId()} or 
     * {@link Role#getRolename()}
     * </p>
     * @param roles                             Set of roles to find. Must not 
     *                                          be {@code null} or empty
     * @param matchSize                         Use {@code true} to ensure the 
     *                                          returned size matches the 
     *                                          requested size
     * @return                                  Set of roles found, never 
     *                                          {@code null}
     * @since 1.0.0
     */
    protected List<Role> findRoles(@Nullable
                                   final List<Role> roles,
                                   final boolean matchSize)
    {
        final List<Role> rolesResult = Optional.ofNullable(
            roles
        ).map(
            rolesToProcess -> (List<Role>)Stream.concat(
                getRoleRepository().findAllById(
                    rolesToProcess.stream().filter(
                        roleToProcess -> Objects.nonNull(
                            roleToProcess.getId()
                        )
                    ).map(
                        roleToProcess -> roleToProcess.getId()
                    ).toList()
                ).stream(),
                getRoleRepository().findAllByRolenameIgnoreCase(
                    rolesToProcess.stream().filter(
                        roleToProcess -> Objects.nonNull(
                            roleToProcess.getRolename()
                        )
                    ).map(
                        roleToProcess -> roleToProcess.getRolename()
                    ).filter(
                        rolenameToProcess -> rolenameToProcess.toUpperCase().startsWith(
                            DefaultRoles.PREFIX_ROLE
                        )
                    ).toList()
                ).stream()
            ).collect(
                Collectors.toCollection(
                    TreeSet::new
                )
            )
        ).orElseGet(
            () -> Collections.emptyList()
        );
        
        if(matchSize)
        {
            final int expectedSize = Optional.ofNullable(
                roles
            ).map(
                List::size
            ).orElse(
                0
            );
            if(rolesResult.size() != expectedSize)
            {
                throw new EntityNotFoundException(
                    String.format(
                        "One or more entity object `%s` type IS NOT FOUND ; looking for %s %n%nreturned %n%s",
                        Role.class.getName(),
                        Optional.ofNullable(
                            roles
                        ).map(
                            rolesToProcess -> rolesToProcess.stream().map(
                                roleToProcess -> Optional.ofNullable(
                                    roleToProcess.getId()
                                ).map(
                                    roleIdToProcess -> String.format(
                                        "%n\tBy `role::id` %s",
                                        roleIdToProcess.toString()
                                    )
                                ).orElseGet(
                                    () -> String.format(
                                        "%n\tBy `role::rolename` %s",
                                        roleToProcess.getRolename()
                                    )
                                )
                            ).collect(
                                Collectors.joining(
                                    "\n\t"
                                )
                            )
                        ).orElse(
                            "\n"
                        ),
                        rolesResult.stream().map(
                            roleToProcess -> String.format(
                                "%n\tResult `role::id` %s | `role::rolename` %s",
                                roleToProcess.getId().toString(),
                                roleToProcess.getRolename()
                            )
                        ).collect(
                            Collectors.joining(
                                "\n\t"
                            )
                        )
                    )
                );
            }
        }
        
        return rolesResult;
    }
    
    /**
     * Hash password (using {@code Argon2})
     * @param passwordPlain                     Plain password. Must not be 
     *                                          {@code null} or blank/empty
     * @return                                  Hashed password, never {@code null} 
     *                                          or blank/empty
     * @since 1.0.0
     */
    protected String hashPassword(@Nonnull
                                  @NotBlank
                                  @NotEmpty
                                  final String passwordPlain)
    {
        return Base64.encodeBase64URLSafeString(
            Password.hash(
                passwordPlain
            ).addSalt(
                appSecurity.getAuthnUser().getSalt()
            ).withArgon2().getResultAsBytes()
        );
    }
    
    /**
     * Verify password (using {@code Argon2})
     * @param passwordPlain                     Plain password. Must not be 
     *                                          {@code null} or blank/empty
     * @param passwordHash                      Hashed password. Must not be 
     *                                          {@code null} or blank/empty
     * @return                                  {@code true} if verified, 
     *                                          {@code false} otherwise
     * @since 1.0.0
     */
    protected boolean verifyPassword(@Nullable
                                     final String passwordPlain,
                                     @Nonnull
                                     @NotBlank
                                     @NotEmpty
                                     final String passwordHash)
    {
        return Password.check(
            Optional.ofNullable(
                passwordPlain
            ).filter(
                passwordPlainToProcess -> !passwordPlainToProcess.equals(
                    ""
                )
            ).orElseThrow(
                () -> new BadCredentialsException(
                    "Password IS NULL OR INVALID"
                )
            ), 
            new String(
                Base64.decodeBase64(
                    passwordHash
                ),
                StandardCharsets.UTF_8
            )
        ).addSalt(
            Optional.ofNullable(
                appSecurity.getAuthnUser()
            ).map(
                authnUserToProcess -> authnUserToProcess.getSalt()
            ).orElseThrow(
                () -> new InternalException(
                    "Salt IS NULL OR INVALID"
                )
            )
        ).withArgon2();
    }
}
