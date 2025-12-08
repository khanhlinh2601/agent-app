package org.linhtk.orchestrator.model.user;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.linhtk.common.model.AbstractAuditEntity;

import java.util.UUID;

/**
 * Entity representing system users.
 * Stores user authentication and profile information.
 * Used for audit tracking across all entities via created_by and updated_by fields.
 */
@Entity
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class User extends AbstractAuditEntity {
    
    /**
     * Unique identifier for the user.
     * Generated as UUID for distributed system compatibility.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    /**
     * Unique username for authentication.
     * Used as login credential.
     */
    @Column(nullable = false, unique = true, length = 50)
    private String username;
    
    /**
     * Encrypted password for authentication.
     * Should be hashed using BCrypt or similar algorithm before storage.
     */
    @Column(nullable = false, length = 255)
    private String password;
    
    /**
     * Flag indicating if the user account is active.
     * Used to disable accounts without deletion.
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean enabled = true;
    
    /**
     * Display name of the user.
     * Used in UI and audit logs.
     */
    @Column(nullable = false, length = 100)
    private String name;
    
    /**
     * User's email address.
     * Used for notifications and account recovery.
     */
    @Column(nullable = false, unique = true, length = 100)
    private String email;
}
