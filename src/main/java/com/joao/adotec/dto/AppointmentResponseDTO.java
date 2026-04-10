package com.joao.adotec.dto;

import com.joao.adotec.enums.AppointmentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentResponseDTO {
    
    private Long appointmentId;
    private AppointmentStatus status;
    private Instant createdAt;
    
    private Long adopterId;
    private String adopterName;

    private Long employeeId;
    private String employeeName;

    private Long petId;
    private String petName;

    private Long timeSlotId;
    private String timeSlotDetails; // could be Date + Time
}
