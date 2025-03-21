package com.abarigena.calldataservice.controller;

import com.abarigena.calldataservice.dto.CdrReportResponse;
import com.abarigena.calldataservice.dto.CdrReportsRequest;
import com.abarigena.calldataservice.dto.SimpleCdrReportRequest;
import com.abarigena.calldataservice.service.CdrReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

/**
 * REST-контроллер для работы с CDR-отчетами.
 * Предоставляет API для генерации отчетов.
 */
@RestController
@RequestMapping("/api/cdr/reports")
@Tag(name = "CDR Reports", description = "API для работы с отчетами по CDR-записям")
public class CdrReportController {

    private static final Logger logger = LoggerFactory.getLogger(CdrReportController.class);

    private final CdrReportService cdrReportService;

    @Autowired
    public CdrReportController(CdrReportService cdrReportService) {
        this.cdrReportService = cdrReportService;
    }

    /**
     * Генерирует CDR-отчет для указанного абонента за указанный период.
     *
     * @param request Запрос на генерацию отчета
     * @return Ответ с UUID запроса и статусом выполнения
     */
    @PostMapping("/generate")
    @Operation(
            summary = "Генерация CDR-отчета по периоду",
            description = "Генерирует отчет для указанного абонента за заданный период времени"
    )
    public ResponseEntity<CdrReportResponse> generateReport(@RequestBody CdrReportsRequest request) {
        logger.info("Получен запрос на генерацию CDR-отчета для абонента {}", request.getMsisdn());

        CdrReportResponse response = cdrReportService.generateReport(request);

        if ("ERROR".equals(response.getStatus())) {
            logger.error("Ошибка при генерации отчета: {}", response.getErrorMessage());
            return ResponseEntity.badRequest().body(response);
        }

        logger.info("Запрос на генерацию отчета принят, requestId: {}", response.getRequestId());
        return ResponseEntity.ok(response);
    }

    /**
     * Генерирует CDR-отчет для указанного абонента за указанное количество дней назад.
     *
     * @param request Упрощенный запрос на генерацию отчета
     * @return Ответ с UUID запроса и статусом выполнения
     */
    @Operation(
            summary = "Упрощенная генерация CDR-отчета",
            description = "Генерирует отчет для указанного абонента за заданное количество дней от текущей даты"
    )
    @PostMapping("/generate-simple")
    public ResponseEntity<CdrReportResponse> generateSimpleReport(@RequestBody SimpleCdrReportRequest request) {
        logger.info("Получен упрощенный запрос на генерацию CDR-отчета для абонента {} за {} дней",
                request.getMsisdn(), request.getPeriodInDays());

        // Преобразуем упрощенный запрос в полный
        CdrReportsRequest fullRequest = new CdrReportsRequest();
        fullRequest.setMsisdn(request.getMsisdn());
        fullRequest.setEndDate(LocalDateTime.now());
        fullRequest.setStartDate(LocalDateTime.now().minusDays(request.getPeriodInDays()));

        CdrReportResponse response = cdrReportService.generateReport(fullRequest);

        if ("ERROR".equals(response.getStatus())) {
            logger.error("Ошибка при генерации отчета: {}", response.getErrorMessage());
            return ResponseEntity.badRequest().body(response);
        }

        logger.info("Запрос на генерацию отчета принят, requestId: {}", response.getRequestId());
        return ResponseEntity.ok(response);
    }
}
