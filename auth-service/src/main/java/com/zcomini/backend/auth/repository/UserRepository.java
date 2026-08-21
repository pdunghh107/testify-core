package com.zcomini.backend.auth.repository;

import com.zcomini.backend.auth.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {

     Optional<UserEntity> findByEmailIgnoreCase(String email);

     boolean existsByEmailIgnoreCase(String email);

     Optional<UserEntity> findByPhone(String phone);

     boolean existsByPhone(String phone);
}
