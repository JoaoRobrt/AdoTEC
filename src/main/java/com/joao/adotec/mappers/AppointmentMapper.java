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
    @Mapping(target = "timeSlotId", source = "timeSlot.timeSlotId")
    @Mapping(target = "timeSlotDetails", expression = "java(appointment.getTimeSlot() != null ? appointment.getTimeSlot().getDate().toString() + \" \" + appointment.getTimeSlot().getStartTime().toString() : null)")
    AppointmentResponseDTO toDTO(Appointment appointment);

    List<AppointmentResponseDTO> toDTOList(List<Appointment> appointments);
}
