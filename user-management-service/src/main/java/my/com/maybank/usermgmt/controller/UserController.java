package my.com.maybank.usermgmt.controller;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import my.com.maybank.core.constants.DefaultRoles;
import my.com.maybank.core.controller.BaseSecurityController;
import my.com.maybank.core.exception.InvalidCredentialsException;
import my.com.maybank.core.exception.PermissionDeniedException;
import my.com.maybank.core.exception.ResourceNotFoundException;
import my.com.maybank.schema.entity.Role;
import my.com.maybank.schema.entity.User;
import my.com.maybank.usermgmt.constants.AppCodes;
import my.com.maybank.usermgmt.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * User controller REST APIs
 * @since 1.0.0
 * @author ChristopherCKW
 */
@Slf4j
@Getter
@Setter
@RequiredArgsConstructor
@RestController
@RequestMapping(
    "/api"
)
public class UserController
       implements BaseSecurityController
{
    /**
     * User service
     * @since 1.0.0
     */
    @Nonnull
    @NonNull
    private final UserService userService;
    
    /**
     * API to authenticate the user
     * @param user                              User request to authenticate
     * @return                                  Authenticated user details
     * @since 1.0.0
     */
    @Nonnull
    @PostMapping(
        "/v1/user/auth"
    )
    @ResponseBody
    public User authenticateUser(@Nonnull
                                 @RequestBody
                                 final User user)
    {
        return Optional.of(
            // auth w/o finding the user first as we do not want to give any 
            // hints to an attacker that there's no such user
            getUserService().authenticateUser(
                user
            )
        ).filter(
            userIsAuthenticatedToProcess -> userIsAuthenticatedToProcess
        ).map(
            userIsAuthenticatedToProcess -> getUserService().findUser(
                user
            ).orElseThrow(
                () -> new ResourceNotFoundException(
                    String.format(
                        "User '%s' IS NOT FOUND",
                        user.getUsername()
                    )
                ).setCode(
                    AppCodes.ERROR_CODE_AUTH_USER_NOT_FOUND
                )
            )
        ).orElseThrow(
            () -> new InvalidCredentialsException(
                String.format(
                    "User '%s' VERIFY AUTHENTICATION UNSUCCESSFUL",
                    user.getUsername()
                )
            ).setCode(
                AppCodes.ERROR_CODE_AUTH_BAD_CREDENTIALS
            )
        );
    }
    
    /**
     * API to get the user details by the primary key {@link java.lang.Long} 
     * type
     * @param authentication                    Authentication object for API 
     *                                          security
     * @param id                                Path variable of the user 
     *                                          identifier number type to obtain 
     *                                          for its user details
     * @return                                  {@link User}, never {@code null}
     * @since 1.0.0
     */
    @Nonnull
    @PreAuthorize(
        "hasRole('" + DefaultRoles.ROLE_USER + "')"
    )
    @GetMapping(
        "/v1/user/{id}"
    )
    @ResponseBody
    public User getUserById(@Nullable
                            final Authentication authentication,
                            @Nonnull
                            @Min(
                                1l
                            )
                            @Max(
                                Long.MAX_VALUE
                            )
                            @Min(
                                1l
                            )
                            @PathVariable(
                                "id"
                            ) 
                            final Long id)
    {
        return Optional.of(
            currentUserIsAdmin(
                authentication
            )
        ).filter(
            currentUserIsAdminToProcess -> currentUserIsAdminToProcess
        ).map(
            // admin is allowed to proceed to get any user id
            currentUserIsAdminToProcess -> getUserService().findUser(
                new User().setId(
                    id
                )
            ).orElseThrow(
                () -> new ResourceNotFoundException(
                    String.format(
                        "User id '%d' NOT FOUND",
                        id
                    )
                ).setCode(
                    AppCodes.ERROR_CODE_AUTH_USER_NOT_FOUND
                )
            )
        ).orElseGet(
            () -> Optional.of(
                currentUserIsCustomer(
                    authentication
                )
            ).filter(
                currentUserIsCustomerToProcess -> currentUserIsCustomerToProcess
            ).map(
                currentUserIsCustomerToProcess -> Optional.of(
                    Objects.equals(
                        currentUser(
                            authentication
                        ).getId(),
                        id
                    )
                ).filter(
                    currentUserIsSelfToProcess -> currentUserIsSelfToProcess
                ).map(
                    currentUserIsSelfToProcess -> getUserService().findUser(
                        new User().setId(
                            id
                        )
                    ).orElseThrow(
                        () -> new ResourceNotFoundException(
                            String.format(
                                "User id '%d' NOT FOUND",
                                id
                            )
                        ).setCode(
                            AppCodes.ERROR_CODE_AUTH_USER_NOT_FOUND
                        )
                    )
                ).orElseThrow(
                    () -> new PermissionDeniedException(
                        String.format(
                            "Customer user search not-self IS NOT ALLOWED/PERMITTED ; attempting to search user id '%s' by current user id '%s'",
                            String.valueOf(
                                id
                            ),
                            String.valueOf(
                                currentUser(
                                    authentication
                                ).getId()
                            )
                        )
                    ).setCode(
                        AppCodes.ERROR_CODE_AUTH_FORBIDDEN
                    )
                )
            ).orElseThrow(
                () -> new PermissionDeniedException(
                    "API role IS NOT ADMIN/CUSTOMER"
                ).setCode(
                    AppCodes.ERROR_CODE_AUTH_FORBIDDEN
                )
            )
        );
    }
}
