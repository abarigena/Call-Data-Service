package com.abarigena.calldataservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Упрощенный DTO для запроса на генерацию CDR-отчета.
 * Позволяет указать период в днях вместо точных дат.
 */
@Schema(description = "Упрощенный запрос на генерацию CDR-отчета")
public class SimpleCdrReportRequest {
    private String msisdn;
    private int periodInDays;

    public String getMsisdn() {
        return msisdn;
    }

    public void setMsisdn(String msisdn) {
        this.msisdn = msisdn;
    }

    public int getPeriodInDays() {
        return periodInDays;
    }

    public void setPeriodInDays(int periodInDays) {
        this.periodInDays = periodInDays;
    }
}
