package com.abarigena.calldataservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * DTO для запроса на генерацию CDR-отчета.
 * Содержит номер абонента и период, за который требуется сформировать отчет.
 */
@Schema(description = "Запрос на генерацию CDR-отчета")
public class CdrReportsRequest {
    private String msisdn;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    public String getMsisdn() {
        return msisdn;
    }

    public void setMsisdn(String msisdn) {
        this.msisdn = msisdn;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }
}
