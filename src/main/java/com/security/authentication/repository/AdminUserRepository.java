package com.security.authentication.repository;

import com.security.authentication.entity.AdminUsers;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminUserRepository extends JpaRepository<AdminUsers, UUID> {
    Optional<AdminUsers> findByEmail(String email);
    Optional<AdminUsers> findByUsername(String username);
}

