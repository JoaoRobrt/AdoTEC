package com.joao.adotec.services;

import com.joao.adotec.dto.UserResponse;
import com.joao.adotec.models.User;
import com.joao.adotec.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public List<UserResponse> findEmployees() {
        return userRepository.findByRoles_RoleName(com.joao.adotec.enums.AppRole.ROLE_EMPLOYEE).stream()
                .map(user -> new UserResponse(user.getUserId(), user.getName(), user.getEmail()))
                .toList();
    }
}
