package com.joao.adotec.mappers;

import com.joao.adotec.dto.AppointmentResponseDTO;
import com.joao.adotec.dto.TimeSlotSummaryDTO;
import com.joao.adotec.models.Appointment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AppointmentMapper {

    AppointmentMapper INSTANCE = Mappers.getMapper(AppointmentMapper.class);

    @Mapping(target = "adopterId", source = "adopter.userId")
    @Mapping(target = "adopterName", source = "adopter.name")
    @Mapping(target = "employeeId", source = "employee.userId")
    @Mapping(target = "employeeName", source = "employee.name")
    @Mapping(target = "petId", source = "pet.petId")
    @Mapping(target = "petName", source = "pet.petName")
    @Mapping(target = "timeSlot", source = "appointment", qualifiedByName = "mapTimeSlotSummary")
    AppointmentResponseDTO toDTO(Appointment appointment);

    @Named("mapTimeSlotSummary")
    default TimeSlotSummaryDTO mapTimeSlotSummary(Appointment appointment) {
        if (appointment == null || appointment.getAppointmentDate() == null || appointment.getStartTime() == null) {
            return null;
        }
        String slotId = appointment.getAppointmentDate().toString() + "_" + appointment.getStartTime().toString();
        return new TimeSlotSummaryDTO(
                slotId,
                appointment.getAppointmentDate(),
                appointment.getStartTime(),
                appointment.getEndTime()
        );
    }

    List<AppointmentResponseDTO> toDTOList(List<Appointment> appointments);
}
