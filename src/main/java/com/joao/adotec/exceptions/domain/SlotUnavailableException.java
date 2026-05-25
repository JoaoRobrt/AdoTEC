package com.joao.adotec.exceptions.domain;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class SlotUnavailableException extends BusinessException {
    public SlotUnavailableException(String message) {
        super(message);
    }

    public SlotUnavailableException() {
        super("Horário indisponível: capacidade máxima atingida.");
    }
}
