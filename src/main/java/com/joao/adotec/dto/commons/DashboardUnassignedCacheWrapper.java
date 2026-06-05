package com.joao.adotec.dto.commons;

import com.joao.adotec.dto.AppointmentResponseDTO;
import java.util.List;

/**
 * Wrapper class to hold a list of AppointmentResponseDTOs for caching purposes.
 * This ensures GenericJacksonJsonRedisSerializer can properly deserialize
 * the inner records without type erasure issues.
 */
public class DashboardUnassignedCacheWrapper {
    private List<AppointmentResponseDTO> appointments;

    public DashboardUnassignedCacheWrapper() {
    }

    public DashboardUnassignedCacheWrapper(List<AppointmentResponseDTO> appointments) {
        this.appointments = appointments;
    }

    public List<AppointmentResponseDTO> getAppointments() {
        return appointments;
    }

    public void setAppointments(List<AppointmentResponseDTO> appointments) {
        this.appointments = appointments;
    }
}
