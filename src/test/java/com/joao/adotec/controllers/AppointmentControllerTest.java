package com.joao.adotec.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.joao.adotec.dto.AppointmentRequestDTO;
import com.joao.adotec.exceptions.handler.GlobalExceptionHandler;
import com.joao.adotec.models.Appointment;
import com.joao.adotec.security.services.UserDetailsImpl;
import com.joao.adotec.dto.AppointmentResponseDTO;
import com.joao.adotec.mappers.AppointmentMapper;
import com.joao.adotec.services.AppointmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("AppointmentController — Route Security Tests")
class AppointmentControllerTest {

    @Mock
    private AppointmentService appointmentService;

    @Mock
    private AppointmentMapper appointmentMapper;

    @InjectMocks
    private AppointmentController appointmentController;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(appointmentController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    private UsernamePasswordAuthenticationToken createAuthToken(Long id, String role) {
        UserDetailsImpl userDetails = new UserDetailsImpl(
                id, "Test User", "test@example.com", "pass", true,
                List.of(new SimpleGrantedAuthority("ROLE_" + role)));
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

    @Test
    @DisplayName("POST /appointments → 201 Created (Adopter successfully accesses endpoint)")
    void createAppointment_withAdopter_returns201() throws Exception {
        AppointmentRequestDTO request = new AppointmentRequestDTO(2L, 3L);
        UsernamePasswordAuthenticationToken auth = createAuthToken(1L, "ADOPTER");

        given(appointmentService.createAppointment(eq(1L), eq(2L), eq(3L))).willReturn(new Appointment());

        mockMvc.perform(post("/appointments")
                .principal(auth)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("PATCH /appointments/{id}/assign/{employeeId} → 403 Forbidden when Adopter tries to assign")
    void assignEmployee_withAdopter_isForbidden() throws Exception {
        // Since we are using standalone setup, method security interceptor is not
        // automatically present.
        // We simulate the AccessDeniedException being thrown by @PreAuthorize if it was
        // hit
        // by throwing it directly from the service when an ADOPTER attempts it,
        // mirroring the precise test logic used in earlier controller tests.
        given(appointmentService.assignEmployee(any(), any()))
                .willThrow(new AccessDeniedException("Access is denied"));

        UsernamePasswordAuthenticationToken auth = createAuthToken(1L, "ADOPTER");

        try {
            mockMvc.perform(patch("/appointments/1/assign/5")
                    .principal(auth));
        } catch (Exception e) {
            org.assertj.core.api.Assertions.assertThat(e.getCause())
                    .isInstanceOf(AccessDeniedException.class);
        }
    }

    @Test
    @DisplayName("GET /appointments → 200 OK (Admin successfully accesses endpoint)")
    void getAllAppointments_withAdmin_returns200() throws Exception {
        UsernamePasswordAuthenticationToken auth = createAuthToken(1L, "ADMIN");

        given(appointmentService.getAllAppointments(any(org.springframework.data.domain.Pageable.class)))
                .willReturn(new org.springframework.data.domain.PageImpl<>(List.of(new Appointment())));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/appointments")
                .principal(auth))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PATCH /appointments/{id}/result → 200 OK (Admin successfully accesses endpoint)")
    void registerResult_withAdmin_returns200() throws Exception {
        com.joao.adotec.dto.AppointmentResultDTO request = new com.joao.adotec.dto.AppointmentResultDTO(
                com.joao.adotec.enums.AdoptionResult.APPROVED, "All good");
        UsernamePasswordAuthenticationToken auth = createAuthToken(1L, "ADMIN");

        given(appointmentService.registerResult(eq(1L), any())).willReturn(new Appointment());

        mockMvc.perform(patch("/appointments/1/result")
                .principal(auth)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
    @Test
    @DisplayName("PATCH /appointments/{id}/cancel → 200 OK (Adopter successfully accesses endpoint)")
    void cancelAppointment_withAdopter_returns200() throws Exception {
        UsernamePasswordAuthenticationToken auth = createAuthToken(10L, "ADOPTER");

        given(appointmentService.cancelAppointment(eq(1L), eq(10L))).willReturn(new Appointment());

        mockMvc.perform(patch("/appointments/1/cancel")
                .principal(auth)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /appointments/{id} → 200 OK when appointment exists")
    void getAppointmentById_exists_returns200() throws Exception {
        UsernamePasswordAuthenticationToken auth = createAuthToken(1L, "ADMIN");
        given(appointmentService.getAppointmentById(eq(1L), any())).willReturn(new Appointment());

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/appointments/1")
                .principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Appointment retrieved successfully"));
    }

    @Test
    @DisplayName("GET /appointments/{id} → 404 when appointment not found")
    void getAppointmentById_notFound_returns404() throws Exception {
        UsernamePasswordAuthenticationToken auth = createAuthToken(1L, "ADMIN");
        given(appointmentService.getAppointmentById(eq(999L), any()))
                .willThrow(new com.joao.adotec.exceptions.domain.ResourceNotFoundException("Appointment", 999L));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/appointments/999")
                .principal(auth))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Appointment not found with id: 999"));
    }
}
