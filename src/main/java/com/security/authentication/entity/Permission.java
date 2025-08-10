package com.security.authentication.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "permissions",
       indexes = {
           @Index(name = "idx_permissions_name", columnList = "name", unique = true)
       })
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Permission extends BaseEntity {

    @Column(name = "name", nullable = false, unique = true, length = 128)
    private String name;

    @Column(name = "description", length = 255)
    private String description;

}

