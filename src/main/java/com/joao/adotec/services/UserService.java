package com.joao.adotec.services;

import com.joao.adotec.dto.CreateEmployeeRequest;
import com.joao.adotec.dto.UpdateEmployeeRequest;
import com.joao.adotec.dto.UserResponse;
import com.joao.adotec.enums.AppRole;
import com.joao.adotec.exceptions.domain.BusinessException;
import com.joao.adotec.exceptions.domain.DomainException;
import com.joao.adotec.exceptions.domain.ResourceNotFoundException;
import com.joao.adotec.models.Role;
import com.joao.adotec.models.User;
import com.joao.adotec.repositories.RoleRepository;
import com.joao.adotec.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    // ── Listagem ──────────────────────────────────────────────

    /**
     * Retorna todos os usuários com ROLE_EMPLOYEE ou ROLE_ADMIN (staff).
     */
    @Transactional(readOnly = true)
    public List<UserResponse> findEmployees() {
        return userRepository.findByRoles_RoleName(AppRole.ROLE_EMPLOYEE).stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Retorna todos os usuários staff (EMPLOYEE + ADMIN) para o dashboard.
     */
    @Transactional(readOnly = true)
    public List<UserResponse> findAllStaff() {
        List<User> employees = userRepository.findByRoles_RoleName(AppRole.ROLE_EMPLOYEE);
        List<User> admins = userRepository.findByRoles_RoleName(AppRole.ROLE_ADMIN);

        // Merge sem duplicatas usando userId
        java.util.Map<Long, User> staffMap = new java.util.LinkedHashMap<>();
        employees.forEach(u -> staffMap.put(u.getUserId(), u));
        admins.forEach(u -> staffMap.put(u.getUserId(), u));

        return staffMap.values().stream()
                .map(this::toResponse)
                .toList();
    }

    // ── Busca por ID ──────────────────────────────────────────

    @Transactional(readOnly = true)
    public UserResponse findEmployeeById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", id));
        assertIsStaff(user);
        return toResponse(user);
    }

    // ── Criação ───────────────────────────────────────────────

    @Transactional
    public UserResponse createEmployee(CreateEmployeeRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DomainException(HttpStatus.CONFLICT, "E-mail já está em uso.");
        }

        User user = new User(
                request.getName(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()));

        AppRole targetRole = resolveRole(request.getRole());
        Role role = roleRepository.findByRoleName(targetRole)
                .orElseThrow(() -> new DomainException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Role '" + targetRole + "' não encontrada no banco."));

        user.setRoles(Set.of(role));
        userRepository.save(user);

        return toResponse(user);
    }

    // ── Atualização ───────────────────────────────────────────

    @Transactional
    public UserResponse updateEmployee(Long id, UpdateEmployeeRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", id));
        assertIsStaff(user);

        // Verifica e-mail duplicado (se mudou)
        if (!user.getEmail().equalsIgnoreCase(request.getEmail())
                && userRepository.existsByEmail(request.getEmail())) {
            throw new DomainException(HttpStatus.CONFLICT, "E-mail já está em uso.");
        }

        user.setName(request.getName());
        user.setEmail(request.getEmail());
        userRepository.save(user);

        return toResponse(user);
    }

    // ── Ativar / Desativar ────────────────────────────────────

    @Transactional
    public UserResponse toggleActive(Long id, Long currentUserId) {
        if (id.equals(currentUserId)) {
            throw new BusinessException("Você não pode desativar a si mesmo.");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", id));
        assertIsStaff(user);

        user.setIsActive(!user.getIsActive());
        userRepository.save(user);

        return toResponse(user);
    }

    // ── Helpers ───────────────────────────────────────────────

    private void assertIsStaff(User user) {
        boolean isStaff = user.getRoles().stream()
                .anyMatch(r -> r.getRoleName() == AppRole.ROLE_EMPLOYEE
                        || r.getRoleName() == AppRole.ROLE_ADMIN);
        if (!isStaff) {
            throw new BusinessException("O usuário informado não é um funcionário.");
        }
    }

    private AppRole resolveRole(String role) {
        if (role == null || role.isBlank() || "EMPLOYEE".equalsIgnoreCase(role)) {
            return AppRole.ROLE_EMPLOYEE;
        }
        if ("ADMIN".equalsIgnoreCase(role)) {
            return AppRole.ROLE_ADMIN;
        }
        throw new BusinessException("Role inválida: '" + role + "'. Use EMPLOYEE ou ADMIN.");
    }

    private UserResponse toResponse(User user) {
        List<String> roles = user.getRoles().stream()
                .map(r -> r.getRoleName().name())
                .toList();
        return new UserResponse(
                user.getUserId(),
                user.getName(),
                user.getEmail(),
                user.getIsActive(),
                roles);
    }
}
