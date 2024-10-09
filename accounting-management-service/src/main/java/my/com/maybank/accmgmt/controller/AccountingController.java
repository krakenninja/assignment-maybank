package my.com.maybank.accmgmt.controller;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import my.com.maybank.accmgmt.configuration.DefaultAppService;
import my.com.maybank.accmgmt.constants.AppCodes;
import my.com.maybank.accmgmt.models.AccountTransaction;
import my.com.maybank.accmgmt.models.AccountTransactionCriteria;
import my.com.maybank.accmgmt.models.AccountTransactions;
import my.com.maybank.accmgmt.service.AccountingService;
import my.com.maybank.core.configuration.service.AppService;
import my.com.maybank.core.constants.DefaultRoles;
import my.com.maybank.core.controller.BaseSecurityController;
import my.com.maybank.core.exception.AuthenticationException;
import my.com.maybank.core.exception.BadParameterException;
import my.com.maybank.core.exception.InternalException;
import my.com.maybank.core.exception.PermissionDeniedException;
import my.com.maybank.core.models.MessageModel;
import my.com.maybank.schema.entity.Account;
import my.com.maybank.schema.entity.Transaction;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Accounting controller REST APIs
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
public class AccountingController
       implements BaseSecurityController
{
    /**
     * Application service configuration
     * @since 1.0.0
     * @see my.com.maybank.accmgmt.configuration.DefaultAppService
     */
    @NonNull
    @Nonnull
    private final AppService appService;
    
    /**
     * Accounting service
     * @since 1.0.0
     */
    @NonNull
    @Nonnull
    private final AccountingService accountingService;
    
    /**
     * Upload account transactions CSV file
     * @param authentication                    Authentication object for API 
     *                                          security
     * @param file                              Account transaction CSV file
     * @return                                  {@link ResponseEntity} with  
     *                                          basic message model
     * @since 1.0.0
     */
    @Nonnull
    @PreAuthorize(
        "hasRole('" + DefaultRoles.ROLE_ADMIN + "')"
    )
    @PostMapping(
        path = "/v1/accounting/upload",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @ResponseBody
    public ResponseEntity<MessageModel.MessageModelResponse> uploadAccountTransactions(@Nullable
                                                                                       final Authentication authentication,
                                                                                       @Nonnull
                                                                                       @RequestParam(
                                                                                           "file"
                                                                                       )
                                                                                       final MultipartFile file)
    {
        final AtomicReference<Path> uploadedCsvPath = new AtomicReference<>();
        try
        {
            return Optional.of(
                file.isEmpty()
            ).filter(
                fileHasNoContentToProcess -> !fileHasNoContentToProcess
            ).map(
                fileHasNoContentToProcess -> {
                    try
                    {
                        // file I/O processing
                        final Path uploadDestBasePath = ((DefaultAppService)getAppService()).getUploadDestBasePath();
                        final Path uploadTransferPath = uploadDestBasePath.resolve(
                            Long.toHexString(
                                System.nanoTime()
                            )
                        );
                        if(!Files.exists(
                            uploadTransferPath
                        ))
                        {
                            Files.createDirectories(
                                uploadTransferPath
                            );
                        }

                        uploadedCsvPath.set(
                            uploadTransferPath.resolve(
                                file.getOriginalFilename()
                            )
                        );
                        file.transferTo(
                            uploadedCsvPath.get()
                        );

                        // now let us trigger the batch pipeline to a batch process
                        final long batchLinesProcessed = getAccountingService().batchProducerCreateAccountTransactions(
                            uploadedCsvPath.get()
                        );

                        // all good, response back some kind of basic message
                        return ResponseEntity.status(
                            HttpStatus.OK
                        ).body(
                            MessageModel.infoMessage(
                                String.format(
                                    "File '%s' upload SUCCESS ; total no. of %d batch record(s)",
                                    file.getOriginalFilename(),
                                    batchLinesProcessed
                                )
                            )
                        );
                    }
                    catch(Exception e)
                    {
                        throw new InternalException(
                            String.format(
                                "File '%s' upload FAILED ; %s",
                                file.getOriginalFilename(),
                                e.getMessage()
                            )
                        ).setCode(
                            AppCodes.ERROR_CODE_UPLOAD_FAILURE
                        );
                    }
                }
            ).orElseThrow(
                () -> new BadParameterException(
                    String.format(
                        "File '%s' upload HAS NO CONTENT",
                        file.getOriginalFilename()
                    )
                ).setCode(
                    AppCodes.ERROR_CODE_UPLOAD_NO_CONTENT
                )
            );
        }
        catch(InternalException e)
        {
            throw e;
        }
        catch(Exception e)
        {
            throw new InternalException(
                String.format(
                    "Upload transaction create FAILED ; %s",
                    e.getMessage()
                )
            ).setCode(
                AppCodes.ERROR_CODE_UPLOAD_FAILURE
            );
        }
        finally
        {
            // clear the resource
            Optional.ofNullable(
                uploadedCsvPath.get()
            ).ifPresent(
                uploadedCsvPathToProcess -> uploadedCsvPathToProcess.toFile().delete()
            );
        }
    }
    
    /**
     * API to update the user accounting transactions
     * @param authentication                    Authentication object for API 
     *                                          security
     * @param accountUpdateCritera              Account transaction update  
     *                                          criteria request
     * @return                                  {@link ResponseEntity} with  
     *                                          basic message model
     * @since 1.0.0
     */
    @Nonnull
    @PreAuthorize(
        "hasRole('" + DefaultRoles.ROLE_USER + "')"
    )
    @PatchMapping(
        path = "/v1/accounting",
        consumes = {
            MediaType.APPLICATION_JSON_VALUE,
            "application/json-patch+json"
        }
    )
    @ResponseBody
    public ResponseEntity<MessageModel.MessageModelResponse> updateAccountTransactions(@Nullable
                                                                                       final Authentication authentication,
                                                                                       @Nonnull
                                                                                       @RequestBody
                                                                                       final AccountTransactionCriteria accountUpdateCritera)
    {
        try
        {
            Optional.of(
                currentUserIsCustomer(
                    authentication
                )
            ).filter(
                currentUserIsCustomerToProcess -> currentUserIsCustomerToProcess
            ).ifPresent(
                currentUserIsCustomerToProcess -> Optional.ofNullable(
                    currentUser(
                        authentication
                    ).getId()
                ).filter(
                    currentUserIdToProcess -> Long.class.isAssignableFrom(
                        currentUserIdToProcess.getClass()
                    )
                ).ifPresentOrElse(
                    currentUserIdToProcess -> Optional.ofNullable(
                        accountUpdateCritera.getTransaction()
                    ).filter(
                        accountUpdateTransactionCriteraToProcess -> Objects.nonNull(
                            accountUpdateTransactionCriteraToProcess.getAccount()
                        )
                    ).filter(
                        accountUpdateTransactionCriteriaToProcess -> Objects.nonNull(
                            accountUpdateTransactionCriteriaToProcess.getAccount().getCustomerId()
                        )
                    ).ifPresentOrElse(
                        accountUpdateTransactionCriteraToProcess -> Optional.ofNullable(
                            accountUpdateTransactionCriteraToProcess.getAccount().getCustomerId()
                        ).filter(
                            customerIdCriteriaToProcess -> customerIdCriteriaToProcess.equals(
                                currentUserIdToProcess
                            )
                        ).orElseThrow(
                            () -> new PermissionDeniedException(
                                String.format(
                                    "Authenticated user identifier (%d) ATTEMPTING TO UPDATE INACCESSIBLE RECORD",
                                    (Long)currentUserIdToProcess
                                )
                            ).setCode(AppCodes.ERROR_CODE_AUTH_FORBIDDEN
                            )
                        ),
                        () -> accountUpdateCritera.getTransaction().setAccount(
                            new Account().setCustomerId(
                                (Long)currentUserIdToProcess
                            )
                        )
                    ),
                    // can't get the id
                    () -> {
                        throw new AuthenticationException(
                            "Authenticated user id IS NULL OR INVALID"
                        ).setCode(
                            AppCodes.ERROR_CODE_AUTH_FAILURE
                        );
                    }
                )
            );
            
            // now let us trigger the batch pipeline to a batch process
            final long batchLinesProcessed = getAccountingService().batchProducerUpdateAccountTransactions(
                accountUpdateCritera
            );

            // all good, response back some kind of basic message
            return ResponseEntity.status(
                HttpStatus.OK
            ).body(
                MessageModel.infoMessage(
                    String.format(
                        "Criteria transaction update SUCCESS ; total no. of %d batch record(s)",
                        batchLinesProcessed
                    )
                )
            );
        }
        catch(InternalException e)
        {
            throw e;
        }
        catch(Exception e)
        {
            throw new InternalException(
                String.format(
                    "Criteria transaction update FAILED ; %s",
                    e.getMessage()
                )
            ).setCode(
                AppCodes.ERROR_CODE_UPDATE_FAILURE
            );
        }
    }
    
    /**
     * API to retrieve the user accounting 
     * @param authentication                    Authentication object for API 
     *                                          security
     * @param accountSearchCritera              Account transaction search 
     *                                          criteria request
     * @return                                  Paginated results containing the 
     *                                          {@link AccountTransaction}
     * @since 1.0.0
     */
    @Nonnull
    @PreAuthorize(
        "hasRole('" + DefaultRoles.ROLE_USER + "')"
    )
    @PostMapping(
        path = "/v1/accounting",
        consumes = {
            MediaType.APPLICATION_JSON_VALUE
        }
    )
    @ResponseBody
    public AccountTransactions retrieveAccountTransactions(@Nullable
                                                           final Authentication authentication,
                                                           @Nonnull
                                                           @RequestBody
                                                           final AccountTransactionCriteria accountSearchCritera)
    {
        // for security, if the user is not of admin, he/she will be forced 
        // to obtain only his/her own records
        return Optional.of(
            currentUserIsAdmin(
                authentication
            )
        ).filter(
            currentUserIsAdminToProcess -> currentUserIsAdminToProcess
        ).map(
            // able to retrieve anyone anyhow anywhen...
            currentUserIsAdminToProcess -> getAccountingService().retrieveAccountTransactions(
                accountSearchCritera
            )
        ).orElseGet(// manipulation to override/set the customer id is needed
            () -> {
                final AccountTransactionCriteria restrictedAccountSearchCritera = Optional.ofNullable(
                    currentUser(
                        authentication
                    ).getId()
                ).filter(
                    currentUserIdToProcess -> Long.class.isAssignableFrom(
                        currentUserIdToProcess.getClass()
                    )
                ).map(
                    currentUserIdToProcess -> Optional.ofNullable(
                        accountSearchCritera.getTransaction()
                    ).filter(
                        accountSearchTransactionCriteraToProcess -> Objects.nonNull(
                            accountSearchTransactionCriteraToProcess.getAccount()
                        )
                    ).filter(
                        accountSearchTransactionCriteraToProcess -> Objects.nonNull(
                            accountSearchTransactionCriteraToProcess.getAccount().getCustomerId()
                        )
                    ).map(
                        accountSearchTransactionCriteraToProcess -> Optional.ofNullable(
                            accountSearchTransactionCriteraToProcess.getAccount().getCustomerId()
                        ).filter(
                            customerIdCriteriaToProcess -> customerIdCriteriaToProcess.equals(
                                currentUserIdToProcess
                            )
                        ).map(
                            customerIdCriteriaToProcess -> accountSearchCritera
                        ).orElseThrow(
                            () -> new PermissionDeniedException(
                                String.format(
                                    "Authenticated user identifier (%d) ATTEMPTING TO SEARCH INACCESSIBLE RECORD",
                                    (Long)currentUserIdToProcess
                                )
                            ).setCode(
                                AppCodes.ERROR_CODE_AUTH_FORBIDDEN
                            )
                        )
                    ).orElseGet(
                        () -> Optional.ofNullable(
                            accountSearchCritera.getTransaction()
                        ).map(
                            transactionToProcess -> {
                                transactionToProcess.setAccount(
                                    new Account().setCustomerId(
                                        (Long)currentUserIdToProcess
                                    )
                                );
                                return accountSearchCritera;
                            }
                        ).orElseGet(
                            () -> accountSearchCritera.setTransaction(
                                new Transaction().setAccount(
                                    new Account().setCustomerId(
                                        (Long)currentUserIdToProcess
                                    )
                                )
                            )
                        )
                    )
                ).orElseThrow(
                    // can't get the id
                    () -> new AuthenticationException(
                        "Authenticated user id IS NULL OR INVALID"
                    ).setCode(
                        AppCodes.ERROR_CODE_AUTH_FAILURE
                    )
                );
                
                return getAccountingService().retrieveAccountTransactions(
                    restrictedAccountSearchCritera
                );
            }
        );
    }
}
