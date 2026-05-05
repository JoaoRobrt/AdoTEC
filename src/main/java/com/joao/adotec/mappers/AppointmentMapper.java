package com.joao.adotec.mappers;

import com.joao.adotec.dto.AppointmentResponseDTO;
import com.joao.adotec.models.Appointment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
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
    @Mapping(target = "timeSlot", source = "timeSlot")
    AppointmentResponseDTO toDTO(Appointment appointment);

    @Mapping(target = "timeSlotId", source = "timeSlotId")
    @Mapping(target = "date", source = "date")
    @Mapping(target = "startTime", source = "startTime")
    @Mapping(target = "endTime", source = "endTime")
    com.joao.adotec.dto.TimeSlotSummaryDTO toTimeSlotSummary(com.joao.adotec.models.TimeSlot timeSlot);

    List<AppointmentResponseDTO> toDTOList(List<Appointment> appointments);
}
