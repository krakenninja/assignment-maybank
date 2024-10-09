package my.com.maybank.schema.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Min;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import my.com.maybank.core.models.UserModel;
import my.com.maybank.jpa.annotations.LongIdGenerator;
import my.com.maybank.schema.EntityConstants;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;

/**
 * User entity type to map against the provided sample "dataSource.txt"
 * @since 1.0.0
 * @author ChristopherCKW
 */
@JsonInclude(
    JsonInclude.Include.NON_NULL
)
@Accessors(
    fluent = false,
    chain = true
)
@Getter
@Setter
@EqualsAndHashCode(
    doNotUseGetters = true
)
@Entity
@Table(
    name = EntityConstants.TABLE_NAME_USER,
    uniqueConstraints = @UniqueConstraint(
        columnNames = {
            EntityConstants.COLUMN_NAME_USERNAME
        }
    )
)
public class User
       implements UserModel<Long>
{
    /**
     * User primary key {@link my.com.maybank.schema.EntityConstants#COLUMN_NAME_ID}
     * @since 1.0.0
     */
    @EqualsAndHashCode.Include
    @Min(
        value = 1L
    )
    @Id
    @LongIdGenerator
    @Column(
        name = EntityConstants.COLUMN_NAME_ID
    )
    private Long id;
    
    /**
     * Unique username
     * <p>
     * NOTE : Ideally in a multi-tenant cloud implementation, it will be 
     * accompanied by a {@code tenantId} (in OpenID world, this would generally 
     * be the {@code client-id} and in a multi-tenant world, it would be the 
     * {@code tenant-id}) --- but in this case, we'll not over-design this 
     * assignment goals
     * </p>
     * @since 1.0.0
     */
    @EqualsAndHashCode.Include
    @Nonnull
    @Column(
        name = EntityConstants.COLUMN_NAME_USERNAME,
        nullable = false
    )
    private String username;
    
    /**
     * User password {@link my.com.maybank.schema.EntityConstants#COLUMN_NAME_PASSWORD}
     * @since 1.0.0
     */
    @JsonProperty(
        access = JsonProperty.Access.WRITE_ONLY
    )
    @Nonnull
    @Column(
        name = EntityConstants.COLUMN_NAME_PASSWORD,
        nullable = false
    )
    private String password;
    
    /**
     * User old password (this is used in case we want to update the password)
     * @since 1.0.0
     */
    @JsonProperty(
        access = JsonProperty.Access.WRITE_ONLY
    )
    @Nullable
    @Transient
    private String oldPassword;
    
    /**
     * Plain password transient type to basically be used for authentication 
     * process
     * @since 1.0.0
     */
    @Nullable
    @Transient
    private String plainPassword;
    
    /**
     * User created date/time
     * @since 1.0.0
     */
    @JsonProperty(
        access = JsonProperty.Access.READ_ONLY
    )
    @Nonnull
    @CreationTimestamp
    @Column(
        name = EntityConstants.COLUMN_NAME_CREATED
    )
    private LocalDateTime created;
    
    /**
     * User modified date/time
     * @since 1.0.0
     */
    @JsonProperty(
        access = JsonProperty.Access.READ_ONLY
    )
    @Nonnull
    @UpdateTimestamp
    @Column(
        name = EntityConstants.COLUMN_NAME_MODIFIED
    )
    private LocalDateTime modified;
    
    /**
     * User roles "granted authorities" details
     * @since 1.0.0
     * @see #getAuthorities()
     */
    @Nullable
    @ManyToMany(
        fetch = FetchType.EAGER, 
        cascade = CascadeType.ALL
    )
    @JoinTable(
        name = EntityConstants.TABLE_NAME_USER_ROLES,
        joinColumns = @JoinColumn(
            name = EntityConstants.COLUMN_NAME_USER_ID, 
            referencedColumnName = EntityConstants.COLUMN_NAME_ID,
            table = EntityConstants.TABLE_NAME_USER
        ),
        inverseJoinColumns = @JoinColumn(
            name = EntityConstants.COLUMN_NAME_ROLE_ID, 
            referencedColumnName = EntityConstants.COLUMN_NAME_ID,
            table = EntityConstants.TABLE_NAME_ROLE
        )
    )
    private List<Role> roles;
    
    /**
     * Implements the {@link org.springframework.security.core.userdetails.UserDetails#getAuthorities()}
     * <p>
     * Guarantees to never return {@code null}
     * </p>
     * @return                                  Set of granted role(s), never 
     *                                          {@code null}
     * @since 1.0.0
     * @see #getRoles() 
     */
    @JsonIgnore
    @Nonnull
    @Override
    public List<Role> getAuthorities()
    {
        return Optional.ofNullable(
            getRoles()
        ).orElseGet(
            () -> new ArrayList<>()
        );
    }
    
    /**
     * Normalize the {@link User#username}
     * @since 1.0.0
     */
    @PrePersist
    @PreUpdate
    public void normalizeUsername()
    {
        Optional.ofNullable(
            getUsername()
        ).ifPresent(
            usernameToProcess -> setUsername(
                usernameToProcess.trim().toLowerCase()
            )
        );
    }
}