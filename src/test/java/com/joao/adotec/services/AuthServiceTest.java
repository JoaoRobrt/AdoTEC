package com.joao.adotec.services;

import com.joao.adotec.dto.LoginRequest;
import com.joao.adotec.dto.SignupRequest;
import com.joao.adotec.dto.UserInfoResponse;
import com.joao.adotec.enums.AppRole;
import com.joao.adotec.exceptions.domain.DomainException;
import com.joao.adotec.models.Role;
import com.joao.adotec.models.User;
import com.joao.adotec.repositories.RoleRepository;
import com.joao.adotec.repositories.UserRepository;
import com.joao.adotec.security.jwt.JwtUtils;
import com.joao.adotec.security.services.UserDetailsImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService — Unit Tests")
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private PasswordEncoder encoder;
    @Mock
    private JwtUtils jwtUtils;

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("authenticateUser → Should authenticate and return UserInfoResponse with JWT")
    void authenticateUser_shouldReturnUserInfoResponse() {
        // Arrange
        LoginRequest loginRequest = new LoginRequest("test@test.com", "password");
        Authentication authentication = mock(Authentication.class);
        
        List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADOPTER"));
        UserDetailsImpl userDetails = new UserDetailsImpl(1L, "Test User", "test@test.com", "password", true, authorities);

        given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).willReturn(authentication);
        given(authentication.getPrincipal()).willReturn(userDetails);
        given(jwtUtils.generateTokenFromUsername(userDetails.getUsername())).willReturn("mock-jwt-token");

        // Act
        UserInfoResponse response = authService.authenticateUser(loginRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Test User");
        assertThat(response.getEmail()).isEqualTo("test@test.com");
        assertThat(response.getJwtToken()).isEqualTo("mock-jwt-token");
        assertThat(response.getRoles()).containsExactly("ROLE_ADOPTER");
        
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtils).generateTokenFromUsername("test@test.com");
    }

    @Test
    @DisplayName("registerUser → Should throw DomainException (CONFLICT) when email already exists")
    void registerUser_whenEmailExists_shouldThrowDomainException() {
        // Arrange
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setName("Test User");
        signupRequest.setEmail("test@test.com");
        signupRequest.setPassword("password");
        given(userRepository.existsByEmail(signupRequest.getEmail())).willReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> authService.registerUser(signupRequest))
                .isInstanceOf(DomainException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.CONFLICT)
                .hasMessage("Error: Email is already in use!");
    }

    @Test
    @DisplayName("registerUser → Should throw DomainException (INTERNAL_SERVER_ERROR) when default role is not found")
    void registerUser_whenRoleNotFound_shouldThrowDomainException() {
        // Arrange
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setName("Test User");
        signupRequest.setEmail("test@test.com");
        signupRequest.setPassword("password");
        given(userRepository.existsByEmail(signupRequest.getEmail())).willReturn(false);
        given(encoder.encode(signupRequest.getPassword())).willReturn("encoded-password");
        given(roleRepository.findByRoleName(AppRole.ROLE_ADOPTER)).willReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> authService.registerUser(signupRequest))
                .isInstanceOf(DomainException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.INTERNAL_SERVER_ERROR)
                .hasMessage("Error: Default role 'ADOPTER' is not found.");
    }

    @Test
    @DisplayName("registerUser → Should register user, assign ROLE_ADOPTER, and return UserInfoResponse")
    void registerUser_whenValid_shouldRegisterAndAuthenticate() {
        // Arrange
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setName("Test User");
        signupRequest.setEmail("test@test.com");
        signupRequest.setPassword("password");
        Role role = new Role(AppRole.ROLE_ADOPTER);
        
        given(userRepository.existsByEmail(signupRequest.getEmail())).willReturn(false);
        given(encoder.encode(signupRequest.getPassword())).willReturn("encoded-password");
        given(roleRepository.findByRoleName(AppRole.ROLE_ADOPTER)).willReturn(Optional.of(role));
        // Note: userRepository.save(user) doesn't need a mock return since save is not heavily used for its return here.
        
        // Mocking the nested authenticateUser call
        Authentication authentication = mock(Authentication.class);
        List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADOPTER"));
        UserDetailsImpl userDetails = new UserDetailsImpl(1L, "Test User", "test@test.com", "password", true, authorities);

        given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).willReturn(authentication);
        given(authentication.getPrincipal()).willReturn(userDetails);
        given(jwtUtils.generateTokenFromUsername(anyString())).willReturn("mock-jwt-token");

        // Act
        UserInfoResponse response = authService.registerUser(signupRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getEmail()).isEqualTo("test@test.com");
        assertThat(response.getJwtToken()).isEqualTo("mock-jwt-token");
        assertThat(response.getRoles()).containsExactly("ROLE_ADOPTER");

        verify(userRepository).existsByEmail("test@test.com");
        verify(encoder).encode("password");
        verify(roleRepository).findByRoleName(AppRole.ROLE_ADOPTER);
        verify(userRepository).save(any(User.class));
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }
}
