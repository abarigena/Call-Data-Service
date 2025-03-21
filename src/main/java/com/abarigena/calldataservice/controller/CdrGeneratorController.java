package com.abarigena.calldataservice.controller;

import com.abarigena.calldataservice.service.CdrGeneratorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Контроллер для генерации CDR-записей.
 * Предоставляет REST API для инициирования процесса генерации CDR-записей.
 */
@RestController
@RequestMapping("/api/cdr")
@Tag(name = "CDR Generator", description = "API для генерации CDR-записей")
public class CdrGeneratorController {
    private static final Logger logger = LoggerFactory.getLogger(CdrGeneratorController.class);

    private final CdrGeneratorService cdrGeneratorService;

    @Autowired
    public CdrGeneratorController(CdrGeneratorService cdrGeneratorService) {
        this.cdrGeneratorService = cdrGeneratorService;
    }

    @Operation(
            summary = "Генерация CDR-записей за год",
            description = "Инициирует процесс генерации CDR-записей за весь год. Возвращает время выполнения операции."
    )
    @PostMapping("/generate")
    public ResponseEntity<?> generateCdr() {
        logger.info("Получен запрос на генерацию CDR-записей");
        long startTime = System.currentTimeMillis();
        cdrGeneratorService.generateYearCdrRecords();
        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;

        logger.info("Завершена генерация CDR-записей. Время выполнения: {} мс", executionTime);

        return ResponseEntity.ok().body("CDR генерация успешна. Заняло времени: " + (executionTime) + " мс");
    }
}
