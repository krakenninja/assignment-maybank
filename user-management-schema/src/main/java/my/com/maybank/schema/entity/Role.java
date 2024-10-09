package my.com.maybank.schema.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nonnull;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import my.com.maybank.core.constants.DefaultRoles;
import my.com.maybank.schema.EntityConstants;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;

/**
 * Role entity
 * <p>
 * Roles are just a special type of authority that must be prefixed with 
 * {@code ROLE_}
 * </p>
 * @since 1.0.0
 * @see org.springframework.security.core.GrantedAuthority
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
    name = EntityConstants.TABLE_NAME_ROLE,
    uniqueConstraints = @UniqueConstraint(
        columnNames = {
            EntityConstants.COLUMN_NAME_ROLE_NAME
        }
    )
)
public class Role
       implements GrantedAuthority
{
    /**
     * A unique identifier (as the primary key)
     * @since 1.0.0
     */
    @JsonProperty(
        access = JsonProperty.Access.WRITE_ONLY
    )
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(
        name = EntityConstants.COLUMN_NAME_ID
    )
    private UUID id;
    
    /**
     * Given role name
     * <p>
     * Not used as a primary key because we want to have the feature/support of 
     * renaming a role name (which is usually a very common thing to happen)
     * </p>
     * @since 1.0.0
     * @see #getAuthority()
     */
    @EqualsAndHashCode.Include
    @NotNull
    @NotBlank
    @NotEmpty
    @Pattern(
        regexp = "^" + DefaultRoles.PREFIX_ROLE + "[A-Za-z0-9_]+$",
        flags = Pattern.Flag.CASE_INSENSITIVE
    )
    @Column(
        name = EntityConstants.COLUMN_NAME_ROLE_NAME,
        nullable = false
    )
    private String rolename;
    
    /**
     * Created date/time
     * @since 1.0.0
     */
    @Nonnull
    @CreationTimestamp
    @Column(
        name = EntityConstants.COLUMN_NAME_CREATED
    )
    private LocalDateTime created;
    
    /**
     * Modified date/time
     * @since 1.0.0
     */
    @Nonnull
    @UpdateTimestamp
    @Column(
        name = EntityConstants.COLUMN_NAME_MODIFIED
    )
    private LocalDateTime modified;
    
    /**
     * Many to many relationship to {@link my.com.maybank.schema.entity.User} 
     * entity
     * @since 1.0.0
     * @see my.com.maybank.schema.entity.User#roles
     */
    @JsonIgnore
    @ManyToMany(
        fetch = FetchType.LAZY,
        cascade = CascadeType.ALL
    )
    @JoinTable(
        name = EntityConstants.TABLE_NAME_USER_ROLES, 
        joinColumns = @JoinColumn(
            name = EntityConstants.COLUMN_NAME_ROLE_ID,
            referencedColumnName = EntityConstants.COLUMN_NAME_ID,
            table = EntityConstants.TABLE_NAME_ROLE
        ), 
        inverseJoinColumns = @JoinColumn(
            name = EntityConstants.COLUMN_NAME_USER_ID, 
            referencedColumnName = EntityConstants.COLUMN_NAME_ID,
            table = EntityConstants.TABLE_NAME_USER
        )
    )
    private List<User> users;
    
    /**
     * Authority name of this role (ideally its returned value would be prefixed 
     * with {@link #PREFIX_ROLE})
     * @return                                  Authority role name, never 
     *                                          {@code null}
     * @since 1.0.0
     * @see #getRoleName() 
     * @see #normalizeRoleName()
     */
    @JsonIgnore
    @Nonnull
    @Override
    public String getAuthority()
    {
        return getRolename();
    }
    
    /**
     * Normalize the {@link Role#rolename}
     * @since 1.0.0
     */
    @PrePersist
    @PreUpdate
    public void normalizeRolename()
    {
        Optional.ofNullable(
            getRolename()
        ).ifPresent(
            roleNameToProcess -> setRolename(
                Optional.of(
                    roleNameToProcess.trim().toUpperCase()
                ).filter(
                    roleNameUpperCaseToProcess -> !roleNameUpperCaseToProcess.startsWith(
                        DefaultRoles.PREFIX_ROLE
                    )
                ).map(
                    roleNameUpperCaseToProcess -> String.format(
                        "%s%s",
                        DefaultRoles.PREFIX_ROLE,
                        roleNameUpperCaseToProcess
                    )
                ).orElse(
                    roleNameToProcess.trim().toUpperCase()
                )
            )
        );
    }

    /**
     * Get the role name basically
     * @return                                  Role name
     * @since 1.0.0
     */
    @Override
    public String toString()
    {
        return getRolename();
    }
}
