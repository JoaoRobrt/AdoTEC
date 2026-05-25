package com.joao.adotec.exceptions.domain;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateAppointmentException extends BusinessException {
    public DuplicateAppointmentException(String message) {
        super(message);
    }

    public DuplicateAppointmentException() {
        super("Você já possui um agendamento neste horário.");
    }
}
