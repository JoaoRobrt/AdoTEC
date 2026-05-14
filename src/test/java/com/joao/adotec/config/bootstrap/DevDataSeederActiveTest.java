package com.joao.adotec.config.bootstrap;

import com.joao.adotec.repositories.RoleRepository;
import com.joao.adotec.repositories.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:seedtestdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.driverClassName=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@ActiveProfiles("dev") // "dev" IS active
@Transactional
public class DevDataSeederActiveTest {

    @Autowired
    private ApplicationContext context;

    @Autowired
    private DevDataSeeder devDataSeeder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Test
    @DisplayName("DevDataSeeder should run when 'dev' profile is active")
    void shouldLoadSeederBeanAndCreateUsers() throws Exception {
        // Assert that the bean is loaded
        assertThat(context.containsBean("devDataSeeder")).isTrue();

        // Run the seeder manually to simulate the application startup 
        devDataSeeder.run();

        // Verify that roles exist
        assertThat(roleRepository.count()).isGreaterThanOrEqualTo(3);

        // Verify that users are created
        assertThat(userRepository.existsByEmail("admin@adotec.com")).isTrue();
        assertThat(userRepository.existsByEmail("employee@adotec.com")).isTrue();
        assertThat(userRepository.existsByEmail("adopter@adotec.com")).isTrue();

        // Verify Idempotence: run again and expect no duplication errors
        long userCountBefore = userRepository.count();
        devDataSeeder.run();
        long userCountAfter = userRepository.count();

        assertThat(userCountAfter).isEqualTo(userCountBefore);
    }
}
