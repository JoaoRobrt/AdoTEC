package com.joao.adotec.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DashboardMetricsDTO {
    private long petsAvailable;
    private long appointmentsTotal;
    private long pendingTotal;
    private long pendingToday;
    private long unassignedTotal;
    private long employeesTotal;
}
