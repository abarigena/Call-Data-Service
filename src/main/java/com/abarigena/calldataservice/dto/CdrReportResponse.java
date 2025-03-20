package com.abarigena.calldataservice.dto;

import java.util.UUID;

/**
 * DTO для ответа на запрос генерации CDR-отчета.
 * Содержит UUID запроса и статус выполнения.
 */
public class CdrReportResponse {
    private UUID requestId;
    private String status;
    private String fileName;
    private String errorMessage;

    public CdrReportResponse() {
    }

    public CdrReportResponse(UUID requestId, String status, String fileName) {
        this.requestId = requestId;
        this.status = status;
        this.fileName = fileName;
    }

    public static CdrReportResponse success(UUID requestId, String fileName) {
        return new CdrReportResponse(requestId, "SUCCESS", fileName);
    }

    public static CdrReportResponse error(UUID requestId, String errorMessage) {
        CdrReportResponse response = new CdrReportResponse(requestId, "ERROR", null);
        response.setErrorMessage(errorMessage);
        return response;
    }

    public UUID getRequestId() {
        return requestId;
    }

    public void setRequestId(UUID requestId) {
        this.requestId = requestId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
