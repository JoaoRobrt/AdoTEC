package com.joao.adotec.config.bootstrap;

import com.joao.adotec.enums.AppRole;
import com.joao.adotec.models.Role;
import com.joao.adotec.models.User;
import com.joao.adotec.repositories.RoleRepository;
import com.joao.adotec.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Componente responsável pelo bootstrap de dados essenciais para o ambiente de desenvolvimento.
 * ATENÇÃO: Esta classe deve ser executada APENAS quando o profile "dev" estiver ativo.
 * Isso resolve o BUG-01 e evita a criação de credenciais conhecidas em ambiente de produção.
 */
@Component
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class DevDataSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        log.info("Iniciando DevDataSeeder - Configuração de dados de desenvolvimento...");

        // 1. Create Roles if they don't exist
        if (roleRepository.count() == 0) {
            log.info("Criando Roles no banco de dados...");
            roleRepository.save(new Role(AppRole.ROLE_ADMIN));
            roleRepository.save(new Role(AppRole.ROLE_EMPLOYEE));
            roleRepository.save(new Role(AppRole.ROLE_ADOPTER));
        }

        // 2. Fetch Roles
        Role adminRole = roleRepository.findByRoleName(AppRole.ROLE_ADMIN)
                .orElseThrow(() -> new RuntimeException("Error: ADMIN Role is not found."));
        Role employeeRole = roleRepository.findByRoleName(AppRole.ROLE_EMPLOYEE)
                .orElseThrow(() -> new RuntimeException("Error: EMPLOYEE Role is not found."));
        Role adopterRole = roleRepository.findByRoleName(AppRole.ROLE_ADOPTER)
                .orElseThrow(() -> new RuntimeException("Error: ADOPTER Role is not found."));

        // 3. Create Admin User
        if (!userRepository.existsByEmail("admin@adotec.com")) {
            log.info("Criando usuário admin padrão...");
            User admin = new User("Admin User", "admin@adotec.com", passwordEncoder.encode("admin123"));
            admin.setRoles(Set.of(adminRole, employeeRole, adopterRole));
            userRepository.save(admin);
        }

        // 4. Create Employee User
        if (!userRepository.existsByEmail("employee@adotec.com")) {
            log.info("Criando usuário employee padrão...");
            User employee = new User("Employee User", "employee@adotec.com", passwordEncoder.encode("employee123"));
            employee.setRoles(Set.of(employeeRole, adopterRole));
            userRepository.save(employee);
        }

        // 5. Create Adopter User
        if (!userRepository.existsByEmail("adopter@adotec.com")) {
            log.info("Criando usuário adopter padrão...");
            User adopter = new User("Adopter User", "adopter@adotec.com", passwordEncoder.encode("adopter123"));
            adopter.setRoles(Set.of(adopterRole));
            userRepository.save(adopter);
        }
        
        log.info("DevDataSeeder finalizado com sucesso.");
    }
}
