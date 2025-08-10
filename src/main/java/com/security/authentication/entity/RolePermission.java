package com.security.authentication.entity;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "role_permissions",
       uniqueConstraints = @UniqueConstraint(name = "uk_role_permissions", columnNames = {"role_id", "permission_id"}),
       indexes = {
           @Index(name = "idx_role_permissions_role", columnList = "role_id"),
           @Index(name = "idx_role_permissions_permission", columnList = "permission_id")
       })
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(exclude = {"role", "permission"})
public class RolePermission extends BaseEntity {

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "permission_id", nullable = false)
    private Permission permission;

}

