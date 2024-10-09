package my.com.maybank.core.controller;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import my.com.maybank.core.constants.DefaultRoles;
import my.com.maybank.core.models.UserModel;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Base security controller
 * @since 1.0.0
 * @author ChristopherCKW
 */
public interface BaseSecurityController
{
    /**
     * Checks the current {@code authentication} matches with the 
     * {@code expectedUserDetails} username field
     * @param authentication                    Authentication object to obtain 
     *                                          the current user
     * @param expectedUserDetails               Compare with an expected 
     *                                          {@link UserDetails#getUsername()} 
     * @return                                  {@code true} if matches, 
     *                                          {@code false} otherwise
     * @since 1.0.0
     */
    default boolean isCurrentUser(@Nullable
                                  final Authentication authentication,
                                  @Nonnull
                                  final UserDetails expectedUserDetails)
    {
        final UserDetails currentUserDetails = currentUser(
            authentication
        );
        
        try
        {
            return expectedUserDetails.getUsername().trim().equalsIgnoreCase(
                currentUserDetails.getUsername().trim()
            );
        }
        catch(Exception e)
        {
            return false;
        }
    }
    
    /**
     * Current user is administrator priviledged
     * @param authentication                    Authentication object to check  
     *                                          the current user and its granted 
     *                                          authorities is administrator 
     *                                          priviledged
     * @return                                  {@code true} if is administrator, 
     *                                          {@code false} otherwise
     * @since 1.0.0
     */
    default boolean currentUserIsAdmin(@Nullable
                                       final Authentication authentication)
    {
        return currentUserHasRoles(
            authentication,
            Stream.of(
                (GrantedAuthority) () -> DefaultRoles.ROLE_ADMIN
            ).toList()
        );
    }
    
    /**
     * Current user is customer priviledged
     * @param authentication                    Authentication object to check  
     *                                          the current user and its granted 
     *                                          authorities is customer 
     *                                          priviledged
     * @return                                  {@code true} if is customer, 
     *                                          {@code false} otherwise
     * @since 1.0.0
     */
    default boolean currentUserIsCustomer(@Nullable
                                          final Authentication authentication)
    {
        return currentUserHasRoles(
            authentication,
            Stream.of(
                (GrantedAuthority) () -> DefaultRoles.ROLE_CUSTOMER
            ).toList()
        );
    }
    
    /**
     * Checks the current {@code authentication} has any of the {@code expectedRoles} 
     * authority field
     * @param authentication                    Authentication object to obtain 
     *                                          the current user and its granted 
     *                                          authorities
     * @param expectedRoles                     Compare with an expected set of 
     *                                          {@link GrantedAuthority#getAuthority()} 
     * @return                                  {@code true} if found any of the 
     *                                          expected role(s), {@code false} 
     *                                          otherwise
     * @since 1.0.0
     */
    default boolean currentUserHasRoles(@Nullable
                                        final Authentication authentication,
                                        @Nonnull
                                        @NotEmpty
                                        final List<GrantedAuthority> expectedRoles)
    {
        final UserDetails currentUserDetails = currentUser(
            authentication
        );
        
        try
        {
            return Optional.ofNullable(
                currentUserDetails.getAuthorities()
            ).map(
                userDetailsAuthoritiesToProcess -> userDetailsAuthoritiesToProcess.stream().filter(
                    userDetailsAuthorityToProcess -> expectedRoles.stream().anyMatch(
                        expectedAuthorityToProcess -> expectedAuthorityToProcess.getAuthority().trim().equalsIgnoreCase(
                            userDetailsAuthorityToProcess.getAuthority().trim()
                        )
                    )
                ).findFirst().map(
                    foundMatchedAuthorityToProcess -> Boolean.TRUE
                ).orElse(
                    Boolean.FALSE
                )
            ).orElse(
                Boolean.FALSE
            );
        }
        catch(Exception e)
        {
            return false;
        }
    }
    
    /**
     * Obtain the current user
     * @param authentication                    Authentication object to obtain 
     *                                          the current user
     * @return                                  {@link UserDetails} principal object
     * @throws InsufficientAuthenticationException  If there are no sufficient 
     *                                              authentication details to 
     *                                              process the current user 
     *                                              principal object
     * @since 1.0.0
     */
    @Nonnull
    default UserModel currentUser(@Nullable
                                  final Authentication authentication)
    {
        return Optional.ofNullable(
            authentication
        ).filter(
            authenticationToProcess -> !AnonymousAuthenticationToken.class.isAssignableFrom(
                authenticationToProcess.getClass()
            )
        ).map(
            authenticationToProcess -> Optional.ofNullable(
                authenticationToProcess.getPrincipal()
            ).filter(
                authenticationPrincipalToProcess -> UserModel.class.isAssignableFrom(
                    authenticationPrincipalToProcess.getClass()
                )
            ).map(
                authenticationPrincipalToProcess -> (UserModel)authenticationPrincipalToProcess
            ).orElseThrow(
                () -> new InsufficientAuthenticationException(
                    String.format(
                        "Authentication object `%s` type RETURNED UNSUPPORTED/UNEXPECTED PRINCIPAL `%s` TYPE ; expecting object `%s` type",
                        authenticationToProcess.getClass().getName(),
                        Optional.ofNullable(
                            authenticationToProcess.getPrincipal()
                        ).map(
                            authenticationPrincipalToProcess -> authenticationPrincipalToProcess.getClass().getName()
                        ).orElse(
                            null
                        ),
                        UserModel.class.getName()
                    )
                )
            )
        ).orElseThrow(
            () -> new InsufficientAuthenticationException(
                String.format(
                    "API security REQUIRES VALID AUTHENTICATION ; expecting object `%s` type",
                    Authentication.class.getName()
                )
            )
        );
    }
}
