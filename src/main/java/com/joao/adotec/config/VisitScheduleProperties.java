package com.joao.adotec.config;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
@Validated
@Configuration
@ConfigurationProperties(prefix = "visits.schedule")
public class VisitScheduleProperties {

    @NotEmpty(message = "Pelo menos um dia válido deve ser configurado.")
    private List<DayOfWeek> days;

    @NotEmpty(message = "Pelo menos um horário de visita deve ser configurado.")
    private List<Slot> slots;

    @Getter
    @Setter
    public static class Slot {
        @NotNull
        private LocalTime start;
        
        @NotNull
        private LocalTime end;
        
        @NotNull
        private Integer capacity;
    }
}
