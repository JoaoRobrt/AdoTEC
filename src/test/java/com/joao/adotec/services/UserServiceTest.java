package com.joao.adotec.services;

import com.joao.adotec.dto.UserResponse;
import com.joao.adotec.enums.AppRole;
import com.joao.adotec.models.User;
import com.joao.adotec.repositories.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService — Unit Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("findEmployees → Should return list of mapped UserResponse")
    void findEmployees_shouldReturnMappedList() {
        // Arrange
        User user1 = new User();
        user1.setUserId(1L);
        user1.setName("Employee 1");
        user1.setEmail("emp1@test.com");

        User user2 = new User();
        user2.setUserId(2L);
        user2.setName("Employee 2");
        user2.setEmail("emp2@test.com");

        given(userRepository.findByRoles_RoleName(AppRole.ROLE_EMPLOYEE)).willReturn(Arrays.asList(user1, user2));

        // Act
        List<UserResponse> result = userService.findEmployees();

        // Assert
        assertThat(result).hasSize(2);
        
        assertThat(result.get(0).id()).isEqualTo(1L);
        assertThat(result.get(0).name()).isEqualTo("Employee 1");
        assertThat(result.get(0).email()).isEqualTo("emp1@test.com");

        assertThat(result.get(1).id()).isEqualTo(2L);
        assertThat(result.get(1).name()).isEqualTo("Employee 2");
        assertThat(result.get(1).email()).isEqualTo("emp2@test.com");

        verify(userRepository).findByRoles_RoleName(AppRole.ROLE_EMPLOYEE);
    }

    @Test
    @DisplayName("findEmployees → Should return empty list if no employees found")
    void findEmployees_whenNoEmployees_shouldReturnEmptyList() {
        // Arrange
        given(userRepository.findByRoles_RoleName(AppRole.ROLE_EMPLOYEE)).willReturn(Collections.emptyList());

        // Act
        List<UserResponse> result = userService.findEmployees();

        // Assert
        assertThat(result).isEmpty();
        verify(userRepository).findByRoles_RoleName(AppRole.ROLE_EMPLOYEE);
    }
}
