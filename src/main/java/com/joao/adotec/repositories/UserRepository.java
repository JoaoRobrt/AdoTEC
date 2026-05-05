package com.joao.adotec.repositories;

import com.joao.adotec.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Boolean existsByEmail(String email);

    boolean existsByName(String name);

    List<User> findByRoles_RoleName(com.joao.adotec.enums.AppRole roleName);
}
