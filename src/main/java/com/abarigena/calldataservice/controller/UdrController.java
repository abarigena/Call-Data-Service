package com.abarigena.calldataservice.controller;

import com.abarigena.calldataservice.dto.UdrReport;
import com.abarigena.calldataservice.service.UdrService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Контроллер для работы с UDR-отчетами.
 * Предоставляет REST API для получения отчетов по абонентам.
 */
@RestController
@RequestMapping("/api/udr")
public class UdrController {
    private static final Logger logger = LoggerFactory.getLogger(UdrController.class);

    private final UdrService udrService;

    @Autowired
    public UdrController(UdrService udrService) {
        this.udrService = udrService;
    }

    /**
     * Получение UDR-отчета для одного абонента
     *
     * @param msisdn номер абонента
     * @param year   год (опционально)
     * @param month  месяц (опционально)
     * @return UDR-отчет для абонента
     */
    @GetMapping("/subscriber/{msisdn}")
    public ResponseEntity<UdrReport> getUdrForSubscriber(
            @PathVariable String msisdn,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {

        logger.info("Получен запрос на UDR-отчет для абонента {}, год: {}, месяц: {}", msisdn, year, month);

        UdrReport report = udrService.getUdrForSubscriber(msisdn, year, month);

        logger.info("Отправлен UDR-отчет для абонента {}", msisdn);
        return ResponseEntity.ok(report);
    }

    /**
     * Получение UDR-отчетов для всех абонентов за указанный месяц
     *
     * @param year  год
     * @param month месяц
     * @return список UDR-отчетов для всех абонентов
     */
    @GetMapping("/subscribers")
    public ResponseEntity<List<UdrReport>> getAllUdrsByMonth(
            @RequestParam int year,
            @RequestParam int month) {

        logger.info("Получен запрос на UDR-отчеты для всех абонентов за {}-{}", year, month);

        List<UdrReport> reports = udrService.getAllUdrsByMonth(year, month);

        logger.info("Отправлены UDR-отчеты для {} абонентов за {}-{}", reports.size(), year, month);
        return ResponseEntity.ok(reports);
    }
}
