package com.joao.adotec.config.bootstrap;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test") // "dev" is NOT active
public class DevDataSeederInactiveTest {

    @Autowired
    private ApplicationContext context;

    @Test
    @DisplayName("DevDataSeeder should NOT run when 'dev' profile is inactive")
    void shouldNotLoadSeederBean() {
        boolean hasSeeder = context.containsBean("devDataSeeder");
        assertThat(hasSeeder).isFalse();
    }
}
